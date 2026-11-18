package com.example.coastlinenews.di

import com.example.coastlinenews.data.remote.ApiKeyInterceptor
import com.example.coastlinenews.data.remote.NewsApiService
import com.example.coastlinenews.data.repository.NewsRepositoryImpl
import com.example.coastlinenews.domain.repository.NewsRepository
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class AppContainer(
    apiKey: String
) {
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(ApiKeyInterceptor(apiKey))
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://newsapi.org/v2/")
        .client(httpClient)
        .addConverterFactory(
            Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType())
        )
        .build()

    val newsRepository: NewsRepository =
        NewsRepositoryImpl(retrofit.create(NewsApiService::class.java), apiKey)
}
