package com.aura.wallpapers.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),     // 16px cards
    large = RoundedCornerShape(20.dp),      // 20px wallpaper cards
    extraLarge = RoundedCornerShape(28.dp)  // 28px hero card & floating docks
)
