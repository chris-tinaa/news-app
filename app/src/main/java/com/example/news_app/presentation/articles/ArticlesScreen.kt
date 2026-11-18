package com.example.news_app.presentation.articles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.snapshotFlow
import coil3.compose.AsyncImage
import com.example.news_app.domain.model.NewsArticle
import com.example.news_app.domain.repository.NewsRepository
import com.example.news_app.presentation.common.EmptyState
import com.example.news_app.presentation.common.ErrorState
import com.example.news_app.presentation.common.LoadingFooter
import com.example.news_app.presentation.common.LoadingState
import com.example.news_app.presentation.common.SearchField
import com.example.news_app.presentation.common.formatPublishedAt
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun ArticlesScreen(
    sourceId: String,
    sourceName: String,
    repository: NewsRepository,
    onBack: () -> Unit,
    onArticleSelected: (NewsArticle) -> Unit
) {
    val viewModel: ArticlesViewModel = viewModel(
        factory = ArticlesViewModelFactory(repository, sourceId, sourceName)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(listState, uiState.articles.size, uiState.hasMore) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
        }.distinctUntilChanged().collect { lastIndex ->
            if (uiState.hasMore && lastIndex >= uiState.articles.lastIndex - 2) {
                viewModel.loadMore()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = sourceName.ifBlank { "News" },
                        maxLines = 1
                    )
                },
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
                placeholder = "Search articles",
                onValueChange = viewModel::onQueryChanged,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
            when {
                uiState.isLoading && uiState.articles.isEmpty() -> LoadingState()
                uiState.error != null && uiState.articles.isEmpty() -> ErrorState(
                    message = uiState.error.orEmpty(),
                    onRetry = viewModel::loadFirstPage
                )
                uiState.articles.isEmpty() -> EmptyState(
                    message = if (uiState.query.isBlank()) {
                        "No articles are available right now."
                    } else {
                        "No articles match \"${uiState.query}\"."
                    },
                    modifier = Modifier.weight(1f)
                )
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.articles,
                        key = { it.url }
                    ) { article ->
                        ArticleCard(
                            article = article,
                            onClick = { onArticleSelected(article) }
                        )
                    }
                    if (uiState.isLoadingMore) {
                        item {
                            LoadingFooter(
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                    if (uiState.error != null && uiState.articles.isNotEmpty() && !uiState.isLoadingMore) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = uiState.error.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                                TextButton(onClick = viewModel::loadMore) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleCard(
    article: NewsArticle,
    onClick: () -> Unit
) {
    val validUrl = remember(article.url) {
        article.url.startsWith("https://")
    }
    ElevatedCard(
        onClick = onClick,
        enabled = validUrl,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            ArticleImage(article.imageUrl)
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium
                )
                article.description?.let {
                    Text(
                        text = it,
                        maxLines = 3,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = buildString {
                        append(article.sourceName.ifBlank { "News" })
                        formatPublishedAt(article.publishedAt)
                            .takeIf { it.isNotBlank() }
                            ?.let {
                                append(" • ")
                                append(it)
                            }
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                article.author?.let {
                    Text(
                        text = it,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!validUrl) {
                    RowUnavailable()
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Article,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


@Composable
private fun ArticleImage(imageUrl: String?) {
    var imageFailed by remember(imageUrl) { mutableStateOf(false) }
    if (imageUrl.isNullOrBlank() || imageFailed) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.BrokenImage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
            contentScale = ContentScale.Crop,
            onError = { imageFailed = true }
        )
    }
}

@Composable
private fun RowUnavailable() {
    Text(
        text = "Article link unavailable",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.error
    )
}
