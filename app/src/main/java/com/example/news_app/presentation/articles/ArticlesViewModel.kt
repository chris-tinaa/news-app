package com.example.news_app.presentation.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.news_app.domain.model.NewsArticle
import com.example.news_app.domain.repository.NewsRepository
import com.example.news_app.presentation.common.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ArticlesUiState(
    val sourceId: String,
    val sourceName: String,
    val query: String = "",
    val articles: List<NewsArticle> = emptyList(),
    val page: Int = 0,
    val totalResults: Int = 0,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null
)

class ArticlesViewModel(
    private val repository: NewsRepository,
    sourceId: String,
    sourceName: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ArticlesUiState(
            sourceId = sourceId,
            sourceName = sourceName
        )
    )
    val uiState: StateFlow<ArticlesUiState> = _uiState.asStateFlow()

    private var requestJob: Job? = null
    private var searchJob: Job? = null

    init {
        loadFirstPage()
    }

    fun onQueryChanged(value: String) {
        _uiState.update { it.copy(query = value) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            loadFirstPage()
        }
    }

    fun loadFirstPage() {
        requestJob?.cancel()
        requestJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    articles = emptyList(),
                    page = 0,
                    totalResults = 0,
                    isLoading = true,
                    isLoadingMore = false,
                    hasMore = true,
                    error = null
                )
            }
            runCatching {
                repository.getArticles(
                    sourceId = _uiState.value.sourceId,
                    query = _uiState.value.query.trim(),
                    page = 1
                )
            }.onSuccess { result ->
                val articles = result.articles.distinctBy { it.url }
                _uiState.update {
                    it.copy(
                        articles = articles,
                        page = 1,
                        totalResults = result.totalResults,
                        isLoading = false,
                        hasMore = hasMore(
                            page = 1,
                            totalResults = result.totalResults,
                            articleCount = articles.size
                        ),
                        error = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasMore = false,
                        error = throwable.toUserMessage()
                    )
                }
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || !state.hasMore) return

        requestJob = viewModelScope.launch {
            val nextPage = state.page + 1
            _uiState.update { it.copy(isLoadingMore = true, error = null) }
            runCatching {
                repository.getArticles(
                    sourceId = state.sourceId,
                    query = state.query.trim(),
                    page = nextPage
                )
            }.onSuccess { result ->
                val currentUrls = _uiState.value.articles.mapTo(mutableSetOf()) { it.url }
                val newArticles = result.articles.filter { currentUrls.add(it.url) }
                _uiState.update {
                    it.copy(
                        articles = it.articles + newArticles,
                        page = nextPage,
                        totalResults = result.totalResults,
                        isLoadingMore = false,
                        hasMore = newArticles.isNotEmpty() && hasMore(
                            page = nextPage,
                            totalResults = result.totalResults,
                            articleCount = it.articles.size + newArticles.size
                        ),
                        error = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        error = throwable.toUserMessage()
                    )
                }
            }
        }
    }

    private fun hasMore(
        page: Int,
        totalResults: Int,
        articleCount: Int
    ): Boolean {
        return articleCount > 0 && page * PAGE_SIZE < totalResults
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}

class ArticlesViewModelFactory(
    private val repository: NewsRepository,
    private val sourceId: String,
    private val sourceName: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArticlesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ArticlesViewModel(repository, sourceId, sourceName) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
