package com.example.news_app.presentation.sources

import com.example.news_app.domain.model.NewsSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SourcesUiStateTest {

    private val sources = (1..20).map { index -> source("Source $index") }

    @Test
    fun `visible sources are capped at visible count`() {
        val state = SourcesUiState(category = "general", allSources = sources, visibleCount = 8)

        assertEquals(sources.take(8), state.visibleSources)
    }

    @Test
    fun `can load more while hidden sources remain`() {
        val state = SourcesUiState(category = "general", allSources = sources, visibleCount = 8)

        assertTrue(state.canLoadMore)
    }

    @Test
    fun `cannot load more once every source is visible`() {
        val state = SourcesUiState(category = "general", allSources = sources, visibleCount = 20)

        assertFalse(state.canLoadMore)
    }

    @Test
    fun `filtered sources follow the query`() {
        val state = SourcesUiState(
            category = "general",
            allSources = listOf(source("BBC News"), source("ESPN")),
            query = "espn"
        )

        assertEquals(listOf("ESPN"), state.filteredSources.map { it.name })
    }

    @Test
    fun `visible sources come from the filtered list`() {
        val state = SourcesUiState(
            category = "general",
            allSources = sources,
            query = "Source 1",
            visibleCount = 8
        )

        assertEquals(8, state.visibleSources.size)
        assertTrue(state.visibleSources.all { it.name.contains("Source 1") })
    }

    @Test
    fun `empty source list has nothing to show or load`() {
        val state = SourcesUiState(category = "general")

        assertEquals(emptyList<NewsSource>(), state.visibleSources)
        assertFalse(state.canLoadMore)
        assertEquals("", state.query)
        assertFalse(state.isLoading)
        assertEquals(null, state.error)
    }

    private fun source(name: String) = NewsSource(
        id = name.lowercase().replace(" ", "-"),
        name = name,
        description = "",
        url = "https://example.com",
        category = "general",
        language = "en",
        country = "us"
    )
}
