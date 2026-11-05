package com.aura.wallpapers.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MotionPhotosOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aura.wallpapers.R
import com.aura.wallpapers.domain.model.Category
import com.aura.wallpapers.domain.model.Wallpaper
import com.aura.wallpapers.ui.components.AuraTopBar
import com.aura.wallpapers.ui.components.ErrorStateView
import com.aura.wallpapers.ui.components.WallpaperCard
import com.aura.wallpapers.ui.theme.WallixaAccentLavender
import com.aura.wallpapers.ui.theme.WallixaPrimary
import com.aura.wallpapers.ui.theme.WallixaPrimaryContainer
import com.aura.wallpapers.ui.theme.WallixaSecondaryContainer
import com.aura.wallpapers.ui.theme.WallixaTertiary
import com.aura.wallpapers.ui.theme.WallixaVipGold

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToDetail: (String, Boolean) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToWallpapers: () -> Unit,
    onNavigateToLive: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        AuraTopBar(
            title = "Lark Wall",
            showBrandLogo = true,
            onSearchClick = onNavigateToSearch,
            onSettingsClick = onNavigateToSettings
        )

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = WallixaPrimary)
            }
        } else if (uiState.error != null) {
            ErrorStateView(
                message = uiState.error ?: stringResource(R.string.error_network),
                onRetry = { viewModel.loadHomeData() }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                // 1. Horizontal Category Sub-Nav Chips (Explore, Live, Categories, Old Retro)
                item {
                    WallixaSubNavChips(
                        selectedCategoryId = uiState.selectedCategoryId,
                        onExploreClick = { viewModel.onCategorySelected(null) },
                        onLiveClick = onNavigateToLive,
                        onCategoriesClick = onNavigateToWallpapers
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // 2. Featured Wallpaper of the Day Hero Card
                if (uiState.dailyWallpaper != null) {
                    item {
                        WallpaperOfTheDayHero(
                            wallpaper = uiState.dailyWallpaper!!,
                            onClick = { onNavigateToDetail(uiState.dailyWallpaper!!.id, false) },
                            onFavoriteToggle = { viewModel.toggleFavorite(uiState.dailyWallpaper!!) },
                            onApplyClick = { onNavigateToDetail(uiState.dailyWallpaper!!.id, false) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // 3. Fast Shortcuts Row (Trending, New, Popular, Editor's)
                item {
                    WallixaShortcutsRow(
                        onTrendingClick = onNavigateToWallpapers,
                        onNewClick = onNavigateToWallpapers,
                        onPopularClick = onNavigateToWallpapers,
                        onEditorsClick = onNavigateToWallpapers
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // 4. Categories Horizontal Gallery
                if (uiState.categories.isNotEmpty()) {
                    item {
                        WallixaSectionHeader(
                            title = stringResource(R.string.section_categories),
                            actionText = stringResource(R.string.explore_all),
                            onActionClick = onNavigateToWallpapers
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.categories, key = { it.id }) { cat ->
                                CategoryCardItem(
                                    category = cat,
                                    onClick = { viewModel.onCategorySelected(cat.id) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // 5. Today's Picks 2-Column Grid
                item {
                    WallixaSectionHeader(
                        title = stringResource(R.string.section_todays_picks),
                        actionText = stringResource(R.string.view_all),
                        onActionClick = onNavigateToWallpapers
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Two-Column Grid Items chunked in pairs
                val wallpapersList = if (uiState.trendingWallpapers.isNotEmpty()) uiState.trendingWallpapers else uiState.recentlyAddedWallpapers
                val pairs = wallpapersList.chunked(2)

                items(pairs) { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (item in rowItems) {
                            Box(modifier = Modifier.weight(1f)) {
                                WallpaperCard(
                                    wallpaper = item,
                                    onClick = { onNavigateToDetail(item.id, false) },
                                    onFavoriteToggle = { viewModel.toggleFavorite(item) }
                                )
                            }
                        }
                        // If odd number, balance with empty box
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WallixaSubNavChips(
    selectedCategoryId: String?,
    onExploreClick: () -> Unit,
    onLiveClick: () -> Unit,
    onCategoriesClick: () -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Explore (Active Primary)
        item {
            PillChip(
                label = stringResource(R.string.subnav_explore),
                icon = Icons.Filled.Explore,
                isActive = selectedCategoryId == null,
                onClick = onExploreClick
            )
        }
        // Live
        item {
            PillChip(
                label = stringResource(R.string.subnav_live),
                icon = Icons.Filled.MotionPhotosOn,
                isActive = false,
                containerColor = WallixaSecondaryContainer.copy(alpha = 0.5f),
                contentColor = WallixaTertiary,
                onClick = onLiveClick
            )
        }
        // Categories
        item {
            PillChip(
                label = stringResource(R.string.subnav_categories),
                icon = Icons.Filled.Category,
                isActive = false,
                onClick = onCategoriesClick
            )
        }
        // Old (with Retro badge)
        item {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onCategoriesClick() }
                    .padding(start = 14.dp, end = 10.dp, top = 6.dp, bottom = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        tint = WallixaTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.subnav_old),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(WallixaPrimary)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.badge_retro),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PillChip(
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    containerColor: Color = if (isActive) WallixaPrimary else MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}

@Composable
private fun WallpaperOfTheDayHero(
    wallpaper: Wallpaper,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onApplyClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(270.dp)
            .padding(horizontal = 16.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = WallixaPrimary.copy(alpha = 0.25f)
            )
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(wallpaper.previewUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = wallpaper.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Scrim overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Black.copy(alpha = 0.25f),
                            0.4f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.9f)
                        )
                    )
            )

            // Top Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gold Star "Wallpaper of the Day"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = WallixaVipGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Wallpaper of the Day",
                            color = Color(0xFF1A1B20),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.85f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (wallpaper.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (wallpaper.isFavorite) Color(0xFFEF4444) else Color(0xFF1A1B20),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Bottom Hero Content
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = WallixaAccentLavender,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Ultra HDR · 4K UHD",
                            color = WallixaAccentLavender,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = wallpaper.title,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Curated by Lark Wall Studio",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = onApplyClick,
                    shape = RoundedCornerShape(9999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WallixaPrimaryContainer,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Wallpaper,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.action_apply),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun WallixaShortcutsRow(
    onTrendingClick: () -> Unit,
    onNewClick: () -> Unit,
    onPopularClick: () -> Unit,
    onEditorsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ShortcutItem(
            title = stringResource(R.string.shortcut_trending),
            icon = Icons.Filled.LocalFireDepartment,
            onClick = onTrendingClick
        )
        ShortcutItem(
            title = stringResource(R.string.shortcut_new),
            icon = Icons.AutoMirrored.Filled.ArrowForward,
            onClick = onNewClick
        )
        ShortcutItem(
            title = stringResource(R.string.shortcut_popular),
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            onClick = onPopularClick
        )
        ShortcutItem(
            title = stringResource(R.string.shortcut_editors),
            icon = Icons.Filled.WorkspacePremium,
            onClick = onEditorsClick
        )
    }
}

@Composable
private fun ShortcutItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(WallixaSecondaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = WallixaPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun WallixaSectionHeader(
    title: String,
    actionText: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(WallixaPrimary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onActionClick)
        ) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = WallixaPrimary
            )
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = WallixaPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun CategoryCardItem(
    category: Category,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .height(175.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = WallixaPrimary.copy(alpha = 0.12f)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(category.coverImageUrl ?: "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=400&auto=format&fit=crop&q=80")
                .crossfade(true)
                .build(),
                contentDescription = category.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Transparent,
                            0.5f to Color.Black.copy(alpha = 0.2f),
                            1.0f to Color.Black.copy(alpha = 0.85f)
                        )
                    )
                    .padding(10.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    Text(
                        text = category.name,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${if (category.wallpaperCount > 0) category.wallpaperCount else 420} pieces",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
