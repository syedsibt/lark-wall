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
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Catalog of the "Signature" live wallpapers.
 *
 * Every entry here is a structurally different design (its own drawing routine),
 * not a recolour of another wallpaper. Each one ships with a single curated palette.
 */
object SignatureCatalog {

    data class Entry(
        val id: String,
        val title: String,
        val family: EngineFamily,
        val palette: ColorPalette,
        val interaction: InteractionType,
        val description: String
    )

    val ENTRIES: List<Entry> = listOf(
        // Cyberpunk & Neon
        Entry("sig_terminal_boot", "Terminal Boot", EngineFamily.CYBERPUNK_NEON, ColorPalette.AMBER_TERMINAL, InteractionType.PULSE_WAVE,
            "Retro amber terminal with scrolling code blocks, line gutter and a blinking cursor."),
        Entry("sig_neon_tunnel", "Neon Tunnel", EngineFamily.CYBERPUNK_NEON, ColorPalette.TOKYO_NIGHT, InteractionType.DEPTH_PARALLAX_TILT,
            "Endless twisting tunnel of neon frames rushing toward you."),
        Entry("sig_hex_shield", "Hex Shield", EngineFamily.CYBERPUNK_NEON, ColorPalette.CYBER_NEON, InteractionType.RIPPLE_DISPERSION,
            "Honeycomb energy shield with charge waves. Touch to send a pulse."),

        // Abstract Geometry
        Entry("sig_spirograph", "Spirograph Bloom", EngineFamily.ABSTRACT_GEOMETRY, ColorPalette.SACRED_GOLD, InteractionType.VORTEX_SWIRL,
            "Two breathing hypotrochoid curves traced by a glowing pen."),
        Entry("sig_truchet", "Truchet Weave", EngineFamily.ABSTRACT_GEOMETRY, ColorPalette.DEEP_NEBULA, InteractionType.RIPPLE_DISPERSION,
            "Quarter-circle tiles that flip and re-route an endless woven maze."),
        Entry("sig_phyllotaxis", "Golden Phyllotaxis", EngineFamily.ABSTRACT_GEOMETRY, ColorPalette.SOLAR_CORONA, InteractionType.PULSE_WAVE,
            "Sunflower seed spiral built on the golden angle, pulsing outward."),
        Entry("sig_prism_mosaic", "Prism Mosaic", EngineFamily.ABSTRACT_GEOMETRY, ColorPalette.ABYSS_JELLYFISH, InteractionType.LIGHT_TRAIL_GLOW,
            "Triangle tessellation lit by slow drifting light fields."),

        // Fluid & Liquid
        Entry("sig_lava_lamp", "Lava Lamp", EngineFamily.FLUID_LIQUID, ColorPalette.LAVA_LAMP, InteractionType.MAGNETIC_ATTRACTION,
            "Warm wax blobs rising and sinking inside a glowing lamp."),
        Entry("sig_ink_bloom", "Ink Bloom", EngineFamily.FLUID_LIQUID, ColorPalette.INK_WASH, InteractionType.RIPPLE_DISPERSION,
            "Watercolour ink drops spreading in soft wobbly rings."),
        Entry("sig_flow_field", "Silk Flow Field", EngineFamily.FLUID_LIQUID, ColorPalette.LIQUID_CHROME, InteractionType.VORTEX_SWIRL,
            "Hundreds of silk threads combed by an invisible current."),

        // Celestial & Space
        Entry("sig_orrery", "Orrery", EngineFamily.CELESTIAL_SPACE, ColorPalette.SOLAR_CORONA, InteractionType.DEPTH_PARALLAX_TILT,
            "A tiny solar system: planets on tilted orbits around a living sun."),
        Entry("sig_meteor_shower", "Meteor Shower", EngineFamily.CELESTIAL_SPACE, ColorPalette.ARCTIC_ICE, InteractionType.LIGHT_TRAIL_GLOW,
            "Shooting stars over a quiet mountain ridge."),
        Entry("sig_lunar_cycle", "Lunar Cycle", EngineFamily.CELESTIAL_SPACE, ColorPalette.ZEN_TWILIGHT, InteractionType.DEPTH_PARALLAX_TILT,
            "The moon moving through all of its phases, with a phase timeline."),
        Entry("sig_spiral_galaxy", "Spiral Galaxy", EngineFamily.CELESTIAL_SPACE, ColorPalette.DEEP_NEBULA, InteractionType.VORTEX_SWIRL,
            "Two-armed galaxy with differential rotation and a glowing core."),

        // Nature
        Entry("sig_night_ocean", "Night Ocean", EngineFamily.BIOLUMINESCENT_NATURE, ColorPalette.MIDNIGHT_OCEAN, InteractionType.RIPPLE_DISPERSION,
            "Rolling waves under a full moon with a shimmering reflection path."),
        Entry("sig_snowfall", "Silent Snowfall", EngineFamily.BIOLUMINESCENT_NATURE, ColorPalette.ARCTIC_ICE, InteractionType.GRAVITATIONAL_REPULSION,
            "Three layers of snow drifting over pine-covered mountains."),
        Entry("sig_bamboo", "Bamboo Grove", EngineFamily.BIOLUMINESCENT_NATURE, ColorPalette.BAMBOO_MIST, InteractionType.DEPTH_PARALLAX_TILT,
            "Tall bamboo stalks swaying gently in the mist."),
        Entry("sig_dandelion", "Dandelion Drift", EngineFamily.BIOLUMINESCENT_NATURE, ColorPalette.ENCHANTED_FOREST, InteractionType.GRAVITATIONAL_REPULSION,
            "Dandelion seeds letting go one by one and floating away."),

        // Minimal & Zen
        Entry("sig_sand_garden", "Sand Garden", EngineFamily.MINIMAL_ZEN, ColorPalette.ZEN_TWILIGHT, InteractionType.RIPPLE_DISPERSION,
            "Karesansui rock garden: raked lines flowing around three stones."),
        Entry("sig_breathe", "Breathe", EngineFamily.MINIMAL_ZEN, ColorPalette.ABYSS_JELLYFISH, InteractionType.PULSE_WAVE,
            "Six-petal breathing flower paced for slow 4-4-4-4 box breathing."),
        Entry("sig_lanterns", "Lantern Festival", EngineFamily.MINIMAL_ZEN, ColorPalette.LANTERN_GLOW, InteractionType.MAGNETIC_ATTRACTION,
            "Paper lanterns rising over a calm river."),

        // Bold Chromatics
        Entry("sig_equalizer", "Spectrum Equalizer", EngineFamily.BOLD_CHROMATIC, ColorPalette.SYNTHWAVE_SUNSET, InteractionType.PULSE_WAVE,
            "Mirrored audio-style spectrum bars with falling peak caps."),
        Entry("sig_vasarely", "Vasarely Bulge", EngineFamily.BOLD_CHROMATIC, ColorPalette.PRISMATIC_OP, InteractionType.MAGNETIC_ATTRACTION,
            "Op-art checkerboard that bulges toward a wandering lens. Drag to move it."),
        Entry("sig_chroma_wheel", "Chroma Wheel", EngineFamily.BOLD_CHROMATIC, ColorPalette.PRISMATIC_OP, InteractionType.VORTEX_SWIRL,
            "Counter-rotating rings of rainbow segments.")
    )

    val BY_ID: Map<String, Entry> = ENTRIES.associateBy { it.id }

    fun isSignature(presetId: String): Boolean = presetId.startsWith("sig_")
}

/**
 * Renders the Signature designs. All coordinates are computed relative to the
 * surface size, and paints/paths/shaders are reused between frames.
 */
class SignatureDesignsPipeline {

    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val bgPaint = Paint().apply { isDither = true }
    private val path = Path()
    private val path2 = Path()
    private val rect = RectF()
    private val hsv = FloatArray(3)

    // Unit radial gradients (radius 1 at the origin) cached per colour; drawn via canvas scale.
    private val glowCache = HashMap<Int, RadialGradient>()

