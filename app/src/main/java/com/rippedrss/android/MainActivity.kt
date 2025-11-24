package com.rippedrss.android

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rippedrss.android.ui.screens.*
import com.rippedrss.android.ui.theme.RippedRichRSSTheme
import com.rippedrss.android.ui.theme.ThemeMode
import com.rippedrss.android.ui.viewmodel.ArticleViewModel
import com.rippedrss.android.ui.viewmodel.ArticleViewModelFactory
import com.rippedrss.android.ui.viewmodel.FeedViewModel
import com.rippedrss.android.ui.viewmodel.FeedViewModelFactory
import com.rippedrss.android.ui.viewmodel.SettingsViewModel
import com.rippedrss.android.ui.viewmodel.SettingsViewModelFactory
import com.rippedrss.android.worker.FeedRefreshWorker

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object Feeds : Screen("feeds", "Feeds", Icons.Default.RssFeed)
    data object Articles : Screen("articles", "Articles", Icons.Default.Article)
    data object Saved : Screen("saved", "Saved", Icons.Default.Bookmark)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object ArticleReader : Screen("article_reader", "Article", Icons.Default.Article)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val application = application as RippedRssApplication

            val feedViewModel: FeedViewModel = viewModel(
                factory = FeedViewModelFactory(application.feedRepository)
            )
            val articleViewModel: ArticleViewModel = viewModel(
                factory = ArticleViewModelFactory(application.articleRepository)
            )
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(application.appPreferences, applicationContext)
            )

            val settingsUiState by settingsViewModel.uiState.collectAsState()
            val themeMode = settingsUiState.themeMode
            val textScale = settingsUiState.textScale

            // Monitor background refresh setting and schedule/cancel work accordingly
            LaunchedEffect(settingsUiState.backgroundRefreshEnabled) {
                if (settingsUiState.backgroundRefreshEnabled) {
                    FeedRefreshWorker.scheduleWork(this@MainActivity)
                } else {
                    FeedRefreshWorker.cancelWork(this@MainActivity)
                }
            }

            // Monitor reset completion
            LaunchedEffect(Unit) {
                settingsViewModel.resetComplete.collect { success ->
                    val message = if (success) "All data has been reset" else "Failed to reset data"
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                }
            }

            RippedRichRSSTheme(themeMode = themeMode) {
                MainScreen(
                    feedViewModel = feedViewModel,
                    articleViewModel = articleViewModel,
                    settingsViewModel = settingsViewModel,
                    themeMode = themeMode,
                    textScale = textScale
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    feedViewModel: FeedViewModel,
    articleViewModel: ArticleViewModel,
    settingsViewModel: SettingsViewModel,
    themeMode: ThemeMode,
    textScale: com.rippedrss.android.ui.theme.TextScale
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val feedUiState by feedViewModel.uiState.collectAsState()
    val articleUiState by articleViewModel.uiState.collectAsState()
    val settingsUiState by settingsViewModel.uiState.collectAsState()

    val bottomNavItems = listOf(
        Screen.Feeds,
        Screen.Articles,
        Screen.Saved,
        Screen.Settings
    )

    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                        NavigationBarItem(
                            icon = {
                                Icon(screen.icon, contentDescription = screen.title)
                            },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Feeds.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Feeds.route) {
                FeedsScreen(
                    uiState = feedUiState,
                    onRefresh = { feedViewModel.refreshAllFeeds() },
                    onAddFeed = { feedViewModel.showAddDialog() },
                    onDeleteFeed = { feedViewModel.deleteFeed(it) },
                    onToggleFavorite = { feedViewModel.toggleFavorite(it) },
                    onFeedClick = { /* TODO: Navigate to feed articles */ },
                    onUpdateAddFeedUrl = { feedViewModel.updateAddFeedUrl(it) },
                    onConfirmAddFeed = { feedViewModel.addFeed() },
                    onDismissAddDialog = { feedViewModel.hideAddDialog() }
                )
            }

            composable(Screen.Articles.route) {
                ArticlesScreen(
                    uiState = articleUiState,
                    onArticleClick = { article ->
                        articleViewModel.selectArticle(article)
                        navController.navigate(Screen.ArticleReader.route)
                    },
                    onToggleSaved = { articleViewModel.toggleSaved(it) }
                )
            }

            composable(Screen.Saved.route) {
                ArticlesScreen(
                    uiState = articleUiState,
                    onArticleClick = { article ->
                        articleViewModel.selectArticle(article)
                        navController.navigate(Screen.ArticleReader.route)
                    },
                    onToggleSaved = { articleViewModel.toggleSaved(it) },
                    showSavedOnly = true,
                    onLoadSaved = { articleViewModel.loadSavedArticles() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    uiState = settingsUiState,
                    onBackgroundRefreshChanged = { settingsViewModel.setBackgroundRefreshEnabled(it) },
                    onWifiOnlyChanged = { settingsViewModel.setWifiOnly(it) },
                    onThemeModeChanged = { settingsViewModel.setThemeMode(it) },
                    onTextScaleChanged = { settingsViewModel.setTextScale(it) },
                    onResetData = { settingsViewModel.resetAllData() }
                )
            }

            composable(Screen.ArticleReader.route) {
                articleUiState.selectedArticle?.let { article ->
                    ArticleReaderScreen(
                        article = article,
                        onBack = { navController.popBackStack() },
                        onToggleSaved = { articleViewModel.toggleSaved(article) },
                        themeMode = themeMode,
                        textScale = textScale
                    )
                }
            }
        }
    }
}
