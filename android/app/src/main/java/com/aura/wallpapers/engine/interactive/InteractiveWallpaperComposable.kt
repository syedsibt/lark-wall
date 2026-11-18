package com.aura.wallpapers.engine.interactive

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun InteractiveWallpaperComposable(
    config: InteractiveWallpaperConfig,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = remember(config.presetId) {
        InteractiveWallpaperView(context).apply {
            setConfig(config)
        }
    }

    DisposableEffect(config.presetId) {
        onDispose {
            // Cleanup if needed
        }
    }

    AndroidView(
        factory = { view },
        update = { it.setConfig(config) },
        modifier = modifier.fillMaxSize()
    )
}
