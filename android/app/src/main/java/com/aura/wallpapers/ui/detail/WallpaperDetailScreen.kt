package com.aura.wallpapers.ui.detail

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MotionPhotosOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aura.wallpapers.R
import com.aura.wallpapers.domain.model.ApplyTarget
import com.aura.wallpapers.ui.theme.WallixaAccentLavender
import com.aura.wallpapers.ui.theme.WallixaPrimary
import com.aura.wallpapers.ui.theme.WallixaPrimaryContainer
import com.aura.wallpapers.ui.theme.WallixaSecondaryContainer
import com.aura.wallpapers.ui.theme.WallixaTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DetailPreviewMode {
    NORMAL,
    LOCK_SCREEN,
    HOME_SCREEN,
    CLEAN
}

@OptIn(UnstableApi::class)
@Composable
fun WallpaperDetailScreen(
    viewModel: WallpaperDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var previewMode by remember { mutableStateOf(DetailPreviewMode.NORMAL) }
    var isInfoCardVisible by remember { mutableStateOf(false) }
    var selectedTarget by remember { mutableStateOf(ApplyTarget.BOTH_SCREENS) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable {
                // Tap canvas to cycle through preview modes when previewing
                if (previewMode != DetailPreviewMode.NORMAL) {
                    previewMode = when (previewMode) {
                        DetailPreviewMode.LOCK_SCREEN -> DetailPreviewMode.HOME_SCREEN
                        DetailPreviewMode.HOME_SCREEN -> DetailPreviewMode.CLEAN
                        DetailPreviewMode.CLEAN -> DetailPreviewMode.NORMAL
                        DetailPreviewMode.NORMAL -> DetailPreviewMode.LOCK_SCREEN
                    }
                }
            }
    ) {
        // 1. Immersive Full-Bleed Media (Static image or Media3 live loop)
        if (uiState.isLive && uiState.liveWallpaper != null) {
            val live = uiState.liveWallpaper!!

            // Fallback crisp thumbnail while video buffers
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(live.thumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = live.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Video player with explicit key on previewVideoUrl
            val exoPlayer = remember(live.previewVideoUrl) {
                ExoPlayer.Builder(context).build().apply {
                    repeatMode = Player.REPEAT_MODE_ALL
                    volume = 0f
                    videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                    setMediaItem(MediaItem.fromUri(live.previewVideoUrl))
                    prepare()
                    playWhenReady = true
                }
            }

            DisposableEffect(exoPlayer) {
                onDispose {
                    exoPlayer.stop()
                    exoPlayer.release()
                }
            }

            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else if (uiState.wallpaper != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(uiState.wallpaper!!.previewUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = uiState.wallpaper!!.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (uiState.isLoading) {
            CircularProgressIndicator(
                color = WallixaPrimary,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 2. Normal Ambient Shadow Overlays for Action Legibility
        AnimatedVisibility(
            visible = previewMode == DetailPreviewMode.NORMAL,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Black.copy(alpha = 0.5f),
                            0.2f to Color.Transparent,
                            0.5f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.95f)
                        )
                    )
            )
        }

        // 3. Top Floating Frosted Bar (Back & Options in Normal Mode)
        AnimatedVisibility(
            visible = previewMode == DetailPreviewMode.NORMAL,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.25f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Interactive Preview Mode Launcher Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(Color.White.copy(alpha = 0.25f))
                        .clickable { previewMode = DetailPreviewMode.LOCK_SCREEN }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = WallixaAccentLavender,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Live Preview",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                IconButton(
                    onClick = { /* options */ },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.25f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Options",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // 4. Right Side Floating Action Rail (Normal Mode)
        AnimatedVisibility(
            visible = previewMode == DetailPreviewMode.NORMAL,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 90.dp, end = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Heart / Like Action
                RailActionButton(
                    icon = if (uiState.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    label = if (uiState.isFavorite) "Saved" else "24.5k",
                    iconColor = if (uiState.isFavorite) Color(0xFFEF4444) else Color.White,
                    onClick = { viewModel.toggleFavorite() }
                )

                // Share Action
                RailActionButton(
                    icon = Icons.Filled.Share,
                    label = "Share",
                    onClick = { /* Share */ }
                )

                // 4K Download Action
                RailActionButton(
                    icon = Icons.Filled.Download,
                    label = "4K",
                    onClick = { viewModel.downloadAsset() }
                )

                // Info Action Button
                RailActionButton(
                    icon = Icons.Filled.Info,
                    label = "Info",
                    onClick = { isInfoCardVisible = !isInfoCardVisible }
                )
            }
        }

        // 5. Collapsible Artist & Details Card
        AnimatedVisibility(
            visible = isInfoCardVisible && previewMode == DetailPreviewMode.NORMAL,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 220.dp, start = 16.dp, end = 16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(WallixaSecondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "L",
                                    color = WallixaPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                val title = uiState.wallpaper?.title ?: uiState.liveWallpaper?.title ?: "Astral Vista"
                                Text(
                                    text = title,
                                    color = Color(0xFF1A1B20),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Curated by Lark Wall Studio",
                                    color = Color(0xFF4A4455),
                                    fontSize = 12.sp
                                )
                            }
                        }
                        IconButton(onClick = { isInfoCardVisible = false }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF4A4455),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF4F3FA))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(text = "Aspect Ratio", color = WallixaPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "9 : 19.5 (Bezel-Free)", color = Color(0xFF1A1B20), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF4F3FA))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(text = "Palette Accent", color = WallixaPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "Ultraviolet #7C3AED", color = Color(0xFF1A1B20), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 6. Bottom Interactive Sheet Area (Normal Mode)
        AnimatedVisibility(
            visible = previewMode == DetailPreviewMode.NORMAL,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Specs Summary Pill Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(WallixaAccentLavender)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val resolution = uiState.wallpaper?.resolutionLabel ?: uiState.liveWallpaper?.resolutionLabel ?: "3840×2160"
                        val size = uiState.wallpaper?.formattedSize ?: uiState.liveWallpaper?.formattedSize ?: "4.2 MB"
                        Text(
                            text = if (uiState.isLive) "Live 60 FPS • Seamless Loop • OLED Optimized" else "4K Ultra HD • $resolution • $size • OLED",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Segmented Target Selector for static wallpapers
                if (!uiState.isLive) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TargetSegmentButton(
                            title = "Home Screen",
                            icon = Icons.Filled.Home,
                            isSelected = selectedTarget == ApplyTarget.HOME_SCREEN,
                            onClick = { selectedTarget = ApplyTarget.HOME_SCREEN },
                            modifier = Modifier.weight(1f)
                        )
                        TargetSegmentButton(
                            title = "Lock Screen",
                            icon = Icons.Filled.Lock,
                            isSelected = selectedTarget == ApplyTarget.LOCK_SCREEN,
                            onClick = { selectedTarget = ApplyTarget.LOCK_SCREEN },
                            modifier = Modifier.weight(1f)
                        )
                        TargetSegmentButton(
                            title = "Both Screens",
                            icon = Icons.Filled.PhoneAndroid,
                            isSelected = selectedTarget == ApplyTarget.BOTH_SCREENS,
                            onClick = { selectedTarget = ApplyTarget.BOTH_SCREENS },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Main Set As Wallpaper Action Button
                Button(
                    onClick = {
                        if (uiState.isLive) {
                            viewModel.applyLiveWallpaper()
                        } else {
                            viewModel.applyStaticWallpaper(selectedTarget)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(9999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WallixaPrimaryContainer,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    if (uiState.isApplying) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Applying...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Wallpaper,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isLive) stringResource(R.string.action_set_live) else stringResource(R.string.action_set_as_wallpaper),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        // =================================================================
        // 7. INTERACTIVE DEVICE PREVIEW OVERLAYS (Lock Screen, Home Screen, Clean)
        // =================================================================
        if (previewMode != DetailPreviewMode.NORMAL) {
            // Top Interactive Mode Switcher Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(9999.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PreviewTabPill(
                        label = "Lock Screen",
                        isSelected = previewMode == DetailPreviewMode.LOCK_SCREEN,
                        onClick = { previewMode = DetailPreviewMode.LOCK_SCREEN }
                    )
                    PreviewTabPill(
                        label = "Home Screen",
                        isSelected = previewMode == DetailPreviewMode.HOME_SCREEN,
                        onClick = { previewMode = DetailPreviewMode.HOME_SCREEN }
                    )
                    PreviewTabPill(
                        label = "Clean",
                        isSelected = previewMode == DetailPreviewMode.CLEAN,
                        onClick = { previewMode = DetailPreviewMode.CLEAN }
                    )
                    IconButton(
                        onClick = { previewMode = DetailPreviewMode.NORMAL },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Exit Preview",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // A. LOCK SCREEN SIMULATION
            if (previewMode == DetailPreviewMode.LOCK_SCREEN) {
                val timeString = remember { SimpleDateFormat("hh:mm", Locale.getDefault()).format(Date()) }
                val dateString = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Space below status switcher
                    Spacer(modifier = Modifier.height(60.dp))

                    // Center Lock Screen Clock & Notification
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = timeString,
                            fontSize = 76.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = (-2).sp
                        )
                        Text(
                            text = dateString,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // Simulated Lock Screen Notification Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.White.copy(alpha = 0.18f))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(WallixaPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.MotionPhotosOn,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Lark Wall • Live Engine",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Seamless 60 FPS motion loop active",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Bottom Lock Screen Hints & Quick Shortcuts
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 20.dp)
                    ) {
                        Text(
                            text = "Swipe up to unlock",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.45f))
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Bolt,
                                    contentDescription = "Flashlight",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.45f))
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PhotoCamera,
                                    contentDescription = "Camera",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            // B. HOME SCREEN SIMULATION
            if (previewMode == DetailPreviewMode.HOME_SCREEN) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.height(60.dp))

                    // Google-style Search Widget at top of Home Screen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(9999.dp))
                            .background(Color.White.copy(alpha = 0.85f))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "G",
                                    color = WallixaPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Search…",
                                    color = Color(0xFF555555),
                                    fontSize = 14.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = Color(0xFF555555),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Spacer
                    Spacer(modifier = Modifier.weight(1f))

                    // Bottom Dock with 4 Android App Icons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(vertical = 14.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MockAppIcon(name = "Phone", icon = Icons.Filled.Phone, color = Color(0xFF22C55E))
                        MockAppIcon(name = "Messages", icon = Icons.Filled.Email, color = Color(0xFF3B82F6))
                        MockAppIcon(name = "Lark Wall", icon = Icons.Filled.MotionPhotosOn, color = WallixaPrimary)
                        MockAppIcon(name = "Camera", icon = Icons.Filled.PhotoCamera, color = Color(0xFFEF4444))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Notification Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 80.dp)
        )
    }
}

@Composable
private fun PreviewTabPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(if (isSelected) WallixaPrimary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
private fun MockAppIcon(
    name: String,
    icon: ImageVector,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            fontSize = 10.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RailActionButton(
    icon: ImageVector,
    label: String,
    iconColor: Color = Color.White,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TargetSegmentButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) WallixaPrimary else Color.White,
                    modifier = Modifier.size(16.dp)
                )
                if (isSelected) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = WallixaTertiary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) WallixaPrimary else Color.White
            )
        }
    }
}
