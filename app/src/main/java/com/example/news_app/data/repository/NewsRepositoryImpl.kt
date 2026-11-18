package com.example.news_app.data.repository

import com.example.news_app.data.remote.MissingApiKeyException
import com.example.news_app.data.remote.NewsApiException
import com.example.news_app.data.remote.NewsApiService
import com.example.news_app.data.remote.model.ArticlesResponse
import com.example.news_app.data.remote.model.SourcesResponse
import com.example.news_app.data.remote.toDomain
import com.example.news_app.domain.model.ArticlePage
import com.example.news_app.domain.model.NewsSource
import com.example.news_app.domain.repository.NewsRepository

class NewsRepositoryImpl(
    private val api: NewsApiService,
    private val apiKey: String
) : NewsRepository {
    override suspend fun getSources(category: String): List<NewsSource> {
        ensureApiKey()
        return api.getSources(category).toSources()
    }

    override suspend fun getArticles(sourceId: String, query: String, page: Int): ArticlePage {
        ensureApiKey()
        val response = if (query.isBlank()) {
            api.getTopHeadlines(sourceId, page, PAGE_SIZE)
        } else {
            api.searchArticles(sourceId, query, page, PAGE_SIZE)
        }
        return response.toArticlePage()
    }

    private fun ensureApiKey() {
        if (apiKey.isBlank()) throw MissingApiKeyException()
    }

    private fun SourcesResponse.toSources(): List<NewsSource> {
        ensureSuccess(status, message)
        return sources.mapNotNull { it.toDomain() }
    }

    private fun ArticlesResponse.toArticlePage(): ArticlePage {
        ensureSuccess(status, message)
        return ArticlePage(
            articles = articles.mapNotNull { it.toDomain() },
            totalResults = totalResults
        )
    }

    private fun ensureSuccess(status: String, message: String?) {
        if (status != "ok") {
            throw NewsApiException(message ?: "News API error")
        }
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}
