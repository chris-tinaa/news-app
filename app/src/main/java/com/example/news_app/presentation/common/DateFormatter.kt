package com.example.news_app.presentation.common

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val articleDateFormatter = DateTimeFormatter
    .ofPattern("dd MMM yyyy, HH:mm", Locale.getDefault())
    .withZone(ZoneId.systemDefault())

fun formatPublishedAt(value: String?): String {
    if (value.isNullOrBlank()) return ""
    return runCatching {
        articleDateFormatter.format(Instant.parse(value))
    }.getOrDefault(value)
}
