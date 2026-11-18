package com.example.coastlinenews.data.remote.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NewsApiResponseSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `articles response decodes news api payload`() {
        val body = """
            {
              "status": "ok",
              "totalResults": 42,
              "articles": [
                {
                  "source": {"id": "bbc-news", "name": "BBC News"},
                  "author": "Jane Doe",
                  "title": "Headline",
                  "description": "Short summary",
                  "url": "https://example.com/article",
                  "urlToImage": "https://example.com/image.jpg",
                  "publishedAt": "2026-11-19T10:00:00Z",
                  "content": "Body"
                }
              ]
            }
        """.trimIndent()

        val response = json.decodeFromString<ArticlesResponse>(body)

        assertEquals("ok", response.status)
        assertEquals(42, response.totalResults)
        assertEquals("https://example.com/image.jpg", response.articles.single().imageUrl)
        assertEquals("bbc-news", response.articles.single().source?.id)
    }

    @Test
    fun `error response decodes code and message`() {
        val body = """{"status":"error","code":"apiKeyInvalid","message":"Your API key is invalid."}"""

        val response = json.decodeFromString<ArticlesResponse>(body)

        assertEquals("error", response.status)
        assertEquals("apiKeyInvalid", response.code)
        assertEquals("Your API key is invalid.", response.message)
        assertEquals(0, response.totalResults)
        assertEquals(emptyList<NewsArticleDto>(), response.articles)
    }

    @Test
    fun `sources response defaults missing collections and error fields`() {
        val response = json.decodeFromString<SourcesResponse>("""{"status":"ok"}""")

        assertEquals("ok", response.status)
        assertEquals(emptyList<NewsSourceDto>(), response.sources)
        assertNull(response.code)
        assertNull(response.message)
    }

    @Test
    fun `unknown fields are ignored`() {
        val response = json.decodeFromString<SourcesResponse>(
            """{"status":"ok","sources":[],"extra":{"nested":true}}"""
        )

        assertEquals("ok", response.status)
    }
}
