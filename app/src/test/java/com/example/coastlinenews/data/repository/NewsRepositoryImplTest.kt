package com.example.news_app.data.repository

import com.example.news_app.data.remote.MissingApiKeyException
import com.example.news_app.data.remote.NewsApiException
import com.example.news_app.data.remote.NewsApiService
import com.example.news_app.data.remote.model.ArticlesResponse
import com.example.news_app.data.remote.model.NewsArticleDto
import com.example.news_app.data.remote.model.NewsSourceDto
import com.example.news_app.data.remote.model.SourcesResponse
import com.example.news_app.data.remote.model.SourceRefDto
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class NewsRepositoryImplTest {

    private val api = FakeNewsApiService()

    private fun repository(apiKey: String = "key") = NewsRepositoryImpl(api, apiKey)

    @Test
    fun `blank api key fails before any request`() {
        assertThrows(MissingApiKeyException::class.java) {
            runBlocking { repository(apiKey = "  ").getSources("general") }
        }
        assertThrows(MissingApiKeyException::class.java) {
            runBlocking { repository(apiKey = "").getArticles("bbc-news", "", 1) }
        }
        assertEquals(emptyList<String>(), api.calls)
    }

    @Test
    fun `getSources maps sources and drops those without id`() = runBlocking {
        api.sourcesResponse = SourcesResponse(
            status = "ok",
            sources = listOf(
                NewsSourceDto(id = "bbc-news", name = "BBC News"),
                NewsSourceDto(id = null, name = "No id"),
                NewsSourceDto(id = " ", name = "Blank id")
            )
        )

        val sources = repository().getSources("general")

        assertEquals(listOf("sources:general"), api.calls)
        assertEquals(1, sources.size)
        assertEquals("bbc-news", sources.single().id)
        assertEquals("BBC News", sources.single().name)
    }

    @Test
    fun `getSources throws api message on error status`() {
        api.sourcesResponse = SourcesResponse(
            status = "error",
            code = "rateLimited",
            message = "Rate limit exceeded."
        )

        val error = assertThrows(NewsApiException::class.java) {
            runBlocking { repository().getSources("general") }
        }
        assertEquals("Rate limit exceeded.", error.message)
    }

    @Test
    fun `getSources falls back to default message on error without message`() {
        api.sourcesResponse = SourcesResponse(status = "error", message = null)

        val error = assertThrows(NewsApiException::class.java) {
            runBlocking { repository().getSources("general") }
        }
        assertEquals("News API error", error.message)
    }

    @Test
    fun `blank query fetches top headlines with fixed page size`() = runBlocking {
        api.articlesResponse = ArticlesResponse(status = "ok", totalResults = 0)

        repository().getArticles("bbc-news", "   ", page = 3)

        assertEquals(listOf("headlines:bbc-news:3:20"), api.calls)
    }

    @Test
    fun `non blank query searches articles`() = runBlocking {
        api.articlesResponse = ArticlesResponse(status = "ok")

        repository().getArticles("bbc-news", "election", page = 2)

        assertEquals(listOf("search:bbc-news:election:2:20"), api.calls)
    }

    @Test
    fun `getArticles maps articles and total results`() = runBlocking {
        api.articlesResponse = ArticlesResponse(
            status = "ok",
            totalResults = 120,
            articles = listOf(
                NewsArticleDto(
                    source = SourceRefDto(id = "bbc-news", name = "BBC News"),
                    title = "Headline",
                    url = "https://example.com/a"
                ),
                NewsArticleDto(title = "No url"),
                NewsArticleDto(title = " ", url = "https://example.com/b")
            )
        )

        val page = repository().getArticles("bbc-news", "", 1)

        assertEquals(120, page.totalResults)
        assertEquals(1, page.articles.size)
        assertEquals("Headline", page.articles.single().title)
        assertEquals("bbc-news", page.articles.single().sourceId)
    }

    @Test
    fun `getArticles throws api message on error status`() {
        api.articlesResponse = ArticlesResponse(
            status = "error",
            code = "apiKeyInvalid",
            message = "Your API key is invalid."
        )

        val error = assertThrows(NewsApiException::class.java) {
            runBlocking { repository().getArticles("bbc-news", "", 1) }
        }
        assertEquals("Your API key is invalid.", error.message)
    }

    private class FakeNewsApiService : NewsApiService {
        val calls = mutableListOf<String>()
        var sourcesResponse = SourcesResponse(status = "ok")
        var articlesResponse = ArticlesResponse(status = "ok")

        override suspend fun getSources(category: String): SourcesResponse {
            calls += "sources:$category"
            return sourcesResponse
        }

        override suspend fun getTopHeadlines(
            sourceId: String,
            page: Int,
            pageSize: Int
        ): ArticlesResponse {
            calls += "headlines:$sourceId:$page:$pageSize"
            return articlesResponse
        }

        override suspend fun searchArticles(
            sourceId: String,
            query: String,
            page: Int,
            pageSize: Int,
            sortBy: String
        ): ArticlesResponse {
            calls += "search:$sourceId:$query:$page:$pageSize"
            return articlesResponse
        }
    }
}
