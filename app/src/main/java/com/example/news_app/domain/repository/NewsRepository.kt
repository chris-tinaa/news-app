package com.example.coastlinenews.domain.repository

import com.example.coastlinenews.domain.model.ArticlePage
import com.example.coastlinenews.domain.model.NewsSource

interface NewsRepository {
    suspend fun getSources(category: String): List<NewsSource>
    suspend fun getArticles(sourceId: String, query: String, page: Int): ArticlePage
}
