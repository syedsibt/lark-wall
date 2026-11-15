package com.aura.wallpapers.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Wallpapers : Screen("wallpapers?categoryId={categoryId}&sort={sort}") {
        fun createRoute(categoryId: String? = null, sort: String? = null): String {
            val cat = categoryId ?: ""
            val s = sort ?: ""
            return "wallpapers?categoryId=$cat&sort=$s"
        }
    }
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
