package com.example.coastlinenews.data.remote

import com.example.coastlinenews.data.remote.model.ArticlesResponse
import com.example.coastlinenews.data.remote.model.SourcesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("top-headlines/sources")
    suspend fun getSources(
        @Query("category") category: String
    ): SourcesResponse

    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("sources") sourceId: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): ArticlesResponse

    @GET("everything")
    suspend fun searchArticles(
        @Query("sources") sourceId: String,
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("sortBy") sortBy: String = "publishedAt"
    ): ArticlesResponse
}
