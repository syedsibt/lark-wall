package com.aura.wallpapers.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aura.wallpapers.ui.onboarding.SplashOnboardingScreen
import com.aura.wallpapers.ui.detail.WallpaperDetailScreen
import com.aura.wallpapers.ui.detail.WallpaperDetailViewModel
import com.aura.wallpapers.ui.downloads.DownloadsScreen
import com.aura.wallpapers.ui.downloads.DownloadsViewModel
import com.aura.wallpapers.ui.favorites.FavoritesScreen
import com.aura.wallpapers.ui.favorites.FavoritesViewModel
import com.aura.wallpapers.ui.home.HomeScreen
import com.aura.wallpapers.ui.home.HomeViewModel
import com.aura.wallpapers.ui.live.LiveWallpapersScreen
import com.aura.wallpapers.ui.live.LiveWallpapersViewModel
import com.aura.wallpapers.ui.search.SearchScreen
import com.aura.wallpapers.ui.search.SearchViewModel
import com.aura.wallpapers.ui.settings.PrivacyPolicyScreen
import com.aura.wallpapers.ui.settings.SettingsScreen
import com.aura.wallpapers.ui.settings.SettingsViewModel
import com.aura.wallpapers.ui.settings.TermsScreen
import com.aura.wallpapers.ui.wallpapers.WallpapersScreen
import com.aura.wallpapers.ui.wallpapers.WallpapersViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier.padding(paddingValues)
    ) {
        composable(Screen.Onboarding.route) {
            SplashOnboardingScreen(
                onGetStartedClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onNavigateToDetail = { id, isLive ->
                    navController.navigate(Screen.WallpaperDetail.createRoute(id, isLive))
                },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToWallpapers = { navController.navigate(Screen.Wallpapers.route) },
                onNavigateToLive = { navController.navigate(Screen.LiveWallpapers.route) }
            )
        }

        composable(Screen.Wallpapers.route) {
            val viewModel: WallpapersViewModel = hiltViewModel()
            WallpapersScreen(
                viewModel = viewModel,
                onNavigateToDetail = { id, isLive ->
                    navController.navigate(Screen.WallpaperDetail.createRoute(id, isLive))
                },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.LiveWallpapers.route) {
            val viewModel: LiveWallpapersViewModel = hiltViewModel()
            LiveWallpapersScreen(
                viewModel = viewModel,
                onNavigateToDetail = { id, isLive ->
                    navController.navigate(Screen.WallpaperDetail.createRoute(id, isLive))
                },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Favorites.route) {
            val viewModel: FavoritesViewModel = hiltViewModel()
            FavoritesScreen(
                viewModel = viewModel,
                onNavigateToDetail = { id, isLive ->
                    navController.navigate(Screen.WallpaperDetail.createRoute(id, isLive))
                },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Downloads.route) {
            val viewModel: DownloadsViewModel = hiltViewModel()
            DownloadsScreen(
                viewModel = viewModel,
                onNavigateToDetail = { id, isLive ->
                    navController.navigate(Screen.WallpaperDetail.createRoute(id, isLive))
                },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Search.route) {
            val viewModel: SearchViewModel = hiltViewModel()
            SearchScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { id, isLive ->
                    navController.navigate(Screen.WallpaperDetail.createRoute(id, isLive))
                }
            )
        }

        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPrivacy = { navController.navigate("privacy") },
                onNavigateToTerms = { navController.navigate("terms") }
            )
        }

        composable("privacy") {
            PrivacyPolicyScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("terms") {
            TermsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.WallpaperDetail.route,
            arguments = listOf(
                navArgument("assetId") { type = NavType.StringType },
                navArgument("isLive") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) {
            val viewModel: WallpaperDetailViewModel = hiltViewModel()
            WallpaperDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
