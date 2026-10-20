package com.example.guardiannews

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.guardiannews.ui.screens.ArticleDetailScreen
import com.example.guardiannews.ui.screens.NewsFeedScreen
import com.example.guardiannews.ui.screens.SettingsScreen
import com.example.guardiannews.ui.theme.GuardianNewsTheme
import com.example.guardiannews.viewmodel.NewsFeedViewModel

object AppDestinations {
    const val FEED_ROUTE = "feed"
    const val SETTINGS_ROUTE = "settings"
    const val ARTICLE_ID_ARG = "articleId"
    const val DETAIL_ROUTE_TEMPLATE = "article/{$ARTICLE_ID_ARG}"

    fun createDetailRoute(articleId: String): String {
        return "article/${Uri.encode(articleId)}"
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GuardianNewsTheme {
                GuardianNewsApp()
            }
        }
    }
}

@Composable
fun GuardianNewsApp(
    newsViewModel: NewsFeedViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.FEED_ROUTE
    ) {
        composable(route = AppDestinations.FEED_ROUTE) {
            NewsFeedScreen(
                newsViewModel = newsViewModel,
                onItemClick = { articleId ->
                    navController.navigate(AppDestinations.createDetailRoute(articleId))
                },
                onSettingsClick = {
                    navController.navigate(AppDestinations.SETTINGS_ROUTE)
                }
            )
        }

        composable(
            route = AppDestinations.DETAIL_ROUTE_TEMPLATE,
            arguments = listOf(
                navArgument(AppDestinations.ARTICLE_ID_ARG) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val encodedId = backStackEntry.arguments?.getString(AppDestinations.ARTICLE_ID_ARG).orEmpty()
            val articleId = Uri.decode(encodedId)

            ArticleDetailScreen(
                articleId = articleId,
                newsViewModel = newsViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = AppDestinations.SETTINGS_ROUTE) {
            SettingsScreen(
                newsViewModel = newsViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
