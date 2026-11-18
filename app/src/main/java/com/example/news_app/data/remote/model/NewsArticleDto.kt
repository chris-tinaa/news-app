package com.example.coastlinenews.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewsArticleDto(
    val source: SourceRefDto? = null,
    val author: String? = null,
    val title: String = "",
    val description: String? = null,
    val url: String? = null,
    @SerialName("urlToImage")
    val imageUrl: String? = null,
    val publishedAt: String? = null,
    val content: String? = null
)

@Serializable
data class SourceRefDto(
    val id: String? = null,
    val name: String? = null
)
