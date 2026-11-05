package com.aura.wallpapers.domain.repository

import com.aura.wallpapers.domain.model.DownloadItem
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    fun getAllDownloads(): Flow<List<DownloadItem>>
    fun getCompletedDownloads(): Flow<List<DownloadItem>>
    suspend fun getDownloadByAssetId(assetId: String): DownloadItem?
    suspend fun enqueueDownload(
        assetId: String,
        title: String,
        isLive: Boolean,
        downloadUrl: String,
        thumbnailUrl: String,
        fileSizeBytes: Long
    )
    suspend fun deleteDownload(id: String)
    suspend fun clearAllDownloads()
}
