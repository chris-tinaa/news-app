package com.example.news_app.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class SourcesResponse(
    val status: String,
    val sources: List<NewsSourceDto> = emptyList(),
    val code: String? = null,
    val message: String? = null
)

@Serializable
data class ArticlesResponse(
    val status: String,
    val totalResults: Int = 0,
    val articles: List<NewsArticleDto> = emptyList(),
    val code: String? = null,
    val message: String? = null
)
