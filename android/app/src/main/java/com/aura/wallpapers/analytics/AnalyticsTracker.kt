package com.aura.wallpapers.analytics

import android.os.Bundle
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

interface AnalyticsTracker {
    fun logScreenView(screenName: String)
    fun logWallpaperApplied(assetId: String, isLive: Boolean, target: String)
    fun logDownloadStarted(assetId: String, isLive: Boolean)
    fun logCategorySelected(categoryName: String)
}

@Singleton
class AuraAnalyticsTracker @Inject constructor() : AnalyticsTracker {

    private val tag = "AuraAnalytics"

    override fun logScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString("screen_name", screenName)
        }
        Log.d(tag, "ScreenView: $screenName")
    }

    override fun logWallpaperApplied(assetId: String, isLive: Boolean, target: String) {
        val bundle = Bundle().apply {
            putString("asset_id", assetId)
            putBoolean("is_live", isLive)
            putString("apply_target", target)
        }
        Log.d(tag, "WallpaperApplied: $assetId ($target, isLive=$isLive)")
    }

    override fun logDownloadStarted(assetId: String, isLive: Boolean) {
        val bundle = Bundle().apply {
            putString("asset_id", assetId)
            putBoolean("is_live", isLive)
        }
        Log.d(tag, "DownloadStarted: $assetId (isLive=$isLive)")
    }

    override fun logCategorySelected(categoryName: String) {
        val bundle = Bundle().apply {
            putString("category_name", categoryName)
        }
        Log.d(tag, "CategorySelected: $categoryName")
    }
}
