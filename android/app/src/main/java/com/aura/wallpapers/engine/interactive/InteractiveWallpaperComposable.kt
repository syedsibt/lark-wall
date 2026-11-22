package com.aura.wallpapers.engine.interactive

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun InteractiveWallpaperComposable(
    config: InteractiveWallpaperConfig,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { ctx ->
            InteractiveWallpaperView(ctx).apply {
                setConfig(config)
            }
        },
        update = { view ->
            view.setConfig(config)
        },
        modifier = modifier.fillMaxSize()
    )
}
