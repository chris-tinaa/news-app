package com.example.news_app.domain.repository

import com.example.news_app.domain.model.ArticlePage
import com.example.news_app.domain.model.NewsSource

interface NewsRepository {
    suspend fun getSources(category: String): List<NewsSource>
    suspend fun getArticles(sourceId: String, query: String, page: Int): ArticlePage
}
