package com.aura.wallpapers.engine.interactive

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * MotionLoopsPipeline renders the 53 distinct live motion loop wallpapers.
 * Every animation is mathematically continuous and seamless, designed with
 * generous negative space for mobile home screens, zero-allocation render loops,
 * and high battery efficiency.
 */
class MotionLoopsPipeline {

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

    private val bgPaint = Paint().apply { isDither = true }
    private val tempPath = Path()
    private val tempRect = RectF()

    fun render(
        canvas: Canvas,
        archetype: String,
        simTime: Float,
        dt: Float,
        width: Int,
        height: Int,
        touchX: Float,
        touchY: Float,
        isTouching: Boolean,
        palette: ColorPalette
    ) {
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w * 0.5f
        val cy = h * 0.5f

        // Touch parallax reaction
        val tx = if (isTouching && touchX >= 0) (touchX - cx) * 0.05f else 0f
        val ty = if (isTouching && touchY >= 0) (touchY - cy) * 0.05f else 0f

        canvas.save()
        canvas.translate(tx, ty)

        when (archetype) {
            // Futuristic and graphic motion designs (#01-#31)
            "ml_01_aurora_drift" -> drawAuroraDrift(canvas, simTime, w, h, cx, cy, palette)
            "ml_02_liquid_chrome_bloom" -> drawLiquidChromeBloom(canvas, simTime, w, h, cx, cy, palette)
            "ml_03_orbital_eclipse" -> drawOrbitalEclipse(canvas, simTime, w, h, cx, cy, palette)
            "ml_04_prism_cascade" -> drawPrismCascade(canvas, simTime, w, h, cx, cy, palette)
            "ml_05_biolume_garden" -> drawBiolumeGarden(canvas, simTime, w, h, cx, cy, palette)
            "ml_06_dune_pulse" -> drawDunePulse(canvas, simTime, w, h, cx, cy, palette)
            "ml_07_neon_rain" -> drawNeonRain(canvas, simTime, w, h, cx, cy, palette)
            "ml_08_celestial_dust" -> drawCelestialDust(canvas, simTime, w, h, cx, cy, palette)
            "ml_09_ribbon_infinity" -> drawRibbonInfinity(canvas, simTime, w, h, cx, cy, palette)
            "ml_10_solar_halo" -> drawSolarHalo(canvas, simTime, w, h, cx, cy, palette)
            "ml_11_pixel_flora" -> drawPixelFlora(canvas, simTime, w, h, cx, cy, palette)
            "ml_12_magnetic_field" -> drawMagneticField(canvas, simTime, w, h, cx, cy, palette)
            "ml_13_y2k_halo_orbit" -> drawY2kHaloOrbit(canvas, simTime, w, h, cx, cy, palette)
            "ml_14_glass_aurora" -> drawGlassAurora(canvas, simTime, w, h, cx, cy, palette)
            "ml_15_dewdrop_biophilia" -> drawDewdropBiophilia(canvas, simTime, w, h, cx, cy, palette)
            "ml_16_velvet_jelly_forms" -> drawVelvetJellyForms(canvas, simTime, w, h, cx, cy, palette)
            "ml_17_dream_gradient_grid" -> drawDreamGradientGrid(canvas, simTime, w, h, cx, cy, palette)
            "ml_18_glass_portal" -> drawGlassPortal(canvas, simTime, w, h, cx, cy, palette)
            "ml_19_neon_vector_pulse" -> drawNeonVectorPulse(canvas, simTime, w, h, cx, cy, palette)
            "ml_20_pixel_constellation" -> drawPixelConstellation(canvas, simTime, w, h, cx, cy, palette)
            "ml_21_parallax_slabs" -> drawParallaxSlabs(canvas, simTime, w, h, cx, cy, palette)
            "ml_22_outline_reveal" -> drawOutlineReveal(canvas, simTime, w, h, cx, cy, palette)
            "ml_23_pixel_to_line" -> drawPixelToLine(canvas, simTime, w, h, cx, cy, palette)
            "ml_24_future_monolith" -> drawFutureMonolith(canvas, simTime, w, h, cx, cy, palette)
            "ml_25_vector_sweep" -> drawVectorSweep(canvas, simTime, w, h, cx, cy, palette)
            "ml_26_shape_morph" -> drawShapeMorph(canvas, simTime, w, h, cx, cy, palette)
            "ml_27_gradient_wipe" -> drawGradientWipe(canvas, simTime, w, h, cx, cy, palette)
            "ml_28_orbiting_dashes" -> drawOrbitingDashes(canvas, simTime, w, h, cx, cy, palette)
            "ml_29_modular_assembly" -> drawModularAssembly(canvas, simTime, w, h, cx, cy, palette)
            "ml_30_contour_flow" -> drawContourFlow(canvas, simTime, w, h, cx, cy, palette)
            "ml_31_radial_burst" -> drawRadialBurst(canvas, simTime, w, h, cx, cy, palette)

            // Minimal Heritage Designs (#32-#37)
            "ml_32_mughal_arch" -> drawMughalArch(canvas, simTime, w, h, cx, cy, palette)
            "ml_33_ukiyo_e_wave" -> drawUkiyoEWave(canvas, simTime, w, h, cx, cy, palette)
            "ml_34_greek_marble" -> drawGreekMarble(canvas, simTime, w, h, cx, cy, palette)
            "ml_35_egyptian_sun_lotus" -> drawEgyptianSunLotus(canvas, simTime, w, h, cx, cy, palette)
            "ml_36_persian_star_tile" -> drawPersianStarTile(canvas, simTime, w, h, cx, cy, palette)
            "ml_37_renaissance_astrolabe" -> drawRenaissanceAstrolabe(canvas, simTime, w, h, cx, cy, palette)

            // Focused Subject Stories (#38-#45)
            "ml_38_star_catcher" -> drawStarCatcher(canvas, simTime, w, h, cx, cy, palette)
            "ml_39_the_key" -> drawTheKey(canvas, simTime, w, h, cx, cy, palette)
            "ml_40_koi_circuit" -> drawKoiCircuit(canvas, simTime, w, h, cx, cy, palette)
            "ml_41_bloom_dial" -> drawBloomDial(canvas, simTime, w, h, cx, cy, palette)
            "ml_42_cassette_target" -> drawCassetteTarget(canvas, simTime, w, h, cx, cy, palette)
            "ml_43_moon_diver" -> drawMoonDiver(canvas, simTime, w, h, cx, cy, palette)
            "ml_44_north" -> drawNorth(canvas, simTime, w, h, cx, cy, palette)
            "ml_45_portal_window" -> drawPortalWindow(canvas, simTime, w, h, cx, cy, palette)

            // Simple Continuous Loops (#46-#53)
            "ml_46_tea_steam" -> drawTeaSteam(canvas, simTime, w, h, cx, cy, palette)
            "ml_47_pendulum" -> drawPendulum(canvas, simTime, w, h, cx, cy, palette)
            "ml_48_floating_petal" -> drawFloatingPetal(canvas, simTime, w, h, cx, cy, palette)
            "ml_49_jellyfish" -> drawJellyfish(canvas, simTime, w, h, cx, cy, palette)
            "ml_50_rain_on_leaf" -> drawRainOnLeaf(canvas, simTime, w, h, cx, cy, palette)
            "ml_51_moon_cloud" -> drawMoonCloud(canvas, simTime, w, h, cx, cy, palette)
            "ml_52_firefly" -> drawFirefly(canvas, simTime, w, h, cx, cy, palette)
            "ml_53_fountain_ripple" -> drawFountainRipple(canvas, simTime, w, h, cx, cy, palette)

            else -> drawAuroraDrift(canvas, simTime, w, h, cx, cy, palette)
        }

        canvas.restore()
    }

    // -------------------------------------------------------------------------
    // 01. Aurora Drift: Waving ribbons across twilight lake
    // -------------------------------------------------------------------------
    private fun drawAuroraDrift(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val horizonY = h * 0.65f
        drawPaint.style = Paint.Style.STROKE

        for (band in 0 until 4) {
            val bandOffset = band * 0.4f
            val alpha = (160 - band * 30).coerceIn(40, 200)
            drawPaint.color = (alpha shl 24) or (if (band % 2 == 0) pal.primary else pal.secondary and 0x00FFFFFF)
            drawPaint.strokeWidth = 36f - band * 6f

            tempPath.reset()
            val steps = 30
            for (i in 0..steps) {
                val x = (i / steps.toFloat()) * w
                val phase = (i * 0.2f) + t * 0.8f + bandOffset
                val ySky = h * 0.25f + sin(phase) * 70f + cos(phase * 0.7f) * 40f
                if (i == 0) tempPath.moveTo(x, ySky) else tempPath.lineTo(x, ySky)
            }
            canvas.drawPath(tempPath, drawPaint)

            // Reflected ribbon on lake
            tempPath.reset()
            for (i in 0..steps) {
                val x = (i / steps.toFloat()) * w
                val phase = (i * 0.2f) + t * 0.8f + bandOffset
                val ySky = h * 0.25f + sin(phase) * 70f + cos(phase * 0.7f) * 40f
                val yReflect = horizonY + (horizonY - ySky) * 0.35f + sin(phase * 1.5f + t) * 6f
                if (i == 0) tempPath.moveTo(x, yReflect) else tempPath.lineTo(x, yReflect)
            }
            drawPaint.strokeWidth = 18f
            drawPaint.color = ((alpha / 3) shl 24) or (pal.primary and 0x00FFFFFF)
            canvas.drawPath(tempPath, drawPaint)
        }

        // Mountain silhouette separating sky & lake
        fillPaint.color = (0xFF shl 24) or (pal.background and 0x00FFFFFF)
        tempPath.reset()
        tempPath.moveTo(0f, horizonY)
        tempPath.lineTo(w * 0.25f, horizonY - 90f)
        tempPath.lineTo(w * 0.45f, horizonY - 40f)
        tempPath.lineTo(w * 0.7f, horizonY - 120f)
        tempPath.lineTo(w, horizonY - 50f)
        tempPath.lineTo(w, h)
        tempPath.lineTo(0f, h)
        tempPath.close()
        canvas.drawPath(tempPath, fillPaint)
    }

