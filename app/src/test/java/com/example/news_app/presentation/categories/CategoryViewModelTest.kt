package com.example.news_app.presentation.categories

import com.example.news_app.domain.model.NewsCategories
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryViewModelTest {

    @Test
    fun `exposes every news category in display order`() {
        assertEquals(NewsCategories.all, CategoryViewModel().categories)
    }

    @Test
    fun `all seven categories are displayed`() {
        val ids = CategoryViewModel().categories.map { it.id }

        assertEquals(
            listOf(
                "business",
                "entertainment",
                "general",
                "health",
                "science",
                "sports",
                "technology"
            ),
            ids
        )
    }

    @Test
    fun `categories have unique non blank identifiers and titles`() {
        val categories = CategoryViewModel().categories

        assertTrue(categories.isNotEmpty())
        assertEquals(categories.size, categories.map { it.id }.distinct().size)
        assertTrue(categories.all { it.id.isNotBlank() && it.title.isNotBlank() })
    }
}
