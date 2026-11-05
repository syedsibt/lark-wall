package com.aura.wallpapers.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.aura.wallpapers.data.local.dao.DownloadDao
import com.aura.wallpapers.data.local.entity.DownloadEntity
import com.aura.wallpapers.domain.model.DownloadStatus
import com.aura.wallpapers.util.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

@HiltWorker
class WallpaperDownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val downloadDao: DownloadDao,
    private val okHttpClient: OkHttpClient
) : CoroutineWorker(context, workerParams) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val downloadId = inputData.getString(Constants.KEY_DOWNLOAD_ID) ?: return@withContext Result.failure()
        val downloadUrl = inputData.getString(Constants.KEY_DOWNLOAD_URL) ?: return@withContext Result.failure()
        val title = inputData.getString(Constants.KEY_DOWNLOAD_TITLE) ?: "Wallpaper"
        val isLive = inputData.getBoolean(Constants.KEY_IS_LIVE, false)
        val fileName = inputData.getString(Constants.KEY_DOWNLOAD_FILE_NAME) ?: "wallpaper_${System.currentTimeMillis()}.${if (isLive) "mp4" else "webp"}"

        createNotificationChannel()
        val notificationId = downloadId.hashCode()

        // Update DB status to DOWNLOADING
        val existing = downloadDao.getDownloadById(downloadId)
        if (existing != null) {
            downloadDao.updateDownload(existing.copy(status = DownloadStatus.DOWNLOADING.name))
        }

        try {
            val request = Request.Builder().url(downloadUrl).build()
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful || response.body == null) {
                markFailed(existing)
                return@withContext Result.failure()
            }

            val body = response.body!!
            val totalBytes = body.contentLength()

            val targetDir = if (isLive) {
                context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
            } else {
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir
            }
            if (!targetDir.exists()) targetDir.mkdirs()

            val targetFile = File(targetDir, fileName)

            body.byteStream().use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalRead = 0L
                    var lastProgress = 0

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalRead += bytesRead

                        if (totalBytes > 0) {
                            val progress = ((totalRead * 100) / totalBytes).toInt()
                            if (progress != lastProgress && progress % 5 == 0) {
                                lastProgress = progress
                                updateProgress(notificationId, title, progress)
                                if (existing != null) {
                                    downloadDao.updateDownload(existing.copy(progress = progress))
                                }
                            }
                        }
                    }
                    outputStream.flush()
                }
            }

            // Mark completed
            if (existing != null) {
                downloadDao.updateDownload(
                    existing.copy(
                        status = DownloadStatus.COMPLETED.name,
                        progress = 100,
                        localFilePath = targetFile.absolutePath,
                        fileSizeBytes = targetFile.length()
                    )
                )
            }

            showCompletedNotification(notificationId, title)
            Result.success(workDataOf("local_path" to targetFile.absolutePath))

        } catch (e: Exception) {
            markFailed(existing)
            Result.failure()
        }
    }

    private suspend fun markFailed(existing: DownloadEntity?) {
        if (existing != null) {
            downloadDao.updateDownload(existing.copy(status = DownloadStatus.FAILED.name))
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.DOWNLOAD_NOTIFICATION_CHANNEL_ID,
                Constants.DOWNLOAD_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress of wallpaper asset downloads"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateProgress(notificationId: Int, title: String, progress: Int) {
        val builder = NotificationCompat.Builder(context, Constants.DOWNLOAD_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Downloading $title")
            .setContentText("$progress%")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, false)
            .setOngoing(true)
        notificationManager.notify(notificationId, builder.build())
    }

    private fun showCompletedNotification(notificationId: Int, title: String) {
        val builder = NotificationCompat.Builder(context, Constants.DOWNLOAD_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Download Complete")
            .setContentText("$title is ready to apply")
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
        notificationManager.notify(notificationId, builder.build())
    }
}
