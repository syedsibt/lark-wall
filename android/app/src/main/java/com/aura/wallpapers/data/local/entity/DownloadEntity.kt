package com.aura.wallpapers.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aura.wallpapers.domain.model.DownloadItem
import com.aura.wallpapers.domain.model.DownloadStatus

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: String,
    val assetId: String,
    val title: String,
    val isLive: Boolean,
    val localFilePath: String?,
    val fileSizeBytes: Long,
    val progress: Int,
    val status: String,
    val downloadUrl: String,
    val thumbnailUrl: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): DownloadItem = DownloadItem(
        id = id,
        assetId = assetId,
        title = title,
        isLive = isLive,
        localFilePath = localFilePath,
        fileSizeBytes = fileSizeBytes,
        progress = progress,
        status = runCatching { DownloadStatus.valueOf(status) }.getOrDefault(DownloadStatus.PENDING),
        downloadUrl = downloadUrl,
        thumbnailUrl = thumbnailUrl,
        createdAt = createdAt
    )
}

fun DownloadItem.toEntity(): DownloadEntity = DownloadEntity(
    id = id,
    assetId = assetId,
    title = title,
    isLive = isLive,
    localFilePath = localFilePath,
    fileSizeBytes = fileSizeBytes,
    progress = progress,
    status = status.name,
    downloadUrl = downloadUrl,
    thumbnailUrl = thumbnailUrl,
    createdAt = createdAt
)
