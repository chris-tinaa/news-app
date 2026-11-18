package com.example.news_app.presentation.articles

import com.example.news_app.data.remote.NewsApiException
import com.example.news_app.domain.model.ArticlePage
import com.example.news_app.domain.model.NewsArticle
import com.example.news_app.domain.model.NewsSource
import com.example.news_app.domain.repository.NewsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
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
class ArticlesViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeNewsRepository()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `first page loads articles and marks whether more exist`() = runTest(dispatcher) {
        repository.pages[1] = page(
            totalResults = 25,
            articles = listOf(article("a"), article("b"))
        )

        val viewModel = ArticlesViewModel(repository, "bbc-news", "BBC News")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(Request(sourceId = "bbc-news", query = "", page = 1), repository.requests.single())
        assertEquals(listOf("a", "b"), state.articles.map { it.url })
        assertEquals(1, state.page)
        assertEquals(25, state.totalResults)
        assertTrue(state.hasMore)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `first page drops duplicate urls`() = runTest(dispatcher) {
        repository.pages[1] = page(
            totalResults = 10,
            articles = listOf(article("a"), article("a"), article("b"))
        )

        val viewModel = ArticlesViewModel(repository, "bbc-news", "BBC News")
        advanceUntilIdle()

        assertEquals(listOf("a", "b"), viewModel.uiState.value.articles.map { it.url })
    }

