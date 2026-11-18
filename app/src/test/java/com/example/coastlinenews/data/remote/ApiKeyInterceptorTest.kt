package com.example.news_app.data.remote

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.concurrent.atomic.AtomicReference

class ApiKeyInterceptorTest {

    private fun requestThroughApiKeyInterceptor(apiKey: String): Request {
        val captured = AtomicReference<Request>()
        val terminal = Interceptor { chain ->
            captured.set(chain.request())
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("{}".toResponseBody("application/json".toMediaType()))
                .build()
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(ApiKeyInterceptor(apiKey))
            .addInterceptor(terminal)
            .build()

        client.newCall(Request.Builder().url("https://newsapi.org/v2/top-headlines").build())
            .execute()
            .close()

        return captured.get()
    }

    @Test
    fun `adds api key header`() {
        assertEquals("secret-key", requestThroughApiKeyInterceptor("secret-key").header("X-Api-Key"))
    }

    @Test
    fun `blank api key adds no header`() {
        assertNull(requestThroughApiKeyInterceptor("   ").header("X-Api-Key"))
    }

    @Test
    fun `request url is unchanged`() {
        assertEquals(
            "https://newsapi.org/v2/top-headlines",
            requestThroughApiKeyInterceptor("secret-key").url.toString()
        )
    }
}
