package com.example.news_app.data.remote

import com.example.news_app.data.remote.model.NewsArticleDto
import com.example.news_app.data.remote.model.NewsSourceDto
import com.example.news_app.data.remote.model.SourceRefDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NewsApiMappersTest {

    @Test
    fun `source maps every field`() {
        val dto = NewsSourceDto(
            id = "bbc-news",
            name = "BBC News",
            description = "Trusted world news",
            url = "https://www.bbc.co.uk/news",
            category = "general",
            language = "en",
            country = "gb"
        )

        val source = dto.toDomain()

        assertEquals(
            com.example.news_app.domain.model.NewsSource(
                id = "bbc-news",
                name = "BBC News",
                description = "Trusted world news",
                url = "https://www.bbc.co.uk/news",
                category = "general",
                language = "en",
                country = "gb"
            ),
            source
        )
    }

    @Test
    fun `source without id is dropped`() {
        assertNull(NewsSourceDto(id = null, name = "No id").toDomain())
    }

    @Test
    fun `source with blank id is dropped`() {
        assertNull(NewsSourceDto(id = "   ", name = "Blank id").toDomain())
    }

    @Test
    fun `source blank optional fields become empty strings`() {
        val source = NewsSourceDto(id = "abc", name = "ABC", description = null, url = null)
            .toDomain()

        assertEquals("", source?.description)
        assertEquals("", source?.url)
        assertEquals("", source?.category)
        assertEquals("", source?.language)
        assertEquals("", source?.country)
    }

    @Test
    fun `article maps every field`() {
        val dto = NewsArticleDto(
            source = SourceRefDto(id = "bbc-news", name = "BBC News"),
            author = "Jane Doe",
            title = "Headline",
            description = "Short summary",
            url = "https://example.com/article",
            imageUrl = "https://example.com/image.jpg",
            publishedAt = "2026-11-19T10:00:00Z",
            content = "Body"
        )

        val article = dto.toDomain()

        assertEquals("bbc-news", article?.sourceId)
        assertEquals("BBC News", article?.sourceName)
        assertEquals("Jane Doe", article?.author)
        assertEquals("Headline", article?.title)
        assertEquals("Short summary", article?.description)
        assertEquals("https://example.com/article", article?.url)
        assertEquals("https://example.com/image.jpg", article?.imageUrl)
        assertEquals("2026-11-19T10:00:00Z", article?.publishedAt)
        assertEquals("Body", article?.content)
    }

    @Test
    fun `article with missing url is dropped`() {
        assertNull(NewsArticleDto(title = "No url").toDomain())
    }

    @Test
    fun `article with blank url is dropped`() {
        assertNull(NewsArticleDto(title = "Blank url", url = " ").toDomain())
    }

    @Test
    fun `article with blank title is dropped`() {
        assertNull(NewsArticleDto(title = "   ", url = "https://example.com/a").toDomain())
    }

    @Test
    fun `article title is trimmed`() {
        val article = NewsArticleDto(title = "  Spaced  ", url = "https://example.com/a").toDomain()

        assertEquals("Spaced", article?.title)
    }

    @Test
    fun `article blank optional fields become null`() {
        val article = NewsArticleDto(
            source = null,
            author = " ",
            title = "Headline",
            description = " ",
            url = "https://example.com/a",
            imageUrl = "",
            publishedAt = null,
            content = " "
        ).toDomain()

        assertEquals("", article?.sourceId)
        assertEquals("", article?.sourceName)
        assertNull(article?.author)
        assertNull(article?.description)
        assertNull(article?.imageUrl)
        assertNull(article?.publishedAt)
        assertNull(article?.content)
    }
}
