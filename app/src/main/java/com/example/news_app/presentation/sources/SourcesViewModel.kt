package com.example.news_app.presentation.sources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.news_app.domain.model.NewsSource
import com.example.news_app.domain.repository.NewsRepository
import com.example.news_app.presentation.common.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SourcesUiState(
    val category: String,
    val allSources: List<NewsSource> = emptyList(),
    val query: String = "",
    val visibleCount: Int = 8,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val filteredSources: List<NewsSource>
        get() = filterSources(allSources, query)

    val visibleSources: List<NewsSource>
        get() = filteredSources.take(visibleCount)

    val canLoadMore: Boolean
        get() = visibleCount < filteredSources.size
}

class SourcesViewModel(
    private val repository: NewsRepository,
    private val category: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(SourcesUiState(category = category))
    val uiState: StateFlow<SourcesUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                repository.getSources(category)
            }.onSuccess { sources ->
                _uiState.update {
                    it.copy(
                        allSources = sources.sortedBy { source -> source.name.lowercase() },
                        visibleCount = 8,
                        isLoading = false,
                        error = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isLoading = false, error = throwable.toUserMessage())
                }
            }
        }
    }

    fun onQueryChanged(value: String) {
        _uiState.update { it.copy(query = value, visibleCount = 8) }
    }

    fun loadMore() {
        _uiState.update { state ->
            if (state.canLoadMore) {
                state.copy(visibleCount = state.visibleCount + 8)
            } else {
                state
            }
        }
    }
}

class SourcesViewModelFactory(
    private val repository: NewsRepository,
    private val category: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SourcesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SourcesViewModel(repository, category) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
