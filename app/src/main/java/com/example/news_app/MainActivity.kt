package com.example.news_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.compose.ui.platform.LocalContext
import com.example.news_app.presentation.navigation.AppNavGraph
import com.example.news_app.ui.theme.CoastlineNewsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)
        setContent {
            CoastlineNewsTheme {
                val application = LocalContext.current.applicationContext as NewsApplication
                AppNavGraph(repository = application.container.newsRepository)
            }
        }
    }
}
