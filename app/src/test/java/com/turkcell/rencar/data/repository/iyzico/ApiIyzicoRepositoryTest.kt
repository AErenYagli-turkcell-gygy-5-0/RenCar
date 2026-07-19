package com.turkcell.rencar.data.repository.iyzico

import com.turkcell.rencar.data.remote.iyzico.IyzicoApiService
import com.turkcell.rencar.domain.iyzico.IyzicoCheckoutSession
import com.turkcell.rencar.domain.iyzico.IyzicoError
import com.turkcell.rencar.domain.iyzico.IyzicoResult
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

class ApiIyzicoRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: ApiIyzicoRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = createRepository(server)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `initialize checkout sends rental basket id and maps session`() = runTest {
        server.enqueue(
            jsonResponse(
                code = 201,
                body = """
                    {
                      "status": "success",
                      "token": "checkout-token",
                      "tokenExpireTime": 1800,
                      "paymentPageUrl": "https://sandbox.iyzipay.com/pay/token",
                      "checkoutFormContent": "<script></script>"
                    }
                """.trimIndent()
            )
        )

        val result = repository.initializeCheckoutForm(
            price = 150.5,
            description = "RenCar yolculuk odemesi",
            basketId = "rental-rental-1",
            enabledInstallments = listOf(1)
        )

        val request = server.takeRequest()
        assertEquals("/iyzico/checkout-form/initialize", request.path)
        assertEquals(
            """{"price":150.5,"description":"RenCar yolculuk odemesi","basketId":"rental-rental-1","enabledInstallments":[1]}""",
            request.body.readUtf8()
        )
        assertEquals(
            IyzicoResult.Success(
                IyzicoCheckoutSession(
                    token = "checkout-token",
                    paymentPageUrl = "https://sandbox.iyzipay.com/pay/token",
                    tokenExpireTime = 1800
                )
            ),
            result
        )
    }

    @Test
    fun `checkout result maps successful payment`() = runTest {
        server.enqueue(
            jsonResponse(
                body = """
                    {
                      "status": "success",
                      "paymentId": "36677190",
                      "paidPrice": 150.5,
                      "currency": "TRY",
                      "paymentStatus": "SUCCESS"
                    }
                """.trimIndent()
            )
        )

        val result = repository.getCheckoutFormResult("checkout-token")

        val request = server.takeRequest()
        assertEquals("/iyzico/checkout-form/result/checkout-token", request.path)
        assertTrue(result is IyzicoResult.Success)
        val payment = (result as IyzicoResult.Success).data
        assertEquals("36677190", payment.paymentId)
        assertTrue(payment.isSuccessful)
    }

    @Test
    fun `service unavailable maps to iyzico service error`() = runTest {
        server.enqueue(jsonResponse(code = 503, body = """{"message":"Iyzico keys missing"}"""))

        val result = repository.initializeCheckoutForm(
            price = 150.5,
            description = "RenCar yolculuk odemesi",
            basketId = "rental-rental-1",
            enabledInstallments = listOf(1)
        )

        assertEquals(IyzicoResult.Failure(IyzicoError.ServiceUnavailable), result)
    }

    private fun createRepository(mockWebServer: MockWebServer): ApiIyzicoRepository {
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return ApiIyzicoRepository(retrofit.create(IyzicoApiService::class.java))
    }

    private fun jsonResponse(code: Int = 200, body: String): MockResponse =
        MockResponse()
            .setResponseCode(code)
            .setHeader("Content-Type", "application/json")
            .setBody(body)
}
