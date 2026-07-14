package com.turkcell.rencar.di

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

internal fun OkHttpClient.Builder.applyDebugLogging(): OkHttpClient.Builder = apply {
    addInterceptor(
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    )
}
