package com.aura.wallpapers.engine.interactive

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RadialGradient
import android.graphics.Shader
import android.view.MotionEvent
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class InteractiveWallpaperRenderer(
    var config: InteractiveWallpaperConfig
) {
    private var width: Int = 1080
    private var height: Int = 1920

    private val bgPaint = Paint().apply { isDither = true }
    private val drawPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val ripplePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    // Touch & Interactive State
    private var touchX: Float = -1f
    private var touchY: Float = -1f
    private var isTouching: Boolean = false
    private var parallaxOffsetX: Float = 0f

    // Reusable ripple records (pool of 6 ripples)
    private class Ripple(
        var active: Boolean = false,
        var x: Float = 0f,
        var y: Float = 0f,
        var radius: Float = 0f,
        var maxRadius: Float = 600f,
        var alpha: Float = 1f,
        var color: Int = Color.WHITE
    )
    private val ripples = Array(6) { Ripple() }

    // Reusable touch trail points (pool of 20 points)
    private val trailX = FloatArray(20)
    private val trailY = FloatArray(20)
    private var trailCount = 0

    // Particle Pool (Pre-allocated arrays to avoid GC allocations during render loops)
    private val maxParticles = 120
    private val pX = FloatArray(maxParticles)
    private val pY = FloatArray(maxParticles)
    private val pVx = FloatArray(maxParticles)
    private val pVy = FloatArray(maxParticles)
    private val pSize = FloatArray(maxParticles)
    private val pAlpha = FloatArray(maxParticles)
    private val pAngle = FloatArray(maxParticles)
    private val pColor = IntArray(maxParticles)
    private val pBaseX = FloatArray(maxParticles)
    private val pBaseY = FloatArray(maxParticles)
    private var activeParticleCount = 60

    private var powerSaveMode: Boolean = false
    private var simulationTime: Float = 0f

    private val tempPath = Path()

    init {
        initParticles()
    }

    fun setDimensions(w: Int, h: Int) {
        if (w > 0 && h > 0) {
            width = w
            height = h
            initParticles()
        }
    }

    fun setPowerSaveMode(active: Boolean) {
        powerSaveMode = active
        activeParticleCount = if (active) (config.particleCount / 2).coerceAtLeast(20) else config.particleCount
    }

    fun setParallaxOffset(xOffset: Float) {
        parallaxOffsetX = (xOffset - 0.5f) * 120f
    }

    fun onTouchEvent(eventX: Float, eventY: Float, action: Int) {
        touchX = eventX
        touchY = eventY

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                isTouching = true
                spawnRipple(eventX, eventY)
                addTrailPoint(eventX, eventY)
            }
            MotionEvent.ACTION_MOVE -> {
                isTouching = true
                addTrailPoint(eventX, eventY)
                if (config.interactionType == InteractionType.RIPPLE_DISPERSION && Math.random() < 0.15) {
                    spawnRipple(eventX, eventY)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isTouching = false
            }
        }
    }

    private fun spawnRipple(x: Float, y: Float) {
        for (r in ripples) {
            if (!r.active) {
                r.active = true
                r.x = x
                r.y = y
                r.radius = 10f
                r.maxRadius = (width * 0.45f) * config.interactionStrength
                r.alpha = 1f
                r.color = config.palette.primary
                break
            }
        }
    }

    private fun addTrailPoint(x: Float, y: Float) {
        if (trailCount < trailX.size) {
            trailX[trailCount] = x
            trailY[trailCount] = y
            trailCount++
        } else {
            // Shift left
            System.arraycopy(trailX, 1, trailX, 0, trailX.size - 1)
            System.arraycopy(trailY, 1, trailY, 0, trailY.size - 1)
            trailX[trailX.size - 1] = x
            trailY[trailY.size - 1] = y
        }
    }

    private fun initParticles() {
        val rng = Random(config.seed)
        activeParticleCount = if (powerSaveMode) (config.particleCount / 2).coerceAtLeast(20) else config.particleCount

        val colors = intArrayOf(
            config.palette.primary,
            config.palette.secondary,
            config.palette.tertiary,
            config.palette.accent
        )

        for (i in 0 until maxParticles) {
            pX[i] = rng.nextFloat() * width
            pY[i] = rng.nextFloat() * height
            pBaseX[i] = pX[i]
            pBaseY[i] = pY[i]

            val speedMultiplier = config.speed * 40f
            pVx[i] = (rng.nextFloat() - 0.5f) * speedMultiplier
            pVy[i] = (rng.nextFloat() - 0.5f) * speedMultiplier
            pSize[i] = 3f + rng.nextFloat() * (config.glowRadius.coerceAtLeast(4f))
            pAlpha[i] = 0.4f + rng.nextFloat() * 0.6f
            pAngle[i] = rng.nextFloat() * 6.28318f
            pColor[i] = colors[rng.nextInt(colors.size)]
        }
    }

    fun updateConfig(newConfig: InteractiveWallpaperConfig) {
        config = newConfig
        initParticles()
    }

    fun render(canvas: Canvas, deltaSeconds: Float) {
        val dt = deltaSeconds.coerceIn(0.001f, 0.05f)
        simulationTime += dt * config.speed

        // 1. Draw Canvas Ambient Deep Background
        bgPaint.color = config.palette.background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. Dispatch Archetype Procedural Pipeline
        canvas.save()
        // Apply home-screen swipe parallax offset
        canvas.translate(parallaxOffsetX, 0f)

        when (config.family) {
            EngineFamily.CYBERPUNK_NEON -> drawCyberpunkPipeline(canvas, dt)
            EngineFamily.ABSTRACT_GEOMETRY -> drawGeometryPipeline(canvas, dt)
            EngineFamily.FLUID_LIQUID -> drawFluidPipeline(canvas, dt)
            EngineFamily.CELESTIAL_SPACE -> drawCelestialPipeline(canvas, dt)
            EngineFamily.BIOLUMINESCENT_NATURE -> drawNaturePipeline(canvas, dt)
            EngineFamily.MINIMAL_ZEN -> drawMinimalPipeline(canvas, dt)
            EngineFamily.BOLD_CHROMATIC -> drawChromaticPipeline(canvas, dt)
        }

        // 3. Draw Touch Trail Glow if active
        if (config.interactionType == InteractionType.LIGHT_TRAIL_GLOW && trailCount > 1) {
            drawLightTrail(canvas)
        }

        // 4. Update and Draw Expanding Interactive Ripples
        drawRipples(canvas, dt)

        canvas.restore()
    }

    // =========================================================================
    // 1. CYBERPUNK & NEON PIPELINE
    // =========================================================================
    private fun drawCyberpunkPipeline(canvas: Canvas, dt: Float) {
        // Perspective Grid Horizon
        drawPaint.color = config.palette.secondary
        drawPaint.alpha = (50 * config.complexity).toInt().coerceIn(20, 100)
        drawPaint.strokeWidth = 2f

        val horizonY = height * 0.65f
        for (i in 0..12) {
            val startX = (width / 12f) * i
            canvas.drawLine(startX, height.toFloat(), width * 0.5f, horizonY, drawPaint)
        }
        for (i in 0..6) {
            val y = horizonY + (height - horizonY) * (i / 6f) * (i / 6f)
            canvas.drawLine(0f, y, width.toFloat(), y, drawPaint)
        }

        // Kinetic Circuit Lines / Data Cascades
        val pCount = activeParticleCount
        for (i in 0 until pCount) {
            // Apply velocity
            pY[i] += (120f * config.speed + i % 5 * 20f) * dt
            if (pY[i] > height + 20f) {
                pY[i] = -20f
                pX[i] = (i * 29f + simulationTime * 10f) % width
            }

            // Interactive attraction/deflection
            if (isTouching) {
                val dx = touchX - pX[i]
                val dy = touchY - pY[i]
                val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                if (dist < 450f) {
                    val force = (1f - dist / 450f) * 160f * dt * config.interactionStrength
                    if (config.interactionType == InteractionType.MAGNETIC_ATTRACTION) {
                        pX[i] += (dx / dist) * force
                        pY[i] += (dy / dist) * force
                    } else {
                        pX[i] -= (dx / dist) * force
                        pY[i] -= (dy / dist) * force
                    }
                }
            }

            // Draw glowing data node with trailing line
            glowPaint.color = pColor[i]
            glowPaint.alpha = (pAlpha[i] * 255).toInt().coerceIn(50, 255)
            glowPaint.strokeWidth = pSize[i]

            val trailLength = 35f * config.speed
            canvas.drawLine(pX[i], pY[i] - trailLength, pX[i], pY[i], glowPaint)

            fillPaint.color = config.palette.accent
            canvas.drawCircle(pX[i], pY[i], pSize[i] * 0.6f, fillPaint)
        }
    }

    // =========================================================================
    // 2. ABSTRACT GEOMETRY PIPELINE
    // =========================================================================
    private fun drawGeometryPipeline(canvas: Canvas, dt: Float) {
        val centerX = width * 0.5f
        val centerY = height * 0.5f

        val symmetry = config.symmetry.coerceAtLeast(3)
        val angleStep = (2 * Math.PI / symmetry).toFloat()
        val baseRadius = width * 0.32f

        canvas.save()
        canvas.translate(centerX, centerY)

        var rot = simulationTime * 18f
        if (isTouching && config.interactionType == InteractionType.DEPTH_PARALLAX_TILT) {
            rot += (touchX - centerX) * 0.05f
        }
        canvas.rotate(rot)

        // Draw Nested Rotating Polygonal Mandalas
        for (layer in 1..4) {
            val radius = baseRadius * (layer / 4f) * (0.85f + 0.15f * sin(simulationTime * 1.5f + layer))
            tempPath.reset()

            for (s in 0 until symmetry) {
                val angle = s * angleStep + (if (layer % 2 == 0) simulationTime * 0.2f else -simulationTime * 0.2f)
                val x = (radius * cos(angle)).toFloat()
                val y = (radius * sin(angle)).toFloat()
                if (s == 0) tempPath.moveTo(x, y) else tempPath.lineTo(x, y)
            }
            tempPath.close()

            drawPaint.color = when (layer) {
                1 -> config.palette.primary
                2 -> config.palette.secondary
                3 -> config.palette.tertiary
                else -> config.palette.accent
            }
            drawPaint.strokeWidth = 3.5f
            drawPaint.alpha = (220 - layer * 35).coerceIn(80, 255)
            canvas.drawPath(tempPath, drawPaint)
        }

        // Draw Connecting Nodes & Rays
        for (s in 0 until symmetry) {
            val angle = s * angleStep
            val x = (baseRadius * cos(angle)).toFloat()
            val y = (baseRadius * sin(angle)).toFloat()
            drawPaint.color = config.palette.accent
            drawPaint.strokeWidth = 1.5f
            drawPaint.alpha = 160
            canvas.drawLine(0f, 0f, x, y, drawPaint)

            fillPaint.color = config.palette.primary
            canvas.drawCircle(x, y, 6f, fillPaint)
        }

        canvas.restore()
    }

    // =========================================================================
    // 3. FLUID & LIQUID CHROME PIPELINE
    // =========================================================================
    private fun drawFluidPipeline(canvas: Canvas, dt: Float) {
        val waveBands = 5
        val bandHeight = height / (waveBands + 1f)

        for (band in 0 until waveBands) {
            tempPath.reset()
            val baseY = bandHeight * (band + 1)
            tempPath.moveTo(0f, baseY)

            val waveFreq = 0.003f * config.complexity
            val phase = simulationTime * 1.8f + band * 0.8f

            var x = 0f
            while (x <= width) {
                var y = baseY + sin(x * waveFreq + phase) * 45f

                // Interactive touch fluid depression / lift
                if (isTouching) {
                    val dx = x - touchX
                    val dy = y - touchY
                    val dist = sqrt(dx * dx + dy * dy)
                    if (dist < 320f) {
                        val influence = (1f - dist / 320f) * 65f * config.interactionStrength
                        y += if (config.interactionType == InteractionType.GRAVITATIONAL_REPULSION) influence else -influence
                    }
                }

                tempPath.lineTo(x, y)
                x += 20f
            }
            tempPath.lineTo(width.toFloat(), height.toFloat())
            tempPath.lineTo(0f, height.toFloat())
            tempPath.close()

            fillPaint.color = when (band % 4) {
                0 -> config.palette.primary
                1 -> config.palette.secondary
                2 -> config.palette.tertiary
                else -> config.palette.accent
            }
            fillPaint.alpha = (90 + band * 25).coerceIn(40, 210)
            canvas.drawPath(tempPath, fillPaint)
        }
    }

    // =========================================================================
    // 4. CELESTIAL & DEEP SPACE PIPELINE
    // =========================================================================
    private fun drawCelestialPipeline(canvas: Canvas, dt: Float) {
        val centerX = width * 0.5f + (if (isTouching) (touchX - width * 0.5f) * 0.15f else 0f)
        val centerY = height * 0.45f + (if (isTouching) (touchY - height * 0.45f) * 0.15f else 0f)

        // Cosmic Nebula Glow
        val nebulaRadius = width * 0.65f
        val nebulaShader = RadialGradient(
            centerX, centerY, nebulaRadius,
            intArrayOf(config.palette.tertiary, config.palette.secondary, Color.TRANSPARENT),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        fillPaint.shader = nebulaShader
        fillPaint.alpha = 110
        canvas.drawCircle(centerX, centerY, nebulaRadius, fillPaint)
        fillPaint.shader = null

        // Relativistic Warp Stars
        val pCount = activeParticleCount
        for (i in 0 until pCount) {
            val angle = pAngle[i] + simulationTime * 0.1f
            var dist = (pBaseX[i] + simulationTime * 150f * config.speed) % (width * 0.7f)
            if (dist < 10f) dist = 10f

            val sx = centerX + cos(angle) * dist
            val sy = centerY + sin(angle) * dist

            val tailDist = (dist - 20f * config.speed).coerceAtLeast(0f)
            val tx = centerX + cos(angle) * tailDist
            val ty = centerY + sin(angle) * tailDist

            glowPaint.color = pColor[i]
            glowPaint.strokeWidth = (dist / (width * 0.7f)) * 4f + 1f
            glowPaint.alpha = ((dist / (width * 0.7f)) * 255).toInt().coerceIn(60, 255)
            canvas.drawLine(tx, ty, sx, sy, glowPaint)
        }

        // Accretion Disk / Core
        fillPaint.color = config.palette.accent
        fillPaint.alpha = 240
        canvas.drawCircle(centerX, centerY, 14f, fillPaint)
    }

    // =========================================================================
    // 5. BIOLUMINESCENT NATURE PIPELINE
    // =========================================================================
    private fun drawNaturePipeline(canvas: Canvas, dt: Float) {
        val pCount = activeParticleCount
        for (i in 0 until pCount) {
            // Gentle organic drifting physics
            pAngle[i] += (dt * (0.8f + i % 3 * 0.4f))
            pX[i] += (cos(pAngle[i]) * 25f * config.speed) * dt
            pY[i] -= (35f * config.speed + (i % 4) * 10f) * dt

            if (pY[i] < -20f) pY[i] = height + 20f
            if (pX[i] < -20f) pX[i] = width + 20f
            if (pX[i] > width + 20f) pX[i] = -20f

            // Touch interactive wind vortex
            if (isTouching) {
                val dx = touchX - pX[i]
                val dy = touchY - pY[i]
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < 380f) {
                    val vortexForce = (1f - dist / 380f) * 180f * dt * config.interactionStrength
                    // Tangential swirl
                    pX[i] += (-dy / (dist + 1f)) * vortexForce
                    pY[i] += (dx / (dist + 1f)) * vortexForce
                }
            }

            // Bioluminescent glow pulse
            val pulse = 0.5f + 0.5f * sin(simulationTime * 2.5f + i)
            val currentSize = pSize[i] * (0.8f + 0.4f * pulse)

            fillPaint.color = pColor[i]
            fillPaint.alpha = (pulse * 200 + 40).toInt().coerceIn(30, 240)
            canvas.drawCircle(pX[i], pY[i], currentSize * 2.2f, fillPaint)

            fillPaint.color = config.palette.accent
            fillPaint.alpha = 255
            canvas.drawCircle(pX[i], pY[i], currentSize * 0.6f, fillPaint)
        }
    }

    // =========================================================================
    // 6. MINIMAL & ZEN PIPELINE
    // =========================================================================
    private fun drawMinimalPipeline(canvas: Canvas, dt: Float) {
        val cx = width * 0.5f
        val cy = height * 0.5f

        // Breathing Harmonic Radial Gradients
        val breathe = 0.85f + 0.15f * sin(simulationTime * 1.2f)
        val gradRadius = (width * 0.55f) * breathe

        val shader = RadialGradient(
            cx, cy, gradRadius,
            intArrayOf(config.palette.primary, config.palette.secondary, Color.TRANSPARENT),
            floatArrayOf(0f, 0.6f, 1f),
            Shader.TileMode.CLAMP
        )
        fillPaint.shader = shader
        fillPaint.alpha = 95
        canvas.drawCircle(cx, cy, gradRadius, fillPaint)
        fillPaint.shader = null

        // Zen Harmonic Concentric Wavelets
        for (i in 1..5) {
            val r = (i * 90f + simulationTime * 25f) % (width * 0.7f)
            drawPaint.color = config.palette.tertiary
            drawPaint.strokeWidth = 2f
            drawPaint.alpha = ((1f - r / (width * 0.7f)) * 140).toInt().coerceIn(10, 180)
            canvas.drawCircle(cx, cy, r, drawPaint)
        }
    }

    // =========================================================================
    // 7. BOLD CHROMATIC & OP-ART PIPELINE
    // =========================================================================
    private fun drawChromaticPipeline(canvas: Canvas, dt: Float) {
        val lines = 36
        val spacing = height / lines.toFloat()

        drawPaint.strokeWidth = 3f

        for (i in 0..lines) {
            val y = i * spacing
            val wave = sin(y * 0.008f + simulationTime * 2.2f) * 40f

            // Chromatic Channel 1: Cyan/Blue
            drawPaint.color = config.palette.primary
            drawPaint.alpha = 180
            canvas.drawLine(0f, y + wave, width.toFloat(), y + wave, drawPaint)

            // Chromatic Channel 2: Red/Pink (offset)
            drawPaint.color = config.palette.secondary
            drawPaint.alpha = 160
            canvas.drawLine(0f, y - wave * 0.8f, width.toFloat(), y - wave * 0.8f, drawPaint)
        }
    }

    // =========================================================================
    // LIGHT TRAIL GLOW RENDERING
    // =========================================================================
    private fun drawLightTrail(canvas: Canvas) {
        glowPaint.color = config.palette.accent
        glowPaint.strokeWidth = 6f
        glowPaint.alpha = 230

        for (i in 0 until trailCount - 1) {
            val alphaFactor = (i + 1).toFloat() / trailCount
            glowPaint.alpha = (alphaFactor * 240).toInt().coerceIn(20, 255)
            glowPaint.strokeWidth = alphaFactor * 8f
            canvas.drawLine(trailX[i], trailY[i], trailX[i + 1], trailY[i + 1], glowPaint)
        }
    }

    // =========================================================================
    // RIPPLE SIMULATION & RENDERING
    // =========================================================================
    private fun drawRipples(canvas: Canvas, dt: Float) {
        for (r in ripples) {
            if (r.active) {
                r.radius += (280f * config.speed + 150f) * dt
                r.alpha = (1f - (r.radius / r.maxRadius)).coerceIn(0f, 1f)

                if (r.radius >= r.maxRadius || r.alpha <= 0.01f) {
                    r.active = false
                } else {
                    ripplePaint.color = r.color
                    ripplePaint.strokeWidth = 3.5f * r.alpha + 0.5f
                    ripplePaint.alpha = (r.alpha * 200).toInt().coerceIn(10, 255)
                    canvas.drawCircle(r.x, r.y, r.radius, ripplePaint)
                }
            }
        }
    }
}
