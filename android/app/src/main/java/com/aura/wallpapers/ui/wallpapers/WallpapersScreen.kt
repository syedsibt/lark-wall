package com.aura.wallpapers.ui.wallpapers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.wallpapers.R
import com.aura.wallpapers.domain.usecase.SortOption
import com.aura.wallpapers.ui.components.AuraTopBar
import com.aura.wallpapers.ui.components.CategoryChipRow
import com.aura.wallpapers.ui.components.EmptyStateView
import com.aura.wallpapers.ui.components.ErrorStateView
import com.aura.wallpapers.ui.components.WallpaperCard
import com.aura.wallpapers.ui.theme.AuraGold

@Composable
fun WallpapersScreen(
    viewModel: WallpapersViewModel,
    onNavigateToDetail: (String, Boolean) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AuraTopBar(
            title = stringResource(R.string.nav_wallpapers),
            showBrandLogo = false,
            onSearchClick = onNavigateToSearch,
            onSettingsClick = onNavigateToSettings
        )

        // Category Filter Row
        CategoryChipRow(
            categories = uiState.categories,
            selectedCategoryId = uiState.selectedCategoryId,
            onCategorySelected = { viewModel.setCategory(it) }
        )

        // Sort Options Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SortTab(
                label = stringResource(R.string.sort_popular),
                isSelected = uiState.selectedSort == SortOption.POPULAR,
                onClick = { viewModel.setSort(SortOption.POPULAR) }
            )
            SortTab(
                label = stringResource(R.string.sort_newest),
                isSelected = uiState.selectedSort == SortOption.NEWEST,
                onClick = { viewModel.setSort(SortOption.NEWEST) }
            )
            SortTab(
                label = stringResource(R.string.sort_downloads),
                isSelected = uiState.selectedSort == SortOption.MOST_DOWNLOADED,
                onClick = { viewModel.setSort(SortOption.MOST_DOWNLOADED) }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AuraGold)
            }
        } else if (uiState.error != null) {
            ErrorStateView(
                message = uiState.error ?: stringResource(R.string.error_network),
                onRetry = { viewModel.loadWallpapers() }
            )
        } else if (uiState.wallpapers.isEmpty()) {
            EmptyStateView(
                icon = Icons.Outlined.Image,
                title = "No Wallpapers Found",
                description = "Check back soon or try selecting a different category."
            )
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalItemSpacing = 10.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.wallpapers, key = { it.id }) { wallpaper ->
                    WallpaperCard(
                        wallpaper = wallpaper,
                        onClick = { onNavigateToDetail(wallpaper.id, false) },
                        onFavoriteToggle = { viewModel.toggleFavorite(wallpaper) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SortTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = label,
        color = if (isSelected) AuraGold else MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 12.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier.clickable(onClick = onClick)
    )
}
