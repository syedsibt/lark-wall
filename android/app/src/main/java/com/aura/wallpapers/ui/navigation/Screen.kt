package com.aura.wallpapers.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Wallpapers : Screen("wallpapers")
    object LiveWallpapers : Screen("live_wallpapers")
    object Favorites : Screen("favorites")
    object Downloads : Screen("downloads")
    object Search : Screen("search")
    object Settings : Screen("settings")

    object WallpaperDetail : Screen("wallpaper_detail/{assetId}?isLive={isLive}") {
        fun createRoute(assetId: String, isLive: Boolean = false): String {
            return "wallpaper_detail/$assetId?isLive=$isLive"
        }
    }
}