    // -------------------------------------------------------------------------
    // 02. Liquid Chrome Bloom: Unfolding into metallic flower and closing
    // -------------------------------------------------------------------------
    private fun drawLiquidChromeBloom(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val cycle = (sin(t * 1.2f) + 1f) * 0.5f // 0 to 1 smooth loop
        val petals = 6
        val baseRadius = 80f + cycle * 120f

        tempPath.reset()
        val numPoints = 120
        for (i in 0 until numPoints) {
            val angle = (i / numPoints.toFloat()) * 2f * PI.toFloat()
            val petalMod = sin(angle * petals) * cycle * 60f
            val r = baseRadius + petalMod
            val px = cx + cos(angle) * r
            val py = cy + sin(angle) * r
            if (i == 0) tempPath.moveTo(px, py) else tempPath.lineTo(px, py)
        }
        tempPath.close()

        fillPaint.color = 0xFFCCCCCC.toInt()
        canvas.drawPath(tempPath, fillPaint)

        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 4f
        drawPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawPath(tempPath, drawPaint)

        // Core metallic highlight ring
        drawPaint.strokeWidth = 2f
        drawPaint.color = 0xAAFFFFFF.toInt()
        canvas.drawCircle(cx, cy, baseRadius * 0.4f, drawPaint)
    }

    // -------------------------------------------------------------------------
    // 03. Orbital Eclipse: Stationary dark planet with slow 360° ring orbit
    // -------------------------------------------------------------------------
    private fun drawOrbitalEclipse(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val planetRadius = 110f
        val angle = (t * 0.5f) % (2f * PI.toFloat())

        // Back half of orbital ring
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 3f
        drawPaint.color = 0x66FFFFFF.toInt()
        tempRect.set(cx - 240f, cy - 70f, cx + 240f, cy + 70f)
        canvas.save()
        canvas.rotate(25f, cx, cy)
        canvas.drawArc(tempRect, 180f, 180f, false, drawPaint)

        // Dark planet body
        fillPaint.color = 0xFF080A10.toInt()
        canvas.restore()
        canvas.drawCircle(cx, cy, planetRadius, fillPaint)

        // Rim glow crescent
        drawPaint.strokeWidth = 5f
        drawPaint.color = (0xEE shl 24) or (pal.primary and 0x00FFFFFF)
        tempRect.set(cx - planetRadius, cy - planetRadius, cx + planetRadius, cy + planetRadius)
        canvas.drawArc(tempRect, 200f, 100f, false, drawPaint)

        // Front half of orbital ring
        canvas.save()
        canvas.rotate(25f, cx, cy)
        tempRect.set(cx - 240f, cy - 70f, cx + 240f, cy + 70f)
        drawPaint.strokeWidth = 5f
        drawPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawArc(tempRect, 0f, 180f, false, drawPaint)

        // Orbiting satellite dot
        val satX = cx + cos(angle) * 240f
        val satY = cy + sin(angle) * 70f
        fillPaint.color = pal.accent
        canvas.drawCircle(satX, satY, 7f, fillPaint)
        canvas.restore()
    }

    // -------------------------------------------------------------------------
    // 04. Prism Cascade: Moving light beam dispersing spectral colors
    // -------------------------------------------------------------------------
    private fun drawPrismCascade(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        // Triangular prism outline
        val pSize = 130f
        tempPath.reset()
        tempPath.moveTo(cx, cy - pSize)
        tempPath.lineTo(cx - pSize * 0.866f, cy + pSize * 0.5f)
        tempPath.lineTo(cx + pSize * 0.866f, cy + pSize * 0.5f)
        tempPath.close()

        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 4f
        drawPaint.color = 0x88FFFFFF.toInt()
        canvas.drawPath(tempPath, drawPaint)

        // Sweeping light beam
        val beamOffset = sin(t * 0.8f) * 60f
        val inX = cx - 260f
        val inY = cy - 40f + beamOffset

        drawPaint.strokeWidth = 3f
        drawPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawLine(inX, inY, cx - pSize * 0.43f, cy, drawPaint)

        // Refracted rainbow fan exiting prism
        val colors = intArrayOf(0xFFFF3333.toInt(), 0xFFFFAA00.toInt(), 0xFFFFFF33.toInt(), 0xFF33FF33.toInt(), 0xFF33AAFF.toInt(), 0xFFAA33FF.toInt())
        for (i in colors.indices) {
            val spreadY = (i - 2.5f) * 22f
            drawPaint.color = colors[i]
            drawPaint.strokeWidth = 4f
            canvas.drawLine(cx, cy, cx + 240f, cy + spreadY + beamOffset * 0.5f, drawPaint)
        }
    }

    // -------------------------------------------------------------------------
    // 05. Biolume Garden: Delicate night stems whose glowing flowers gently breathe
    // -------------------------------------------------------------------------
    private fun drawBiolumeGarden(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val breathe = (sin(t * 1.5f) + 1f) * 0.5f
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 3f
        drawPaint.color = (0x99 shl 24) or (pal.secondary and 0x00FFFFFF)

        // 3 gracefully curved stems
        val stems = listOf(-90f, 0f, 90f)
        for ((idx, ox) in stems.withIndex()) {
            val stemX = cx + ox
            tempPath.reset()
            tempPath.moveTo(stemX, h)
            val sway = sin(t + idx) * 15f
            tempPath.cubicTo(stemX, h * 0.75f, stemX + sway, h * 0.55f, stemX + sway * 1.5f, cy + idx * 30f)
            canvas.drawPath(tempPath, drawPaint)

            // Flower blossom at tip
            val fx = stemX + sway * 1.5f
            val fy = cy + idx * 30f
            val radius = 18f + breathe * 8f

            fillPaint.color = (0x44 shl 24) or (pal.primary and 0x00FFFFFF)
            canvas.drawCircle(fx, fy, radius * 2.2f, fillPaint)

            fillPaint.color = (0xCC shl 24) or (if (idx % 2 == 0) pal.primary else pal.accent and 0x00FFFFFF)
            canvas.drawCircle(fx, fy, radius, fillPaint)

            fillPaint.color = 0xFFFFFFFF.toInt()
            canvas.drawCircle(fx, fy, radius * 0.35f, fillPaint)
        }
    }

    // -------------------------------------------------------------------------
    // 06. Dune Pulse: Desert ridge under twilight with sweeping light crest
    // -------------------------------------------------------------------------
    private fun drawDunePulse(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val crestPhase = (t * 0.4f) % 1f // 0 to 1
        val ridgeY = cy + 40f

        tempPath.reset()
        tempPath.moveTo(0f, ridgeY + 80f)
        tempPath.cubicTo(w * 0.35f, ridgeY - 90f, w * 0.65f, ridgeY + 90f, w, ridgeY - 30f)
        tempPath.lineTo(w, h)
        tempPath.lineTo(0f, h)
        tempPath.close()

        fillPaint.color = (0xFF shl 24) or (pal.background and 0x00FFFFFF)
        canvas.drawPath(tempPath, fillPaint)

        // Sweeping luminous crest line
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 5f
        drawPaint.color = (0x88 shl 24) or (pal.primary and 0x00FFFFFF)
        canvas.drawPath(tempPath, drawPaint)

        // Traveling light highlight along ridge
        val sweepX = crestPhase * w
        val sweepY = ridgeY + sin(crestPhase * 3.14f) * -40f
        fillPaint.color = 0xAAFFFFFF.toInt()
        canvas.drawCircle(sweepX, sweepY, 30f, fillPaint)
        fillPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(sweepX, sweepY, 10f, fillPaint)
    }

