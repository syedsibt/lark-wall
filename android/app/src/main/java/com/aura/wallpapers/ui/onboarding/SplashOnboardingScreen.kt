package com.aura.wallpapers.ui.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MotionPhotosOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aura.wallpapers.ui.theme.WallixaAccentLavender
import com.aura.wallpapers.ui.theme.WallixaPrimary
import com.aura.wallpapers.ui.theme.WallixaPrimaryContainer
import com.aura.wallpapers.ui.theme.WallixaSecondaryContainer
import com.aura.wallpapers.ui.theme.WallixaTertiary

@Composable
fun SplashOnboardingScreen(
    onGetStartedClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0B14))
    ) {
        // Full bleed background
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1200&auto=format&fit=crop&q=85")
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient scrim overlays
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.45f),
                        0.4f to Color.Transparent,
                        0.7f to Color(0xFF0D0B14).copy(alpha = 0.85f),
                        1.0f to Color(0xFF0D0B14)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Block
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                // App Icon with glow
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(
                            elevation = 24.dp,
                            shape = RoundedCornerShape(22.dp),
                            spotColor = WallixaPrimary
                        )
                        .clip(RoundedCornerShape(22.dp))
                        .background(WallixaPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "W",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 42.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Wallixa",
                        color = Color.White,
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(WallixaPrimaryContainer)
                    )
                }

                Text(
                    text = "Wallpapers for Every Mood",
                    color = WallixaAccentLavender,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = WallixaAccentLavender,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ULTRA-CURATED DAILY",
                            color = WallixaAccentLavender,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Bottom Feature Stack
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                OnboardingFeatureItem(
                    title = "HD & 4K Wallpapers",
                    subtitle = "Pixel-perfect retina visuals",
                    icon = Icons.Filled.AutoAwesome,
                    badgeIcon = Icons.Filled.Verified
                )
                Spacer(modifier = Modifier.height(10.dp))
                OnboardingFeatureItem(
                    title = "Live Wallpapers",
                    subtitle = "Dynamic fluid motion in 60fps",
                    icon = Icons.Filled.MotionPhotosOn,
                    badgeIcon = Icons.Filled.Bolt
                )
                Spacer(modifier = Modifier.height(10.dp))
                OnboardingFeatureItem(
                    title = "Lightweight & Fast",
                    subtitle = "Zero battery drain guaranteed",
                    icon = Icons.Filled.FlashOn,
                    badgeIcon = Icons.Filled.Speed
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onGetStartedClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WallixaPrimaryContainer,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 12.dp)
                ) {
                    Text(
                        text = "Get Started",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "By continuing, you agree to Terms & Privacy",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun OnboardingFeatureItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeIcon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(WallixaPrimaryContainer.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = WallixaAccentLavender,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = WallixaAccentLavender.copy(alpha = 0.75f),
                    fontSize = 12.sp
                )
            }
        }
        Icon(
            imageVector = badgeIcon,
            contentDescription = null,
            tint = WallixaAccentLavender,
            modifier = Modifier.size(18.dp)
        )
    }
}
