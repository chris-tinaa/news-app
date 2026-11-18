package com.example.news_app.presentation.common

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HttpsUrlTest {

    @Test
    fun `https url is accepted`() {
        assertTrue(isHttpsUrl("https://example.com/article"))
    }

    @Test
    fun `http url is rejected`() {
        assertFalse(isHttpsUrl("http://example.com/article"))
    }

    @Test
    fun `other schemes are rejected`() {
        assertFalse(isHttpsUrl("javascript:alert(1)"))
        assertFalse(isHttpsUrl("file:///etc/passwd"))
        assertFalse(isHttpsUrl("content://media/1"))
    }

    @Test
    fun `blank and relative values are rejected`() {
        assertFalse(isHttpsUrl(""))
        assertFalse(isHttpsUrl("   "))
        assertFalse(isHttpsUrl("/relative/path"))
    }

    @Test
    fun `uppercase scheme is rejected`() {
        assertFalse(isHttpsUrl("HTTPS://example.com/article"))
    }
}
