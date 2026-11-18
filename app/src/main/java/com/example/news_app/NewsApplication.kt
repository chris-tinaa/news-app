package com.example.news_app

import android.app.Application
import com.example.news_app.di.AppContainer

class NewsApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(BuildConfig.NEWS_API_KEY)
    }
}