    @Test
    fun `last page disables further loading`() = runTest(dispatcher) {
        repository.pages[1] = page(totalResults = 20, articles = listOf(article("a")))

        val viewModel = ArticlesViewModel(repository, "bbc-news", "BBC News")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasMore)
    }

    @Test
    fun `empty first page disables further loading`() = runTest(dispatcher) {
        repository.pages[1] = page(totalResults = 100, articles = emptyList())

        val viewModel = ArticlesViewModel(repository, "bbc-news", "BBC News")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasMore)
    }

    @Test
    fun `failed first page exposes user message and stops loading`() = runTest(dispatcher) {
        repository.error = NewsApiException("Service unavailable")

        val viewModel = ArticlesViewModel(repository, "bbc-news", "BBC News")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Service unavailable", state.error)
        assertFalse(state.isLoading)
        assertFalse(state.hasMore)
        assertTrue(state.articles.isEmpty())
    }

    @Test
    fun `load more appends the next page`() = runTest(dispatcher) {
        repository.pages[1] = page(totalResults = 60, articles = listOf(article("a")))
        repository.pages[2] = page(totalResults = 60, articles = listOf(article("b")))

        val viewModel = ArticlesViewModel(repository, "bbc-news", "BBC News")
        advanceUntilIdle()

        viewModel.loadMore()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(Request("bbc-news", "", 2), repository.requests.last())
        assertEquals(listOf("a", "b"), state.articles.map { it.url })
        assertEquals(2, state.page)
        assertFalse(state.isLoadingMore)
        assertTrue(state.hasMore)
    }

    @Test
    fun `load more skips urls already shown`() = runTest(dispatcher) {
        repository.pages[1] = page(totalResults = 60, articles = listOf(article("a"), article("b")))
        repository.pages[2] = page(totalResults = 60, articles = listOf(article("a"), article("c")))

        val viewModel = ArticlesViewModel(repository, "bbc-news", "BBC News")
        advanceUntilIdle()

        viewModel.loadMore()
        advanceUntilIdle()

        assertEquals(listOf("a", "b", "c"), viewModel.uiState.value.articles.map { it.url })
    }

    @Test
    fun `load more stops when the next page adds nothing new`() = runTest(dispatcher) {
        repository.pages[1] = page(totalResults = 100, articles = listOf(article("a")))
        repository.pages[2] = page(totalResults = 100, articles = listOf(article("a")))

        val viewModel = ArticlesViewModel(repository, "bbc-news", "BBC News")
        advanceUntilIdle()

        viewModel.loadMore()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasMore)
    }

    @Test
    fun `load more is ignored without more pages`() = runTest(dispatcher) {
        repository.pages[1] = page(totalResults = 20, articles = listOf(article("a")))

        val viewModel = ArticlesViewModel(repository, "bbc-news", "BBC News")
        advanceUntilIdle()

        viewModel.loadMore()
        advanceUntilIdle()

        assertEquals(1, repository.requests.size)
        assertEquals(1, viewModel.uiState.value.page)
    }

    @Test
    fun `failed load more keeps articles and exposes message`() = runTest(dispatcher) {
        repository.pages[1] = page(totalResults = 40, articles = listOf(article("a")))
        repository.failingPages += 2

        val viewModel = ArticlesViewModel(repository, "bbc-news", "BBC News")
        advanceUntilIdle()

        viewModel.loadMore()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("a"), state.articles.map { it.url })
        assertEquals("News API error", state.error)
        assertFalse(state.isLoadingMore)
        assertTrue(state.hasMore)
    }

    @Test
    fun `query reload waits for the debounce window`() = runTest(dispatcher) {
        repository.pages[1] = page(totalResults = 20, articles = listOf(article("a")))

        val viewModel = ArticlesViewModel(repository, "bbc-news", "BBC News")
        advanceUntilIdle()

        viewModel.onQueryChanged("election")
        advanceTimeBy(200)
        runCurrent()

        assertEquals("election", viewModel.uiState.value.query)
        assertEquals(1, repository.requests.size)

        advanceTimeBy(200)
        advanceUntilIdle()

        assertEquals(Request("bbc-news", "election", 1), repository.requests.last())
        assertEquals(2, repository.requests.size)
    }

    @Test
    fun `rapid queries only request the final value`() = runTest(dispatcher) {
        repository.pages[1] = page(totalResults = 20, articles = listOf(article("a")))

        val viewModel = ArticlesViewModel(repository, "bbc-news", "BBC News")
        advanceUntilIdle()

        viewModel.onQueryChanged("elec")
        advanceTimeBy(100)
        viewModel.onQueryChanged("election")
        advanceUntilIdle()

        assertEquals(2, repository.requests.size)
        assertEquals(Request("bbc-news", "election", 1), repository.requests.last())
    }

    @Test
    fun `query is trimmed before the request`() = runTest(dispatcher) {
        repository.pages[1] = page(totalResults = 20, articles = listOf(article("a")))

        val viewModel = ArticlesViewModel(repository, "bbc-news", "BBC News")
        advanceUntilIdle()

        viewModel.onQueryChanged("  economy  ")
        advanceUntilIdle()

        assertEquals(Request("bbc-news", "economy", 1), repository.requests.last())
    }

    @Test
    fun `load first page resets paging and results`() = runTest(dispatcher) {
        repository.pages[1] = page(totalResults = 40, articles = listOf(article("a")))
        repository.pages[2] = page(totalResults = 40, articles = listOf(article("b")))

        val viewModel = ArticlesViewModel(repository, "bbc-news", "BBC News")
        advanceUntilIdle()
        viewModel.loadMore()
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.page)

        viewModel.loadFirstPage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.page)
        assertEquals(listOf("a"), state.articles.map { it.url })
        assertEquals(40, state.totalResults)
        assertTrue(state.hasMore)
        assertNull(state.error)
    }

    private fun article(url: String) = NewsArticle(
        sourceId = "bbc-news",
        sourceName = "BBC News",
        author = "Author",
        title = "Title $url",
        description = "Description",
        url = url,
        imageUrl = null,
        publishedAt = "2026-11-19T10:00:00Z",
        content = null
    )

    private fun page(totalResults: Int, articles: List<NewsArticle>) =
        ArticlePage(articles = articles, totalResults = totalResults)

    private data class Request(val sourceId: String, val query: String, val page: Int)

    private class FakeNewsRepository : NewsRepository {
        val requests = mutableListOf<Request>()
        val pages = mutableMapOf<Int, ArticlePage>()
        val failingPages = mutableSetOf<Int>()
        var error: Throwable? = null

        override suspend fun getSources(category: String): List<NewsSource> = emptyList()

        override suspend fun getArticles(
            sourceId: String,
            query: String,
            page: Int
        ): ArticlePage {
            requests += Request(sourceId, query, page)
            error?.let { throw it }
            if (page in failingPages) throw NewsApiException("News API error")
            return pages[page] ?: ArticlePage(emptyList(), 0)
        }
    }
}
