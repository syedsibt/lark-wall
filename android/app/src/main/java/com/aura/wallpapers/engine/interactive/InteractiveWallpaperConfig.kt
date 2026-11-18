package com.aura.wallpapers.engine.interactive

import java.util.Random

data class InteractiveWallpaperConfig(
    val presetId: String,
    val title: String,
    val family: EngineFamily,
    val archetype: String,
    val palette: ColorPalette,
    val particleCount: Int,
    val speed: Float,
    val complexity: Float,
    val symmetry: Int,
    val glowRadius: Float,
    val interactionType: InteractionType,
    val interactionStrength: Float,
    val seed: Long
) {
    companion object {
        const val PREFIX = "engine://interactive/"

        fun isInteractiveUri(uri: String): Boolean = uri.startsWith(PREFIX)

        fun extractPresetId(uri: String): String =
            if (uri.startsWith(PREFIX)) uri.removePrefix(PREFIX) else uri
    }
}

object InteractiveWallpaperRegistry {

    private val configsCache = HashMap<String, InteractiveWallpaperConfig>()

    /**
     * Resolves an interactive wallpaper configuration by preset ID.
     * Uses a deterministic algorithm so that a given ID always produces the exact same configuration,
     * maintaining high performance and reproducible graphics without requiring heavy file lookups.
     */
    fun getConfig(presetId: String): InteractiveWallpaperConfig {
        val cleanId = InteractiveWallpaperConfig.extractPresetId(presetId)
        return configsCache.getOrPut(cleanId) {
            buildConfigFromPresetId(cleanId)
        }
    }