    // -------------------------------------------------------------------------
    // 07. Neon Rain: Thin falling neon rain streaks with modulo reset
    // -------------------------------------------------------------------------
    private fun drawNeonRain(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val count = 28
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 2.5f

        for (i in 0 until count) {
            val colX = (i / count.toFloat()) * w + 12f
            val speed = 380f + (i % 7) * 70f
            val dropY = ((t * speed + i * 211f) % (h + 160f)) - 80f
            val len = 40f + (i % 5) * 15f

            val color = if (i % 2 == 0) pal.primary else pal.secondary
            drawPaint.color = (0xCC shl 24) or (color and 0x00FFFFFF)
            canvas.drawLine(colX, dropY, colX, dropY + len, drawPaint)

            // Head drop glow
            fillPaint.color = 0xFFFFFFFF.toInt()
            canvas.drawCircle(colX, dropY + len, 2.5f, fillPaint)
        }
    }

    // -------------------------------------------------------------------------
    // 08. Celestial Dust: Central dark void with orbiting pulsing stardust
    // -------------------------------------------------------------------------
    private fun drawCelestialDust(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val voidR = 90f
        val pulse = (sin(t * 2f) + 1f) * 0.5f

        // Star dust particles along elliptical orbits
        val dustCount = 42
        for (i in 0 until dustCount) {
            val angle = (i / dustCount.toFloat()) * 2f * PI.toFloat() + t * 0.4f
            val dist = voidR + 30f + (i % 5) * 28f + pulse * 15f
            val px = cx + cos(angle) * dist * 1.4f
            val py = cy + sin(angle) * dist * 0.7f

            val alpha = (120 + (i % 4) * 40).coerceIn(50, 255)
            fillPaint.color = (alpha shl 24) or (if (i % 3 == 0) pal.accent else pal.primary and 0x00FFFFFF)
            canvas.drawCircle(px, py, 2.5f + (i % 3) * 1.5f, fillPaint)
        }

        // Central dark void disc
        fillPaint.color = 0xFF05050A.toInt()
        canvas.drawCircle(cx, cy, voidR, fillPaint)

        // Shimmering void edge ring
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 3f + pulse * 2f
        drawPaint.color = (0xCC shl 24) or (pal.tertiary and 0x00FFFFFF)
        canvas.drawCircle(cx, cy, voidR, drawPaint)
    }

    // -------------------------------------------------------------------------
    // 09. Ribbon Infinity: Continuous highlight on figure-eight path
    // -------------------------------------------------------------------------
    private fun drawRibbonInfinity(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val scale = 190f
        val steps = 80
        tempPath.reset()

        for (i in 0..steps) {
            val theta = (i / steps.toFloat()) * 2f * PI.toFloat()
            val denom = 1f + sin(theta) * sin(theta)
            val px = cx + (scale * cos(theta)) / denom
            val py = cy + (scale * sin(theta) * cos(theta)) / denom
            if (i == 0) tempPath.moveTo(px, py) else tempPath.lineTo(px, py)
        }

        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 8f
        drawPaint.color = (0x55 shl 24) or (pal.primary and 0x00FFFFFF)
        canvas.drawPath(tempPath, drawPaint)

        drawPaint.strokeWidth = 2f
        drawPaint.color = 0xAAFFFFFF.toInt()
        canvas.drawPath(tempPath, drawPaint)

        // Travelling highlight packet along infinity loop
        val travelTheta = (t * 1.2f) % (2f * PI.toFloat())
        val denom = 1f + sin(travelTheta) * sin(travelTheta)
        val hx = cx + (scale * cos(travelTheta)) / denom
        val hy = cy + (scale * sin(travelTheta) * cos(travelTheta)) / denom

        fillPaint.color = 0x66FFFFFF.toInt()
        canvas.drawCircle(hx, hy, 26f, fillPaint)
        fillPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(hx, hy, 8f, fillPaint)
    }

    // -------------------------------------------------------------------------
    // 10. Solar Halo: Obsidian disc with breathing solar corona flares
    // -------------------------------------------------------------------------
    private fun drawSolarHalo(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val discR = 95f
        val rays = 36
        drawPaint.style = Paint.Style.STROKE

        for (i in 0 until rays) {
            val angle = (i / rays.toFloat()) * 2f * PI.toFloat()
            val breathe = sin(t * 2f + i * 0.4f) * 25f
            val len = discR + 35f + breathe

            val x1 = cx + cos(angle) * discR
            val y1 = cy + sin(angle) * discR
            val x2 = cx + cos(angle) * len
            val y2 = cy + sin(angle) * len

            drawPaint.strokeWidth = 3f
            drawPaint.color = (0x88 shl 24) or (if (i % 2 == 0) pal.primary else pal.accent and 0x00FFFFFF)
            canvas.drawLine(x1, y1, x2, y2, drawPaint)
        }

        // Obsidian center disc
        fillPaint.color = 0xFF020204.toInt()
        canvas.drawCircle(cx, cy, discR, fillPaint)

        drawPaint.strokeWidth = 4f
        drawPaint.color = 0xFFFFAA00.toInt()
        canvas.drawCircle(cx, cy, discR, drawPaint)
    }

    // -------------------------------------------------------------------------
    // 11. Pixel Flora: 16-bit stepped blooming flower
    // -------------------------------------------------------------------------
    private fun drawPixelFlora(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val cycle = ((sin(t * 1.5f) + 1f) * 0.5f * 4f).toInt() // 0 to 4 steps
        val pxSize = 16f
        fillPaint.color = pal.primary

        // Center pixel core
        canvas.drawRect(cx - pxSize, cy - pxSize, cx + pxSize, cy + pxSize, fillPaint)

        // Stepwise petals
        val maxDist = cycle + 1
        for (step in 1..maxDist) {
            val dist = step * pxSize * 2f
            fillPaint.color = if (step % 2 == 0) pal.secondary else pal.accent

            // 4 cardinal petals
            canvas.drawRect(cx - pxSize, cy - dist - pxSize, cx + pxSize, cy - dist + pxSize, fillPaint)
            canvas.drawRect(cx - pxSize, cy + dist - pxSize, cx + pxSize, cy + dist + pxSize, fillPaint)
            canvas.drawRect(cx - dist - pxSize, cy - pxSize, cx - dist + pxSize, cy + pxSize, fillPaint)
            canvas.drawRect(cx + dist - pxSize, cy - pxSize, cx + dist + pxSize, cy + pxSize, fillPaint)
        }
    }

    // -------------------------------------------------------------------------
    // 12. Magnetic Field: Swaying bipolar flux lines
    // -------------------------------------------------------------------------
    private fun drawMagneticField(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val sway = sin(t * 1.1f) * 35f
        val lines = 8
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 3f

        for (i in 1..lines) {
            val spread = i * 28f
            drawPaint.color = (0x99 shl 24) or (pal.primary and 0x00FFFFFF)

            // Left flux loop
            tempPath.reset()
            tempPath.moveTo(cx, cy - 180f)
            tempPath.cubicTo(cx - spread - sway, cy - 90f, cx - spread - sway, cy + 90f, cx, cy + 180f)
            canvas.drawPath(tempPath, drawPaint)

            // Right flux loop
            tempPath.reset()
            tempPath.moveTo(cx, cy - 180f)
            tempPath.cubicTo(cx + spread + sway, cy - 90f, cx + spread + sway, cy + 90f, cx, cy + 180f)
            canvas.drawPath(tempPath, drawPaint)
        }

        // North and South magnetic core indicators
        fillPaint.color = 0xFFFF4444.toInt()
        canvas.drawCircle(cx, cy - 180f, 9f, fillPaint)
        fillPaint.color = 0xFF4488FF.toInt()
        canvas.drawCircle(cx, cy + 180f, 9f, fillPaint)
    }

    // -------------------------------------------------------------------------
    // 13. Y2K Halo Orbit: Chrome torus around iridescent sphere
    // -------------------------------------------------------------------------
    private fun drawY2kHaloOrbit(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val sphereR = 90f
        val ringAngle = t * 1.0f

        // Back half of 3D torus ring
        canvas.save()
        canvas.rotate(sin(ringAngle) * 35f, cx, cy)
        tempRect.set(cx - 200f, cy - 60f, cx + 200f, cy + 60f)
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 14f
        drawPaint.color = 0x66DDDDDD.toInt()
        canvas.drawArc(tempRect, 180f, 180f, false, drawPaint)
        canvas.restore()

        // Central iridescent sphere
        fillPaint.color = pal.secondary
        canvas.drawCircle(cx, cy, sphereR, fillPaint)
        fillPaint.color = 0x88FFFFFF.toInt()
        canvas.drawCircle(cx - 25f, cy - 25f, sphereR * 0.4f, fillPaint)

        // Front half of 3D torus ring
        canvas.save()
        canvas.rotate(sin(ringAngle) * 35f, cx, cy)
        drawPaint.strokeWidth = 14f
        drawPaint.color = 0xFFEEEEEE.toInt()
        canvas.drawArc(tempRect, 0f, 180f, false, drawPaint)
        canvas.restore()
    }

