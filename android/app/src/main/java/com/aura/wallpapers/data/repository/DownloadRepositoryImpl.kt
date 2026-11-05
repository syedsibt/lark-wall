package com.aura.wallpapers.data.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.aura.wallpapers.data.local.dao.DownloadDao
import com.aura.wallpapers.data.local.entity.DownloadEntity
import com.aura.wallpapers.data.local.preferences.UserPreferencesManager
import com.aura.wallpapers.data.worker.WallpaperDownloadWorker
import com.aura.wallpapers.domain.model.DownloadItem
import com.aura.wallpapers.domain.model.DownloadStatus
import com.aura.wallpapers.domain.repository.DownloadRepository
import com.aura.wallpapers.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadDao: DownloadDao,
    private val preferencesManager: UserPreferencesManager,
    private val ioDispatcher: CoroutineDispatcher
) : DownloadRepository {

    override fun getAllDownloads(): Flow<List<DownloadItem>> {
        return downloadDao.getAllDownloads()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun getCompletedDownloads(): Flow<List<DownloadItem>> {
        return downloadDao.getCompletedDownloads()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override suspend fun getDownloadByAssetId(assetId: String): DownloadItem? = withContext(ioDispatcher) {
        val entity = downloadDao.getDownloadByAssetId(assetId) ?: return@withContext null
        entity.toDomain()
    }

    override suspend fun enqueueDownload(
        assetId: String,
        title: String,
        isLive: Boolean,
        downloadUrl: String,
        thumbnailUrl: String,
        fileSizeBytes: Long
    ) = withContext(ioDispatcher) {
        val downloadId = UUID.randomUUID().toString()
        val extension = if (isLive) "mp4" else "webp"
        val fileName = "${title.lowercase().replace(" ", "_")}_${assetId.take(8)}.$extension"

        val entity = DownloadEntity(
            id = downloadId,
            assetId = assetId,
            title = title,
            isLive = isLive,
            localFilePath = null,
            fileSizeBytes = fileSizeBytes,
            progress = 0,
            status = DownloadStatus.PENDING.name,
            downloadUrl = downloadUrl,
            thumbnailUrl = thumbnailUrl
        )
        downloadDao.insertOrUpdateDownload(entity)

        val isWifiOnly = preferencesManager.isWifiOnly.first()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (isWifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .build()

        val inputData = Data.Builder()
            .putString(Constants.KEY_DOWNLOAD_ID, downloadId)
            .putString(Constants.KEY_DOWNLOAD_URL, downloadUrl)
            .putString(Constants.KEY_DOWNLOAD_TITLE, title)
            .putString(Constants.KEY_DOWNLOAD_FILE_NAME, fileName)
            .putBoolean(Constants.KEY_IS_LIVE, isLive)
            .build()

        val request = OneTimeWorkRequestBuilder<WallpaperDownloadWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }

    override suspend fun deleteDownload(id: String) = withContext(ioDispatcher) {
        val download = downloadDao.getDownloadById(id)
        if (download?.localFilePath != null) {
            val file = File(download.localFilePath)
            if (file.exists()) file.delete()
        }
        downloadDao.deleteDownload(id)
    }

    override suspend fun clearAllDownloads() = withContext(ioDispatcher) {
        val downloads = downloadDao.getCompletedDownloads().first()
        for (item in downloads) {
            if (item.localFilePath != null) {
                val f = File(item.localFilePath)
                if (f.exists()) f.delete()
            }
        }
        downloadDao.clearAllDownloads()
    }
}
