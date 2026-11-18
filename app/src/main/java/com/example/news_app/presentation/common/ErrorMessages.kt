package com.example.news_app.presentation.common

import com.example.news_app.data.remote.MissingApiKeyException
import com.example.news_app.data.remote.NewsApiException
import java.io.IOException
import retrofit2.HttpException

fun Throwable.toUserMessage(): String = when (this) {
    is MissingApiKeyException -> "Missing Authorization"
    is IOException -> "No internet connection"
    is HttpException -> when (code()) {
        401 -> "Unauthorized Access"
        429 -> "Rate limit has been reached. Try again later."
        else -> "News API returned HTTP ${code()}."
    }
    is NewsApiException -> message ?: "News API error"
    else -> "Something went wrong"
}
