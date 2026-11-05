package com.aura.wallpapers.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.wallpapers.ui.theme.AuraGold

@Composable
fun PrivacyPolicyScreen(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
            Text(
                text = "Privacy Policy",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            PolicySection(
                title = "1. Information We Collect",
                body = "Aura Wallpapers is built with privacy by design. We do not collect personally identifiable information (PII) such as your name, email address, physical location, contacts, or photos. We only log anonymous aggregated download and popularity counters to deliver trending rankings without identifying individual users."
            )
            PolicySection(
                title = "2. Device Permissions",
                body = "We request minimal permissions strictly required to provide wallpaper functionality:\n• SET_WALLPAPER: Used solely to apply selected wallpapers to your home and lock screen.\n• INTERNET & NETWORK STATE: Required to stream and download high-resolution catalog items.\n• POST_NOTIFICATIONS (Android 13+): Optional permission to notify you when a background download finishes."
            )
            PolicySection(
                title = "3. Third-Party Services",
                body = "We use Supabase for catalog queries and Cloudflare CDN for fast edge media delivery. No personal tracking cookies or behavioral advertising profiles are built or shared."
            )
            PolicySection(
                title = "4. Data Retention & Deletion",
                body = "Since Aura Wallpapers does not store personal accounts or user profiles, there is no personal data retained on our servers. You can clear all cached files and download history locally anytime in Settings."
            )
            PolicySection(
                title = "5. Contact & Inquiries",
                body = "For legal inquiries or copyright notices, please contact legal@aura-wallpapers.com."
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PolicySection(title: String, body: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AuraGold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 20.sp
        )
    }
}
