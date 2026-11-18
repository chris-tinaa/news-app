package com.example.news_app.presentation.sources

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import com.example.news_app.domain.model.NewsSource
import com.example.news_app.presentation.common.EmptyState
import com.example.news_app.presentation.common.ErrorState
import com.example.news_app.presentation.common.LoadingState
import com.example.news_app.presentation.common.SearchField

@Composable
fun SourcesScreen(
    category: String,
    repository: com.example.news_app.domain.repository.NewsRepository,
    onBack: () -> Unit,
    onSourceSelected: (NewsSource) -> Unit
) {
    val viewModel: SourcesViewModel = viewModel(
        factory = SourcesViewModelFactory(repository, category)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(listState, uiState.visibleSources.size, uiState.canLoadMore) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
        }.distinctUntilChanged().collect { lastIndex ->
            if (uiState.canLoadMore && lastIndex >= uiState.visibleSources.lastIndex - 2) {
                viewModel.loadMore()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category.replaceFirstChar { it.uppercase() }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchField(
                value = uiState.query,
                placeholder = "Search sources",
                onValueChange = viewModel::onQueryChanged,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
            when {
                uiState.isLoading && uiState.allSources.isEmpty() -> LoadingState()
                uiState.error != null && uiState.allSources.isEmpty() -> ErrorState(
                    message = uiState.error.orEmpty(),
                    onRetry = viewModel::load
                )
                uiState.visibleSources.isEmpty() -> EmptyState(
                    message = if (uiState.query.isBlank()) {
                        "No news sources are available for this category."
                    } else {
                        "No sources match \"${uiState.query}\"."
                    },
                    modifier = Modifier.weight(1f)
                )
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = uiState.visibleSources,
                        key = { it.id }
                    ) { source ->
                        SourceCard(
                            source = source,
                            onClick = { onSourceSelected(source) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceCard(
    source: NewsSource,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = source.name,
                style = MaterialTheme.typography.titleMedium
            )
            if (source.description.isNotBlank()) {
                Text(
                    text = source.description,
                    maxLines = 3,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = listOf(
                        source.language.uppercase(),
                        source.country.uppercase()
                    ).filter { it.isNotBlank() }.joinToString(" • "),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (source.url.isNotBlank()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = source.url.removePrefix("https://").removePrefix("http://"),
                            maxLines = 1,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
