package com.aura.wallpapers.engine.interactive

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class InteractiveWallpaperView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

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
        postInvalidateOnAnimation()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        renderer?.setDimensions(w, h)
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
        if (isAnimating) {
            lastFrameTimeNanos = System.nanoTime()
            postInvalidateOnAnimation()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        renderer?.onTouchEvent(event.x, event.y, event.actionMasked)
        postInvalidateOnAnimation()
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

        renderer?.render(canvas, dt)

        if (isAnimating && isShown) {
            postInvalidateOnAnimation()
        }
    }
}
