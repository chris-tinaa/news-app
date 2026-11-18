package com.example.news_app.di

import com.example.news_app.data.repository.NewsRepositoryImpl
import org.junit.Assert.assertTrue
import org.junit.Test

class AppContainerTest {

    @Test
    fun `news repository is wired to the news api`() {
        val repository = AppContainer(apiKey = "key").newsRepository

        assertTrue(repository is NewsRepositoryImpl)
    }
}
