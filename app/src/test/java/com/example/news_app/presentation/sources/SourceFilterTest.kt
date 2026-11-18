package com.example.news_app.presentation.sources

import com.example.news_app.domain.model.NewsSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SourceFilterTest {

    private val bbc = source(name = "BBC News", description = "Trusted world news")
    private val espn = source(name = "ESPN", description = "Sports coverage")
    private val cnn = source(name = "CNN", description = "Breaking politics")

    private val sources = listOf(bbc, espn, cnn)

    @Test
    fun `blank query returns every source unchanged`() {
        assertEquals(sources, filterSources(sources, ""))
        assertEquals(sources, filterSources(sources, "   "))
    }

    @Test
    fun `matches name case insensitively`() {
        assertEquals(listOf(bbc), filterSources(sources, "bbc"))
        assertEquals(listOf(bbc), filterSources(sources, "BBC"))
    }

    @Test
    fun `matches description`() {
        assertEquals(listOf(espn), filterSources(sources, "sports"))
    }

    @Test
    fun `query is trimmed before matching`() {
        assertEquals(listOf(cnn), filterSources(sources, "  politics  "))
    }

    @Test
    fun `no match returns empty list`() {
        assertTrue(filterSources(sources, "weather").isEmpty())
    }

    private fun source(name: String, description: String) = NewsSource(
        id = name.lowercase().replace(" ", "-"),
        name = name,
        description = description,
        url = "https://example.com",
        category = "general",
        language = "en",
        country = "us"
    )
}