    // -------------------------------------------------------------------------
    // 14. Glass Aurora: Refracting glass facet planes
    // -------------------------------------------------------------------------
    private fun drawGlassAurora(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val phase = t * 0.7f
        val facets = 5

        for (i in 0 until facets) {
            val yTop = h * 0.2f + i * 90f
            val yBottom = yTop + 140f
            val skew = sin(phase + i) * 50f

            tempPath.reset()
            tempPath.moveTo(w * 0.15f + skew, yTop)
            tempPath.lineTo(w * 0.85f - skew, yTop + 30f)
            tempPath.lineTo(w * 0.8f - skew, yBottom)
            tempPath.lineTo(w * 0.2f + skew, yBottom - 20f)
            tempPath.close()

            val alpha = (80 + i * 25).coerceIn(40, 180)
            fillPaint.color = (alpha shl 24) or (if (i % 2 == 0) pal.primary else pal.tertiary and 0x00FFFFFF)
            canvas.drawPath(tempPath, fillPaint)

            drawPaint.style = Paint.Style.STROKE
            drawPaint.strokeWidth = 2.5f
            drawPaint.color = 0xAAFFFFFF.toInt()
            canvas.drawPath(tempPath, drawPaint)
        }
    }

    // -------------------------------------------------------------------------
    // 15. Dewdrop Biophilia: Swelling dewdrop on leaf vein
    // -------------------------------------------------------------------------
    private fun drawDewdropBiophilia(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val swell = (sin(t * 1.8f) + 1f) * 0.5f

        // Leaf vein curve
        tempPath.reset()
        tempPath.moveTo(cx - 160f, cy - 140f)
        tempPath.quadTo(cx, cy - 80f, cx + 160f, cy - 30f)
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 6f
        drawPaint.color = (0xCC shl 24) or (pal.primary and 0x00FFFFFF)
        canvas.drawPath(tempPath, drawPaint)

        // Hanging dewdrop
        val dropRadius = 24f + swell * 10f
        val dropCenterY = cy - 70f + dropRadius

        // Glow
        fillPaint.color = (0x44 shl 24) or (pal.primary and 0x00FFFFFF)
        canvas.drawCircle(cx, dropCenterY, dropRadius * 2.2f, fillPaint)

        // Drop body
        fillPaint.color = (0xDD shl 24) or (pal.accent and 0x00FFFFFF)
        canvas.drawCircle(cx, dropCenterY, dropRadius, fillPaint)

        // Specular reflection
        fillPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(cx - dropRadius * 0.35f, dropCenterY - dropRadius * 0.35f, dropRadius * 0.28f, fillPaint)
    }

    // -------------------------------------------------------------------------
    // 16. Velvet Jelly Forms: Undulating deep-sea organza wave
    // -------------------------------------------------------------------------
    private fun drawVelvetJellyForms(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        for (layer in 0 until 3) {
            val offset = layer * 50f
            val wavePhase = t * 1.1f + layer * 1.2f

            tempPath.reset()
            val points = 30
            for (i in 0..points) {
                val angle = (i / points.toFloat()) * 2f * PI.toFloat()
                val r = 110f + offset + sin(angle * 4f + wavePhase) * 25f
                val px = cx + cos(angle) * r
                val py = cy + sin(angle) * r
                if (i == 0) tempPath.moveTo(px, py) else tempPath.lineTo(px, py)
            }
            tempPath.close()

            fillPaint.color = (0x33 shl 24) or (if (layer == 0) pal.primary else pal.secondary and 0x00FFFFFF)
            canvas.drawPath(tempPath, fillPaint)

            drawPaint.style = Paint.Style.STROKE
            drawPaint.strokeWidth = 3f
            drawPaint.color = (0x99 shl 24) or (pal.primary and 0x00FFFFFF)
            canvas.drawPath(tempPath, drawPaint)
        }
    }

    // -------------------------------------------------------------------------
    // 17. Dream Gradient Grid: Infinite forward-scrolling perspective grid
    // -------------------------------------------------------------------------
    private fun drawDreamGradientGrid(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val horizonY = cy + 40f
        val speed = (t * 120f) % 60f

        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 2f
        drawPaint.color = (0xAA shl 24) or (pal.primary and 0x00FFFFFF)

        // Horizon line
        canvas.drawLine(0f, horizonY, w, horizonY, drawPaint)

        // Perspective rays radiating from center horizon
        val rays = 12
        for (i in 0..rays) {
            val bottomX = (i / rays.toFloat()) * w
            canvas.drawLine(cx, horizonY, bottomX, h, drawPaint)
        }

        // Horizontal scrolling rungs
        val rungs = 14
        for (i in 0 until rungs) {
            val z = ((i * 45f + speed) % (h - horizonY))
            val y = horizonY + z * (z / (h - horizonY))
            canvas.drawLine(0f, y, w, y, drawPaint)
        }
    }

    // -------------------------------------------------------------------------
    // 18. Glass Portal: Light swirling inside glass ring
    // -------------------------------------------------------------------------
    private fun drawGlassPortal(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val ringR = 130f
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 16f
        drawPaint.color = 0x44FFFFFF.toInt()
        canvas.drawCircle(cx, cy, ringR, drawPaint)

        // Swirling light orbiter
        val angle = t * 1.5f
        val lx = cx + cos(angle) * ringR
        val ly = cy + sin(angle) * ringR

        fillPaint.color = (0x66 shl 24) or (pal.primary and 0x00FFFFFF)
        canvas.drawCircle(lx, ly, 32f, fillPaint)

        fillPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(lx, ly, 10f, fillPaint)
    }

    // -------------------------------------------------------------------------
    // 19. Neon Vector Pulse: Sharp neon outline tracing polyhedra
    // -------------------------------------------------------------------------
    private fun drawNeonVectorPulse(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val rot = t * 0.6f
        val r = 120f

        canvas.save()
        canvas.rotate(rot * 57.3f, cx, cy)

        val verts = 6
        tempPath.reset()
        for (i in 0 until verts) {
            val a = (i / verts.toFloat()) * 2f * PI.toFloat()
            val px = cx + cos(a) * r
            val py = cy + sin(a) * r
            if (i == 0) tempPath.moveTo(px, py) else tempPath.lineTo(px, py)
        }
        tempPath.close()

        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 4f
        drawPaint.color = (0xEE shl 24) or (pal.primary and 0x00FFFFFF)
        canvas.drawPath(tempPath, drawPaint)

        // Internal cross diagonals
        for (i in 0 until verts / 2) {
            val a1 = (i / verts.toFloat()) * 2f * PI.toFloat()
            val a2 = a1 + PI.toFloat()
            canvas.drawLine(cx + cos(a1) * r, cy + sin(a1) * r, cx + cos(a2) * r, cy + sin(a2) * r, drawPaint)
        }
        canvas.restore()
    }

    // -------------------------------------------------------------------------
    // 20. Pixel Constellation: Pulsing laser data threads between stars
    // -------------------------------------------------------------------------
    private fun drawPixelConstellation(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val nodes = listOf(
            Pair(-120f, -90f), Pair(0f, -140f), Pair(110f, -80f),
            Pair(-70f, 60f), Pair(90f, 70f), Pair(0f, 130f)
        )

        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 2f
        drawPaint.color = (0x66 shl 24) or (pal.secondary and 0x00FFFFFF)

        for (i in 0 until nodes.size - 1) {
            val (x1, y1) = nodes[i]
            val (x2, y2) = nodes[i + 1]
            canvas.drawLine(cx + x1, cy + y1, cx + x2, cy + y2, drawPaint)
        }

        // Star dots with pulsing glow
        for ((idx, node) in nodes.withIndex()) {
            val pulse = (sin(t * 2f + idx) + 1f) * 0.5f
            val nx = cx + node.first
            val ny = cy + node.second

            fillPaint.color = (0x88 shl 24) or (pal.primary and 0x00FFFFFF)
            canvas.drawCircle(nx, ny, 7f + pulse * 4f, fillPaint)

            fillPaint.color = 0xFFFFFFFF.toInt()
            canvas.drawCircle(nx, ny, 3.5f, fillPaint)
        }
    }

    // -------------------------------------------------------------------------
    // 21. Parallax Slabs: Minimalist floating architectural stone slabs
    // -------------------------------------------------------------------------
    private fun drawParallaxSlabs(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val shift1 = sin(t * 0.8f) * 40f
        val shift2 = -sin(t * 0.8f) * 30f

        // Slab 1 (Back)
        fillPaint.color = (0x55 shl 24) or (pal.secondary and 0x00FFFFFF)
        canvas.drawRoundRect(cx - 160f + shift1, cy - 130f, cx + 80f + shift1, cy + 30f, 16f, 16f, fillPaint)

        // Slab 2 (Front)
        fillPaint.color = (0xAA shl 24) or (pal.primary and 0x00FFFFFF)
        canvas.drawRoundRect(cx - 80f + shift2, cy - 40f, cx + 160f + shift2, cy + 120f, 16f, 16f, fillPaint)

        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 3f
        drawPaint.color = 0xAAFFFFFF.toInt()
        canvas.drawRoundRect(cx - 80f + shift2, cy - 40f, cx + 160f + shift2, cy + 120f, 16f, 16f, drawPaint)
    }

