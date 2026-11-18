package com.example.coastlinenews.data.remote

class MissingApiKeyException : IllegalStateException()

class NewsApiException(
    message: String
) : IllegalStateException(message)
