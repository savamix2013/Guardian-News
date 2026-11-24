package com.example.guardiannews

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.guardiannews.ui.screens.ArticleDetailScreen
import com.example.guardiannews.ui.screens.FavoritesScreen
import com.example.guardiannews.ui.screens.NewsFeedScreen
import com.example.guardiannews.ui.screens.SettingsScreen
import com.example.guardiannews.ui.theme.GuardianNewsTheme
import com.example.guardiannews.viewmodel.NewsFeedViewModel

object AppDestinations {
    const val FEED_ROUTE = "feed"
    const val FAVORITES_ROUTE = "favorites"
    const val SETTINGS_ROUTE = "settings"
    const val ARTICLE_ID_ARG = "articleId"
    const val DETAIL_ROUTE_TEMPLATE = "article/{$ARTICLE_ID_ARG}"

    fun createDetailRoute(articleId: String): String {
        return "article/${Uri.encode(articleId)}"
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkTheme = isSystemInDarkTheme()
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { darkTheme }
                )
                onDispose {}
            }
            GuardianNewsTheme(darkTheme = darkTheme) {
                GuardianNewsApp()
            }
        }
    }
}
@Composable
fun GuardianNewsApp(
    context: Context = LocalContext.current,
    newsViewModel: NewsFeedViewModel = viewModel(
        factory = NewsFeedViewModel.Factory(context)
    )
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
                onOpenFavorites = {
                    navController.navigate(AppDestinations.FAVORITES_ROUTE)
                },
                onSettingsClick = {
                    navController.navigate(AppDestinations.SETTINGS_ROUTE)
                }
            )
        }

        composable(route = AppDestinations.FAVORITES_ROUTE) {
            val favoriteIds by newsViewModel.favoriteIds.collectAsState()
            val favoritesUiState by newsViewModel.favoritesUiState.collectAsState()

            FavoritesScreen(
                uiState = favoritesUiState,
                favoriteIds = favoriteIds,
                onRefreshFavorites = { newsViewModel.loadFavoriteArticles() },
                onToggleFavorite = { articleId -> newsViewModel.toggleFavorite(articleId) },
                onArticleClick = { article ->
                    navController.navigate(AppDestinations.createDetailRoute(article.id))
                },
                onNavigateBack = { navController.popBackStack() }
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
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
