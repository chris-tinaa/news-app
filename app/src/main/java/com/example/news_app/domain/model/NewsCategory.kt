package com.example.news_app.domain.model

data class NewsCategory(
    val id: String,
    val title: String,
    val description: String
)

object NewsCategories {
    val all = listOf(
        NewsCategory("business", "Business", "Markets, companies and money"),
        NewsCategory("entertainment", "Entertainment", "Film, music and culture"),
        NewsCategory("general", "General", "The stories making the news"),
        NewsCategory("health", "Health", "Health, medicine and wellbeing"),
        NewsCategory("science", "Science", "Research, space and discovery"),
        NewsCategory("sports", "Sports", "Teams, games and competition"),
        NewsCategory("technology", "Technology", "Products, software and innovation")
    )
}
