package com.example.news_app.presentation.common

import com.example.news_app.data.remote.MissingApiKeyException
import com.example.news_app.data.remote.NewsApiException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ErrorMessagesTest {

    @Test
    fun `missing api key maps to missing authorization`() {
        assertEquals("Missing Authorization", MissingApiKeyException().toUserMessage())
    }

    @Test
    fun `io exception maps to no internet connection`() {
        assertEquals("No internet connection", IOException().toUserMessage())
    }

    @Test
    fun `http 401 maps to unauthorized access`() {
        assertEquals("Unauthorized Access", httpError(401).toUserMessage())
    }

    @Test
    fun `http 429 maps to rate limit message`() {
        assertEquals(
            "Rate limit has been reached. Try again later.",
            httpError(429).toUserMessage()
        )
    }

    @Test
    fun `other http codes include the status code`() {
        assertEquals("News API returned HTTP 500.", httpError(500).toUserMessage())
    }

    @Test
    fun `news api exception uses its message`() {
        assertEquals("Rate limit exceeded.", NewsApiException("Rate limit exceeded.").toUserMessage())
    }

    @Test
    fun `unknown throwable maps to generic message`() {
        assertEquals("Something went wrong", IllegalStateException("boom").toUserMessage())
    }

    private fun httpError(code: Int): HttpException = HttpException(
        Response.error<Any>(code, "{}".toResponseBody("application/json".toMediaType()))
    )
}