    // -------------------------------------------------------------------------
    // 22. Outline Reveal: Travelling spark along geometric diamond outline
    // -------------------------------------------------------------------------
    private fun drawOutlineReveal(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val size = 120f
        tempPath.reset()
        tempPath.moveTo(cx, cy - size)
        tempPath.lineTo(cx + size, cy)
        tempPath.lineTo(cx, cy + size)
        tempPath.lineTo(cx - size, cy)
        tempPath.close()

        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 4f
        drawPaint.color = (0x55 shl 24) or (pal.primary and 0x00FFFFFF)
        canvas.drawPath(tempPath, drawPaint)

        // Travelling spark
        val progress = (t * 0.8f) % 4f
        val side = progress.toInt()
        val frac = progress - side
        val (sx, sy) = when (side) {
            0 -> Pair(cx + frac * size, cy - size + frac * size)
            1 -> Pair(cx + size - frac * size, cy + frac * size)
            2 -> Pair(cx - frac * size, cy + size - frac * size)
            else -> Pair(cx - size + frac * size, cy - frac * size)
        }

        fillPaint.color = 0x88FFFFFF.toInt()
        canvas.drawCircle(sx, sy, 18f, fillPaint)
        fillPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(sx, sy, 6f, fillPaint)
    }

    // -------------------------------------------------------------------------
    // 23. Pixel-to-Line: Scatter pixels resolving into laser line and dissolving
    // -------------------------------------------------------------------------
    private fun drawPixelToLine(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val cycle = (sin(t * 1.5f) + 1f) * 0.5f // 0 to 1
        val numPixels = 24
        fillPaint.color = pal.primary

        for (i in 0 until numPixels) {
            val targetX = cx - 180f + (i / numPixels.toFloat()) * 360f
            val targetY = cy

            val scatterX = targetX + sin(i * 1.7f) * 60f * (1f - cycle)
            val scatterY = targetY + cos(i * 2.3f) * 90f * (1f - cycle)

            canvas.drawRect(scatterX - 4f, scatterY - 4f, scatterX + 4f, scatterY + 4f, fillPaint)
        }

        if (cycle > 0.8f) {
            drawPaint.style = Paint.Style.STROKE
            drawPaint.strokeWidth = 3f
            drawPaint.color = ((((cycle - 0.8f) * 5f * 255).toInt()) shl 24) or 0x00FFFFFF
            canvas.drawLine(cx - 180f, cy, cx + 180f, cy, drawPaint)
        }
    }

    // -------------------------------------------------------------------------
    // 24. Future Monolith: Standing dark monolith with travelling light pulse
    // -------------------------------------------------------------------------
    private fun drawFutureMonolith(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val mw = 70f
        val mh = 340f

        // Monolith body
        fillPaint.color = 0xFF080C14.toInt()
        canvas.drawRect(cx - mw * 0.5f, cy - mh * 0.5f, cx + mw * 0.5f, cy + mh * 0.5f, fillPaint)

        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 3f
        drawPaint.color = (0x88 shl 24) or (pal.primary and 0x00FFFFFF)
        canvas.drawRect(cx - mw * 0.5f, cy - mh * 0.5f, cx + mw * 0.5f, cy + mh * 0.5f, drawPaint)

        // Travelling light seam down center
        val pulseY = cy - mh * 0.5f + ((t * 160f) % mh)
        fillPaint.color = 0xFF00FFFF.toInt()
        canvas.drawCircle(cx, pulseY, 8f, fillPaint)
        fillPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(cx, pulseY, 3.5f, fillPaint)
    }

    // -------------------------------------------------------------------------
    // 25. Vector Sweep: Radar sweep revealing target blips
    // -------------------------------------------------------------------------
    private fun drawVectorSweep(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val sweepAngle = t * 1.5f
        val radarR = 140f

        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 2f
        drawPaint.color = (0x55 shl 24) or (pal.primary and 0x00FFFFFF)
        canvas.drawCircle(cx, cy, radarR * 0.5f, drawPaint)
        canvas.drawCircle(cx, cy, radarR, drawPaint)

        // Sweep line
        val sx = cx + cos(sweepAngle) * radarR
        val sy = cy + sin(sweepAngle) * radarR
        drawPaint.strokeWidth = 3f
        drawPaint.color = (0xDD shl 24) or (pal.accent and 0x00FFFFFF)
        canvas.drawLine(cx, cy, sx, sy, drawPaint)

        // Blip
        val blipAngle = 1.2f
        val diff = abs(((sweepAngle - blipAngle) % (2f * PI.toFloat())))
        val blipAlpha = ((1f - (diff / 1.5f).coerceIn(0f, 1f)) * 255).toInt()
        if (blipAlpha > 10) {
            fillPaint.color = (blipAlpha shl 24) or (pal.primary and 0x00FFFFFF)
            canvas.drawCircle(cx + cos(blipAngle) * 90f, cy + sin(blipAngle) * 90f, 7f, fillPaint)
        }
    }

    // -------------------------------------------------------------------------
    // 26. Shape Morph: Circle -> Triangle -> Square -> Circle
    // -------------------------------------------------------------------------
    private fun drawShapeMorph(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val cycle = (t * 0.7f) % 3f // 0=circle, 1=triangle, 2=square
        val phase = cycle.toInt()
        val frac = cycle - phase
        val r = 110f

        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 5f
        drawPaint.color = (0xEE shl 24) or (pal.primary and 0x00FFFFFF)

        tempPath.reset()
        val steps = 60
        for (i in 0 until steps) {
            val a = (i / steps.toFloat()) * 2f * PI.toFloat()
            // Radial distance variation based on morph stage
            val mod = when (phase) {
                0 -> sin(a * 3f) * frac * 30f // circle to triangle
                1 -> (cos(a * 4f) * frac - sin(a * 3f) * (1f - frac)) * 25f // triangle to square
                else -> cos(a * 4f) * (1f - frac) * 25f // square to circle
            }
            val px = cx + cos(a) * (r + mod)
            val py = cy + sin(a) * (r + mod)
            if (i == 0) tempPath.moveTo(px, py) else tempPath.lineTo(px, py)
        }
        tempPath.close()
        canvas.drawPath(tempPath, drawPaint)
    }

    // -------------------------------------------------------------------------
    // 27. Gradient Wipe: Diagonal split gradient sweeping across screen
    // -------------------------------------------------------------------------
    private fun drawGradientWipe(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val offset = sin(t * 0.9f) * 120f
        tempPath.reset()
        tempPath.moveTo(0f, 0f)
        tempPath.lineTo(w, 0f)
        tempPath.lineTo(w, cy + offset)
        tempPath.lineTo(0f, cy + offset + 180f)
        tempPath.close()

        fillPaint.color = (0x88 shl 24) or (pal.primary and 0x00FFFFFF)
        canvas.drawPath(tempPath, fillPaint)

        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 4f
        drawPaint.color = 0xAAFFFFFF.toInt()
        canvas.drawLine(0f, cy + offset + 180f, w, cy + offset, drawPaint)
    }

    // -------------------------------------------------------------------------
    // 28. Orbiting Dashes: Counter-rotating concentric dashed rings
    // -------------------------------------------------------------------------
    private fun drawOrbitingDashes(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 6f

        // Outer ring (clockwise)
        val outerR = 140f
        drawPaint.color = (0xCC shl 24) or (pal.primary and 0x00FFFFFF)
        tempRect.set(cx - outerR, cy - outerR, cx + outerR, cy + outerR)
        canvas.save()
        canvas.rotate((t * 40f) % 360f, cx, cy)
        for (i in 0 until 8) {
            canvas.drawArc(tempRect, i * 45f, 25f, false, drawPaint)
        }
        canvas.restore()

        // Inner ring (counter-clockwise)
        val innerR = 90f
        drawPaint.color = (0xCC shl 24) or (pal.secondary and 0x00FFFFFF)
        tempRect.set(cx - innerR, cy - innerR, cx + innerR, cy + innerR)
        canvas.save()
        canvas.rotate((-t * 40f) % 360f, cx, cy)
        for (i in 0 until 6) {
            canvas.drawArc(tempRect, i * 60f, 30f, false, drawPaint)
        }
        canvas.restore()
    }

