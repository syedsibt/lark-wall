package com.aura.wallpapers.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aura.wallpapers.data.local.preferences.UserPreferencesManager
import com.aura.wallpapers.domain.model.ThemeMode
import com.aura.wallpapers.ui.components.AuraBottomNavigation
import com.aura.wallpapers.ui.navigation.NavGraph
import com.aura.wallpapers.ui.navigation.Screen
import com.aura.wallpapers.ui.theme.AuraWallpapersTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MainViewModel @Inject constructor(
    preferencesManager: UserPreferencesManager
) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> = preferencesManager.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by mainViewModel.themeMode.collectAsState()

            AuraWallpapersTheme(themeMode = themeMode) {
                AuraApp()
            }
        }
    }
}

@Composable
fun AuraApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom navigation on detail, search, and legal screens
    val hideBottomNavRoutes = listOf(
        Screen.Onboarding.route,
        Screen.WallpaperDetail.route,
        Screen.Search.route,
        "privacy",
        "terms"
    )
    val shouldShowBottomBar = currentRoute !in hideBottomNavRoutes && currentRoute?.startsWith("wallpaper_detail") != true

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (shouldShowBottomBar) {
                AuraBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigateToRoute = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            paddingValues = innerPadding
        )
    }
}