    // Background gradient cache keyed by design + size
    private var bgKey: String = ""
    private var bgShader: Shader? = null

    // Hexagon template for the hex shield
    private val hexPath = Path()
    private var hexRadius = -1f

    // Equalizer peak caps
    private val peaks = FloatArray(64)

    fun render(
        canvas: Canvas,
        id: String,
        t: Float,
        dt: Float,
        width: Int,
        height: Int,
        touchX: Float,
        touchY: Float,
        isTouching: Boolean,
        p: ColorPalette
    ) {
        if (width <= 0 || height <= 0) return
        val w = width.toFloat()
        val h = height.toFloat()
        when (id) {
            "sig_terminal_boot" -> terminal(canvas, t, w, h, p)
            "sig_neon_tunnel" -> tunnel(canvas, t, w, h, touchX, touchY, isTouching, p)
            "sig_hex_shield" -> hexShield(canvas, t, w, h, touchX, touchY, isTouching, p)
            "sig_spirograph" -> spirograph(canvas, t, w, h, p)
            "sig_truchet" -> truchet(canvas, t, w, h, p)
            "sig_phyllotaxis" -> phyllotaxis(canvas, t, w, h, p)
            "sig_prism_mosaic" -> prismMosaic(canvas, t, w, h, p)
            "sig_lava_lamp" -> lavaLamp(canvas, t, w, h, touchX, touchY, isTouching, p)
            "sig_ink_bloom" -> inkBloom(canvas, t, w, h, p)
            "sig_flow_field" -> flowField(canvas, t, w, h, p)
            "sig_orrery" -> orrery(canvas, t, w, h, p)
            "sig_meteor_shower" -> meteorShower(canvas, t, w, h, p)
            "sig_lunar_cycle" -> lunarCycle(canvas, t, w, h, p)
            "sig_spiral_galaxy" -> spiralGalaxy(canvas, t, w, h, p)
            "sig_night_ocean" -> nightOcean(canvas, t, w, h, p)
            "sig_snowfall" -> snowfall(canvas, t, w, h, touchX, touchY, isTouching, p)
            "sig_bamboo" -> bamboo(canvas, t, w, h, p)
            "sig_dandelion" -> dandelion(canvas, t, w, h, p)
            "sig_sand_garden" -> sandGarden(canvas, t, w, h, p)
            "sig_breathe" -> breathe(canvas, t, w, h, p)
            "sig_lanterns" -> lanterns(canvas, t, w, h, p)
            "sig_equalizer" -> equalizer(canvas, t, dt, w, h, p)
            "sig_vasarely" -> vasarely(canvas, t, w, h, touchX, touchY, isTouching, p)
            "sig_chroma_wheel" -> chromaWheel(canvas, t, w, h, p)
            else -> phyllotaxis(canvas, t, w, h, p)
        }
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    /** Deterministic pseudo random in [0, 1). */
    private fun rnd(i: Int): Float {
        var x = i * 374761393 + 668265263
        x = (x xor (x ushr 13)) * 1274126177
        x = x xor (x ushr 16)
        return (x and 0xFFFFFF) / 16777216f
    }

    private fun lerp(a: Float, b: Float, f: Float) = a + (b - a) * f

    private fun lerpColor(c0: Int, c1: Int, f: Float): Int {
        val k = f.coerceIn(0f, 1f)
        val r = (Color.red(c0) + (Color.red(c1) - Color.red(c0)) * k).toInt()
        val g = (Color.green(c0) + (Color.green(c1) - Color.green(c0)) * k).toInt()
        val b = (Color.blue(c0) + (Color.blue(c1) - Color.blue(c0)) * k).toInt()
        return Color.rgb(r, g, b)
    }

    private fun shade(c: Int, factor: Float): Int = lerpColor(Color.BLACK, c, factor)

    private fun smooth(e0: Float, e1: Float, x: Float): Float {
        val k = ((x - e0) / (e1 - e0)).coerceIn(0f, 1f)
        return k * k * (3f - 2f * k)
    }

    private fun glow(canvas: Canvas, x: Float, y: Float, r: Float, color: Int, alpha: Int) {
        if (r <= 0.5f || alpha <= 0) return
        val key = color or (0xFF shl 24)
        val shader = glowCache.getOrPut(key) {
            RadialGradient(
                0f, 0f, 1f,
                intArrayOf(key, (key and 0x00FFFFFF) or (0x55 shl 24), key and 0x00FFFFFF),
                floatArrayOf(0f, 0.35f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        glowPaint.shader = shader
        glowPaint.alpha = alpha.coerceIn(0, 255)
        canvas.save()
        canvas.translate(x, y)
        canvas.scale(r, r)
        canvas.drawCircle(0f, 0f, 1f, glowPaint)
        canvas.restore()
        glowPaint.shader = null
    }

    private fun verticalBackground(canvas: Canvas, key: String, w: Float, h: Float, top: Int, bottom: Int) {
        val k = "$key-${w.toInt()}x${h.toInt()}"
        if (k != bgKey || bgShader == null) {
            bgKey = k
            bgShader = LinearGradient(0f, 0f, 0f, h, top, bottom, Shader.TileMode.CLAMP)
        }
        bgPaint.shader = bgShader
        canvas.drawRect(0f, 0f, w, h, bgPaint)
        bgPaint.shader = null
    }

    private fun stars(canvas: Canvas, t: Float, w: Float, h: Float, count: Int, maxY: Float, color: Int, seed: Int) {
        fill.color = color
        for (i in 0 until count) {
            val x = rnd(seed + i * 3) * w
            val y = rnd(seed + i * 3 + 1) * maxY
            val tw = 0.5f + 0.5f * sin(t * (1f + rnd(seed + i * 3 + 2) * 2f) + i)
            fill.alpha = (60 + tw * 180).toInt()
            canvas.drawCircle(x, y, 1.2f + rnd(seed + i) * 2.2f, fill)
        }
    }

    // =====================================================================
    // CYBERPUNK
    // =====================================================================

    private fun terminal(canvas: Canvas, t: Float, w: Float, h: Float, p: ColorPalette) {
        val lineH = h / 34f
        val scroll = t * 42f
        val firstLine = floor(scroll / lineH).toInt()
        val offset = scroll % lineH
        val gutter = w * 0.11f
        val blockH = lineH * 0.42f

        // gutter separator
        stroke.color = p.tertiary
        stroke.alpha = 160
        stroke.strokeWidth = 2f
        canvas.drawLine(gutter, 0f, gutter, h, stroke)

        val visible = (h / lineH).toInt() + 2
        for (row in 0 until visible) {
            val line = firstLine + row
            val y = row * lineH - offset + lineH * 0.5f
            // line number block
            fill.color = p.tertiary
            fill.alpha = 200
            val digits = 1 + (line % 1000).toString().length
            canvas.drawRect(gutter - 14f - digits * w * 0.012f, y - blockH * 0.4f, gutter - 14f, y + blockH * 0.4f, fill)

            // indentation + "tokens"
            val indent = (rnd(line * 7) * 4f).toInt()
            var x = gutter + 26f + indent * w * 0.045f
            val tokens = 1 + (rnd(line * 7 + 1) * 6f).toInt()
            if (rnd(line * 7 + 2) < 0.12f) continue // blank line
            for (k in 0 until tokens) {
                val tw = w * (0.03f + rnd(line * 31 + k) * 0.13f)
                if (x + tw > w - 30f) break
                val kind = rnd(line * 53 + k * 5)
                fill.color = when {
                    k == 0 && kind < 0.5f -> p.secondary
                    kind < 0.15f -> p.accent
                    else -> p.primary
                }
                fill.alpha = if (kind < 0.15f) 255 else 190
                canvas.drawRoundRect(x, y - blockH * 0.5f, x + tw, y + blockH * 0.5f, 4f, 4f, fill)
                x += tw + w * 0.018f
            }
        }

        // blinking cursor on the last full line
        if (sin(t * 6f) > 0f) {
            val cy = h - lineH * 1.5f
            fill.color = p.accent
            fill.alpha = 255
            canvas.drawRect(gutter + 26f, cy - lineH * 0.35f, gutter + 26f + w * 0.03f, cy + lineH * 0.35f, fill)
        }

        // CRT scanlines + vignette glow
        stroke.color = Color.BLACK
        stroke.alpha = 70
        stroke.strokeWidth = 2f
        var sy = 0f
        while (sy < h) {
            canvas.drawLine(0f, sy, w, sy, stroke)
            sy += 6f
        }
        glow(canvas, w * 0.5f, h * 0.5f, h * 0.7f, p.primary, 26)
    }

    private fun tunnel(canvas: Canvas, t: Float, w: Float, h: Float, tx: Float, ty: Float, touching: Boolean, p: ColorPalette) {
        val cx = w * 0.5f + (if (touching && tx >= 0) (tx - w * 0.5f) * 0.25f else sin(t * 0.4f) * w * 0.05f)
        val cy = h * 0.5f + (if (touching && ty >= 0) (ty - h * 0.5f) * 0.25f else cos(t * 0.3f) * h * 0.03f)
        glow(canvas, cx, cy, w * 0.35f, p.accent, 90)
        val n = 18
        val maxS = sqrt(w * w + h * h) * 0.75f
        for (j in 0 until n) {
            // draw from far to near
            val z = ((j + t * 0.9f) % n) / n
            val s = 12f + z * z * maxS
            val rot = t * 18f + (1f - z) * 140f
            stroke.color = if ((j % 2) == 0) p.primary else p.secondary
            stroke.alpha = (z * 255).toInt().coerceIn(10, 255)
            stroke.strokeWidth = 1.5f + z * 10f
            canvas.save()
            canvas.translate(lerp(w * 0.5f, cx, 1f - z), lerp(h * 0.5f, cy, 1f - z))
            canvas.rotate(rot)
            canvas.drawRoundRect(-s, -s * 0.62f, s, s * 0.62f, s * 0.08f, s * 0.08f, stroke)
            canvas.restore()
        }
        // speed streaks
        stroke.strokeWidth = 2f
        for (i in 0 until 40) {
            val a = rnd(i) * 2f * PI.toFloat()
            val f = (t * (0.6f + rnd(i + 99)) + rnd(i + 7)) % 1f
            val r0 = f * f * maxS
            val r1 = r0 + 30f + f * 120f
            stroke.color = p.tertiary
            stroke.alpha = (f * 200).toInt()
            canvas.drawLine(cx + cos(a) * r0, cy + sin(a) * r0, cx + cos(a) * r1, cy + sin(a) * r1, stroke)
        }
    }

    private fun hexShield(canvas: Canvas, t: Float, w: Float, h: Float, tx: Float, ty: Float, touching: Boolean, p: ColorPalette) {
        val r = w / 15f
        if (r != hexRadius) {
            hexRadius = r
            hexPath.reset()
            for (s in 0..5) {
                val a = (PI / 3.0 * s + PI / 6.0).toFloat()
                val px = cos(a) * r * 0.92f
                val py = sin(a) * r * 0.92f
                if (s == 0) hexPath.moveTo(px, py) else hexPath.lineTo(px, py)
            }
            hexPath.close()
        }
        val hw = sqrt(3f) * r
        val vh = 1.5f * r
        val cols = (w / hw).toInt() + 2
        val rows = (h / vh).toInt() + 2
        val ox = if (touching && tx >= 0) tx else w * 0.5f + sin(t * 0.35f) * w * 0.3f
        val oy = if (touching && ty >= 0) ty else h * 0.5f + cos(t * 0.27f) * h * 0.3f
        stroke.strokeWidth = 2.5f
        for (row in -1 until rows) {
            for (col in -1 until cols) {
                val x = col * hw + (if (row % 2 != 0) hw * 0.5f else 0f)
                val y = row * vh
                val dx = x - ox
                val dy = y - oy
                val d = sqrt(dx * dx + dy * dy)
                val wave = max(0f, sin(d * 0.011f - t * 3.2f))
                val energy = wave * wave * wave * wave
                canvas.save()
                canvas.translate(x, y)
                if (energy > 0.02f) {
                    fill.color = lerpColor(p.tertiary, p.primary, energy)
                    fill.alpha = (energy * 170).toInt()
                    canvas.drawPath(hexPath, fill)
                }
                stroke.color = lerpColor(p.tertiary, p.secondary, energy)
                stroke.alpha = (70 + energy * 185).toInt()
                canvas.drawPath(hexPath, stroke)
                canvas.restore()
            }
        }
        glow(canvas, ox, oy, w * 0.18f, p.accent, 120)
    }

    // =====================================================================
    // GEOMETRY
    // =====================================================================

    private fun spirograph(canvas: Canvas, t: Float, w: Float, h: Float, p: ColorPalette) {
        val cx = w * 0.5f
        val cy = h * 0.5f
        glow(canvas, cx, cy, w * 0.55f, p.secondary, 40)

        fun curve(rr: Float, laps: Int, d: Float, scale: Float, rot: Float, color: Int, alpha: Int, width: Float) {
            path.reset()
            val steps = 720
            val k = (1f - rr) / rr
            val total = laps * 2f * PI.toFloat()
            var lastX = 0f
            var lastY = 0f
            for (i in 0..steps) {
                val th = i / steps.toFloat() * total
                val x = (1f - rr) * cos(th) + d * cos(k * th)
                val y = (1f - rr) * sin(th) - d * sin(k * th)
                val xr = x * cos(rot) - y * sin(rot)
                val yr = x * sin(rot) + y * cos(rot)
                lastX = cx + xr * scale
                lastY = cy + yr * scale
                if (i == 0) path.moveTo(lastX, lastY) else path.lineTo(lastX, lastY)
            }
            stroke.color = color
            stroke.alpha = alpha
            stroke.strokeWidth = width
            canvas.drawPath(path, stroke)
        }

        val d1 = 0.45f + 0.15f * sin(t * 0.35f)
        curve(3f / 8f, 3, d1, w * 0.44f, t * 0.12f, p.primary, 220, 2.6f)
        val d2 = 0.30f + 0.12f * cos(t * 0.27f)
        curve(2f / 5f, 2, d2, w * 0.30f, -t * 0.2f, p.accent, 200, 2f)
        curve(1f / 3f, 1, 0.25f, w * 0.14f, t * 0.5f, p.tertiary, 230, 2f)

        // glowing pen
        val th = t * 1.3f
        val rr = 3f / 8f
        val k = (1f - rr) / rr
        val x = (1f - rr) * cos(th) + d1 * cos(k * th)
        val y = (1f - rr) * sin(th) - d1 * sin(k * th)
        val rot = t * 0.12f
        val px = cx + (x * cos(rot) - y * sin(rot)) * w * 0.44f
        val py = cy + (x * sin(rot) + y * cos(rot)) * w * 0.44f
        glow(canvas, px, py, 60f, p.tertiary, 255)
        fill.color = Color.WHITE
        fill.alpha = 255
        canvas.drawCircle(px, py, 6f, fill)
    }

    private fun truchet(canvas: Canvas, t: Float, w: Float, h: Float, p: ColorPalette) {
        val s = w / 9f
        val cols = (w / s).toInt() + 1
        val rows = (h / s).toInt() + 1
        stroke.strokeWidth = s * 0.16f
        stroke.strokeCap = Paint.Cap.BUTT
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val i = r * 31 + c
                val base = if (rnd(i) < 0.5f) 0f else 90f
                // each tile flips periodically with an eased rotation
                val ph = (t * 0.12f + rnd(i + 500)) % 1f
                val flip = smooth(0f, 0.12f, ph)
                val cycles = floor(t * 0.12f + rnd(i + 500)).toInt()
                val angle = base + (cycles % 2) * 90f + flip * 90f
                val x = c * s + s * 0.5f
                val y = r * s + s * 0.5f
                val f = (y / h + 0.25f * sin(t * 0.3f + x / w * 3f)).coerceIn(0f, 1f)
                stroke.color = lerpColor(p.primary, p.accent, f)
                stroke.alpha = 230
                canvas.save()
                canvas.translate(x, y)
                canvas.rotate(angle)
                val hs = s * 0.5f
                canvas.drawArc(-hs - hs, -hs - hs, -hs + hs, -hs + hs, 0f, 90f, false, stroke)
                canvas.drawArc(hs - hs, hs - hs, hs + hs, hs + hs, 180f, 90f, false, stroke)
                canvas.restore()
            }
        }
        stroke.strokeCap = Paint.Cap.ROUND
    }

    private fun phyllotaxis(canvas: Canvas, t: Float, w: Float, h: Float, p: ColorPalette) {
        val cx = w * 0.5f
        val cy = h * 0.5f
        val n = 700
        val c = w * 0.0175f
        val golden = 137.50776f * (PI.toFloat() / 180f)
        glow(canvas, cx, cy, w * 0.5f, p.primary, 50)
        val spin = t * 0.08f
        for (i in 1..n) {
            val a = i * golden + spin
            val r = c * sqrt(i.toFloat())
            val x = cx + cos(a) * r
            val y = cy + sin(a) * r
            val pulse = 0.5f + 0.5f * sin(i * 0.045f - t * 2.4f)
            val size = (2f + sqrt(i.toFloat()) * 0.42f) * (0.7f + 0.6f * pulse)
            val f = i / n.toFloat()
            fill.color = if (pulse > 0.92f) p.accent else lerpColor(p.tertiary, p.secondary, f)
            fill.alpha = (150 + pulse * 105).toInt()
            canvas.drawCircle(x, y, size, fill)
        }
    }

    private fun prismMosaic(canvas: Canvas, t: Float, w: Float, h: Float, p: ColorPalette) {
        val s = w / 7f
        val th = s * 0.866f
        val rows = (h / th).toInt() + 2
        val cols = (w / s).toInt() + 3
        stroke.strokeWidth = 2f
        for (r in 0 until rows) {
            val y0 = r * th
            val shift = if (r % 2 == 0) 0f else s * 0.5f
            for (c in -1 until cols) {
                for (up in 0..1) {
                    val x0 = c * s + shift + up * s * 0.5f
                    path.reset()
                    if (up == 0) {
                        path.moveTo(x0, y0 + th); path.lineTo(x0 + s * 0.5f, y0); path.lineTo(x0 + s, y0 + th)
                    } else {
                        path.moveTo(x0, y0); path.lineTo(x0 + s, y0); path.lineTo(x0 + s * 0.5f, y0 + th)
                    }
                    path.close()
                    val mx = x0 + s * 0.5f
                    val my = y0 + th * 0.5f
                    val v = 0.5f + 0.25f * sin(mx * 0.004f + t * 0.7f) + 0.25f * cos(my * 0.003f - t * 0.5f + up)
                    val band = (v * 4f).toInt().coerceIn(0, 3)
                    fill.color = when (band) {
                        0 -> shade(p.secondary, 0.35f)
                        1 -> shade(p.primary, 0.6f)
                        2 -> p.primary
                        else -> p.accent
                    }
                    fill.alpha = (110 + v * 120).toInt().coerceIn(0, 255)
                    canvas.drawPath(path, fill)
                    stroke.color = p.background
                    stroke.alpha = 255
                    canvas.drawPath(path, stroke)
                }
            }
        }
    }

    // =====================================================================
    // FLUID
    // =====================================================================

    private fun lavaLamp(canvas: Canvas, t: Float, w: Float, h: Float, tx: Float, ty: Float, touching: Boolean, p: ColorPalette) {
        verticalBackground(canvas, "lava", w, h, shade(p.tertiary, 0.35f), shade(p.secondary, 0.25f))
        val blobs = 8
        for (i in 0 until blobs) {
            val speed = 0.05f + rnd(i) * 0.05f
            val phase = (t * speed + rnd(i + 20)) % 1f
            // rise then sink: triangle wave eased
            val tri = if (phase < 0.5f) phase * 2f else 2f - phase * 2f
            val e = tri * tri * (3f - 2f * tri)
            var x = w * (0.2f + 0.6f * rnd(i + 40)) + sin(t * 0.5f + i) * w * 0.06f
            var y = lerp(h * 0.92f, h * 0.1f, e)
            if (touching && tx >= 0) {
                x = lerp(x, tx, 0.15f)
                y = lerp(y, ty, 0.15f)
            }
            val r = w * (0.09f + rnd(i + 60) * 0.1f) * (1f + 0.08f * sin(t * 1.3f + i))
            val col = if (i % 2 == 0) p.primary else p.secondary
            glow(canvas, x, y, r * 2.4f, col, 120)
            fill.color = lerpColor(col, p.accent, 0.25f)
            fill.alpha = 235
            canvas.drawOval(x - r, y - r * (1.05f + 0.1f * sin(t + i)), x + r, y + r * (1.05f - 0.1f * sin(t + i)), fill)
            fill.color = p.accent
            fill.alpha = 90
            canvas.drawCircle(x - r * 0.35f, y - r * 0.4f, r * 0.28f, fill)
        }
        // pooled wax at the bottom
        path.reset()
        path.moveTo(0f, h)
        var x = 0f
        while (x <= w) {
            path.lineTo(x, h * 0.95f + sin(x * 0.01f + t) * 14f)
            x += 30f
        }
        path.lineTo(w, h)
        path.close()
        fill.color = p.primary
        fill.alpha = 220
        canvas.drawPath(path, fill)
    }

    private fun inkBloom(canvas: Canvas, t: Float, w: Float, h: Float, p: ColorPalette) {
        val colors = intArrayOf(p.primary, p.secondary, p.tertiary, p.accent)
        val sources = 5
        for (s in 0 until sources) {
            val cx = w * (0.15f + 0.7f * rnd(s * 11))
            val cy = h * (0.12f + 0.76f * rnd(s * 11 + 1))
            val life = (t * 0.07f + rnd(s * 11 + 2)) % 1f
            val maxR = w * (0.35f + 0.2f * rnd(s * 11 + 3))
            val color = colors[s % colors.size]
            for (ring in 0 until 6) {
                val rf = (life - ring * 0.06f)
                if (rf <= 0f) continue
                val radius = maxR * sqrt(rf)
                val fade = (1f - rf).coerceIn(0f, 1f)
                path.reset()
                val pts = 64
                for (k in 0..pts) {
                    val a = k / pts.toFloat() * 2f * PI.toFloat()
                    val wob = 1f + 0.07f * sin(a * 5f + s * 2f + ring) + 0.04f * sin(a * 9f - t * 0.6f + s)
                    val x = cx + cos(a) * radius * wob
                    val y = cy + sin(a) * radius * wob
                    if (k == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                fill.color = color
                fill.alpha = (fade * 38).toInt()
                canvas.drawPath(path, fill)
                stroke.color = color
                stroke.strokeWidth = 2.5f
                stroke.alpha = (fade * 150).toInt()
                canvas.drawPath(path, stroke)
            }
        }
    }

    private fun flowField(canvas: Canvas, t: Float, w: Float, h: Float, p: ColorPalette) {
        val lines = 170
        val steps = 34
        val stepLen = w * 0.013f
        stroke.strokeWidth = 2f
        for (i in 0 until lines) {
            var x = rnd(i * 2) * w * 1.2f - w * 0.1f
            var y = rnd(i * 2 + 1) * h * 1.1f - h * 0.05f
            path.reset()
            path.moveTo(x, y)
            for (s in 0 until steps) {
                val a = sin(x * 0.0026f + t * 0.25f) * 1.8f + cos(y * 0.0021f - t * 0.18f) * 1.8f + sin((x + y) * 0.001f + t * 0.1f)
                x += cos(a) * stepLen
                y += sin(a) * stepLen
                path.lineTo(x, y)
            }
            val f = rnd(i * 2 + 7)
            stroke.color = when {
                f < 0.08f -> p.accent
                f < 0.5f -> p.primary
                else -> p.secondary
            }
            stroke.alpha = if (f < 0.08f) 230 else 120
            canvas.drawPath(path, stroke)
        }
    }

    // =====================================================================
    // SPACE
    // =====================================================================

    private fun orrery(canvas: Canvas, t: Float, w: Float, h: Float, p: ColorPalette) {
        stars(canvas, t, w, h, 120, h, Color.WHITE, 900)
        val cx = w * 0.5f
        val cy = h * 0.5f
        val tilt = 0.42f
        glow(canvas, cx, cy, w * 0.3f, p.primary, 200)
        fill.color = p.accent
        fill.alpha = 255
        canvas.drawCircle(cx, cy, w * 0.055f, fill)

        val planets = 6
        val pColors = intArrayOf(p.tertiary, p.secondary, Color.rgb(96, 165, 250), Color.rgb(248, 113, 113), Color.rgb(253, 224, 71), Color.rgb(165, 180, 252))
        for (k in 0 until planets) {
            val orbit = w * (0.12f + k * 0.065f)
            stroke.color = Color.WHITE
            stroke.alpha = 45
            stroke.strokeWidth = 1.5f
            canvas.drawOval(cx - orbit, cy - orbit * tilt, cx + orbit, cy + orbit * tilt, stroke)
            val period = 0.9f / (1f + k * 0.55f)
            val a = t * period + rnd(k * 9) * 6.28f
            val px = cx + cos(a) * orbit
            val py = cy + sin(a) * orbit * tilt
            val behind = sin(a) < 0f
            val pr = w * (0.012f + rnd(k * 9 + 1) * 0.018f) * (if (behind) 0.85f else 1.1f)
            fill.color = pColors[k]
            fill.alpha = if (behind) 170 else 255
            canvas.drawCircle(px, py, pr, fill)
            // terminator shading facing away from the sun
            fill.color = Color.BLACK
            fill.alpha = 110
            val dx = (px - cx)
            val dy = (py - cy)
            val dl = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
            canvas.drawCircle(px + dx / dl * pr * 0.45f, py + dy / dl * pr * 0.45f, pr * 0.8f, fill)
            if (k == 4) {
                stroke.color = pColors[k]
                stroke.alpha = 200
                stroke.strokeWidth = 2f
                canvas.drawOval(px - pr * 2f, py - pr * 0.6f, px + pr * 2f, py + pr * 0.6f, stroke)
            }
        }
    }

    private fun meteorShower(canvas: Canvas, t: Float, w: Float, h: Float, p: ColorPalette) {
        verticalBackground(canvas, "meteor", w, h, p.background, shade(p.tertiary, 0.8f))
        stars(canvas, t, w, h, 160, h * 0.8f, Color.WHITE, 300)
        val n = 12
        for (i in 0 until n) {
            val period = 2.2f + rnd(i) * 2.5f
            val ph = ((t + rnd(i + 3) * period) % period) / period
            if (ph > 0.45f) continue
            val f = ph / 0.45f
            val sx = w * (0.2f + 1.0f * rnd(i + 10))
            val sy = h * (0.02f + 0.3f * rnd(i + 20))
            val len = w * (0.25f + 0.2f * rnd(i + 30))
            val ang = (PI * 0.75).toFloat() + (rnd(i + 40) - 0.5f) * 0.2f
            val hx = sx + cos(ang) * len * 1.6f * f
            val hy = sy + sin(ang) * len * 1.6f * f
            val fadeIn = (1f - abs(f - 0.5f) * 2f).coerceIn(0f, 1f)
            val segs = 10
            for (s in 0 until segs) {
                val a0 = s / segs.toFloat()
                val a1 = (s + 1) / segs.toFloat()
                stroke.color = if (s < 2) Color.WHITE else p.secondary
                stroke.strokeWidth = 4.5f * (1f - a0) + 0.5f
                stroke.alpha = ((1f - a0) * 255 * fadeIn).toInt()
                canvas.drawLine(hx - cos(ang) * len * a0, hy - sin(ang) * len * a0, hx - cos(ang) * len * a1, hy - sin(ang) * len * a1, stroke)
            }
            glow(canvas, hx, hy, 30f, p.primary, (fadeIn * 255).toInt())
        }
        // mountain ridges
        for (layer in 0..1) {
            path.reset()
            val base = h * (0.82f + layer * 0.07f)
            path.moveTo(0f, h)
            var x = 0f
            while (x <= w + 40f) {
                val y = base - (sin(x * 0.004f + layer * 2f) * 0.5f + 0.5f) * h * 0.08f - (sin(x * 0.013f + layer) * 0.5f + 0.5f) * h * 0.03f
                path.lineTo(x, y)
                x += 40f
            }
            path.lineTo(w, h)
            path.close()
            fill.color = if (layer == 0) shade(p.tertiary, 0.6f) else Color.rgb(2, 4, 8)
            fill.alpha = 255
            canvas.drawPath(path, fill)
        }
    }

    private fun lunarCycle(canvas: Canvas, t: Float, w: Float, h: Float, p: ColorPalette) {
        stars(canvas, t, w, h, 140, h, Color.WHITE, 77)
        val cx = w * 0.5f
        val cy = h * 0.42f
        val r = w * 0.3f
        val phase = (t * 0.03f) % 1f
        glow(canvas, cx, cy, r * 2.2f, p.primary, (40 + 60 * (1f - abs(phase - 0.5f) * 2f)).toInt())

        // earthshine disk
        fill.color = shade(p.primary, 0.18f)
        fill.alpha = 255
        canvas.drawCircle(cx, cy, r, fill)

        // lit region
        val s = if (phase < 0.5f) 1f else -1f
        val k = cos(2f * PI.toFloat() * phase)
        path.reset()
        val steps = 48
        for (i in 0..steps) {
            val phi = -PI.toFloat() / 2f + PI.toFloat() * i / steps
            val x = cx + s * r * cos(phi)
            val y = cy + r * sin(phi)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        for (i in steps downTo 0) {
            val phi = -PI.toFloat() / 2f + PI.toFloat() * i / steps
            path.lineTo(cx + s * r * k * cos(phi), cy + r * sin(phi))
        }
        path.close()
        fill.color = p.accent
        fill.alpha = 255
        canvas.drawPath(path, fill)

        // subtle maria (drawn faintly over everything)
        fill.color = p.primary
        fill.alpha = 45
        canvas.drawCircle(cx - r * 0.25f, cy - r * 0.2f, r * 0.22f, fill)
        canvas.drawCircle(cx + r * 0.2f, cy + r * 0.15f, r * 0.16f, fill)
        canvas.drawCircle(cx + r * 0.05f, cy - r * 0.45f, r * 0.1f, fill)

        // phase timeline
        val ty = h * 0.8f
        val count = 8
        val spacing = w * 0.1f
        val startX = cx - spacing * (count - 1) / 2f
        for (i in 0 until count) {
            val x = startX + i * spacing
            val active = ((phase * count).toInt() % count) == i
            stroke.color = p.primary
            stroke.alpha = if (active) 255 else 90
            stroke.strokeWidth = 2f
            canvas.drawCircle(x, ty, w * 0.022f, stroke)
            if (active) {
                fill.color = p.accent
                fill.alpha = 255
                canvas.drawCircle(x, ty, w * 0.012f, fill)
            }
        }
    }

    private fun spiralGalaxy(canvas: Canvas, t: Float, w: Float, h: Float, p: ColorPalette) {
        stars(canvas, t, w, h, 90, h, Color.WHITE, 4242)
        val cx = w * 0.5f
        val cy = h * 0.5f
        val radius = w * 0.62f
        glow(canvas, cx, cy, w * 0.5f, p.primary, 110)
        canvas.save()
        canvas.translate(cx, cy)
        canvas.rotate(-24f)
        val n = 950
        for (i in 0 until n) {
            val arm = i % 2
            val d = rnd(i * 3).pow(0.65f)
            val jitter = (rnd(i * 3 + 1) - 0.5f) * (0.55f - d * 0.25f)
            val ang = d * 7.5f + arm * PI.toFloat() + jitter + t * 0.18f * (1.15f - d)
            val rr = d * radius
            val x = cos(ang) * rr
            val y = sin(ang) * rr * 0.55f
            fill.color = when {
                d < 0.18f -> p.accent
                rnd(i * 3 + 2) < 0.5f -> p.primary
                else -> p.secondary
            }
            fill.alpha = (255 * (1f - d * 0.6f)).toInt()
            canvas.drawCircle(x, y, 1.5f + (1f - d) * 3.5f * rnd(i + 17), fill)
        }
        canvas.restore()
        glow(canvas, cx, cy, w * 0.12f, p.accent, 255)
    }

    // =====================================================================
    // NATURE
    // =====================================================================

    private fun nightOcean(canvas: Canvas, t: Float, w: Float, h: Float, p: ColorPalette) {
        val horizon = h * 0.5f
        verticalBackground(canvas, "ocean", w, h, p.background, shade(p.primary, 0.45f))
        stars(canvas, t, w, h, 90, horizon * 0.9f, Color.WHITE, 555)
        val mx = w * 0.68f
        val my = h * 0.2f
        glow(canvas, mx, my, w * 0.45f, p.accent, 70)
        fill.color = p.accent
        fill.alpha = 255
        canvas.drawCircle(mx, my, w * 0.075f, fill)

        val layers = 10
        for (l in 0 until layers) {
            val f = l / (layers - 1f)
            val y0 = horizon + (h - horizon) * f.pow(1.6f)
            val amp = 6f + f * 40f
            path.reset()
            path.moveTo(0f, h)
            var x = 0f
            while (x <= w + 20f) {
                val y = y0 + sin(x * (0.012f - f * 0.008f) + t * (0.8f + l * 0.12f) + l) * amp + sin(x * 0.031f - t * 1.3f + l * 2f) * amp * 0.3f
                path.lineTo(x, y)
                x += 20f
            }
            path.lineTo(w, h)
            path.close()
            fill.color = lerpColor(shade(p.primary, 0.5f), shade(p.tertiary, 0.6f), f)
            fill.alpha = 255
            canvas.drawPath(path, fill)
            stroke.color = p.secondary
            stroke.alpha = (40 + f * 60).toInt()
            stroke.strokeWidth = 2f
            canvas.drawPath(path, stroke)
        }

        // moon glitter path
        stroke.color = p.accent
        stroke.strokeWidth = 3f
        for (i in 0 until 70) {
            val f = rnd(i * 5)
            val y = horizon + (h - horizon) * f
            val spread = 20f + f * w * 0.18f
            val x = mx + (rnd(i * 5 + 1) - 0.5f) * spread * 2f + sin(t * 2f + i) * 10f
            val shimmer = 0.5f + 0.5f * sin(t * 4f + i * 1.7f)
            stroke.alpha = (shimmer * 220 * (1f - f * 0.5f)).toInt()
            val len = 8f + f * 40f
            canvas.drawLine(x - len, y, x + len, y, stroke)
        }
    }

    private fun snowfall(canvas: Canvas, t: Float, w: Float, h: Float, tx: Float, ty: Float, touching: Boolean, p: ColorPalette) {
        verticalBackground(canvas, "snow", w, h, p.background, shade(p.tertiary, 0.9f))
        glow(canvas, w * 0.25f, h * 0.18f, w * 0.5f, p.secondary, 40)

        // far snow
        drawSnowLayer(canvas, t, w, h, 0, 90, 1.6f, 25f, 120, tx, ty, touching, p)
        // mountains
        for (layer in 0..2) {
            path.reset()
            val base = h * (0.62f + layer * 0.1f)
            path.moveTo(0f, h)
            var x = 0f
            val peakH = h * (0.16f - layer * 0.035f)
            while (x <= w + 30f) {
                val u1 = x / w * (1.6f + layer * 0.9f) + layer * 0.37f
                val u2 = x / w * (4.1f + layer * 1.3f) + layer * 0.71f
                val tri1 = 1f - 2f * abs(u1 - floor(u1) - 0.5f)
                val tri2 = 1f - 2f * abs(u2 - floor(u2) - 0.5f)
                val y = base - (0.7f * tri1 + 0.3f * tri2) * peakH
                path.lineTo(x, y)
                x += 30f
            }
            path.lineTo(w, h)
            path.close()
            fill.color = lerpColor(shade(p.tertiary, 1f), Color.rgb(3, 6, 12), layer / 2f)
            fill.alpha = 255
            canvas.drawPath(path, fill)
            // snow caps
            if (layer == 0) {
                stroke.color = p.primary
                stroke.alpha = 120
                stroke.strokeWidth = 3f
                canvas.drawPath(path, stroke)
            }
        }
        // pines on the front ridge
        fill.color = Color.rgb(2, 5, 9)
        for (i in 0 until 14) {
            val x = w * rnd(i + 700)
            val base = h * (0.86f + rnd(i + 710) * 0.06f)
            val th = h * (0.06f + rnd(i + 720) * 0.07f)
            val tw = th * 0.38f
            path.reset()
            path.moveTo(x, base - th)
            path.lineTo(x - tw, base)
            path.lineTo(x + tw, base)
            path.close()
            canvas.drawPath(path, fill)
        }
        // mid + near snow
        drawSnowLayer(canvas, t, w, h, 1, 70, 3f, 55f, 190, tx, ty, touching, p)
        drawSnowLayer(canvas, t, w, h, 2, 35, 5.5f, 95f, 240, tx, ty, touching, p)
    }

    private fun drawSnowLayer(
        canvas: Canvas, t: Float, w: Float, h: Float, layer: Int, count: Int, size: Float,
        speed: Float, alpha: Int, tx: Float, ty: Float, touching: Boolean, p: ColorPalette
    ) {
        fill.color = p.accent
        fill.alpha = alpha
        for (i in 0 until count) {
            val seed = layer * 1000 + i
            var x = (rnd(seed) * w + sin(t * 0.6f + i) * 30f * (layer + 1)) % w
            val y = (rnd(seed + 1) * h + t * speed * (0.7f + rnd(seed + 2) * 0.6f)) % (h + 20f) - 10f
            if (touching && tx >= 0) {
                val dx = x - tx
                val dy = y - ty
                val d = sqrt(dx * dx + dy * dy)
                if (d < 220f && d > 1f) x += dx / d * (220f - d) * 0.5f
            }
            canvas.drawCircle(x, y, size * (0.7f + rnd(seed + 3) * 0.6f), fill)
        }
    }

    private fun bamboo(canvas: Canvas, t: Float, w: Float, h: Float, p: ColorPalette) {
        verticalBackground(canvas, "bamboo", w, h, shade(p.tertiary, 0.6f), p.background)
        val stalks = 11
        for (layer in 0..1) {
            for (i in 0 until stalks) {
                val seed = layer * 100 + i
                if (layer == 1 && i % 2 == 1) continue
                if (layer == 1 && rnd(seed + 9) < 0.25f) continue
                val baseX = w * (i + 0.5f) / stalks + (rnd(seed) - 0.5f) * w * 0.06f
                val thick = w * (if (layer == 0) 0.016f else 0.032f) * (0.8f + rnd(seed + 1) * 0.4f)
                val sway = sin(t * 0.7f + i * 0.6f) * (if (layer == 0) 0.012f else 0.02f)
                val segs = 7 + (rnd(seed + 2) * 4f).toInt()
                val segH = h * 1.1f / segs
                val col = if (layer == 0) shade(p.primary, 0.22f) else shade(p.primary, 0.5f)
                for (s in 0 until segs) {
                    val y1 = h - s * segH
                    val y0 = y1 - segH + 6f
                    val bend = sway * (h - y1) * (h - y1) / h
                    fill.color = col
                    fill.alpha = 255
                    canvas.drawRoundRect(baseX + bend - thick, y0, baseX + bend + thick, y1 - 4f, thick * 0.4f, thick * 0.4f, fill)
                    // node ring
                    fill.color = p.accent
                    fill.alpha = if (layer == 0) 40 else 90
                    canvas.drawRect(baseX + bend - thick * 1.1f, y0 - 3f, baseX + bend + thick * 1.1f, y0 + 3f, fill)
                    // leaves on some nodes
                    if (rnd(seed * 13 + s) < (if (layer == 1) 0.45f else 0.25f) && s > 1) {
                        for (lf in 0 until 3) {
                            val dir = if ((s + lf) % 2 == 0) 1f else -1f
                            val ang = dir * (25f + lf * 18f) + sin(t * 1.4f + s + lf) * 6f
                            canvas.save()
                            canvas.translate(baseX + bend + dir * thick, y0)
                            canvas.rotate(ang + (if (dir > 0) 0f else 180f))
                            fill.color = shade(p.secondary, if (layer == 1) 0.45f else 0.25f)
                            fill.alpha = 220
                            path2.reset()
                            path2.moveTo(0f, 0f)
                            path2.quadTo(thick * 2.6f, -thick * 0.55f, thick * 6f, 0f)
                            path2.quadTo(thick * 2.6f, thick * 0.55f, 0f, 0f)
                            path2.close()
                            canvas.drawPath(path2, fill)
                            canvas.restore()
                        }
                    }
                }
            }
            if (layer == 0) {
                // mist between layers
                glow(canvas, w * 0.5f, h * 0.7f, w * 0.9f, p.accent, 22)
            }
        }
    }

    private fun dandelion(canvas: Canvas, t: Float, w: Float, h: Float, p: ColorPalette) {
        val hx = w * 0.45f
        val hy = h * 0.58f
        // stem
        path.reset()
        path.moveTo(hx, hy)
        path.cubicTo(hx + w * 0.02f, hy + h * 0.15f, hx - w * 0.05f, hy + h * 0.28f, hx + w * 0.03f, h)
        stroke.color = p.primary
        stroke.alpha = 220
        stroke.strokeWidth = 6f
        canvas.drawPath(path, stroke)
        glow(canvas, hx, hy, w * 0.3f, p.accent, 50)

        val seeds = 70
        val len = w * 0.13f
        for (i in 0 until seeds) {
            val a = i / seeds.toFloat() * 2f * PI.toFloat() + rnd(i) * 0.1f
            val cycle = 26f
            val ph = ((t + rnd(i + 300) * cycle) % cycle) / cycle
            var sx: Float
            var sy: Float
            var alpha = 230
            val attached = ph < 0.55f
            if (attached) {
                sx = hx + cos(a) * len
                sy = hy + sin(a) * len
                stroke.color = p.accent
                stroke.alpha = 120
                stroke.strokeWidth = 1.5f
                canvas.drawLine(hx, hy, sx, sy, stroke)
            } else {
                val f = (ph - 0.55f) / 0.45f
                val startX = hx + cos(a) * len
                val startY = hy + sin(a) * len
                sx = startX + f * w * (0.4f + rnd(i + 5) * 0.6f) + sin(t * 1.5f + i) * 30f * f
                sy = startY - f * h * (0.35f + rnd(i + 6) * 0.4f)
                alpha = ((1f - f) * 230).toInt()
            }
            // pappus (little umbrella)
            stroke.color = p.accent
            stroke.alpha = alpha
            stroke.strokeWidth = 1.5f
            val outward = if (attached) a else -PI.toFloat() / 2f + sin(t + i) * 0.4f
            for (k in -3..3) {
                val fa = outward + k * 0.22f
                canvas.drawLine(sx, sy, sx + cos(fa) * 18f, sy + sin(fa) * 18f, stroke)
            }
            if (!attached) {
                stroke.alpha = alpha / 2
                canvas.drawLine(sx, sy, sx - cos(outward) * 26f, sy - sin(outward) * 26f, stroke)
            }
        }
        fill.color = p.tertiary
        fill.alpha = 255
        canvas.drawCircle(hx, hy, w * 0.018f, fill)
    }

    // =====================================================================
    // ZEN
    // =====================================================================

    private fun sandGarden(canvas: Canvas, t: Float, w: Float, h: Float, p: ColorPalette) {
        val sx = floatArrayOf(w * 0.3f, w * 0.7f, w * 0.42f)
        val sy = floatArrayOf(h * 0.3f, h * 0.55f, h * 0.78f)
        val sr = floatArrayOf(w * 0.08f, w * 0.11f, w * 0.06f)
        val clear = 5.2f
        stroke.strokeWidth = 2.5f
        // raked straight lines, broken around the stone rings
        val spacing = h / 70f
        var y = spacing * 0.5f
        while (y < h) {
            path.reset()
            var drawing = false
            var x = 0f
            while (x <= w) {
                var inside = false
                for (k in 0..2) {
                    val dx = x - sx[k]
                    val dy = y - sy[k]
                    if (dx * dx + dy * dy < (sr[k] * clear) * (sr[k] * clear)) { inside = true; break }
                }
                if (!inside) {
                    val yy = y + sin(x * 0.01f + y * 0.02f) * 2f
                    if (!drawing) { path.moveTo(x, yy); drawing = true } else path.lineTo(x, yy)
                } else drawing = false
                x += 18f
            }
            val shimmer = 0.5f + 0.5f * sin(y * 0.01f - t * 0.6f)
            stroke.color = p.primary
            stroke.alpha = (60 + shimmer * 70).toInt()
            canvas.drawPath(path, stroke)
            y += spacing
        }
        // concentric rings around stones
        for (k in 0..2) {
            var r = sr[k] * 1.4f
            var ring = 0
            while (r < sr[k] * clear) {
                val breathe = 1f + 0.015f * sin(t * 0.8f + ring * 0.6f + k)
                val shimmer = 0.5f + 0.5f * sin(ring * 0.8f - t * 0.9f)
                stroke.color = p.primary
                stroke.alpha = (70 + shimmer * 90).toInt()
                canvas.drawCircle(sx[k], sy[k], r * breathe, stroke)
                r += spacing
                ring++
            }
            // stone
            fill.color = shade(p.primary, 0.3f)
            fill.alpha = 255
            canvas.drawOval(sx[k] - sr[k], sy[k] - sr[k] * 0.8f, sx[k] + sr[k], sy[k] + sr[k] * 0.8f, fill)
            fill.color = p.accent
            fill.alpha = 40
            canvas.drawOval(sx[k] - sr[k] * 0.7f, sy[k] - sr[k] * 0.7f, sx[k] + sr[k] * 0.2f, sy[k] - sr[k] * 0.1f, fill)
        }
    }

    private fun breathe(canvas: Canvas, t: Float, w: Float, h: Float, p: ColorPalette) {
        val cx = w * 0.5f
        val cy = h * 0.48f
        // 16 second box breathing: inhale 4, hold 4, exhale 4, hold 4
        val cyc = (t % 16f)
        val b = when {
            cyc < 4f -> smooth(0f, 4f, cyc)
            cyc < 8f -> 1f
            cyc < 12f -> 1f - smooth(8f, 12f, cyc)
            else -> 0f
        }
        val maxR = w * 0.2f
        val petalR = maxR * (0.35f + 0.65f * b)
        val dist = petalR * (0.2f + 0.8f * b)
        glow(canvas, cx, cy, w * (0.3f + 0.25f * b), p.primary, 90)
        canvas.save()
        canvas.translate(cx, cy)
        canvas.rotate(b * 60f + t * 2f)
        for (i in 0 until 6) {
            val a = i * PI.toFloat() / 3f
            fill.color = if (i % 2 == 0) p.primary else p.secondary
            fill.alpha = 95
            canvas.drawCircle(cos(a) * dist, sin(a) * dist, petalR, fill)
        }
        canvas.restore()

        // progress ring of 64 dots
        val ringR = w * 0.42f
        val progress = cyc / 16f
        for (i in 0 until 64) {
            val a = -PI.toFloat() / 2f + i / 64f * 2f * PI.toFloat()
            val on = i / 64f <= progress
            fill.color = if (on) p.accent else p.tertiary
            fill.alpha = if (on) 230 else 80
            canvas.drawCircle(cx + cos(a) * ringR, cy + sin(a) * ringR, if (i % 16 == 0) 7f else 4f, fill)
        }
    }

    private fun lanterns(canvas: Canvas, t: Float, w: Float, h: Float, p: ColorPalette) {
        verticalBackground(canvas, "lantern", w, h, p.background, shade(p.tertiary, 0.9f))
        val river = h * 0.82f
        // river
        fill.color = Color.rgb(4, 3, 10)
        fill.alpha = 255
        canvas.drawRect(0f, river, w, h, fill)
        val n = 26
        for (i in 0 until n) {
            val depth = 0.35f + rnd(i) * 0.65f
            val speed = 0.018f + depth * 0.02f
            val ph = (t * speed + rnd(i + 50)) % 1f
            val x = w * rnd(i + 100) + sin(t * 0.5f + i) * 25f * depth
            val y = lerp(river - 20f, -h * 0.1f, ph)
            val s = w * 0.045f * depth
            val flicker = 0.85f + 0.15f * sin(t * 9f + i * 3f)
            glow(canvas, x, y, s * 3.2f, p.primary, (150 * depth * flicker).toInt())
            fill.color = lerpColor(p.secondary, p.primary, 0.5f + 0.5f * flicker)
            fill.alpha = (255 * (0.5f + depth * 0.5f)).toInt()
            canvas.drawRoundRect(x - s * 0.7f, y - s, x + s * 0.7f, y + s, s * 0.35f, s * 0.35f, fill)
            fill.color = p.accent
            fill.alpha = (200 * flicker).toInt()
            canvas.drawOval(x - s * 0.35f, y - s * 0.5f, x + s * 0.35f, y + s * 0.6f, fill)
            fill.color = Color.rgb(40, 20, 10)
            fill.alpha = 220
            canvas.drawRect(x - s * 0.45f, y - s - s * 0.15f, x + s * 0.45f, y - s, fill)
            // reflection on river
            if (depth > 0.6f) {
                val ry = river + (river - y) * 0.08f + 20f
                if (ry < h) {
                    fill.color = p.primary
                    fill.alpha = (60 * depth).toInt()
                    canvas.drawOval(x - s * 0.8f, ry, x + s * 0.8f, ry + s * 0.25f, fill)
                }
            }
        }
        stroke.color = p.primary
        stroke.alpha = 60
        stroke.strokeWidth = 2f
        canvas.drawLine(0f, river, w, river, stroke)
    }

    // =====================================================================
    // CHROMATIC
    // =====================================================================

    private fun equalizer(canvas: Canvas, t: Float, dt: Float, w: Float, h: Float, p: ColorPalette) {
        val bars = 28
        val cy = h * 0.55f
        val gap = w * 0.008f
        val bw = (w - gap * (bars + 1)) / bars
        val maxH = h * 0.32f
        for (i in 0 until bars) {
            val f = i / (bars - 1f)
            val v = (0.35f + 0.25f * sin(t * 3.1f + i * 0.45f) + 0.2f * sin(t * 5.3f - i * 0.8f) +
                0.2f * sin(t * 1.7f + i * i * 0.07f)) * (1f - abs(f - 0.5f) * 0.6f)
            val bh = (v.coerceIn(0.03f, 1f)) * maxH
            if (bh > peaks[i]) peaks[i] = bh else peaks[i] = (peaks[i] - maxH * 0.35f * dt).coerceAtLeast(bh)
            val x = gap + i * (bw + gap)
            val col = when {
                f < 0.5f -> lerpColor(p.tertiary, p.secondary, f * 2f)
                else -> lerpColor(p.secondary, p.primary, (f - 0.5f) * 2f)
            }
            // stacked LED segments
            val seg = maxH / 22f
            var sy = 0f
            while (sy < bh) {
                fill.color = col
                fill.alpha = 235
                canvas.drawRoundRect(x, cy - sy - seg + 3f, x + bw, cy - sy, 3f, 3f, fill)
                fill.alpha = 60
                canvas.drawRoundRect(x, cy + sy + 6f, x + bw, cy + sy + seg + 3f, 3f, 3f, fill)
                sy += seg
            }
            fill.color = p.accent
            fill.alpha = 255
            canvas.drawRect(x, cy - peaks[i] - seg - 6f, x + bw, cy - peaks[i] - seg + 2f, fill)
        }
        glow(canvas, w * 0.5f, cy, w * 0.7f, p.secondary, 35)
        stroke.color = p.accent
        stroke.alpha = 120
        stroke.strokeWidth = 2f
        canvas.drawLine(0f, cy + 3f, w, cy + 3f, stroke)
    }

    private fun vasarely(canvas: Canvas, t: Float, w: Float, h: Float, tx: Float, ty: Float, touching: Boolean, p: ColorPalette) {
        val cols = 12
        val s = w / cols
        val rows = (h / s).toInt() + 2
        val lx = if (touching && tx >= 0) tx else w * 0.5f + sin(t * 0.45f) * w * 0.28f
        val ly = if (touching && ty >= 0) ty else h * 0.5f + sin(t * 0.31f) * h * 0.25f
        val sigma = w * 0.38f
        val strength = 0.55f

        fun wx(x: Float, y: Float): Float {
            val dx = x - lx; val dy = y - ly
            return x + dx * strength * exp(-(dx * dx + dy * dy) / (sigma * sigma))
        }
        fun wy(x: Float, y: Float): Float {
            val dx = x - lx; val dy = y - ly
            return y + dy * strength * exp(-(dx * dx + dy * dy) / (sigma * sigma))
        }

        fill.color = p.background
        fill.alpha = 255
        canvas.drawRect(0f, 0f, w, h, fill)
        for (r in -1 until rows) {
            for (c in -1..cols) {
                if (((r + c) and 1) != 0) continue
                val x0 = c * s; val y0 = r * s; val x1 = x0 + s; val y1 = y0 + s
                path.reset()
                path.moveTo(wx(x0, y0), wy(x0, y0))
                path.lineTo(wx(x1, y0), wy(x1, y0))
                path.lineTo(wx(x1, y1), wy(x1, y1))
                path.lineTo(wx(x0, y1), wy(x0, y1))
                path.close()
                val dx = x0 + s * 0.5f - lx
                val dy = y0 + s * 0.5f - ly
                val near = exp(-(dx * dx + dy * dy) / (sigma * sigma))
                fill.color = lerpColor(p.primary, if (near > 0.5f) p.secondary else p.tertiary, near)
                fill.alpha = 255
                canvas.drawPath(path, fill)
            }
        }
    }

    private fun chromaWheel(canvas: Canvas, t: Float, w: Float, h: Float, p: ColorPalette) {
        val cx = w * 0.5f
        val cy = h * 0.5f
        val rings = 7
        val inner = w * 0.08f
        val ringW = w * 0.058f
        stroke.strokeCap = Paint.Cap.BUTT
        for (r in 0 until rings) {
            val segs = 10 + r * 4
            val rad = inner + r * ringW * 1.15f + ringW * 0.5f
            val dir = if (r % 2 == 0) 1f else -1f
            val rot = t * (12f + r * 3f) * dir
            stroke.strokeWidth = ringW
            rect.set(cx - rad, cy - rad, cx + rad, cy + rad)
            val sweep = 360f / segs
            for (s in 0 until segs) {
                hsv[0] = ((s.toFloat() / segs) * 360f + t * 20f + r * 25f) % 360f
                hsv[1] = 0.85f
                hsv[2] = 1f
                stroke.color = Color.HSVToColor(hsv)
                stroke.alpha = if ((s + r) % 3 == 0) 255 else 170
                canvas.drawArc(rect, rot + s * sweep, sweep * 0.82f, false, stroke)
            }
        }
        stroke.strokeCap = Paint.Cap.ROUND
        fill.color = p.primary
        fill.alpha = 255
        canvas.drawCircle(cx, cy, inner * 0.6f, fill)
        glow(canvas, cx, cy, inner * 1.6f, Color.WHITE, 120)
    }
}
