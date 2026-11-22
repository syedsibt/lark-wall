package com.aura.wallpapers.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.aura.wallpapers.engine.interactive.InteractiveWallpaperConfig
import com.aura.wallpapers.engine.interactive.InteractiveWallpaperRegistry
import com.aura.wallpapers.engine.interactive.InteractiveWallpaperRenderer
import com.aura.wallpapers.util.Constants
import java.io.File
import java.io.FileOutputStream

class AuraLiveWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return AuraUnifiedEngine()
    }

    inner class AuraUnifiedEngine : Engine() {

        private val TAG = "LarkLiveEngine"

        // Engine Modes
        private var currentMode: String = "interactive" // "interactive" or "video"
        private var activePresetId: String? = null
        private var activeVideoPath: String? = null

        // Video Engine State
        private var exoPlayer: ExoPlayer? = null
        private var mediaPlayer: MediaPlayer? = null

        // Interactive Procedural Engine State
        private var interactiveRenderer: InteractiveWallpaperRenderer? = null
        private var renderThread: RenderThread? = null

        private var currentHolder: SurfaceHolder? = null
        private var isVisibleOnScreen: Boolean = false
        private var powerSaveModeActive: Boolean = false
        private var surfaceWidth: Int = 1080
        private var surfaceHeight: Int = 1920

        private val wallpaperReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    PowerManager.ACTION_POWER_SAVE_MODE_CHANGED -> {
                        checkPowerSaveStatus()
                        evaluateState()
                    }
                    ACTION_UPDATE_LIVE_WALLPAPER -> {
                        val newMode = intent.getStringExtra("mode")
                        val newPresetId = intent.getStringExtra("preset_id")
                        val newPath = intent.getStringExtra("video_path")

                        Log.d(TAG, "Broadcast received: mode=$newMode preset=$newPresetId path=$newPath")

                        if (!newMode.isNullOrBlank()) {
                            currentMode = newMode
                        } else if (!newPresetId.isNullOrBlank()) {
                            currentMode = "interactive"
                        } else if (!newPath.isNullOrBlank()) {
                            currentMode = "video"
                        }

                        activePresetId = newPresetId
                        activeVideoPath = newPath

                        currentHolder?.let { holder ->
                            reloadEngine(holder)
                        }
                    }
                }
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            // Enable touch screen events on the wallpaper window
            setTouchEventsEnabled(true)

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
            initEngine(holder)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            currentHolder = holder
            if (width > 0 && height > 0) {
                surfaceWidth = width
                surfaceHeight = height
            }

            if (currentMode == "interactive") {
                interactiveRenderer?.setDimensions(surfaceWidth, surfaceHeight)
            } else {
                exoPlayer?.setVideoSurfaceHolder(holder)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            currentHolder = null
            stopAllEngines()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            isVisibleOnScreen = visible
            evaluateState()
        }

        override fun onTouchEvent(event: MotionEvent) {
            super.onTouchEvent(event)
            if (currentMode == "interactive") {
                runCatching {
                    interactiveRenderer?.onTouchEvent(event.x, event.y, event.actionMasked)
                }
            }
        }

        override fun onOffsetsChanged(
            xOffset: Float,
            yOffset: Float,
            xOffsetStep: Float,
            yOffsetStep: Float,
            xPixelOffset: Int,
            yPixelOffset: Int
        ) {
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset)
            if (currentMode == "interactive") {
                interactiveRenderer?.setParallaxOffset(xOffset)
            }
        }

        private fun reloadEngine(holder: SurfaceHolder) {
            stopAllEngines()
            initEngine(holder)
        }

        private fun initEngine(holder: SurfaceHolder) {
            val prefs = getSharedPreferences("aura_engine_prefs", Context.MODE_PRIVATE)
            val savedMode = prefs.getString(Constants.PREF_KEY_ACTIVE_LIVE_MODE, null)

            val modeToUse = if (!savedMode.isNullOrBlank()) {
                savedMode
            } else {
                currentMode
            }
            currentMode = modeToUse

            if (currentMode == "interactive") {
                initInteractiveEngine(holder)
            } else {
                initVideoEngine(holder)
            }
        }

        // ---------------------------------------------------------------------
        // Interactive Procedural Engine
        // ---------------------------------------------------------------------
        private fun initInteractiveEngine(holder: SurfaceHolder) {
            val prefs = getSharedPreferences("aura_engine_prefs", Context.MODE_PRIVATE)
            val presetId = activePresetId ?: prefs.getString(Constants.PREF_KEY_ACTIVE_LIVE_PRESET_ID, "cyber_neon_1") ?: "cyber_neon_1"

            val frame = runCatching { holder.surfaceFrame }.getOrNull()
            if (frame != null && frame.width() > 0 && frame.height() > 0) {
                surfaceWidth = frame.width()
                surfaceHeight = frame.height()
            }

            Log.d(TAG, "Initializing Interactive Engine with preset: $presetId (${surfaceWidth}x${surfaceHeight})")

            val config = InteractiveWallpaperRegistry.getConfig(presetId)
            interactiveRenderer = InteractiveWallpaperRenderer(config).apply {
                setDimensions(surfaceWidth, surfaceHeight)
                setPowerSaveMode(powerSaveModeActive)
            }

            renderThread = RenderThread(holder).apply {
                start()
            }
            evaluateState()
        }

        private fun stopInteractiveEngine() {
            renderThread?.stopRendering()
            renderThread = null
            interactiveRenderer = null
        }

        private inner class RenderThread(private val holder: SurfaceHolder) : Thread("InteractiveRenderThread") {
            @Volatile
            private var running = true
            private val pauseLock = Object()
            @Volatile
            private var paused = false

            fun stopRendering() {
                running = false
                resumeRendering()
                runCatching { join(500) }
            }

            fun pauseRendering() {
                paused = true
            }

            fun resumeRendering() {
                synchronized(pauseLock) {
                    paused = false
                    pauseLock.notifyAll()
                }
            }

            override fun run() {
                var lastTime = System.nanoTime()

                while (running) {
                    synchronized(pauseLock) {
                        while (paused && running) {
                            try {
                                pauseLock.wait()
                            } catch (e: InterruptedException) {
                                break
                            }
                        }
                    }

                    if (!running) break

                    if (!holder.surface.isValid) {
                        try {
                            sleep(16L)
                        } catch (e: InterruptedException) {
                            break
                        }
                        continue
                    }

                    val now = System.nanoTime()
                    val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                    lastTime = now

                    val canvas: Canvas? = try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            runCatching { holder.lockHardwareCanvas() }.getOrNull() ?: holder.lockCanvas()
                        } else {
                            holder.lockCanvas()
                        }
                    } catch (t: Throwable) {
                        null
                    }

                    if (canvas != null) {
                        try {
                            interactiveRenderer?.render(canvas, dt)
                        } catch (t: Throwable) {
                            Log.e(TAG, "Render exception: ${t.message}", t)
                        } finally {
                            try {
                                holder.unlockCanvasAndPost(canvas)
                            } catch (t: Throwable) {
                                // Surface destroyed or invalid
                            }
                        }
                    }

                    val targetMs = if (powerSaveModeActive) 33L else 16L
                    val elapsedMs = (System.nanoTime() - now) / 1_000_000L
                    val sleepMs = (targetMs - elapsedMs).coerceAtLeast(2L)
                    try {
                        sleep(sleepMs)
                    } catch (e: InterruptedException) {
                        break
                    }
                }
            }
        }

        // ---------------------------------------------------------------------
        // Video Engine (Backward Compatibility)
        // ---------------------------------------------------------------------
        @OptIn(UnstableApi::class)
        private fun initVideoEngine(holder: SurfaceHolder) {
            val videoFile = resolveActiveVideoFile()
            Log.d(TAG, "Initializing Video Engine with video: ${videoFile.absolutePath}")

            try {
                exoPlayer = ExoPlayer.Builder(applicationContext).build().apply {
                    repeatMode = Player.REPEAT_MODE_ALL
                    volume = 0f
                    videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                    setMediaItem(MediaItem.fromUri(Uri.fromFile(videoFile)))
                    setVideoSurfaceHolder(holder)
                    prepare()
                    addListener(object : Player.Listener {
                        override fun onPlayerError(error: PlaybackException) {
                            Log.e(TAG, "ExoPlayer error, activating fallback: ${error.message}")
                            releaseExoPlayer()
                            initNativeMediaPlayer(holder, videoFile)
                        }
                    })
                }
                evaluateState()
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
                    setOnPreparedListener { evaluateState() }
                    setOnErrorListener { _, _, _ -> true }
                    prepareAsync()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Native MediaPlayer init failed", e)
            }
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

            val fallbackFile = File(filesDir, "default_live_wallpaper.mp4")
            val expectedLength = runCatching {
                assets.openFd("live_videos/liquid_chrome_loop.mp4").use { it.length }
            }.getOrDefault(-1L)

            if (!fallbackFile.exists() || fallbackFile.length() == 0L || (expectedLength > 0L && fallbackFile.length() != expectedLength)) {
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

        // ---------------------------------------------------------------------
        // State Evaluation & Cleanup
        // ---------------------------------------------------------------------
        private fun checkPowerSaveStatus() {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            powerSaveModeActive = powerManager?.isPowerSaveMode == true
            interactiveRenderer?.setPowerSaveMode(powerSaveModeActive)
        }

        private fun evaluateState() {
            val prefs = getSharedPreferences("aura_engine_prefs", Context.MODE_PRIVATE)
            val batterySaverPref = prefs.getBoolean(Constants.PREF_KEY_BATTERY_SAVER_ENABLED, true)
            val shouldPlay = isVisibleOnScreen && !(powerSaveModeActive && batterySaverPref)

            if (currentMode == "interactive") {
                if (shouldPlay) {
                    renderThread?.resumeRendering()
                } else {
                    renderThread?.pauseRendering()
                }
            } else {
                exoPlayer?.let { player ->
                    if (shouldPlay) {
                        if (!player.isPlaying) player.play()
                    } else {
                        if (player.isPlaying) player.pause()
                    }
                }
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

        private fun stopAllEngines() {
            stopInteractiveEngine()
            releaseExoPlayer()
            releaseNativeMediaPlayer()
        }

        override fun onDestroy() {
            super.onDestroy()
            runCatching { unregisterReceiver(wallpaperReceiver) }
            stopAllEngines()
        }
    }

    companion object {
        const val ACTION_UPDATE_LIVE_WALLPAPER = "com.aura.wallpapers.ACTION_UPDATE_LIVE_WALLPAPER"
    }
}
