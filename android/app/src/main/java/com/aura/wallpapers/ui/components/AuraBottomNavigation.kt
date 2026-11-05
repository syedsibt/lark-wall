package com.aura.wallpapers.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MotionPhotosOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MotionPhotosOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.wallpapers.ui.navigation.Screen
import com.aura.wallpapers.ui.theme.WallixaPrimary
import com.aura.wallpapers.ui.theme.WallixaSecondaryContainer

data class NavigationItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    NavigationItem(Screen.Home.route, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    NavigationItem(Screen.LiveWallpapers.route, "Live", Icons.Filled.MotionPhotosOn, Icons.Outlined.MotionPhotosOn),
    NavigationItem(Screen.Wallpapers.route, "Explore", Icons.Filled.GridView, Icons.Outlined.GridView),
    NavigationItem(Screen.Favorites.route, "Saved", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    NavigationItem(Screen.Settings.route, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
)

@Composable
fun AuraBottomNavigation(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Floating pill container dock
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(9999.dp),
                    spotColor = WallixaPrimary.copy(alpha = 0.25f)
                )
                .clip(RoundedCornerShape(9999.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentRoute == item.route
                val pillColor by animateColorAsState(
                    targetValue = if (selected) WallixaSecondaryContainer.copy(alpha = 0.8f) else Color.Transparent,
                    animationSpec = tween(durationMillis = 200),
                    label = "pill_anim"
                )
                val iconTint by animateColorAsState(
                    targetValue = if (selected) WallixaPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    animationSpec = tween(durationMillis = 200),
                    label = "tint_anim"
                )

                // Balanced 1f weight ensures no tab overlaps or collides with its neighbors
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onNavigateToRoute(item.route) }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Compact active pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(pillColor)
                                .padding(horizontal = 12.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                                tint = iconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.title,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = iconTint
                        )
                    }
                }
            }
        }
    }
}
