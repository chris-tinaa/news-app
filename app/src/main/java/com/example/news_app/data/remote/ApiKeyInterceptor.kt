package com.example.news_app.data.remote

import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor(
    private val apiKey: String
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        if (apiKey.isNotBlank()) {
            request.addHeader("X-Api-Key", apiKey)
        }
        return chain.proceed(request.build())
    }
}
