package com.turkcell.rencar.data.repository.auth

import com.turkcell.rencar.data.remote.auth.AuthApiService
import com.turkcell.rencar.data.session.SessionTokenHolder
import com.turkcell.rencar.domain.auth.AuthError
import com.turkcell.rencar.domain.auth.AuthResult
import com.turkcell.rencar.domain.auth.LoginChallenge
import com.turkcell.rencar.domain.auth.RegisterRequest
import com.turkcell.rencar.domain.auth.RegisteredUser
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiAuthRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: ApiAuthRepository
    private lateinit var sessionTokenHolder: SessionTokenHolder

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        sessionTokenHolder = SessionTokenHolder()
        repository = createRepository(server, sessionTokenHolder)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `register sends swagger body and exposes user without tokens`() = runTest {
        server.enqueue(
            jsonResponse(
                code = 201,
                body = """
                    {
                      "accessToken": "access-secret",
                      "refreshToken": "refresh-secret",
                      "user": {
                        "id": "user-1",
                        "email": "ahmet@example.com",
                        "phone": "+905551112233",
                        "fullName": "Ahmet Yılmaz",
                        "role": "PENDING",
                        "createdAt": "2026-07-03T10:00:00.000Z",
                        "updatedAt": "2026-07-03T10:00:00.000Z"
                      }
                    }
                """.trimIndent()
            )
        )

        val result = repository.register(
            RegisterRequest(
                email = "ahmet@example.com",
                password = "Sifre123!",
                fullName = "Ahmet Yılmaz",
                phone = "+905551112233"
            )
        )

        val request = server.takeRequest()
        assertEquals("/auth/register", request.path)
        assertEquals(
            """{"email":"ahmet@example.com","password":"Sifre123!","fullName":"Ahmet Yılmaz","phone":"+905551112233"}""",
            request.body.readUtf8()
        )
        assertEquals(
            AuthResult.Success(
                RegisteredUser(
                    id = "user-1",
                    email = "ahmet@example.com",
                    phone = "+905551112233",
                    fullName = "Ahmet Yılmaz",
                    role = "PENDING",
                    createdAt = "2026-07-03T10:00:00.000Z",
                    updatedAt = "2026-07-03T10:00:00.000Z"
                )
            ),
            result
        )
    }

    @Test
    fun `register maps conflict response`() = runTest {
        server.enqueue(jsonResponse(code = 409, body = "{}"))

        val result = repository.register(
            RegisterRequest(
                email = "ahmet@example.com",
                password = "Sifre123!",
                fullName = "Ahmet Yılmaz",
                phone = "+905551112233"
            )
        )

        assertEquals(
            AuthResult.Failure(AuthError.EmailAlreadyRegistered),
            result
        )
    }

    @Test
    fun `login sends normalized phone and maps challenge`() = runTest {
        server.enqueue(
            jsonResponse(
                code = 200,
                body = """
                    {
                      "message": "Doğrulama kodu SMS ile gönderildi.",
                      "phone": "+905320000000",
                      "expiresAt": "2026-07-03T12:05:00.000Z"
                    }
                """.trimIndent()
            )
        )

        val result = repository.requestLogin("+905320000000")

        val request = server.takeRequest()
        assertEquals("/auth/login", request.path)
        assertEquals("""{"phone":"+905320000000"}""", request.body.readUtf8())
        assertEquals(
            AuthResult.Success(
                LoginChallenge(
                    message = "Doğrulama kodu SMS ile gönderildi.",
                    phone = "+905320000000",
                    expiresAt = "2026-07-03T12:05:00.000Z"
                )
            ),
            result
        )
    }

    @Test
    fun `login maps unauthorized response`() = runTest {
        server.enqueue(jsonResponse(code = 401, body = "{}"))

        val result = repository.requestLogin("+905320000000")

        assertEquals(AuthResult.Failure(AuthError.UserNotFound), result)
    }

    @Test
    fun `connection failure maps network error`() = runTest {
        val offlineServer = MockWebServer()
        offlineServer.start()
        val offlineRepository = createRepository(offlineServer, SessionTokenHolder())
        offlineServer.shutdown()

        val result = offlineRepository.requestLogin("+905320000000")

        assertEquals(AuthResult.Failure(AuthError.Network), result)
    }

    @Test
    fun `refresh rotates stored token pair and exposes customer role`() = runTest {
        sessionTokenHolder.update("old-access", "old-refresh")
        server.enqueue(
            jsonResponse(
                code = 200,
                body = """
                    {
                      "accessToken": "new-access",
                      "refreshToken": "new-refresh",
                      "user": {
                        "id": "user-1",
                        "email": "ahmet@example.com",
                        "phone": "+905551112233",
                        "fullName": "Ahmet Yılmaz",
                        "role": "CUSTOMER",
                        "createdAt": "2026-07-03T10:00:00.000Z",
                        "updatedAt": "2026-07-04T10:00:00.000Z"
                      }
                    }
                """.trimIndent()
            )
        )

        val result = repository.refreshSession()

        val request = server.takeRequest()
        assertEquals("/auth/refresh", request.path)
        assertEquals("""{"refreshToken":"old-refresh"}""", request.body.readUtf8())
        assertTrue(result is AuthResult.Success)
        assertEquals("CUSTOMER", (result as AuthResult.Success).data.user.role)
        assertEquals("new-access", sessionTokenHolder.accessToken)
        assertEquals("new-refresh", sessionTokenHolder.refreshToken)
    }

    private fun createRepository(
        mockWebServer: MockWebServer,
        tokenHolder: SessionTokenHolder
    ): ApiAuthRepository {
        val apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
        return ApiAuthRepository(apiService, tokenHolder)
    }

    private fun jsonResponse(code: Int, body: String) = MockResponse()
        .setResponseCode(code)
        .setHeader("Content-Type", "application/json")
        .setBody(body)
}
