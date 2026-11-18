package com.example.coastlinenews.domain.model

data class NewsArticle(
    val sourceId: String,
    val sourceName: String,
    val author: String?,
    val title: String,
    val description: String?,
    val url: String,
    val imageUrl: String?,
    val publishedAt: String?,
    val content: String?
)

data class ArticlePage(
    val articles: List<NewsArticle>,
    val totalResults: Int
)