    private fun buildConfigFromPresetId(presetId: String): InteractiveWallpaperConfig {
        val hash = (presetId.hashCode().toLong() and 0xFFFFFFFFL)
        val rng = Random(hash)

        // Determine family based on prefix or hash
        val family = when {
            presetId.startsWith("cyber_") -> EngineFamily.CYBERPUNK_NEON
            presetId.startsWith("geom_") -> EngineFamily.ABSTRACT_GEOMETRY
            presetId.startsWith("fluid_") -> EngineFamily.FLUID_LIQUID
            presetId.startsWith("space_") -> EngineFamily.CELESTIAL_SPACE
            presetId.startsWith("nature_") -> EngineFamily.BIOLUMINESCENT_NATURE
            presetId.startsWith("zen_") -> EngineFamily.MINIMAL_ZEN
            presetId.startsWith("chroma_") -> EngineFamily.BOLD_CHROMATIC
            else -> {
                val families = EngineFamily.values()
                families[(hash % families.size).toInt()]
            }
        }

        val archetype = when (family) {
            EngineFamily.CYBERPUNK_NEON -> {
                val archetypes = listOf("data_matrix", "circuit_net", "highway_streaks", "prismatic_hud", "cyber_rain")
                archetypes[(rng.nextInt(archetypes.size))]
            }
            EngineFamily.ABSTRACT_GEOMETRY -> {
                val archetypes = listOf("morphing_polyhedra", "kaleido_mandala", "voronoi_lattice", "isometric_cubes", "lissajous_curves")
                archetypes[(rng.nextInt(archetypes.size))]
            }
            EngineFamily.FLUID_LIQUID -> {
                val archetypes = listOf("liquid_chrome_waves", "magnetic_ferrofluid", "oil_slick_swirls", "biolum_plasma", "viscous_ripple_pool")
                archetypes[(rng.nextInt(archetypes.size))]
            }
            EngineFamily.CELESTIAL_SPACE -> {
                val archetypes = listOf("black_hole_lens", "warp_speed_starfield", "nebula_clouds", "orbital_rings", "solar_pulsar")
                archetypes[(rng.nextInt(archetypes.size))]
            }
            EngineFamily.BIOLUMINESCENT_NATURE -> {
                val archetypes = listOf("enchanted_fireflies", "sakura_tempest", "abyssal_jellyfish", "rainforest_rain", "aurora_curtains")
                archetypes[(rng.nextInt(archetypes.size))]
            }
            EngineFamily.MINIMAL_ZEN -> {
                val archetypes = listOf("breathing_mesh", "zen_ripples", "prismatic_glass", "monolith_balance")
                archetypes[(rng.nextInt(archetypes.size))]
            }
            EngineFamily.BOLD_CHROMATIC -> {
                val archetypes = listOf("kinetic_op_art", "chromatic_glitch", "raymarch_waves", "spectral_helix")
                archetypes[(rng.nextInt(archetypes.size))]
            }
        }

        val palette = when (family) {
            EngineFamily.CYBERPUNK_NEON -> {
                listOf(ColorPalette.CYBER_NEON, ColorPalette.TOKYO_NIGHT, ColorPalette.MATRIX_EMERALD, ColorPalette.SYNTHWAVE_SUNSET)[rng.nextInt(4)]
            }
            EngineFamily.ABSTRACT_GEOMETRY -> {
                listOf(ColorPalette.SACRED_GOLD, ColorPalette.PRISMATIC_OP, ColorPalette.CYBER_NEON, ColorPalette.DEEP_NEBULA)[rng.nextInt(4)]
            }
            EngineFamily.FLUID_LIQUID -> {
                listOf(ColorPalette.LIQUID_CHROME, ColorPalette.ABYSS_JELLYFISH, ColorPalette.DEEP_NEBULA, ColorPalette.CYBER_NEON)[rng.nextInt(4)]
            }
            EngineFamily.CELESTIAL_SPACE -> {
                listOf(ColorPalette.DEEP_NEBULA, ColorPalette.SOLAR_CORONA, ColorPalette.ABYSS_JELLYFISH, ColorPalette.SYNTHWAVE_SUNSET)[rng.nextInt(4)]
            }
            EngineFamily.BIOLUMINESCENT_NATURE -> {
                listOf(ColorPalette.ENCHANTED_FOREST, ColorPalette.SAKURA_MIST, ColorPalette.ABYSS_JELLYFISH, ColorPalette.MATRIX_EMERALD)[rng.nextInt(4)]
            }
            EngineFamily.MINIMAL_ZEN -> {
                listOf(ColorPalette.ZEN_TWILIGHT, ColorPalette.SAKURA_MIST, ColorPalette.SACRED_GOLD, ColorPalette.LIQUID_CHROME)[rng.nextInt(4)]
            }
            EngineFamily.BOLD_CHROMATIC -> {
                listOf(ColorPalette.PRISMATIC_OP, ColorPalette.SYNTHWAVE_SUNSET, ColorPalette.CYBER_NEON, ColorPalette.SOLAR_CORONA)[rng.nextInt(4)]
            }
        }

        val interactionType = when (family) {
            EngineFamily.CYBERPUNK_NEON -> listOf(InteractionType.LIGHT_TRAIL_GLOW, InteractionType.PULSE_WAVE, InteractionType.RIPPLE_DISPERSION)[rng.nextInt(3)]
            EngineFamily.ABSTRACT_GEOMETRY -> listOf(InteractionType.DEPTH_PARALLAX_TILT, InteractionType.VORTEX_SWIRL, InteractionType.PULSE_WAVE)[rng.nextInt(3)]
            EngineFamily.FLUID_LIQUID -> listOf(InteractionType.RIPPLE_DISPERSION, InteractionType.VORTEX_SWIRL, InteractionType.MAGNETIC_ATTRACTION)[rng.nextInt(3)]
            EngineFamily.CELESTIAL_SPACE -> listOf(InteractionType.GRAVITATIONAL_REPULSION, InteractionType.MAGNETIC_ATTRACTION, InteractionType.DEPTH_PARALLAX_TILT)[rng.nextInt(3)]
            EngineFamily.BIOLUMINESCENT_NATURE -> listOf(InteractionType.VORTEX_SWIRL, InteractionType.GRAVITATIONAL_REPULSION, InteractionType.LIGHT_TRAIL_GLOW)[rng.nextInt(3)]
            EngineFamily.MINIMAL_ZEN -> listOf(InteractionType.RIPPLE_DISPERSION, InteractionType.DEPTH_PARALLAX_TILT)[rng.nextInt(2)]
            EngineFamily.BOLD_CHROMATIC -> listOf(InteractionType.LIGHT_TRAIL_GLOW, InteractionType.PULSE_WAVE, InteractionType.VORTEX_SWIRL)[rng.nextInt(3)]
        }

        val particleCount = 40 + rng.nextInt(60) // 40..100 particles (super lightweight on mobile GPU)
        val speed = 0.6f + rng.nextFloat() * 0.8f // 0.6..1.4
        val complexity = 0.5f + rng.nextFloat() * 1.5f
        val symmetry = listOf(3, 4, 5, 6, 8)[rng.nextInt(5)]
        val glowRadius = 4f + rng.nextFloat() * 8f
        val interactionStrength = 0.8f + rng.nextFloat() * 0.6f

        val title = generateEvocativeTitle(presetId, family, archetype)

        return InteractiveWallpaperConfig(
            presetId = presetId,
            title = title,
            family = family,
            archetype = archetype,
            palette = palette,
            particleCount = particleCount,
            speed = speed,
            complexity = complexity,
            symmetry = symmetry,
            glowRadius = glowRadius,
            interactionType = interactionType,
            interactionStrength = interactionStrength,
            seed = hash
        )
    }

