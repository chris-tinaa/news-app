package com.example.news_app.presentation.sources

import com.example.news_app.domain.model.NewsSource

fun filterSources(
    sources: List<NewsSource>,
    query: String
): List<NewsSource> {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isBlank()) return sources
    return sources.filter {
        it.name.contains(normalizedQuery, ignoreCase = true) ||
            it.description.contains(normalizedQuery, ignoreCase = true)
    }
}
