package com.aura.wallpapers.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.PowerManager
import android.service.wallpaper.WallpaperService
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

class AuraLiveWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return AuraVideoEngine()
    }

    inner class AuraVideoEngine : Engine() {

        private var exoPlayer: ExoPlayer? = null
        private var isVisibleOnScreen: Boolean = false
        private var powerSaveModeActive: Boolean = false

        private val powerSaveReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (PowerManager.ACTION_POWER_SAVE_MODE_CHANGED == intent?.action) {
                    checkPowerSaveStatus()
                    evaluatePlaybackState()
                }
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            registerReceiver(powerSaveReceiver, IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED))
            checkPowerSaveStatus()
        }

        @OptIn(UnstableApi::class)
        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            initPlayer(holder)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            exoPlayer?.setVideoSurfaceHolder(holder)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            exoPlayer?.pause()
            exoPlayer?.clearVideoSurfaceHolder(holder)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            isVisibleOnScreen = visible
            evaluatePlaybackState()
        }

        @OptIn(UnstableApi::class)
        private fun initPlayer(holder: SurfaceHolder) {
            if (exoPlayer != null) {
                exoPlayer?.setVideoSurfaceHolder(holder)
                evaluatePlaybackState()
                return
            }

            // Read active video path from SharedPreferences
            val prefs = getSharedPreferences("aura_engine_prefs", Context.MODE_PRIVATE)
            val videoPath = prefs.getString(Constants.PREF_KEY_ACTIVE_LIVE_WALLPAPER_PATH, null)

            val mediaItem = if (videoPath != null && File(videoPath).exists()) {
                MediaItem.fromUri(videoPath)
            } else {
                // Fallback bundled or default streaming sample
                MediaItem.fromUri("https://assets.mixkit.co/videos/preview/mixkit-stars-in-space-1610-large.mp4")
            }

            exoPlayer = ExoPlayer.Builder(applicationContext).build().apply {
                repeatMode = Player.REPEAT_MODE_ALL
                volume = 0f // Guaranteed zero audio track emission
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                setMediaItem(mediaItem)
                setVideoSurfaceHolder(holder)
                prepare()
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        // In case of unsupported codec or stream failure, pause gracefully without crashing
                        pause()
                    }
                })
            }

            evaluatePlaybackState()
        }

        private fun checkPowerSaveStatus() {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            powerSaveModeActive = powerManager?.isPowerSaveMode == true
        }

        private fun evaluatePlaybackState() {
            val player = exoPlayer ?: return

            val prefs = getSharedPreferences("aura_engine_prefs", Context.MODE_PRIVATE)
            val batterySaverPref = prefs.getBoolean(Constants.PREF_KEY_BATTERY_SAVER_ENABLED, true)

            // Strict Performance Guard: Only play if surface is visible and not restricted by battery saver
            val shouldPlay = isVisibleOnScreen && !(powerSaveModeActive && batterySaverPref)

            if (shouldPlay) {
                if (!player.isPlaying) {
                    player.play()
                }
            } else {
                if (player.isPlaying) {
                    player.pause()
                }
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            runCatching { unregisterReceiver(powerSaveReceiver) }
            exoPlayer?.stop()
            exoPlayer?.clearVideoSurfaceHolder(surfaceHolder)
            exoPlayer?.release()
            exoPlayer = null
        }
    }
}
