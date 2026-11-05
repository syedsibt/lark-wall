package com.aura.wallpapers.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.wallpapers.R
import com.aura.wallpapers.ui.components.AuraSearchBar
import com.aura.wallpapers.ui.components.EmptyStateView
import com.aura.wallpapers.ui.components.LiveWallpaperCard
import com.aura.wallpapers.ui.components.WallpaperCard
import com.aura.wallpapers.ui.theme.AuraGold

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String, Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            AuraSearchBar(
                query = uiState.query,
                onQueryChange = { viewModel.onQueryChange(it) },
                modifier = Modifier.weight(1f)
            )
        }

        // Filter Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FilterTab(
                label = stringResource(R.string.filter_all),
                isSelected = uiState.activeFilter == SearchFilter.ALL,
                onClick = { viewModel.setFilter(SearchFilter.ALL) }
            )
            FilterTab(
                label = stringResource(R.string.filter_static),
                isSelected = uiState.activeFilter == SearchFilter.STATIC,
                onClick = { viewModel.setFilter(SearchFilter.STATIC) }
            )
            FilterTab(
                label = stringResource(R.string.filter_live),
                isSelected = uiState.activeFilter == SearchFilter.LIVE,
                onClick = { viewModel.setFilter(SearchFilter.LIVE) }
            )
        }

        if (uiState.isSearching) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AuraGold)
            }
        } else if (uiState.query.isBlank()) {
            // Recents & Trending Tags
            Column(modifier = Modifier.padding(16.dp)) {
                if (uiState.recentSearches.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.search_recent),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(R.string.action_clear),
                            style = MaterialTheme.typography.labelSmall,
                            color = AuraGold,
                            modifier = Modifier.clickable { viewModel.clearHistory() }
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.recentSearches.forEach { q ->
                            TagChip(text = q, onClick = { viewModel.onQueryChange(q) })
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Text(
                    text = stringResource(R.string.search_trending),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.trendingTags.forEach { tag ->
                        TagChip(text = "#$tag", onClick = { viewModel.onQueryChange(tag) })
                    }
                }
            }
        } else {
            // Search Results Grid
            val resultsCount = when (uiState.activeFilter) {
                SearchFilter.ALL -> uiState.staticResults.size + uiState.liveResults.size
                SearchFilter.STATIC -> uiState.staticResults.size
                SearchFilter.LIVE -> uiState.liveResults.size
            }

            if (resultsCount == 0) {
                EmptyStateView(
                    icon = Icons.Outlined.Search,
                    title = "No Wallpapers Found",
                    description = "We couldn't find any wallpapers matching \"${uiState.query}\""
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (uiState.activeFilter == SearchFilter.ALL || uiState.activeFilter == SearchFilter.STATIC) {
                        items(uiState.staticResults, key = { "static_${it.id}" }) { wp ->
                            WallpaperCard(
                                wallpaper = wp,
                                onClick = { onNavigateToDetail(wp.id, false) },
                                onFavoriteToggle = {}
                            )
                        }
                    }
                    if (uiState.activeFilter == SearchFilter.ALL || uiState.activeFilter == SearchFilter.LIVE) {
                        items(uiState.liveResults, key = { "live_${it.id}" }) { live ->
                            LiveWallpaperCard(
                                liveWallpaper = live,
                                onClick = { onNavigateToDetail(live.id, true) },
                                onFavoriteToggle = {}
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = label,
        color = if (isSelected) AuraGold else MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun TagChip(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