    // -------------------------------------------------------------------------
    // 29. Modular Assembly: Floating blocks assembling into cube & disassembling
    // -------------------------------------------------------------------------
    private fun drawModularAssembly(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val explode = (sin(t * 1.4f) + 1f) * 0.5f * 45f // 0 to 45px displacement
        val bSize = 38f
        fillPaint.color = pal.primary

        val offsets = listOf(
            Pair(-1f, -1f), Pair(1f, -1f),
            Pair(-1f, 1f), Pair(1f, 1f)
        )

        for ((dx, dy) in offsets) {
            val bx = cx + dx * (bSize * 0.55f + explode)
            val by = cy + dy * (bSize * 0.55f + explode)
            canvas.drawRect(bx - bSize * 0.5f, by - bSize * 0.5f, bx + bSize * 0.5f, by + bSize * 0.5f, fillPaint)

            drawPaint.style = Paint.Style.STROKE
            drawPaint.strokeWidth = 2f
            drawPaint.color = 0xFFFFFFFF.toInt()
            canvas.drawRect(bx - bSize * 0.5f, by - bSize * 0.5f, bx + bSize * 0.5f, by + bSize * 0.5f, drawPaint)
        }
    }

    // -------------------------------------------------------------------------
    // 30. Contour Flow: Topographic elevation lines drifting downward
    // -------------------------------------------------------------------------
    private fun drawContourFlow(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val numLines = 10
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 2.5f

        for (i in 0 until numLines) {
            val baseLineY = ((i * 70f + t * 40f) % (h * 0.7f)) + h * 0.15f
            tempPath.reset()
            val steps = 24
            for (s in 0..steps) {
                val x = (s / steps.toFloat()) * w
                val y = baseLineY + sin(s * 0.4f + i) * 35f
                if (s == 0) tempPath.moveTo(x, y) else tempPath.lineTo(x, y)
            }
            drawPaint.color = (0x88 shl 24) or (pal.primary and 0x00FFFFFF)
            canvas.drawPath(tempPath, drawPaint)
        }
    }

    // -------------------------------------------------------------------------
    // 31. Radial Burst: Minimal radial rays pulsing outward
    // -------------------------------------------------------------------------
    private fun drawRadialBurst(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val rays = 32
        val breathe = (sin(t * 1.6f) + 1f) * 0.5f
        drawPaint.style = Paint.Style.STROKE

        for (i in 0 until rays) {
            val angle = (i / rays.toFloat()) * 2f * PI.toFloat()
            val r1 = 30f + breathe * 20f
            val r2 = 140f + breathe * 50f

            drawPaint.strokeWidth = 2.5f
            drawPaint.color = (0x99 shl 24) or (if (i % 2 == 0) pal.primary else pal.accent and 0x00FFFFFF)
            canvas.drawLine(cx + cos(angle) * r1, cy + sin(angle) * r1, cx + cos(angle) * r2, cy + sin(angle) * r2, drawPaint)
        }
    }

    // -------------------------------------------------------------------------
    // 32. Mughal Arch: Pointed arch silhouette with sweeping gold highlight
    // -------------------------------------------------------------------------
    private fun drawMughalArch(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val archTop = cy - 160f
        val archBottom = cy + 180f
        val archW = 120f

        tempPath.reset()
        tempPath.moveTo(cx - archW, archBottom)
        tempPath.lineTo(cx - archW, cy)
        tempPath.cubicTo(cx - archW, archTop + 60f, cx - 20f, archTop + 30f, cx, archTop)
        tempPath.cubicTo(cx + 20f, archTop + 30f, cx + archW, archTop + 60f, cx + archW, cy)
        tempPath.lineTo(cx + archW, archBottom)

        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 6f
        drawPaint.color = 0xFFD4AF37.toInt() // Mughal Gold
        canvas.drawPath(tempPath, drawPaint)

        // Sweeping golden highlight spark along pointed arch
        val phase = (t * 0.8f) % 2f
        val sparkFrac = if (phase < 1f) phase else (2f - phase)
        val sx = cx - archW + sparkFrac * archW * 2f
        val sy = cy - sin(sparkFrac * 3.14f) * 160f

        fillPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(sx, sy, 7f, fillPaint)
    }

    // -------------------------------------------------------------------------
    // 33. Ukiyo-e Wave: Great wave rolling forward seamlessly
    // -------------------------------------------------------------------------
    private fun drawUkiyoEWave(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val waveBaseY = cy + 80f
        val waveCycle = t * 1.2f

        tempPath.reset()
        tempPath.moveTo(0f, waveBaseY)
        val steps = 30
        for (i in 0..steps) {
            val x = (i / steps.toFloat()) * w
            val y = waveBaseY + sin((i * 0.3f) + waveCycle) * 60f
            tempPath.lineTo(x, y)
        }
        tempPath.lineTo(w, h)
        tempPath.lineTo(0f, h)
        tempPath.close()

        fillPaint.color = 0xFF1D3557.toInt() // Indigo Deep Blue
        canvas.drawPath(tempPath, fillPaint)

        // Sea foam crest
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 6f
        drawPaint.color = 0xFFF1FAEE.toInt() // Foam white
        canvas.drawPath(tempPath, drawPaint)
    }

    // -------------------------------------------------------------------------
    // 34. Greek Marble: Fluted column with shifting shadow angle
    // -------------------------------------------------------------------------
    private fun drawGreekMarble(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val colW = 140f
        val colH = 340f
        val shadowShift = sin(t * 1.0f) * 20f

        fillPaint.color = 0xFFE0E5EC.toInt() // Classical Marble
        canvas.drawRect(cx - colW * 0.5f, cy - colH * 0.5f, cx + colW * 0.5f, cy + colH * 0.5f, fillPaint)

        // Fluted vertical grooves
        val flutes = 5
        for (i in 0 until flutes) {
            val fx = cx - colW * 0.4f + i * (colW * 0.2f) + shadowShift * 0.3f
            drawPaint.style = Paint.Style.STROKE
            drawPaint.strokeWidth = 3f
            drawPaint.color = 0x66708090.toInt()
            canvas.drawLine(fx, cy - colH * 0.5f, fx, cy + colH * 0.5f, drawPaint)
        }
    }

    // -------------------------------------------------------------------------
    // 35. Egyptian Sun Lotus: Sun disc with unfolding lotus petals
    // -------------------------------------------------------------------------
    private fun drawEgyptianSunLotus(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val unfold = (sin(t * 1.2f) + 1f) * 0.5f

        // Sun disc
        fillPaint.color = 0xFFFFD700.toInt()
        canvas.drawCircle(cx, cy - 90f, 32f, fillPaint)

        // Lotus petals below sun
        val lotusCenterY = cy + 40f
        val petals = 5
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 4f
        drawPaint.color = 0xFF1E90FF.toInt() // Sacred Egyptian Blue

        for (i in 0 until petals) {
            val angle = ((i - 2) * 18f * unfold) * (PI.toFloat() / 180f)
            tempPath.reset()
            tempPath.moveTo(cx, lotusCenterY)
            tempPath.cubicTo(
                cx + sin(angle) * 60f, lotusCenterY - 40f,
                cx + sin(angle) * 80f, lotusCenterY - 80f,
                cx + sin(angle) * 70f, lotusCenterY - 110f
            )
            canvas.drawPath(tempPath, drawPaint)
        }
    }

    // -------------------------------------------------------------------------
    // 36. Persian Star Tile: 8-pointed Girih star tile shimmering
    // -------------------------------------------------------------------------
    private fun drawPersianStarTile(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val rOuter = 130f
        val rInner = 65f
        val shimmer = (sin(t * 2f) + 1f) * 0.5f

        tempPath.reset()
        for (i in 0 until 16) {
            val a = (i / 16f) * 2f * PI.toFloat()
            val r = if (i % 2 == 0) rOuter else rInner
            val px = cx + cos(a) * r
            val py = cy + sin(a) * r
            if (i == 0) tempPath.moveTo(px, py) else tempPath.lineTo(px, py)
        }
        tempPath.close()

        fillPaint.color = 0xFF003366.toInt() // Lapis Lazuli Deep Blue
        canvas.drawPath(tempPath, fillPaint)

        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 4f + shimmer * 2f
        drawPaint.color = 0xFFFFD700.toInt() // Gold Foil
        canvas.drawPath(tempPath, drawPaint)
    }

    // -------------------------------------------------------------------------
    // 37. Renaissance Astrolabe: Counter-rotating brass gears
    // -------------------------------------------------------------------------
    private fun drawRenaissanceAstrolabe(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val dialR = 130f
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 5f
        drawPaint.color = 0xFFCD7F32.toInt() // Antique Brass
        canvas.drawCircle(cx, cy, dialR, drawPaint)

        // Clockwise inner ring
        canvas.save()
        canvas.rotate((t * 25f) % 360f, cx, cy)
        canvas.drawLine(cx - dialR, cy, cx + dialR, cy, drawPaint)
        canvas.drawLine(cx, cy - dialR, cx, cy + dialR, drawPaint)
        canvas.restore()

        // Counter-clockwise pointer
        canvas.save()
        canvas.rotate((-t * 40f) % 360f, cx, cy)
        drawPaint.strokeWidth = 3f
        drawPaint.color = 0xFFE5C158.toInt()
        canvas.drawLine(cx, cy, cx + dialR * 0.9f, cy, drawPaint)
        canvas.restore()
    }

