package com.example.news_app.presentation.categories

import androidx.lifecycle.ViewModel
import com.example.news_app.domain.model.NewsCategories

class CategoryViewModel : ViewModel() {
    val categories = NewsCategories.all
}
