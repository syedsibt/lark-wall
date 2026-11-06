package com.aura.wallpapers.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.aura.wallpapers.util.Constants
import java.io.File
import java.io.FileOutputStream

class AuraLiveWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return AuraVideoEngine()
    }

    inner class AuraVideoEngine : Engine() {

        private val TAG = "LarkLiveEngine"
        private var exoPlayer: ExoPlayer? = null
        private var mediaPlayer: MediaPlayer? = null
        private var currentHolder: SurfaceHolder? = null
        private var isVisibleOnScreen: Boolean = false
        private var powerSaveModeActive: Boolean = false
        private var activeVideoPath: String? = null

        private val wallpaperReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    PowerManager.ACTION_POWER_SAVE_MODE_CHANGED -> {
                        checkPowerSaveStatus()
                        evaluatePlaybackState()
                    }
                    ACTION_UPDATE_LIVE_WALLPAPER -> {
                        val newPath = intent.getStringExtra("video_path")
                        if (!newPath.isNullOrBlank()) {
                            Log.d(TAG, "Dynamic update broadcast received: $newPath")
                            activeVideoPath = newPath
                            currentHolder?.let { reloadPlayer(it) }
                        }
                    }
                }
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            val filter = IntentFilter().apply {
                addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
                addAction(ACTION_UPDATE_LIVE_WALLPAPER)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(wallpaperReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(wallpaperReceiver, filter)
            }
            checkPowerSaveStatus()
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            currentHolder = holder
            initPlayer(holder)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            if (currentHolder != holder) {
                currentHolder = holder
                exoPlayer?.setVideoSurfaceHolder(holder)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            currentHolder = null
            releasePlayers()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            isVisibleOnScreen = visible
            evaluatePlaybackState()
        }

        private fun resolveActiveVideoFile(): File {
            val prefs = getSharedPreferences("aura_engine_prefs", Context.MODE_PRIVATE)
            val storedPath = activeVideoPath ?: prefs.getString(Constants.PREF_KEY_ACTIVE_LIVE_WALLPAPER_PATH, null)

            if (!storedPath.isNullOrBlank()) {
                val f = File(storedPath)
                if (f.exists() && f.length() > 0L) {
                    return f
                }
            }

            // Extract default bundled offline live loop to internal storage
            val fallbackFile = File(filesDir, "default_live_wallpaper.mp4")
            if (!fallbackFile.exists() || fallbackFile.length() == 0L) {
                runCatching {
                    assets.open("live_videos/liquid_chrome_loop.mp4").use { input ->
                        FileOutputStream(fallbackFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
            return fallbackFile
        }

        private fun reloadPlayer(holder: SurfaceHolder) {
            releasePlayers()
            initPlayer(holder)
        }

        @OptIn(UnstableApi::class)
        private fun initPlayer(holder: SurfaceHolder) {
            val videoFile = resolveActiveVideoFile()
            Log.d(TAG, "Initializing playback with video: ${videoFile.absolutePath} (${videoFile.length()} bytes)")

            try {
                // Primary: Media3 ExoPlayer Engine
                exoPlayer = ExoPlayer.Builder(applicationContext).build().apply {
                    repeatMode = Player.REPEAT_MODE_ALL
                    volume = 0f
                    videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                    setMediaItem(MediaItem.fromUri(Uri.fromFile(videoFile)))
                    setVideoSurfaceHolder(holder)
                    prepare()
                    addListener(object : Player.Listener {
                        override fun onPlayerError(error: PlaybackException) {
                            Log.e(TAG, "ExoPlayer error occurred, activating MediaPlayer fallback: ${error.message}")
                            // Automatic fallback to native Android MediaPlayer
                            releaseExoPlayer()
                            initNativeMediaPlayer(holder, videoFile)
                        }
                    })
                }
                evaluatePlaybackState()
            } catch (e: Exception) {
                Log.e(TAG, "ExoPlayer init failed, activating MediaPlayer fallback", e)
                releaseExoPlayer()
                initNativeMediaPlayer(holder, videoFile)
            }
        }

        private fun initNativeMediaPlayer(holder: SurfaceHolder, videoFile: File) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(applicationContext, Uri.fromFile(videoFile))
                    setDisplay(holder)
                    isLooping = true
                    setVolume(0f, 0f)
                    setOnPreparedListener { mp ->
                        evaluatePlaybackState()
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.e(TAG, "Native MediaPlayer error what=$what extra=$extra")
                        true
                    }
                    prepareAsync()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Native MediaPlayer init failed", e)
            }
        }

        private fun checkPowerSaveStatus() {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            powerSaveModeActive = powerManager?.isPowerSaveMode == true
        }

        private fun evaluatePlaybackState() {
            val prefs = getSharedPreferences("aura_engine_prefs", Context.MODE_PRIVATE)
            val batterySaverPref = prefs.getBoolean(Constants.PREF_KEY_BATTERY_SAVER_ENABLED, true)
            val shouldPlay = isVisibleOnScreen && !(powerSaveModeActive && batterySaverPref)

            // ExoPlayer control
            exoPlayer?.let { player ->
                if (shouldPlay) {
                    if (!player.isPlaying) player.play()
                } else {
                    if (player.isPlaying) player.pause()
                }
            }

            // Native MediaPlayer control
            mediaPlayer?.let { player ->
                runCatching {
                    if (shouldPlay) {
                        if (!player.isPlaying) player.start()
                    } else {
                        if (player.isPlaying) player.pause()
                    }
                }
            }
        }

        private fun releaseExoPlayer() {
            exoPlayer?.stop()
            currentHolder?.let { exoPlayer?.clearVideoSurfaceHolder(it) }
            exoPlayer?.release()
            exoPlayer = null
        }

        private fun releaseNativeMediaPlayer() {
            runCatching {
                mediaPlayer?.stop()
                mediaPlayer?.reset()
                mediaPlayer?.release()
            }
            mediaPlayer = null
        }

        private fun releasePlayers() {
            releaseExoPlayer()
            releaseNativeMediaPlayer()
        }

        override fun onDestroy() {
            super.onDestroy()
            runCatching { unregisterReceiver(wallpaperReceiver) }
            releasePlayers()
        }
    }

    companion object {
        const val ACTION_UPDATE_LIVE_WALLPAPER = "com.aura.wallpapers.ACTION_UPDATE_LIVE_WALLPAPER"
    }
}
