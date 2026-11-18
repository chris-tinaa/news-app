package com.example.news_app.presentation.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class DateFormatterTest {

    @Test
    fun `null and blank values format to empty string`() {
        assertEquals("", formatPublishedAt(null))
        assertEquals("", formatPublishedAt(""))
        assertEquals("", formatPublishedAt("   "))
    }

    @Test
    fun `valid instant is formatted in system zone`() {
        val instant = Instant.parse("2026-11-19T10:00:00Z")
        val expected = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.getDefault())
            .withZone(ZoneId.systemDefault())
            .format(instant)

        assertEquals(expected, formatPublishedAt("2026-11-19T10:00:00Z"))
    }

    @Test
    fun `valid instant renders day month year and time`() {
        val formatted = formatPublishedAt("2026-11-19T12:00:00Z")

        assertTrue(formatted.matches(Regex("""\d{2} .+ \d{4}, \d{2}:\d{2}""")))
        assertTrue(formatted.contains("2026"))
    }

    @Test
    fun `unparsable value is returned unchanged`() {
        assertEquals("yesterday", formatPublishedAt("yesterday"))
    }
}
