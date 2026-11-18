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
import com.example.news_app.presentation.categories.CategoryScreen
import com.example.news_app.presentation.sources.SourcesScreen

private object Routes {
    const val categories = "categories"
    const val sources = "sources/{category}"

    fun sources(category: NewsCategory): String =
        "sources/${Uri.encode(category.id)}"

    fun articles(source: NewsSource): String =
        "articles/${Uri.encode(source.id)}?name=${Uri.encode(source.name)}"


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
    }
}
