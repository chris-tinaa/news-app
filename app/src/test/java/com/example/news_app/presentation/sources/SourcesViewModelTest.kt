package com.example.news_app.presentation.sources

import com.example.news_app.data.remote.MissingApiKeyException
import com.example.news_app.domain.model.ArticlePage
import com.example.news_app.domain.model.NewsSource
import com.example.news_app.domain.repository.NewsRepository
import com.example.news_app.testing.httpException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SourcesViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeNewsRepository()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `loads sources sorted by name with fixed page size`() = runTest(dispatcher) {
        repository.sources = listOf(source("zeta"), source("Alpha"), source("beta"))

        val viewModel = SourcesViewModel(repository, category = "general")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("general"), repository.categories)
        assertEquals(listOf("Alpha", "beta", "zeta"), state.allSources.map { it.name })
        assertEquals(8, state.visibleCount)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `failed load exposes a user message and stops loading`() = runTest(dispatcher) {
        repository.error = MissingApiKeyException()

        val viewModel = SourcesViewModel(repository, category = "general")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Missing Authorization", state.error)
        assertFalse(state.isLoading)
        assertTrue(state.allSources.isEmpty())
    }

    @Test
    fun `retry clears the error and loads sources`() = runTest(dispatcher) {
        repository.error = MissingApiKeyException()
        val viewModel = SourcesViewModel(repository, category = "general")
        advanceUntilIdle()

        repository.error = null
        repository.sources = listOf(source("Alpha"))
        viewModel.load()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.error)
        assertEquals(1, state.allSources.size)
    }

    @Test
    fun `query change filters sources and resets visible count`() = runTest(dispatcher) {
        repository.sources = (1..20).map { source("Source $it") }
        val viewModel = SourcesViewModel(repository, category = "general")
        advanceUntilIdle()
        viewModel.loadMore()
        assertEquals(16, viewModel.uiState.value.visibleCount)

        viewModel.onQueryChanged("Source 1")

        val state = viewModel.uiState.value
        assertEquals(8, state.visibleCount)
        assertEquals(11, state.filteredSources.size)
        assertEquals(8, state.visibleSources.size)
    }

    @Test
    fun `load more reveals the next batch and stops at the end`() = runTest(dispatcher) {
        repository.sources = (1..10).map { source("Source $it") }
        val viewModel = SourcesViewModel(repository, category = "general")
        advanceUntilIdle()

        viewModel.loadMore()
        assertEquals(10, viewModel.uiState.value.visibleSources.size)
        assertFalse(viewModel.uiState.value.canLoadMore)

        viewModel.loadMore()

        assertEquals(16, viewModel.uiState.value.visibleCount)
        assertEquals(10, viewModel.uiState.value.visibleSources.size)
    }

    @Test
    fun `load more does nothing when everything is already visible`() = runTest(dispatcher) {
        repository.sources = listOf(source("Alpha"))
        val viewModel = SourcesViewModel(repository, category = "general")
        advanceUntilIdle()

        viewModel.loadMore()

        assertEquals(8, viewModel.uiState.value.visibleCount)
    }

    @Test
    fun `empty source list is not an error`() = runTest(dispatcher) {
        repository.sources = emptyList()

        val viewModel = SourcesViewModel(repository, category = "general")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.error)
        assertFalse(state.isLoading)
        assertTrue(state.allSources.isEmpty())
        assertTrue(state.visibleSources.isEmpty())
        assertFalse(state.canLoadMore)
    }

    @Test
    fun `http 401 exposes unauthorized access`() = runTest(dispatcher) {
        repository.error = httpException(401)

        val viewModel = SourcesViewModel(repository, category = "general")
        advanceUntilIdle()

        assertEquals("Unauthorized Access", viewModel.uiState.value.error)
    }

    @Test
    fun `http 429 exposes rate limit message`() = runTest(dispatcher) {
        repository.error = httpException(429)

        val viewModel = SourcesViewModel(repository, category = "general")
        advanceUntilIdle()

        assertEquals(
            "Rate limit has been reached. Try again later.",
            viewModel.uiState.value.error
        )
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

    private class FakeNewsRepository : NewsRepository {
        var sources: List<NewsSource> = emptyList()
        var error: Throwable? = null
        val categories = mutableListOf<String>()

        override suspend fun getSources(category: String): List<NewsSource> {
            categories += category
            error?.let { throw it }
            return sources
        }

        override suspend fun getArticles(
            sourceId: String,
            query: String,
            page: Int
        ): ArticlePage = ArticlePage(emptyList(), 0)
    }
}
