package com.turkcell.rencar.data.remote

import com.turkcell.rencar.data.session.SessionTokenHolder
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val sessionTokenHolder: SessionTokenHolder
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val accessToken = sessionTokenHolder.accessToken
            ?: return chain.proceed(original)

        val authorized = original.newBuilder()
            .header(HEADER_AUTHORIZATION, "$BEARER_PREFIX$accessToken")
            .build()
        return chain.proceed(authorized)
    }

    private companion object {
        const val HEADER_AUTHORIZATION = "Authorization"
        const val BEARER_PREFIX = "Bearer "
    }
}