    private fun generateEvocativeTitle(presetId: String, family: EngineFamily, archetype: String): String {
        val parts = presetId.split("_")
        val num = parts.lastOrNull()?.toIntOrNull() ?: 1
        val numRoman = when (num % 10) {
            0 -> "X"
            1 -> "I"
            2 -> "II"
            3 -> "III"
            4 -> "IV"
            5 -> "V"
            6 -> "VI"
            7 -> "VII"
            8 -> "VIII"
            else -> "IX"
        }

        return when (archetype) {
            "data_matrix" -> "Matrix Cascade $numRoman"
            "circuit_net" -> "Cyber Pulse Net $numRoman"
            "highway_streaks" -> "Neon Horizon Warp $numRoman"
            "prismatic_hud" -> "Cyber Telemetry HUD $numRoman"
            "cyber_rain" -> "Neo Rain Reflex $numRoman"
            "morphing_polyhedra" -> "Hyper-Polyhedron $numRoman"
            "kaleido_mandala" -> "Sacred Mandala $numRoman"
            "voronoi_lattice" -> "Voronoi Prism $numRoman"
            "isometric_cubes" -> "Modular Prism Grid $numRoman"
            "lissajous_curves" -> "Harmonic Lissajous $numRoman"
            "liquid_chrome_waves" -> "Molten Chrome Flow $numRoman"
            "magnetic_ferrofluid" -> "Ferrofluid Spikes $numRoman"
            "oil_slick_swirls" -> "Prismatic Oil Swirl $numRoman"
            "biolum_plasma" -> "Abyssal Plasma $numRoman"
            "viscous_ripple_pool" -> "Liquid Ripple Basin $numRoman"
            "black_hole_lens" -> "Event Horizon Lens $numRoman"
            "warp_speed_starfield" -> "Warp Vector Starfield $numRoman"
            "nebula_clouds" -> "Cosmic Nebula Veil $numRoman"
            "orbital_rings" -> "Planetary Dust Ring $numRoman"
            "solar_pulsar" -> "Pulsar Emission Beam $numRoman"
            "enchanted_fireflies" -> "Firefly Canopy $numRoman"
            "sakura_tempest" -> "Sakura Vortex $numRoman"
            "abyssal_jellyfish" -> "Abyssal Bell Pulse $numRoman"
            "rainforest_rain" -> "Rainforest Twilight $numRoman"
            "aurora_curtains" -> "Ionosphere Aurora $numRoman"
            "breathing_mesh" -> "Harmonic Gradient Mesh $numRoman"
            "zen_ripples" -> "Zen Water Basin $numRoman"
            "prismatic_glass" -> "Spectral Glass Prism $numRoman"
            "monolith_balance" -> "Monolithic Float $numRoman"
            "kinetic_op_art" -> "Kinetic Moiré Grid $numRoman"
            "chromatic_glitch" -> "Chromatic Phase Shift $numRoman"
            "raymarch_waves" -> "Synthwave Terrain $numRoman"
            "spectral_helix" -> "Double Helix Lattice $numRoman"
            else -> "${family.displayName} #$num"
        }
    }
}
