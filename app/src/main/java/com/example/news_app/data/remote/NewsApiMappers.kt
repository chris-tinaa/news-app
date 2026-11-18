package com.example.news_app.data.remote

import com.example.news_app.data.remote.model.NewsArticleDto
import com.example.news_app.data.remote.model.NewsSourceDto
import com.example.news_app.domain.model.NewsArticle
import com.example.news_app.domain.model.NewsSource

fun NewsSourceDto.toDomain(): NewsSource? {
    val sourceId = id?.takeIf { it.isNotBlank() } ?: return null
    return NewsSource(
        id = sourceId,
        name = name,
        description = description.orEmpty(),
        url = url.orEmpty(),
        category = category.orEmpty(),
        language = language.orEmpty(),
        country = country.orEmpty()
    )
}

fun NewsArticleDto.toDomain(): NewsArticle? {
    val articleUrl = url?.takeIf { it.isNotBlank() } ?: return null
    val articleTitle = title.trim()
    if (articleTitle.isBlank()) return null
    return NewsArticle(
        sourceId = source?.id.orEmpty(),
        sourceName = source?.name.orEmpty(),
        author = author?.takeIf { it.isNotBlank() },
        title = articleTitle,
        description = description?.takeIf { it.isNotBlank() },
        url = articleUrl,
        imageUrl = imageUrl?.takeIf { it.isNotBlank() },
        publishedAt = publishedAt,
        content = content?.takeIf { it.isNotBlank() }
    )
}
