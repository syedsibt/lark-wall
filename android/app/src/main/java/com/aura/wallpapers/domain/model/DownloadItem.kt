package com.aura.wallpapers.domain.model

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class DownloadItem(
    val id: String,
    val assetId: String,
    val title: String,
    val isLive: Boolean,
    val localFilePath: String?,
    val fileSizeBytes: Long,
    val progress: Int,
    val status: DownloadStatus,
    val downloadUrl: String,
    val thumbnailUrl: String,
    val createdAt: Long = System.currentTimeMillis()
)
