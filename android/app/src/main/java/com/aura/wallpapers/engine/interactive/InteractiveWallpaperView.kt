package com.aura.wallpapers.engine.interactive

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class InteractiveWallpaperView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val TAG = "InteractiveView"
    private var renderer: InteractiveWallpaperRenderer? = null
    private var lastFrameTimeNanos: Long = 0L
    private var isAnimating: Boolean = false

    fun setConfig(config: InteractiveWallpaperConfig) {
        if (renderer == null) {
            renderer = InteractiveWallpaperRenderer(config).apply {
                if (width > 0 && height > 0) {
                    setDimensions(width, height)
                }
            }
        } else {
            renderer?.updateConfig(config)
        }
        if (isAttachedToWindow && isShown) {
            postInvalidateOnAnimation()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            renderer?.setDimensions(w, h)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isAnimating = true
        lastFrameTimeNanos = System.nanoTime()
        postInvalidateOnAnimation()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isAnimating = false
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        isAnimating = (visibility == VISIBLE)
        if (isAnimating && isAttachedToWindow) {
            lastFrameTimeNanos = System.nanoTime()
            postInvalidateOnAnimation()
        }
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        isAnimating = (visibility == VISIBLE)
        if (isAnimating && isAttachedToWindow) {
            lastFrameTimeNanos = System.nanoTime()
            postInvalidateOnAnimation()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        try {
            renderer?.onTouchEvent(event.x, event.y, event.actionMasked)
        } catch (t: Throwable) {
            Log.e(TAG, "Touch handling error: ${t.message}", t)
        }
        if (isAnimating && isAttachedToWindow) {
            postInvalidateOnAnimation()
        }
        // Return true to consume and receive drag/move events
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val now = System.nanoTime()
        val dt = if (lastFrameTimeNanos > 0L) {
            ((now - lastFrameTimeNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
        } else {
            0.016f
        }
        lastFrameTimeNanos = now

        try {
            renderer?.render(canvas, dt)
        } catch (t: Throwable) {
            Log.e(TAG, "Render error in onDraw: ${t.message}", t)
        }

        if (isAnimating && isShown && isAttachedToWindow) {
            postInvalidateOnAnimation()
        }
    }
}
