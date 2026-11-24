package com.aura.wallpapers.engine.interactive

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
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
    private val motionLoopsPipeline = MotionLoopsPipeline()
    private val signaturePipeline = SignatureDesignsPipeline()

    // Touch events arrive on the UI thread while frames are drawn on a render thread
    // (live wallpaper service), so all mutable state is guarded by this lock.
    private val stateLock = Any()

    // Cached unit-radius gradients (radius 1 at origin), drawn with canvas translate/scale
    // so that no shader objects are allocated per frame.
    private val radialCache = HashMap<String, RadialGradient>()
    private var auroraShader: LinearGradient? = null
    private var auroraKey: Long = 0L

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
        synchronized(stateLock) {
            if (w > 0 && h > 0) {
                width = w
                height = h
                initParticles()
            }
        }
    }

    /** Jumps the animation clock, used to render a representative still frame (thumbnails, exports). */
    fun setSimulationTime(time: Float) {
        synchronized(stateLock) {
            simulationTime = time
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
        synchronized(stateLock) {
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
        synchronized(stateLock) {
            config = newConfig
            trailCount = 0
            for (r in ripples) r.active = false
            initParticles()
        }
    }

    /** Unit radial gradient cached by its colours; draw it with [drawRadial]. */
    private fun unitRadial(colors: IntArray, stops: FloatArray): RadialGradient {
        val key = colors.joinToString(",") + "|" + stops.joinToString(",")
        return radialCache.getOrPut(key) {
            RadialGradient(0f, 0f, 1f, colors, stops, Shader.TileMode.CLAMP)
        }
    }

    private fun drawRadial(canvas: Canvas, cx: Float, cy: Float, radius: Float, shader: RadialGradient, alpha: Int) {
        if (radius <= 0f) return
        fillPaint.shader = shader
        fillPaint.alpha = alpha
        canvas.save()
        canvas.translate(cx, cy)
        canvas.scale(radius, radius)
        canvas.drawCircle(0f, 0f, 1f, fillPaint)
        canvas.restore()
        fillPaint.shader = null
    }

    fun render(canvas: Canvas, deltaSeconds: Float) = synchronized(stateLock) {
        renderLocked(canvas, deltaSeconds)
    }

    private fun renderLocked(canvas: Canvas, deltaSeconds: Float) {
        if (width <= 0 || height <= 0) return

        val dt = deltaSeconds.coerceIn(0.001f, 0.05f)
        simulationTime += dt * config.speed

        // 1. Draw Canvas Ambient Deep Background
        bgPaint.color = config.palette.background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. Dispatch Archetype Procedural Pipeline
        canvas.save()
        canvas.translate(parallaxOffsetX, 0f)

        try {
            if (SignatureCatalog.isSignature(config.archetype)) {
                signaturePipeline.render(
                    canvas, config.archetype, simulationTime, dt, width, height,
                    touchX - parallaxOffsetX, touchY, isTouching, config.palette
                )
            } else when (config.family) {
                EngineFamily.CYBERPUNK_NEON -> drawCyberpunkPipeline(canvas, dt)
                EngineFamily.ABSTRACT_GEOMETRY -> drawGeometryPipeline(canvas, dt)
                EngineFamily.FLUID_LIQUID -> drawFluidPipeline(canvas, dt)
                EngineFamily.CELESTIAL_SPACE -> drawCelestialPipeline(canvas, dt)
                EngineFamily.BIOLUMINESCENT_NATURE -> drawNaturePipeline(canvas, dt)
                EngineFamily.MINIMAL_ZEN -> drawMinimalPipeline(canvas, dt)
                EngineFamily.BOLD_CHROMATIC -> drawChromaticPipeline(canvas, dt)
                EngineFamily.MOTION_LOOPS -> motionLoopsPipeline.render(
                    canvas,
                    config.archetype,
                    simulationTime,
                    dt,
                    width,
                    height,
                    touchX,
                    touchY,
                    isTouching,
                    config.palette
                )
            }

        } catch (t: Throwable) {
            android.util.Log.e("InteractiveRenderer", "Render exception in family ${config.family}: ${t.message}", t)
        }

        canvas.restore()

        // Touch feedback is drawn in screen space (outside the parallax translation)
        // so ripples and trails appear exactly under the finger.
        try {
            // 3. Touch trail: fades out once the finger is lifted instead of staying on screen forever
            if (!isTouching && trailCount > 0) {
                System.arraycopy(trailX, 1, trailX, 0, trailX.size - 1)
                System.arraycopy(trailY, 1, trailY, 0, trailY.size - 1)
                trailCount--
            }
            if (config.interactionType == InteractionType.LIGHT_TRAIL_GLOW && trailCount > 1) {
                drawLightTrail(canvas)
            }

            // 4. Update and Draw Expanding Interactive Ripples
            drawRipples(canvas, dt)
        } catch (t: Throwable) {
            android.util.Log.e("InteractiveRenderer", "Touch overlay exception: ${t.message}", t)
        }
    }

    // =========================================================================
    // 1. CYBERPUNK & NEON PIPELINES (5 distinct archetypes)
    // =========================================================================
    private fun drawCyberpunkPipeline(canvas: Canvas, dt: Float) {
        when (config.archetype) {
            "data_matrix" -> {
                // Falling Digital Rain Columns
                val cols = 18
                val colWidth = width / cols.toFloat()
                fillPaint.color = config.palette.primary
                fillPaint.textSize = width / 38f

                for (c in 0 until cols) {
                    val speed = 80f + (c % 5) * 35f
                    val dropY = ((simulationTime * speed + c * 173f) % (height + 200f)) - 100f
                    val x = c * colWidth + colWidth * 0.3f

                    // Trail characters
                    for (i in 0..12) {
                        val y = dropY - i * fillPaint.textSize * 1.15f
                        if (y in -20f..height.toFloat()) {
                            val alpha = ((1f - i / 12f) * 255).toInt().coerceIn(20, 255)
                            fillPaint.color = if (i == 0) config.palette.accent else config.palette.primary
                            fillPaint.alpha = alpha
                            val charIndex = ((c * 17 + i * 3) % 16).toString(16).uppercase()
                            canvas.drawText(charIndex, x, y, fillPaint)
                        }
                    }
                }
            }
            "circuit_net" -> {
                // Microchip Circuit Traces with conductive pads
                drawPaint.color = config.palette.secondary
                drawPaint.strokeWidth = 3f
                drawPaint.alpha = 140

                val gridSize = 80f
                val cols = (width / gridSize).toInt()
                val rows = (height / gridSize).toInt()

                for (x in 1 until cols step 2) {
                    for (y in 1 until rows step 2) {
                        val px = x * gridSize
                        val py = y * gridSize
                        fillPaint.color = config.palette.primary
                        canvas.drawCircle(px, py, 5f, fillPaint)

                        // Branching right-angle trace
                        val targetX = px + (if ((x + y) % 2 == 0) gridSize else 0f)
                        val targetY = py + (if ((x + y) % 2 != 0) gridSize else 0f)
                        canvas.drawLine(px, py, targetX, targetY, drawPaint)
                    }
                }

                // Pulses traveling on circuit lines
                val pulseT = (simulationTime * 2.5f) % 1f
                for (p in 0 until activeParticleCount.coerceAtMost(30)) {
                    val px = ((p * 137f + simulationTime * 120f) % width)
                    val py = ((p * 191f) % height)
                    fillPaint.color = config.palette.accent
                    fillPaint.alpha = 240
                    canvas.drawCircle(px, py, 6f, fillPaint)
                }
            }
            "highway_streaks" -> {
                // 3D Perspective Synthwave Highway
                val horizonY = height * 0.55f
                val centerX = width * 0.5f

                // Horizon glow
                drawRadial(
                    canvas, centerX, horizonY, width * 0.45f,
                    unitRadial(intArrayOf(config.palette.secondary, Color.TRANSPARENT), floatArrayOf(0f, 1f)),
                    130
                )

                // Highway Road Grid
                drawPaint.color = config.palette.primary
                drawPaint.strokeWidth = 2.5f
                drawPaint.alpha = 180

                for (i in 0..10) {
                    val groundX = (width / 10f) * i
                    canvas.drawLine(groundX, height.toFloat(), centerX, horizonY, drawPaint)
                }
                for (i in 1..8) {
                    val frac = (i / 8f) * (i / 8f)
                    val y = horizonY + (height - horizonY) * frac
                    canvas.drawLine(0f, y, width.toFloat(), y, drawPaint)
                }
            }
            "prismatic_hud" -> {
                // Concentric Cybernetic Telemetry Rings & Radar Sweep
                val cx = width * 0.5f + (if (isTouching) (touchX - width * 0.5f) * 0.1f else 0f)
                val cy = height * 0.45f + (if (isTouching) (touchY - height * 0.45f) * 0.1f else 0f)

                drawPaint.color = config.palette.primary
                drawPaint.strokeWidth = 2.5f
                drawPaint.alpha = 190

                // Outer and inner rings
                val r1 = width * 0.38f
                val r2 = width * 0.26f
                val r3 = width * 0.12f
                canvas.drawCircle(cx, cy, r1, drawPaint)
                canvas.drawCircle(cx, cy, r2, drawPaint)
                canvas.drawCircle(cx, cy, r3, drawPaint)

                // Radar sweep line
                val sweepAngle = simulationTime * 2.2f
                val sx = cx + cos(sweepAngle) * r1
                val sy = cy + sin(sweepAngle) * r1
                drawPaint.color = config.palette.accent
                drawPaint.strokeWidth = 3f
                canvas.drawLine(cx, cy, sx, sy, drawPaint)

                // Reticle Crosshairs
                drawPaint.color = config.palette.secondary
                drawPaint.strokeWidth = 1.5f
                canvas.drawLine(cx - r1 - 20f, cy, cx + r1 + 20f, cy, drawPaint)
                canvas.drawLine(cx, cy - r1 - 20f, cx, cy + r1 + 20f, drawPaint)
            }
            else -> {
                // cyber_rain: Neon city droplets hitting pavement with reflective splash rings
                drawPaint.color = config.palette.primary
                drawPaint.strokeWidth = 2.5f

                val pCount = activeParticleCount
                for (i in 0 until pCount) {
                    pY[i] += (450f * config.speed + (i % 6) * 40f) * dt
                    if (pY[i] > height) {
                        pY[i] = -20f
                        pX[i] = (i * 37f + simulationTime * 40f) % width
                    }

                    drawPaint.alpha = (pAlpha[i] * 230).toInt().coerceIn(50, 255)
                    val rainLen = 35f * config.speed
                    canvas.drawLine(pX[i], pY[i] - rainLen, pX[i], pY[i], drawPaint)

                    // Splash ripple near bottom
                    if (pY[i] > height * 0.8f) {
                        val splashR = (height - pY[i]) * 0.3f
                        ripplePaint.color = config.palette.secondary
                        ripplePaint.strokeWidth = 1.5f
                        ripplePaint.alpha = 120
                        canvas.drawCircle(pX[i], height - 30f, splashR.coerceAtLeast(3f), ripplePaint)
                    }
                }
            }
        }
    }

    // =========================================================================
    // 2. ABSTRACT GEOMETRY PIPELINES (5 distinct archetypes)
    // =========================================================================
    private fun drawGeometryPipeline(canvas: Canvas, dt: Float) {
        val cx = width * 0.5f
        val cy = height * 0.5f

        when (config.archetype) {
            "morphing_polyhedra" -> {
                // 3D Wireframe Rotating Hyper-Polyhedron
                val rotX = simulationTime * 0.8f
                val rotY = simulationTime * 1.2f
                val radius = width * 0.35f

                val vertices = arrayOf(
                    floatArrayOf(-1f, -1f, -1f), floatArrayOf(1f, -1f, -1f),
                    floatArrayOf(1f, 1f, -1f), floatArrayOf(-1f, 1f, -1f),
                    floatArrayOf(-1f, -1f, 1f), floatArrayOf(1f, -1f, 1f),
                    floatArrayOf(1f, 1f, 1f), floatArrayOf(-1f, 1f, 1f)
                )

                // Project to 2D
                val projX = FloatArray(8)
                val projY = FloatArray(8)
                for (v in 0 until 8) {
                    val x0 = vertices[v][0]
                    val y0 = vertices[v][1]
                    val z0 = vertices[v][2]

                    // Rotate Y
                    val x1 = x0 * cos(rotY) + z0 * sin(rotY)
                    val z1 = -x0 * sin(rotY) + z0 * cos(rotY)

                    // Rotate X
                    val y2 = y0 * cos(rotX) - z1 * sin(rotX)
                    val z2 = y0 * sin(rotX) + z1 * cos(rotX)

                    val scale = 2.5f / (3f + z2)
                    projX[v] = cx + x1 * radius * scale
                    projY[v] = cy + y2 * radius * scale
                }

                // Draw Edges
                val edges = arrayOf(
                    intArrayOf(0, 1), intArrayOf(1, 2), intArrayOf(2, 3), intArrayOf(3, 0),
                    intArrayOf(4, 5), intArrayOf(5, 6), intArrayOf(6, 7), intArrayOf(7, 4),
                    intArrayOf(0, 4), intArrayOf(1, 5), intArrayOf(2, 6), intArrayOf(3, 7)
                )

                drawPaint.color = config.palette.primary
                drawPaint.strokeWidth = 3.5f
                drawPaint.alpha = 230

                for (e in edges) {
                    canvas.drawLine(projX[e[0]], projY[e[0]], projX[e[1]], projY[e[1]], drawPaint)
                }

                // Glowing vertices
                fillPaint.color = config.palette.accent
                for (v in 0 until 8) {
                    canvas.drawCircle(projX[v], projY[v], 6f, fillPaint)
                }
            }
            "voronoi_lattice" -> {
                // Shimmering Voronoi Prism Lattice
                val pCount = activeParticleCount.coerceAtMost(35)
                drawPaint.strokeWidth = 1.5f
                for (i in 0 until pCount) {
                    pX[i] += cos(pAngle[i]) * 15f * dt
                    pY[i] += sin(pAngle[i]) * 15f * dt
                    if (pX[i] !in 0f..width.toFloat()) pAngle[i] = Math.PI.toFloat() - pAngle[i]
                    if (pY[i] !in 0f..height.toFloat()) pAngle[i] = -pAngle[i]

                    for (j in i + 1 until pCount) {
                        val dx = pX[i] - pX[j]
                        val dy = pY[i] - pY[j]
                        val dist = sqrt(dx * dx + dy * dy)
                        if (dist < 220f) {
                            drawPaint.color = if ((i + j) % 2 == 0) config.palette.primary else config.palette.secondary
                            drawPaint.alpha = ((1f - dist / 220f) * 190).toInt().coerceIn(20, 230)
                            canvas.drawLine(pX[i], pY[i], pX[j], pY[j], drawPaint)
                        }
                    }

                    fillPaint.color = config.palette.accent
                    canvas.drawCircle(pX[i], pY[i], 4f, fillPaint)
                }
            }
            "isometric_cubes" -> {
                // Isometric Modular Cube Grid with elevation undulations
                val cubeSize = 65f
                val hSize = cubeSize * 0.866f
                val cols = (width / (cubeSize * 1.5f)).toInt() + 1
                val rows = (height / hSize).toInt() + 2

                for (r in 0..rows) {
                    for (c in 0..cols) {
                        val bx = c * cubeSize * 1.5f + (if (r % 2 == 1) cubeSize * 0.75f else 0f)
                        val elevation = sin(simulationTime * 2f + (c + r) * 0.6f) * 20f
                        val by = r * hSize + elevation

                        // Isometric hexagon outline
                        tempPath.reset()
                        for (s in 0..5) {
                            val angle = s * (Math.PI / 3).toFloat()
                            val px = bx + cubeSize * 0.5f * cos(angle)
                            val py = by + cubeSize * 0.5f * sin(angle)
                            if (s == 0) tempPath.moveTo(px, py) else tempPath.lineTo(px, py)
                        }
                        tempPath.close()

                        drawPaint.color = if ((c + r) % 2 == 0) config.palette.primary else config.palette.secondary
                        drawPaint.strokeWidth = 2f
                        drawPaint.alpha = 150
                        canvas.drawPath(tempPath, drawPaint)
                    }
                }
            }
            "lissajous_curves" -> {
                // Harmonograph laser curves tracing complex knot topologies
                val points = 160
                tempPath.reset()
                val a = 3f * config.complexity
                val b = 4f * config.complexity
                val delta = simulationTime * 1.5f

                for (i in 0..points) {
                    val t = (i / points.toFloat()) * (2 * Math.PI.toFloat())
                    val x = cx + sin(a * t + delta) * (width * 0.38f)
                    val y = cy + sin(b * t) * (height * 0.28f)
                    if (i == 0) tempPath.moveTo(x, y) else tempPath.lineTo(x, y)
                }

                glowPaint.color = config.palette.primary
                glowPaint.strokeWidth = 4f
                glowPaint.alpha = 240
                canvas.drawPath(tempPath, glowPaint)

                drawPaint.color = config.palette.accent
                drawPaint.strokeWidth = 2f
                drawPaint.alpha = 255
                canvas.drawPath(tempPath, drawPaint)
            }
            else -> {
                // kaleido_mandala (Sacred Multi-Fold Symmetry Mandala)
                val symmetry = config.symmetry.coerceAtLeast(6)
                val angleStep = (2 * Math.PI / symmetry).toFloat()
                val baseRadius = width * 0.35f

                canvas.save()
                canvas.translate(cx, cy)
                canvas.rotate(simulationTime * 15f)

                for (layer in 1..4) {
                    val radius = baseRadius * (layer / 4f) * (0.85f + 0.15f * sin(simulationTime * 1.8f + layer))
                    tempPath.reset()

                    for (s in 0 until symmetry) {
                        val angle = s * angleStep + (if (layer % 2 == 0) simulationTime * 0.15f else -simulationTime * 0.15f)
                        val x = (radius * cos(angle))
                        val y = (radius * sin(angle))
                        if (s == 0) tempPath.moveTo(x, y) else tempPath.lineTo(x, y)
                    }
                    tempPath.close()

                    drawPaint.color = when (layer) {
                        1 -> config.palette.primary
                        2 -> config.palette.secondary
                        3 -> config.palette.tertiary
                        else -> config.palette.accent
                    }
                    drawPaint.strokeWidth = 3f
                    drawPaint.alpha = (230 - layer * 30).coerceIn(80, 255)
                    canvas.drawPath(tempPath, drawPaint)
                }
                canvas.restore()
            }
        }
    }

    // =========================================================================
    // 3. FLUID & LIQUID CHROME PIPELINES (5 distinct archetypes)
    // =========================================================================
    private fun drawFluidPipeline(canvas: Canvas, dt: Float) {
        when (config.archetype) {
            "magnetic_ferrofluid" -> {
                // Sharp radiating metallic spikes from center reacting to magnetic attraction
                val cx = width * 0.5f + (if (isTouching) (touchX - width * 0.5f) * 0.2f else 0f)
                val cy = height * 0.5f + (if (isTouching) (touchY - height * 0.5f) * 0.2f else 0f)
                val spikes = 32

                tempPath.reset()
                for (s in 0 until spikes) {
                    val angle = s * (2 * Math.PI / spikes).toFloat()
                    val pulse = sin(simulationTime * 3f + s * 0.8f) * 45f
                    val outerR = width * 0.32f + pulse + (if (s % 2 == 0) 60f else 0f)
                    val px = cx + cos(angle) * outerR
                    val py = cy + sin(angle) * outerR
                    if (s == 0) tempPath.moveTo(px, py) else tempPath.lineTo(px, py)
                }
                tempPath.close()

                fillPaint.color = config.palette.primary
                fillPaint.alpha = 200
                canvas.drawPath(tempPath, fillPaint)

                drawPaint.color = config.palette.accent
                drawPaint.strokeWidth = 3f
                canvas.drawPath(tempPath, drawPaint)
            }
            "oil_slick_swirls" -> {
                // Thin-film interference swirls with rainbow dispersion
                val bands = 6
                for (b in 0 until bands) {
                    val r = (width * 0.45f) * ((b + 1) / 6f)
                    val cx = width * 0.5f + sin(simulationTime + b) * 35f
                    val cy = height * 0.5f + cos(simulationTime * 0.8f + b) * 45f

                    drawPaint.color = when (b % 4) {
                        0 -> config.palette.primary
                        1 -> config.palette.secondary
                        2 -> config.palette.tertiary
                        else -> config.palette.accent
                    }
                    drawPaint.strokeWidth = 14f
                    drawPaint.alpha = 150
                    canvas.drawCircle(cx, cy, r, drawPaint)
                }
            }
            "biolum_plasma" -> {
                // Convection heat plumes and glowing plasma tendrils
                val cx = width * 0.5f
                val cy = height * 0.5f
                for (i in 0 until 5) {
                    val radius = width * (0.2f + i * 0.1f)
                    drawRadial(
                        canvas,
                        cx + sin(simulationTime + i) * 60f,
                        cy + cos(simulationTime * 0.7f + i) * 80f,
                        radius,
                        unitRadial(
                            intArrayOf(config.palette.primary, config.palette.secondary, Color.TRANSPARENT),
                            floatArrayOf(0f, 0.5f, 1f)
                        ),
                        90
                    )
                }
            }
            "viscous_ripple_pool" -> {
                // Multi-source water wave simulation with reflective interference crests
                val cx = width * 0.5f
                val cy = height * 0.5f
                for (i in 1..8) {
                    val r = (i * 65f + simulationTime * 45f) % (width * 0.7f)
                    drawPaint.color = config.palette.primary
                    drawPaint.strokeWidth = 3f
                    drawPaint.alpha = ((1f - r / (width * 0.7f)) * 210).toInt().coerceIn(10, 240)
                    canvas.drawCircle(cx, cy, r, drawPaint)
                }
            }
            else -> {
                // liquid_chrome_waves (Undulating molten silver bands with specular sheen)
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
                        if (isTouching) {
                            val dx = x - touchX
                            val dy = y - touchY
                            val dist = sqrt(dx * dx + dy * dy)
                            if (dist < 320f) {
                                y -= (1f - dist / 320f) * 55f * config.interactionStrength
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
        }
    }

    // =========================================================================
    // 4. CELESTIAL & SPACE PIPELINES (5 distinct archetypes)
    // =========================================================================
    private fun drawCelestialPipeline(canvas: Canvas, dt: Float) {
        val cx = width * 0.5f + (if (isTouching) (touchX - width * 0.5f) * 0.15f else 0f)
        val cy = height * 0.45f + (if (isTouching) (touchY - height * 0.45f) * 0.15f else 0f)

        when (config.archetype) {
            "black_hole_lens" -> {
                // Relativistic Einstein Ring & Swirling Accretion Disk
                val accretionR = width * 0.38f
                drawRadial(
                    canvas, cx, cy, accretionR,
                    unitRadial(
                        intArrayOf(Color.BLACK, config.palette.secondary, config.palette.primary, Color.TRANSPARENT),
                        floatArrayOf(0f, 0.4f, 0.8f, 1f)
                    ),
                    230
                )

                // Relativistic Photon Ring
                drawPaint.color = config.palette.accent
                drawPaint.strokeWidth = 4f
                drawPaint.alpha = 255
                canvas.drawCircle(cx, cy, accretionR * 0.45f, drawPaint)

                // Black Hole Event Horizon
                fillPaint.color = Color.BLACK
                fillPaint.alpha = 255
                canvas.drawCircle(cx, cy, accretionR * 0.42f, fillPaint)
            }
            "orbital_rings" -> {
                // Concentric Planetary Rings
                canvas.save()
                canvas.translate(cx, cy)
                canvas.rotate(-25f)

                for (r in 1..8) {
                    val ringRadius = (width * 0.2f) + r * 22f
                    drawPaint.color = if (r % 2 == 0) config.palette.primary else config.palette.secondary
                    drawPaint.strokeWidth = 3f
                    drawPaint.alpha = (140 + r * 10).coerceIn(60, 240)
                    canvas.drawOval(-ringRadius, -ringRadius * 0.35f, ringRadius, ringRadius * 0.35f, drawPaint)
                }

                // Gas Giant Planet Core
                fillPaint.color = config.palette.tertiary
                canvas.drawCircle(0f, 0f, width * 0.18f, fillPaint)
                canvas.restore()
            }
            "solar_pulsar" -> {
                // Neutron Star with Sweeping Collimated Beams
                val beamAngle = simulationTime * 3.5f
                val beamLen = width * 0.8f

                drawPaint.color = config.palette.accent
                drawPaint.strokeWidth = 6f
                drawPaint.alpha = 240
                canvas.drawLine(cx, cy, cx + cos(beamAngle) * beamLen, cy + sin(beamAngle) * beamLen, drawPaint)
                canvas.drawLine(cx, cy, cx - cos(beamAngle) * beamLen, cy - sin(beamAngle) * beamLen, drawPaint)

                // Pulsar Core
                fillPaint.color = config.palette.primary
                canvas.drawCircle(cx, cy, 22f, fillPaint)
                fillPaint.color = Color.WHITE
                canvas.drawCircle(cx, cy, 12f, fillPaint)
            }
            "nebula_clouds" -> {
                // Layered Cosmic Gas Clouds
                val pCount = activeParticleCount
                for (i in 0 until pCount) {
                    val angle = pAngle[i] + simulationTime * 0.05f
                    val dist = (pBaseX[i] * 0.8f)
                    val sx = cx + cos(angle) * dist
                    val sy = cy + sin(angle) * dist

                    fillPaint.color = pColor[i]
                    fillPaint.alpha = (pAlpha[i] * 120).toInt().coerceIn(30, 160)
                    canvas.drawCircle(sx, sy, pSize[i] * 3.5f, fillPaint)
                }
            }
            else -> {
                // warp_speed_starfield (Relativistic Warp Streaks)
                val pCount = activeParticleCount
                for (i in 0 until pCount) {
                    val angle = pAngle[i] + simulationTime * 0.1f
                    var dist = (pBaseX[i] + simulationTime * 180f * config.speed) % (width * 0.75f)
                    if (dist < 10f) dist = 10f

                    val sx = cx + cos(angle) * dist
                    val sy = cy + sin(angle) * dist
                    val tailDist = (dist - 25f * config.speed).coerceAtLeast(0f)
                    val tx = cx + cos(angle) * tailDist
                    val ty = cy + sin(angle) * tailDist

                    glowPaint.color = pColor[i]
                    glowPaint.strokeWidth = (dist / (width * 0.75f)) * 4.5f + 1f
                    glowPaint.alpha = ((dist / (width * 0.75f)) * 255).toInt().coerceIn(60, 255)
                    canvas.drawLine(tx, ty, sx, sy, glowPaint)
                }
            }
        }
    }

    // =========================================================================
    // 5. BIOLUMINESCENT NATURE PIPELINES (5 distinct archetypes)
    // =========================================================================
    private fun drawNaturePipeline(canvas: Canvas, dt: Float) {
        when (config.archetype) {
            "sakura_tempest" -> {
                // Fluttering Sakura Blossom Petals
                val pCount = activeParticleCount
                for (i in 0 until pCount) {
                    pY[i] += (45f * config.speed + (i % 4) * 15f) * dt
                    pX[i] += sin(simulationTime * 2f + i) * 30f * dt
                    if (pY[i] > height) {
                        pY[i] = -20f
                        pX[i] = (i * 29f) % width
                    }

                    fillPaint.color = if (i % 2 == 0) config.palette.primary else config.palette.secondary
                    fillPaint.alpha = 220
                    canvas.save()
                    canvas.translate(pX[i], pY[i])
                    canvas.rotate(pAngle[i] + simulationTime * 30f)
                    canvas.drawOval(-8f, -4f, 8f, 4f, fillPaint)
                    canvas.restore()
                }
            }
            "abyssal_jellyfish" -> {
                // Deep Ocean Translucent Pulsing Jellyfish
                val bellR = width * 0.22f
                val pulse = sin(simulationTime * 2.5f) * 15f
                val jx = width * 0.5f + sin(simulationTime * 0.8f) * 40f
                val jy = height * 0.42f + cos(simulationTime * 0.6f) * 30f

                // Bell
                drawPaint.color = config.palette.primary
                drawPaint.strokeWidth = 3f
                drawPaint.alpha = 230
                canvas.drawArc(jx - bellR, jy - bellR * 0.7f, jx + bellR, jy + bellR * 0.7f, 180f, 180f, false, drawPaint)

                // Tentacles
                for (t in -3..3) {
                    tempPath.reset()
                    val tx = jx + t * (bellR * 0.25f)
                    tempPath.moveTo(tx, jy)
                    for (seg in 1..6) {
                        val segY = jy + seg * 55f
                        val waveX = tx + sin(simulationTime * 3f + seg * 0.8f + t) * 18f
                        tempPath.lineTo(waveX, segY)
                    }
                    drawPaint.color = config.palette.secondary
                    drawPaint.strokeWidth = 2f
                    drawPaint.alpha = (180 - t * t * 15).coerceIn(60, 200)
                    canvas.drawPath(tempPath, drawPaint)
                }
            }
            "aurora_curtains" -> {
                // Shimmering Aurora Borealis Solar Curtains
                val rays = 24
                val rayWidth = width / rays.toFloat()
                val curtainH = height * 0.45f
                val key = (config.palette.primary.toLong() shl 32) xor (config.palette.secondary.toLong() and 0xFFFFFFFFL) xor curtainH.toLong()
                if (auroraShader == null || auroraKey != key) {
                    auroraKey = key
                    auroraShader = LinearGradient(
                        0f, 0f, 0f, curtainH,
                        intArrayOf(Color.TRANSPARENT, config.palette.primary, config.palette.secondary, Color.TRANSPARENT),
                        floatArrayOf(0f, 0.3f, 0.7f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
                fillPaint.shader = auroraShader
                for (layer in 0..1) {
                    for (r in 0 until rays) {
                        val x = r * rayWidth
                        val wave = sin(x * 0.005f + simulationTime * (1.5f - layer * 0.6f) + layer * 2f) * 80f
                        val topY = height * (0.08f + layer * 0.2f) + wave
                        val stretch = 0.8f + 0.35f * sin(x * 0.011f + simulationTime * 0.9f + layer)
                        fillPaint.alpha = if (layer == 0) 140 else 90
                        canvas.save()
                        canvas.translate(x, topY)
                        canvas.scale(1f, stretch)
                        canvas.drawRect(0f, 0f, rayWidth + 1f, curtainH, fillPaint)
                        canvas.restore()
                    }
                }
                fillPaint.shader = null
            }
            "rainforest_rain" -> {
                // Slanted rain falling through a dark leaf canopy onto a rippling pool
                val canopyH = height * 0.22f
                for (i in 0 until 26) {
                    val lx = (i * 97f) % width
                    val ly = (i * 53f) % canopyH
                    val sway = sin(simulationTime * 0.8f + i) * 6f
                    canvas.save()
                    canvas.translate(lx + sway, ly)
                    canvas.rotate((i * 37f) % 180f - 90f)
                    fillPaint.color = if (i % 3 == 0) config.palette.secondary else config.palette.tertiary
                    fillPaint.alpha = 150
                    canvas.drawOval(-width * 0.09f, -width * 0.025f, width * 0.09f, width * 0.025f, fillPaint)
                    canvas.restore()
                }

                val slant = 0.18f
                drawPaint.color = config.palette.primary
                drawPaint.strokeWidth = 2f
                val pCount = activeParticleCount
                val poolY = height * 0.86f
                for (i in 0 until pCount) {
                    pY[i] += (900f * config.speed + (i % 5) * 60f) * dt
                    pX[i] += 900f * slant * dt
                    if (pY[i] > poolY) {
                        pY[i] = canopyH * 0.5f + (i * 13f) % canopyH
                        pX[i] = (i * 71f + simulationTime * 53f) % width
                    }
                    if (pX[i] > width) pX[i] -= width
                    drawPaint.alpha = (pAlpha[i] * 180).toInt().coerceIn(40, 200)
                    val len = 40f
                    canvas.drawLine(pX[i] - len * slant, pY[i] - len, pX[i], pY[i], drawPaint)
                }

                // pool surface + ripple rings
                fillPaint.color = config.palette.tertiary
                fillPaint.alpha = 90
                canvas.drawRect(0f, poolY, width.toFloat(), height.toFloat(), fillPaint)
                ripplePaint.color = config.palette.accent
                ripplePaint.strokeWidth = 1.5f
                for (k in 0 until 12) {
                    val life = (simulationTime * 0.9f + k * 0.173f) % 1f
                    val rx = (k * 211f) % width
                    val ry = poolY + 20f + (k * 37f) % (height - poolY - 30f).coerceAtLeast(1f)
                    ripplePaint.alpha = ((1f - life) * 160).toInt()
                    canvas.drawOval(rx - life * 70f, ry - life * 18f, rx + life * 70f, ry + life * 18f, ripplePaint)
                }
            }
            else -> {
                // enchanted_fireflies (Swarming organic bioluminescent fireflies)
                val pCount = activeParticleCount
                for (i in 0 until pCount) {
                    pAngle[i] += (dt * (0.8f + i % 3 * 0.4f))
                    pX[i] += (cos(pAngle[i]) * 25f * config.speed) * dt
                    pY[i] -= (35f * config.speed + (i % 4) * 10f) * dt

                    if (pY[i] < -20f) pY[i] = height + 20f
                    if (pX[i] < -20f) pX[i] = width + 20f
                    if (pX[i] > width + 20f) pX[i] = -20f

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
        }
    }

    // =========================================================================
    // 6. MINIMAL & ZEN PIPELINES (4 distinct archetypes)
    // =========================================================================
    private fun drawMinimalPipeline(canvas: Canvas, dt: Float) {
        val cx = width * 0.5f
        val cy = height * 0.5f

        when (config.archetype) {
            "zen_ripples" -> {
                // Minimalist Water Drops with Harmonic Rings
                for (i in 1..6) {
                    val r = (i * 85f + simulationTime * 30f) % (width * 0.7f)
                    drawPaint.color = config.palette.primary
                    drawPaint.strokeWidth = 2f
                    drawPaint.alpha = ((1f - r / (width * 0.7f)) * 170).toInt().coerceIn(10, 200)
                    canvas.drawCircle(cx, cy, r, drawPaint)
                }
                fillPaint.color = config.palette.accent
                canvas.drawCircle(cx, cy, 6f, fillPaint)
            }
            "prismatic_glass" -> {
                // Triangular Glass Prism Splitting Light into Spectrum
                val side = width * 0.35f
                val h = side * 0.866f
                tempPath.reset()
                tempPath.moveTo(cx, cy - h * 0.6f)
                tempPath.lineTo(cx - side * 0.5f, cy + h * 0.4f)
                tempPath.lineTo(cx + side * 0.5f, cy + h * 0.4f)
                tempPath.close()

                drawPaint.color = config.palette.primary
                drawPaint.strokeWidth = 3f
                drawPaint.alpha = 240
                canvas.drawPath(tempPath, drawPaint)

                // Spectral rainbow fan exiting prism
                for (s in 0..4) {
                    val color = when (s) {
                        0 -> Color.rgb(255, 60, 60)
                        1 -> Color.rgb(255, 180, 40)
                        2 -> Color.rgb(60, 255, 100)
                        3 -> Color.rgb(60, 200, 255)
                        else -> Color.rgb(180, 80, 255)
                    }
                    drawPaint.color = color
                    drawPaint.strokeWidth = 4f
                    val angle = 0.2f + s * 0.08f + sin(simulationTime * 1.2f) * 0.05f
                    canvas.drawLine(cx + side * 0.3f, cy, width.toFloat(), cy + sin(angle) * (height * 0.4f), drawPaint)
                }
            }
            "monolith_balance" -> {
                // Floating Balanced Monoliths
                val floatY = sin(simulationTime * 1.5f) * 15f
                fillPaint.color = config.palette.primary
                fillPaint.alpha = 220
                canvas.drawRoundRect(cx - 50f, cy - 140f + floatY, cx + 50f, cy + 140f + floatY, 14f, 14f, fillPaint)

                fillPaint.color = config.palette.secondary
                fillPaint.alpha = 180
                canvas.drawRoundRect(cx - 85f, cy + 170f, cx + 85f, cy + 200f, 8f, 8f, fillPaint)
            }
            else -> {
                // breathing_mesh (Calm multi-point radial mesh gradients)
                val breathe = 0.85f + 0.15f * sin(simulationTime * 1.2f)
                val gradRadius = (width * 0.55f) * breathe

                drawRadial(
                    canvas, cx, cy, gradRadius,
                    unitRadial(
                        intArrayOf(config.palette.primary, config.palette.secondary, Color.TRANSPARENT),
                        floatArrayOf(0f, 0.6f, 1f)
                    ),
                    110
                )
            }
        }
    }

    // =========================================================================
    // 7. BOLD CHROMATICS & OP-ART PIPELINES (4 distinct archetypes)
    // =========================================================================
    private fun drawChromaticPipeline(canvas: Canvas, dt: Float) {
        val cx = width * 0.5f
        val cy = height * 0.5f

        when (config.archetype) {
            "kinetic_op_art" -> {
                // Moiré Spiral Interference Rings
                val rings = 22
                for (r in 1..rings) {
                    val radius = r * 22f
                    drawPaint.color = if (r % 2 == 0) config.palette.primary else Color.BLACK
                    drawPaint.strokeWidth = 7f
                    drawPaint.alpha = 240
                    canvas.drawCircle(cx, cy, radius, drawPaint)
                }
            }
            "chromatic_glitch" -> {
                // RGB Channel Phase Splits & Slices
                val slices = 16
                val sliceH = height / slices.toFloat()
                for (s in 0 until slices) {
                    val y = s * sliceH
                    val shift = sin(simulationTime * 4f + s * 1.2f) * 28f
                    drawPaint.color = config.palette.primary
                    drawPaint.strokeWidth = 3f
                    canvas.drawLine(0f, y, width.toFloat() + shift, y, drawPaint)

                    drawPaint.color = config.palette.secondary
                    canvas.drawLine(0f, y + 4f, width.toFloat() - shift, y + 4f, drawPaint)
                }
            }
            "spectral_helix" -> {
                // Rotating 3D Double Helix
                val nodes = 28
                for (n in 0 until nodes) {
                    val t = n / nodes.toFloat()
                    val y = height * 0.15f + t * (height * 0.7f)
                    val angle = t * 4 * Math.PI.toFloat() + simulationTime * 2f
                    val x1 = cx + cos(angle) * (width * 0.28f)
                    val x2 = cx - cos(angle) * (width * 0.28f)

                    drawPaint.color = config.palette.secondary
                    drawPaint.strokeWidth = 2f
                    drawPaint.alpha = 160
                    canvas.drawLine(x1, y, x2, y, drawPaint)

                    fillPaint.color = config.palette.primary
                    canvas.drawCircle(x1, y, 6f, fillPaint)
                    fillPaint.color = config.palette.accent
                    canvas.drawCircle(x2, y, 6f, fillPaint)
                }
            }
            else -> {
                // raymarch_waves (Wavy Synthwave Terrain Lines)
                val lines = 36
                val spacing = height / lines.toFloat()
                drawPaint.strokeWidth = 3f

                for (i in 0..lines) {
                    val y = i * spacing
                    val wave = sin(y * 0.008f + simulationTime * 2.2f) * 40f
                    drawPaint.color = config.palette.primary
                    drawPaint.alpha = 180
                    canvas.drawLine(0f, y + wave, width.toFloat(), y + wave, drawPaint)

                    drawPaint.color = config.palette.secondary
                    drawPaint.alpha = 160
                    canvas.drawLine(0f, y - wave * 0.8f, width.toFloat(), y - wave * 0.8f, drawPaint)
                }
            }
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
                val maxR = if (r.maxRadius > 1f) r.maxRadius else 100f
                r.radius += (280f * config.speed + 150f) * dt
                r.alpha = (1f - (r.radius / maxR)).coerceIn(0f, 1f)

                if (r.radius >= maxR || r.alpha <= 0.01f) {
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