    // -------------------------------------------------------------------------
    // 38. Star Catcher: Silhouette reaching for breathing radiant star
    // -------------------------------------------------------------------------
    private fun drawStarCatcher(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val starBob = sin(t * 1.5f) * 12f
        val starPulse = (sin(t * 2.5f) + 1f) * 0.5f

        // Star
        val sx = cx + 25f
        val sy = cy - 110f + starBob
        fillPaint.color = (0x66 shl 24) or 0x00FFFFFF
        canvas.drawCircle(sx, sy, 24f + starPulse * 12f, fillPaint)

        fillPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(sx, sy, 7f, fillPaint)

        // Solitary hilltop
        tempPath.reset()
        tempPath.moveTo(0f, cy + 120f)
        tempPath.quadTo(cx, cy + 40f, w, cy + 140f)
        tempPath.lineTo(w, h)
        tempPath.lineTo(0f, h)
        tempPath.close()

        fillPaint.color = 0xFF05070B.toInt()
        canvas.drawPath(tempPath, fillPaint)

        // Figure silhouette reaching up
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 5f
        drawPaint.color = 0xFF05070B.toInt()
        canvas.drawLine(cx - 30f, cy + 55f, cx - 15f, cy, drawPaint)
        canvas.drawLine(cx - 15f, cy, sx - 10f, sy + 30f, drawPaint)
    }

    // -------------------------------------------------------------------------
    // 39. The Key: Floating antique key with orbiting lock dial
    // -------------------------------------------------------------------------
    private fun drawTheKey(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val bob = sin(t * 1.4f) * 15f
        val ky = cy + bob

        // Key bow
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 6f
        drawPaint.color = 0xFFE0A96D.toInt()
        canvas.drawCircle(cx, ky - 70f, 28f, drawPaint)

        // Key shaft & bit
        canvas.drawLine(cx, ky - 42f, cx, ky + 80f, drawPaint)
        canvas.drawLine(cx, ky + 60f, cx + 24f, ky + 60f, drawPaint)
        canvas.drawLine(cx, ky + 76f, cx + 18f, ky + 76f, drawPaint)

        // Orbiting glowing lock dial
        val angle = t * 1.2f
        val dialX = cx + cos(angle) * 75f
        val dialY = ky + sin(angle) * 30f
        fillPaint.color = 0xAAFFFFFF.toInt()
        canvas.drawCircle(dialX, dialY, 8f, fillPaint)
    }

    // -------------------------------------------------------------------------
    // 40. Koi Circuit: Minimalist Koi swimming in circular lap
    // -------------------------------------------------------------------------
    private fun drawKoiCircuit(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val orbitR = 110f
        val angle = t * 0.9f

        // Center lily pad
        fillPaint.color = 0xFF2D6A4F.toInt()
        canvas.drawCircle(cx, cy, 35f, fillPaint)

        // Koi position
        val kx = cx + cos(angle) * orbitR
        val ky = cy + sin(angle) * orbitR
        val heading = angle + PI.toFloat() * 0.5f

        canvas.save()
        canvas.translate(kx, ky)
        canvas.rotate(heading * 57.3f)

        // Koi body
        fillPaint.color = 0xFFE63946.toInt() // Red
        tempRect.set(-10f, -24f, 10f, 24f)
        canvas.drawOval(tempRect, fillPaint)

        // White markings
        fillPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(0f, -6f, 6f, fillPaint)

        // Tail fin wiggle
        val tailWiggle = sin(t * 8f) * 12f
        tempPath.reset()
        tempPath.moveTo(0f, 24f)
        tempPath.lineTo(-8f + tailWiggle, 42f)
        tempPath.lineTo(8f + tailWiggle, 42f)
        tempPath.close()
        fillPaint.color = 0xFFE63946.toInt()
        canvas.drawPath(tempPath, fillPaint)

        canvas.restore()
    }

    // -------------------------------------------------------------------------
    // 41. Bloom Dial: Clock petals rotating as hands
    // -------------------------------------------------------------------------
    private fun drawBloomDial(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val dialR = 120f
        fillPaint.color = 0x66FFFFFF.toInt()

        // 12 hour petal ticks
        for (i in 0 until 12) {
            val a = (i / 12f) * 2f * PI.toFloat()
            canvas.drawCircle(cx + cos(a) * dialR, cy + sin(a) * dialR, 6f, fillPaint)
        }

        // Rotating hand petals
        val handAngle = t * 0.6f
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 4f
        drawPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawLine(cx, cy, cx + cos(handAngle) * 90f, cy + sin(handAngle) * 90f, drawPaint)
        canvas.drawLine(cx, cy, cx + cos(handAngle * 0.1f) * 60f, cy + sin(handAngle * 0.1f) * 60f, drawPaint)
    }

    // -------------------------------------------------------------------------
    // 42. Cassette Target: Spinning tape spools & rippling magnetic tape
    // -------------------------------------------------------------------------
    private fun drawCassetteTarget(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val cw = 220f
        val ch = 140f

        // Cassette shell
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 4f
        drawPaint.color = 0x88FFFFFF.toInt()
        canvas.drawRoundRect(cx - cw * 0.5f, cy - ch * 0.5f, cx + cw * 0.5f, cy + ch * 0.5f, 12f, 12f, drawPaint)

        // Left & Right spools
        val spoolR = 24f
        val spoolOffsets = listOf(-55f, 55f)
        for (ox in spoolOffsets) {
            val sx = cx + ox
            canvas.drawCircle(sx, cy, spoolR, drawPaint)

            // Spinning spokes
            canvas.save()
            canvas.rotate((t * 120f) % 360f, sx, cy)
            canvas.drawLine(sx - spoolR, cy, sx + spoolR, cy, drawPaint)
            canvas.restore()
        }

        // Magnetic tape ripple line below
        tempPath.reset()
        tempPath.moveTo(cx - 55f, cy + 30f)
        val rippleY = sin(t * 4f) * 6f
        tempPath.quadTo(cx, cy + 30f + rippleY, cx + 55f, cy + 30f)
        canvas.drawPath(tempPath, drawPaint)
    }

    // -------------------------------------------------------------------------
    // 43. Moon Diver: Diver silhouette descending through deep ocean
    // -------------------------------------------------------------------------
    private fun drawMoonDiver(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val diverY = ((t * 60f) % (h * 0.7f)) + h * 0.15f

        // Diver silhouette
        canvas.save()
        canvas.translate(cx, diverY)
        fillPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(0f, -20f, 6f, fillPaint) // head
        canvas.drawRect(-4f, -12f, 4f, 14f, fillPaint) // torso

        // Air bubbles rising
        fillPaint.color = 0x88A0E0FF.toInt()
        for (i in 1..4) {
            val by = -20f - i * 18f
            canvas.drawCircle(sin(t * 3f + i) * 6f, by, 3f + i * 0.5f, fillPaint)
        }
        canvas.restore()
    }

    // -------------------------------------------------------------------------
    // 44. North: Minimalist oscillating compass needle
    // -------------------------------------------------------------------------
    private fun drawNorth(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val needleOsc = sin(t * 1.8f) * 6f * (PI.toFloat() / 180f)
        val housingR = 120f

        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 3f
        drawPaint.color = 0x66FFFFFF.toInt()
        canvas.drawCircle(cx, cy, housingR, drawPaint)

        // North needle (Red)
        tempPath.reset()
        val nx = cx + sin(needleOsc) * 90f
        val ny = cy - cos(needleOsc) * 90f
        tempPath.moveTo(cx - 10f, cy)
        tempPath.lineTo(nx, ny)
        tempPath.lineTo(cx + 10f, cy)
        tempPath.close()

        fillPaint.color = 0xFFE63946.toInt()
        canvas.drawPath(tempPath, fillPaint)

        // South needle (White)
        tempPath.reset()
        val sx = cx - sin(needleOsc) * 90f
        val sy = cy + cos(needleOsc) * 90f
        tempPath.moveTo(cx - 10f, cy)
        tempPath.lineTo(sx, sy)
        tempPath.lineTo(cx + 10f, cy)
        tempPath.close()

        fillPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawPath(tempPath, fillPaint)
    }

    // -------------------------------------------------------------------------
    // 45. Portal Window: Cathedral rose window shifting sunlight
    // -------------------------------------------------------------------------
    private fun drawPortalWindow(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val winR = 130f
        val petals = 8
        val phase = t * 0.8f

        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 4f
        drawPaint.color = 0xAAFFFFFF.toInt()
        canvas.drawCircle(cx, cy, winR, drawPaint)

        for (i in 0 until petals) {
            val a = (i / petals.toFloat()) * 2f * PI.toFloat()
            val px = cx + cos(a) * (winR * 0.55f)
            val py = cy + sin(a) * (winR * 0.55f)

            val colorIndex = (i + (phase).toInt()) % 4
            fillPaint.color = when (colorIndex) {
                0 -> 0x99FF3366.toInt()
                1 -> 0x9933CCFF.toInt()
                2 -> 0x99FFAA00.toInt()
                else -> 0x999933FF.toInt()
            }
            canvas.drawCircle(px, py, 28f, fillPaint)
            canvas.drawCircle(px, py, 28f, drawPaint)
        }
    }

