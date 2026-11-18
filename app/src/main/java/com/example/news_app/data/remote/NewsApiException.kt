package com.example.news_app.data.remote

class MissingApiKeyException : IllegalStateException()

class NewsApiException(
    message: String
) : IllegalStateException(message)
