package com.example.news_app.presentation.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.news_app.domain.model.NewsCategory
import com.example.news_app.domain.model.NewsSource
import com.example.news_app.domain.repository.NewsRepository
import com.example.news_app.presentation.articles.ArticlesScreen
import com.example.news_app.presentation.categories.CategoryScreen
import com.example.news_app.presentation.sources.SourcesScreen
import com.example.news_app.presentation.webview.ArticleWebViewScreen

private object Routes {
    const val categories = "categories"
    const val sources = "sources/{category}"
    const val articles = "articles/{sourceId}?name={sourceName}"
    const val webview = "webview?title={title}&url={url}"

    fun sources(category: NewsCategory): String =
        "sources/${Uri.encode(category.id)}"

    fun articles(source: NewsSource): String =
        "articles/${Uri.encode(source.id)}?name=${Uri.encode(source.name)}"

    fun webview(title: String, url: String): String =
        "webview?title=${Uri.encode(title)}&url=${Uri.encode(url)}"
}

@Composable
fun AppNavGraph(
    repository: NewsRepository
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.categories
    ) {
        composable(Routes.categories) {
            CategoryScreen(
                onCategorySelected = {
                    navController.navigate(Routes.sources(it))
                }
            )
        }
        composable(
            route = Routes.sources,
            arguments = listOf(
                navArgument("category") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category").orEmpty()
            SourcesScreen(
                category = category,
                repository = repository,
                onBack = { navController.popBackStack() },
                onSourceSelected = {
                    navController.navigate(Routes.articles(it))
                }
            )
        }
        composable(
            route = Routes.articles,
            arguments = listOf(
                navArgument("sourceId") {
                    type = NavType.StringType
                },
                navArgument("sourceName") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val sourceId = backStackEntry.arguments?.getString("sourceId").orEmpty()
            val sourceName = backStackEntry.arguments?.getString("sourceName").orEmpty()
            ArticlesScreen(
                sourceId = sourceId,
                sourceName = sourceName,
                repository = repository,
                onBack = { navController.popBackStack() },
                onArticleSelected = { article ->
                    navController.navigate(Routes.webview(article.title, article.url))
                }
            )
        }
        composable(
            route = Routes.webview,
            arguments = listOf(
                navArgument("title") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("url") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title").orEmpty()
            val url = backStackEntry.arguments?.getString("url").orEmpty()
            ArticleWebViewScreen(
                title = title,
                url = url,
                onBack = { navController.popBackStack() }
            )
        }
    }
}