    // -------------------------------------------------------------------------
    // 46. Tea Steam: Wisps rising from ceramic cup
    // -------------------------------------------------------------------------
    private fun drawTeaSteam(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val cupW = 80f
        val cupH = 65f
        val cupY = cy + 60f

        // Cup body
        fillPaint.color = 0xFFD8D2C2.toInt()
        canvas.drawRoundRect(cx - cupW * 0.5f, cupY, cx + cupW * 0.5f, cupY + cupH, 12f, 12f, fillPaint)

        // Rising curls of steam
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 3f

        for (curl in 0..1) {
            val ox = (curl - 0.5f) * 30f
            val curlProgress = ((t * 0.5f + curl * 0.5f) % 1f)
            val steamY = cupY - curlProgress * 140f
            val alpha = ((1f - curlProgress) * 200).toInt().coerceIn(0, 255)

            drawPaint.color = (alpha shl 24) or 0x00FFFFFF
            tempPath.reset()
            tempPath.moveTo(cx + ox, steamY + 40f)
            tempPath.quadTo(cx + ox + sin(t * 2f + curl) * 18f, steamY + 20f, cx + ox, steamY)
            canvas.drawPath(tempPath, drawPaint)
        }
    }

    // -------------------------------------------------------------------------
    // 47. Pendulum: True harmonic pendulum arc swing
    // -------------------------------------------------------------------------
    private fun drawPendulum(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val pivotY = cy - 160f
        val wireLen = 220f
        val maxAngle = 32f * (PI.toFloat() / 180f)
        val theta = maxAngle * cos(t * 2.2f)

        val bobX = cx + sin(theta) * wireLen
        val bobY = pivotY + cos(theta) * wireLen

        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 2.5f
        drawPaint.color = 0xAAFFFFFF.toInt()
        canvas.drawLine(cx, pivotY, bobX, bobY, drawPaint)

        // Chrome Bob
        fillPaint.color = 0xFFE0E0E0.toInt()
        canvas.drawCircle(bobX, bobY, 20f, fillPaint)
        fillPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(bobX - 5f, bobY - 5f, 6f, fillPaint)
    }

    // -------------------------------------------------------------------------
    // 48. Floating Petal: Cherry blossom petal in spiral air eddy
    // -------------------------------------------------------------------------
    private fun drawFloatingPetal(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val spiralAngle = t * 1.2f
        val spiralR = 80f + sin(t * 2f) * 25f
        val petalX = cx + cos(spiralAngle) * spiralR
        val petalY = ((t * 40f) % (h * 0.6f)) + h * 0.2f

        canvas.save()
        canvas.translate(petalX, petalY)
        canvas.rotate((t * 70f) % 360f)

        fillPaint.color = 0xFFFFB7B2.toInt() // Sakura Pink
        tempRect.set(-12f, -20f, 12f, 20f)
        canvas.drawOval(tempRect, fillPaint)

        fillPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(0f, -6f, 4f, fillPaint)
        canvas.restore()
    }

    // -------------------------------------------------------------------------
    // 49. Jellyfish: Contracting bell pulsating upward
    // -------------------------------------------------------------------------
    private fun drawJellyfish(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val pulse = (sin(t * 2f) + 1f) * 0.5f // contraction vs expansion
        val jY = ((t * 45f) % (h * 0.6f)) + h * 0.2f
        val bellW = 80f - pulse * 20f
        val bellH = 50f + pulse * 15f

        // Translucent Bell Dome
        fillPaint.color = (0x66 shl 24) or 0x00A0FFFF.toInt()
        tempRect.set(cx - bellW * 0.5f, jY - bellH, cx + bellW * 0.5f, jY)
        canvas.drawArc(tempRect, 180f, 180f, true, fillPaint)

        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 3f
        drawPaint.color = 0xFFA0FFFF.toInt()
        canvas.drawArc(tempRect, 180f, 180f, false, drawPaint)

        // Trailing tentacles
        for (i in 0 until 5) {
            val tx = cx - bellW * 0.4f + i * (bellW * 0.2f)
            tempPath.reset()
            tempPath.moveTo(tx, jY)
            tempPath.quadTo(tx + sin(t * 3f + i) * 15f, jY + 50f, tx, jY + 110f)
            canvas.drawPath(tempPath, drawPaint)
        }
    }

    // -------------------------------------------------------------------------
    // 50. Rain on Leaf: Droplet forming and sliding off monstera leaf
    // -------------------------------------------------------------------------
    private fun drawRainOnLeaf(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        // Monstera leaf profile
        tempPath.reset()
        tempPath.moveTo(cx - 140f, cy - 80f)
        tempPath.cubicTo(cx - 50f, cy - 120f, cx + 50f, cy - 40f, cx + 130f, cy + 70f)
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 7f
        drawPaint.color = 0xFF2D6A4F.toInt()
        canvas.drawPath(tempPath, drawPaint)

        // Sliding droplet
        val dropProgress = (t * 0.8f) % 1f
        val dx = cx - 140f + dropProgress * 270f
        val dy = cy - 80f + dropProgress * 150f

        fillPaint.color = 0xAAFFFFFF.toInt()
        canvas.drawCircle(dx, dy, 9f, fillPaint)
        fillPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(dx - 2f, dy - 2f, 3f, fillPaint)
    }

    // -------------------------------------------------------------------------
    // 51. Moon Cloud: Crescent moon with wrapping cloud ribbon
    // -------------------------------------------------------------------------
    private fun drawMoonCloud(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val moonR = 65f

        // Crescent moon
        fillPaint.color = 0xFFFFF1C5.toInt()
        canvas.drawCircle(cx, cy - 40f, moonR, fillPaint)
        fillPaint.color = pal.background
        canvas.drawCircle(cx + 25f, cy - 50f, moonR * 0.9f, fillPaint)

        // Wrapping cloud ribbon
        val cloudX = ((t * 35f) % (w + 240f)) - 120f
        fillPaint.color = 0x55E0E5EC.toInt()
        tempRect.set(cloudX - 100f, cy - 60f, cloudX + 100f, cy - 20f)
        canvas.drawRoundRect(tempRect, 20f, 20f, fillPaint)
    }

    // -------------------------------------------------------------------------
    // 52. Firefly: Soft figure path with breathing light
    // -------------------------------------------------------------------------
    private fun drawFirefly(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val fx = cx + cos(t * 1.4f) * 70f
        val fy = (cy + 20f) + sin(t * 2.8f) * 35f
        val pulse = (sin(t * 3f) + 1f) * 0.5f

        // Firefly glow
        fillPaint.color = ((((pulse * 140).toInt() + 40) shl 24) or 0x00CCFF00.toInt())
        canvas.drawCircle(fx, fy, 24f + pulse * 14f, fillPaint)

        fillPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(fx, fy, 4f, fillPaint)

        // Dark grass blades at bottom
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeWidth = 4f
        drawPaint.color = 0xFF0E1A12.toInt()
        for (i in 0..12) {
            val gx = (i / 12f) * w
            canvas.drawLine(gx, h, gx + sin(i * 1.5f) * 20f, h - 80f, drawPaint)
        }
    }

    // -------------------------------------------------------------------------
    // 53. Fountain Ripple: Droplet creating expanding fading circular ripples
    // -------------------------------------------------------------------------
    private fun drawFountainRipple(canvas: Canvas, t: Float, w: Float, h: Float, cx: Float, cy: Float, pal: ColorPalette) {
        val cycle = (t * 0.7f) % 1f // 0 to 1 loop
        val dropPhase = cycle / 0.35f
        val ripplePhase = (cycle - 0.35f) / 0.65f

        if (dropPhase < 1f) {
            // Droplet falling
            val dropY = (cy - 120f) + dropPhase * 120f
            fillPaint.color = 0xFFE0F7FA.toInt()
            canvas.drawCircle(cx, dropY, 6f, fillPaint)
        } else {
            // Concentric expanding ripples
            drawPaint.style = Paint.Style.STROKE
            val maxR = 190f
            val currentR = ripplePhase * maxR
            val alpha = ((1f - ripplePhase) * 255).toInt().coerceIn(0, 255)

            drawPaint.color = (alpha shl 24) or 0x00B2EBF2.toInt()
            drawPaint.strokeWidth = 3.5f
            tempRect.set(cx - currentR, cy - currentR * 0.45f, cx + currentR, cy + currentR * 0.45f)
            canvas.drawOval(tempRect, drawPaint)

            if (currentR > 40f) {
                val r2 = currentR - 35f
                val alpha2 = ((1f - (r2 / maxR)) * 180).toInt().coerceIn(0, 255)
                drawPaint.color = (alpha2 shl 24) or 0x00B2EBF2.toInt()
                tempRect.set(cx - r2, cy - r2 * 0.45f, cx + r2, cy + r2 * 0.45f)
                canvas.drawOval(tempRect, drawPaint)
            }
        }
    }
}
