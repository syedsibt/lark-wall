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

        val MOTION_LOOP_TITLES = mapOf(
        "ml_01_aurora_drift" to "Aurora Drift",
        "ml_02_liquid_chrome_bloom" to "Liquid Chrome Bloom",
        "ml_03_orbital_eclipse" to "Orbital Eclipse",
        "ml_04_prism_cascade" to "Prism Cascade",
        "ml_05_biolume_garden" to "Biolume Garden",
        "ml_06_dune_pulse" to "Dune Pulse",
        "ml_07_neon_rain" to "Neon Rain",
        "ml_08_celestial_dust" to "Celestial Dust",
        "ml_09_ribbon_infinity" to "Ribbon Infinity",
        "ml_10_solar_halo" to "Solar Halo",
        "ml_11_pixel_flora" to "Pixel Flora",
        "ml_12_magnetic_field" to "Magnetic Field",
        "ml_13_y2k_halo_orbit" to "Y2K Halo Orbit",
        "ml_14_glass_aurora" to "Glass Aurora",
        "ml_15_dewdrop_biophilia" to "Dewdrop Biophilia",
        "ml_16_velvet_jelly_forms" to "Velvet Jelly Forms",
        "ml_17_dream_gradient_grid" to "Dream Gradient Grid",
        "ml_18_glass_portal" to "Glass Portal",
        "ml_19_neon_vector_pulse" to "Neon Vector Pulse",
        "ml_20_pixel_constellation" to "Pixel Constellation",
        "ml_21_parallax_slabs" to "Parallax Slabs",
        "ml_22_outline_reveal" to "Outline Reveal",
        "ml_23_pixel_to_line" to "Pixel-to-Line",
        "ml_24_future_monolith" to "Future Monolith",
        "ml_25_vector_sweep" to "Vector Sweep",
        "ml_26_shape_morph" to "Shape Morph",
        "ml_27_gradient_wipe" to "Gradient Wipe",
        "ml_28_orbiting_dashes" to "Orbiting Dashes",
        "ml_29_modular_assembly" to "Modular Assembly",
        "ml_30_contour_flow" to "Contour Flow",
        "ml_31_radial_burst" to "Radial Burst",
        "ml_32_mughal_arch" to "Mughal Arch",
        "ml_33_ukiyo_e_wave" to "Ukiyo-e Wave",
        "ml_34_greek_marble" to "Greek Marble",
        "ml_35_egyptian_sun_lotus" to "Egyptian Sun Lotus",
        "ml_36_persian_star_tile" to "Persian Star Tile",
        "ml_37_renaissance_astrolabe" to "Renaissance Astrolabe",
        "ml_38_star_catcher" to "Star Catcher",
        "ml_39_the_key" to "The Key",
        "ml_40_koi_circuit" to "Koi Circuit",
        "ml_41_bloom_dial" to "Bloom Dial",
        "ml_42_cassette_target" to "Cassette Target",
        "ml_43_moon_diver" to "Moon Diver",
        "ml_44_north" to "North",
        "ml_45_portal_window" to "Portal Window",
        "ml_46_tea_steam" to "Tea Steam",
        "ml_47_pendulum" to "Pendulum",
        "ml_48_floating_petal" to "Floating Petal",
        "ml_49_jellyfish" to "Jellyfish",
        "ml_50_rain_on_leaf" to "Rain on Leaf",
        "ml_51_moon_cloud" to "Moon Cloud",
        "ml_52_firefly" to "Firefly",
        "ml_53_fountain_ripple" to "Fountain Ripple",
    )

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

        // 1. Resolve Family & Archetype explicitly from preset prefix
        val (family, archetype) = when {
            // Cyberpunk & Neon
            presetId.startsWith("cyber_matrix_") -> Pair(EngineFamily.CYBERPUNK_NEON, "data_matrix")
            presetId.startsWith("cyber_circuit_") -> Pair(EngineFamily.CYBERPUNK_NEON, "circuit_net")
            presetId.startsWith("cyber_highway_") -> Pair(EngineFamily.CYBERPUNK_NEON, "highway_streaks")
            presetId.startsWith("cyber_hud_") -> Pair(EngineFamily.CYBERPUNK_NEON, "prismatic_hud")
            presetId.startsWith("cyber_rain_") -> Pair(EngineFamily.CYBERPUNK_NEON, "cyber_rain")
            presetId.startsWith("cyber_") -> {
                val arch = listOf("data_matrix", "circuit_net", "highway_streaks", "prismatic_hud", "cyber_rain")[((hash % 5).toInt())]
                Pair(EngineFamily.CYBERPUNK_NEON, arch)
            }

            // Abstract Geometry
            presetId.startsWith("geom_polyhedra_") -> Pair(EngineFamily.ABSTRACT_GEOMETRY, "morphing_polyhedra")
            presetId.startsWith("geom_mandala_") -> Pair(EngineFamily.ABSTRACT_GEOMETRY, "kaleido_mandala")
            presetId.startsWith("geom_voronoi_") -> Pair(EngineFamily.ABSTRACT_GEOMETRY, "voronoi_lattice")
            presetId.startsWith("geom_cubes_") -> Pair(EngineFamily.ABSTRACT_GEOMETRY, "isometric_cubes")
            presetId.startsWith("geom_lissajous_") -> Pair(EngineFamily.ABSTRACT_GEOMETRY, "lissajous_curves")
            presetId.startsWith("geom_") -> {
                val arch = listOf("morphing_polyhedra", "kaleido_mandala", "voronoi_lattice", "isometric_cubes", "lissajous_curves")[((hash % 5).toInt())]
                Pair(EngineFamily.ABSTRACT_GEOMETRY, arch)
            }

            // Fluid & Liquid Motion
            presetId.startsWith("fluid_chrome_") -> Pair(EngineFamily.FLUID_LIQUID, "liquid_chrome_waves")
            presetId.startsWith("fluid_ferro_") -> Pair(EngineFamily.FLUID_LIQUID, "magnetic_ferrofluid")
            presetId.startsWith("fluid_oilslick_") -> Pair(EngineFamily.FLUID_LIQUID, "oil_slick_swirls")
            presetId.startsWith("fluid_plasma_") -> Pair(EngineFamily.FLUID_LIQUID, "biolum_plasma")
            presetId.startsWith("fluid_ripple_") -> Pair(EngineFamily.FLUID_LIQUID, "viscous_ripple_pool")
            presetId.startsWith("fluid_") -> {
                val arch = listOf("liquid_chrome_waves", "magnetic_ferrofluid", "oil_slick_swirls", "biolum_plasma", "viscous_ripple_pool")[((hash % 5).toInt())]
                Pair(EngineFamily.FLUID_LIQUID, arch)
            }

            // Celestial & Deep Space
            presetId.startsWith("space_blackhole_") -> Pair(EngineFamily.CELESTIAL_SPACE, "black_hole_lens")
            presetId.startsWith("space_warp_") -> Pair(EngineFamily.CELESTIAL_SPACE, "warp_speed_starfield")
            presetId.startsWith("space_nebula_") -> Pair(EngineFamily.CELESTIAL_SPACE, "nebula_clouds")
            presetId.startsWith("space_rings_") -> Pair(EngineFamily.CELESTIAL_SPACE, "orbital_rings")
            presetId.startsWith("space_pulsar_") -> Pair(EngineFamily.CELESTIAL_SPACE, "solar_pulsar")
            presetId.startsWith("space_") -> {
                val arch = listOf("black_hole_lens", "warp_speed_starfield", "nebula_clouds", "orbital_rings", "solar_pulsar")[((hash % 5).toInt())]
                Pair(EngineFamily.CELESTIAL_SPACE, arch)
            }

            // Bioluminescent Nature
            presetId.startsWith("nature_fireflies_") -> Pair(EngineFamily.BIOLUMINESCENT_NATURE, "enchanted_fireflies")
            presetId.startsWith("nature_sakura_") -> Pair(EngineFamily.BIOLUMINESCENT_NATURE, "sakura_tempest")
            presetId.startsWith("nature_jellyfish_") -> Pair(EngineFamily.BIOLUMINESCENT_NATURE, "abyssal_jellyfish")
            presetId.startsWith("nature_rain_") -> Pair(EngineFamily.BIOLUMINESCENT_NATURE, "rainforest_rain")
            presetId.startsWith("nature_aurora_") -> Pair(EngineFamily.BIOLUMINESCENT_NATURE, "aurora_curtains")
            presetId.startsWith("nature_") -> {
                val arch = listOf("enchanted_fireflies", "sakura_tempest", "abyssal_jellyfish", "rainforest_rain", "aurora_curtains")[((hash % 5).toInt())]
                Pair(EngineFamily.BIOLUMINESCENT_NATURE, arch)
            }

            // Minimal & Zen
            presetId.startsWith("zen_mesh_") -> Pair(EngineFamily.MINIMAL_ZEN, "breathing_mesh")
            presetId.startsWith("zen_ripples_") -> Pair(EngineFamily.MINIMAL_ZEN, "zen_ripples")
            presetId.startsWith("zen_prism_") -> Pair(EngineFamily.MINIMAL_ZEN, "prismatic_glass")
            presetId.startsWith("zen_monolith_") -> Pair(EngineFamily.MINIMAL_ZEN, "monolith_balance")
            presetId.startsWith("zen_") -> {
                val arch = listOf("breathing_mesh", "zen_ripples", "prismatic_glass", "monolith_balance")[((hash % 4).toInt())]
                Pair(EngineFamily.MINIMAL_ZEN, arch)
            }

            // Motion Loops
            presetId.startsWith("ml_") -> Pair(EngineFamily.MOTION_LOOPS, presetId)

            // Bold Chromatics & Op-Art
            presetId.startsWith("chroma_opart_") -> Pair(EngineFamily.BOLD_CHROMATIC, "kinetic_op_art")
            presetId.startsWith("chroma_glitch_") -> Pair(EngineFamily.BOLD_CHROMATIC, "chromatic_glitch")
            presetId.startsWith("chroma_terrain_") -> Pair(EngineFamily.BOLD_CHROMATIC, "raymarch_waves")
            presetId.startsWith("chroma_helix_") -> Pair(EngineFamily.BOLD_CHROMATIC, "spectral_helix")
            else -> {
                val arch = listOf("kinetic_op_art", "chromatic_glitch", "raymarch_waves", "spectral_helix")[((hash % 4).toInt())]
                Pair(EngineFamily.BOLD_CHROMATIC, arch)
            }
        }

        // 2. Select Palette harmonized with family
        val palette = when (family) {
            EngineFamily.CYBERPUNK_NEON -> listOf(ColorPalette.CYBER_NEON, ColorPalette.TOKYO_NIGHT, ColorPalette.MATRIX_EMERALD, ColorPalette.SYNTHWAVE_SUNSET)[(hash % 4).toInt()]
            EngineFamily.ABSTRACT_GEOMETRY -> listOf(ColorPalette.SACRED_GOLD, ColorPalette.PRISMATIC_OP, ColorPalette.CYBER_NEON, ColorPalette.DEEP_NEBULA)[(hash % 4).toInt()]
            EngineFamily.FLUID_LIQUID -> listOf(ColorPalette.LIQUID_CHROME, ColorPalette.ABYSS_JELLYFISH, ColorPalette.DEEP_NEBULA, ColorPalette.CYBER_NEON)[(hash % 4).toInt()]
            EngineFamily.CELESTIAL_SPACE -> listOf(ColorPalette.DEEP_NEBULA, ColorPalette.SOLAR_CORONA, ColorPalette.ABYSS_JELLYFISH, ColorPalette.SYNTHWAVE_SUNSET)[(hash % 4).toInt()]
            EngineFamily.BIOLUMINESCENT_NATURE -> listOf(ColorPalette.ENCHANTED_FOREST, ColorPalette.SAKURA_MIST, ColorPalette.ABYSS_JELLYFISH, ColorPalette.MATRIX_EMERALD)[(hash % 4).toInt()]
            EngineFamily.MINIMAL_ZEN -> listOf(ColorPalette.ZEN_TWILIGHT, ColorPalette.SAKURA_MIST, ColorPalette.SACRED_GOLD, ColorPalette.LIQUID_CHROME)[(hash % 4).toInt()]
            EngineFamily.BOLD_CHROMATIC -> listOf(ColorPalette.PRISMATIC_OP, ColorPalette.SYNTHWAVE_SUNSET, ColorPalette.CYBER_NEON, ColorPalette.SOLAR_CORONA)[(hash % 4).toInt()]
            EngineFamily.MOTION_LOOPS -> listOf(
                ColorPalette.CYBER_NEON, ColorPalette.ZEN_TWILIGHT, ColorPalette.SACRED_GOLD,
                ColorPalette.LIQUID_CHROME, ColorPalette.DEEP_NEBULA, ColorPalette.ENCHANTED_FOREST,
                ColorPalette.PRISMATIC_OP, ColorPalette.TOKYO_NIGHT, ColorPalette.SAKURA_MIST
            )[(hash % 9).toInt()]
        }

        // 3. Tailor interaction type to archetype
        val interactionType = when (archetype) {
            "data_matrix" -> InteractionType.PULSE_WAVE
            "circuit_net" -> InteractionType.LIGHT_TRAIL_GLOW
            "highway_streaks" -> InteractionType.DEPTH_PARALLAX_TILT
            "prismatic_hud" -> InteractionType.DEPTH_PARALLAX_TILT
            "cyber_rain" -> InteractionType.RIPPLE_DISPERSION

            "morphing_polyhedra" -> InteractionType.DEPTH_PARALLAX_TILT
            "kaleido_mandala" -> InteractionType.VORTEX_SWIRL
            "voronoi_lattice" -> InteractionType.PULSE_WAVE
            "isometric_cubes" -> InteractionType.RIPPLE_DISPERSION
            "lissajous_curves" -> InteractionType.DEPTH_PARALLAX_TILT

            "liquid_chrome_waves" -> InteractionType.RIPPLE_DISPERSION
            "magnetic_ferrofluid" -> InteractionType.MAGNETIC_ATTRACTION
            "oil_slick_swirls" -> InteractionType.VORTEX_SWIRL
            "biolum_plasma" -> InteractionType.MAGNETIC_ATTRACTION
            "viscous_ripple_pool" -> InteractionType.RIPPLE_DISPERSION

            "black_hole_lens" -> InteractionType.MAGNETIC_ATTRACTION
            "warp_speed_starfield" -> InteractionType.GRAVITATIONAL_REPULSION
            "nebula_clouds" -> InteractionType.VORTEX_SWIRL
            "orbital_rings" -> InteractionType.DEPTH_PARALLAX_TILT
            "solar_pulsar" -> InteractionType.PULSE_WAVE

            "enchanted_fireflies" -> InteractionType.MAGNETIC_ATTRACTION
            "sakura_tempest" -> InteractionType.VORTEX_SWIRL
            "abyssal_jellyfish" -> InteractionType.GRAVITATIONAL_REPULSION
            "rainforest_rain" -> InteractionType.RIPPLE_DISPERSION
            "aurora_curtains" -> InteractionType.LIGHT_TRAIL_GLOW

            "breathing_mesh" -> InteractionType.DEPTH_PARALLAX_TILT
            "zen_ripples" -> InteractionType.RIPPLE_DISPERSION
            "prismatic_glass" -> InteractionType.DEPTH_PARALLAX_TILT
            "monolith_balance" -> InteractionType.DEPTH_PARALLAX_TILT

            "kinetic_op_art" -> InteractionType.VORTEX_SWIRL
            "chromatic_glitch" -> InteractionType.PULSE_WAVE
            "raymarch_waves" -> InteractionType.DEPTH_PARALLAX_TILT
            "spectral_helix" -> InteractionType.LIGHT_TRAIL_GLOW
            else -> InteractionType.RIPPLE_DISPERSION
        }

        val particleCount = 40 + (hash % 45).toInt()
        val speed = 0.7f + (hash % 50) / 100f
        val complexity = 0.8f + (hash % 60) / 60f
        val symmetry = listOf(4, 5, 6, 8, 12)[(hash % 5).toInt()]
        val glowRadius = 5f + (hash % 6)
        val interactionStrength = 0.9f + (hash % 30) / 100f

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
        if (family == EngineFamily.MOTION_LOOPS) {
            val titleFromMap = MOTION_LOOP_TITLES[presetId]
            if (titleFromMap != null) return titleFromMap
            val clean = presetId.removePrefix("ml_")
            val titlePart = if (clean.length > 3 && clean[2] == '_') clean.substring(3) else clean
            return titlePart.split("_").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
        }
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
            "black_hole_lens" -> "Event Horizon Singularity $numRoman"
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
