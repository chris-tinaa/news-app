package com.example.news_app.presentation.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.news_app.domain.model.NewsCategory
import com.example.news_app.domain.repository.NewsRepository
import com.example.news_app.presentation.categories.CategoryScreen

private object Routes {
    const val categories = "categories"

    fun sources(category: NewsCategory): String =
        "sources/${Uri.encode(category.id)}"

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
    }
}
