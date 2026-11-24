package com.aura.wallpapers.engine.interactive

data class ColorPalette(
    val name: String,
    val background: Int,
    val primary: Int,
    val secondary: Int,
    val tertiary: Int,
    val accent: Int
) {
    companion object {
        private fun rgb(r: Int, g: Int, b: Int): Int =
            (0xFF shl 24) or ((r and 0xFF) shl 16) or ((g and 0xFF) shl 8) or (b and 0xFF)

        val CYBER_NEON = ColorPalette(
            name = "Cyber Neon",
            background = rgb(5, 7, 15),
            primary = rgb(0, 245, 255),       // Cyan
            secondary = rgb(255, 0, 128),     // Hot Pink
            tertiary = rgb(138, 43, 226),     // Violet
            accent = rgb(0, 255, 140)         // Neon Mint
        )

        val TOKYO_NIGHT = ColorPalette(
            name = "Tokyo Night",
            background = rgb(10, 10, 22),
            primary = rgb(255, 42, 109),      // Neon Rose
            secondary = rgb(5, 217, 232),     // Aqua
            tertiary = rgb(255, 179, 0),      // Amber
            accent = rgb(168, 85, 247)        // Purple
        )

        val MATRIX_EMERALD = ColorPalette(
            name = "Matrix Emerald",
            background = rgb(2, 12, 6),
            primary = rgb(0, 255, 100),       // Matrix Green
            secondary = rgb(30, 200, 80),     // Forest Glow
            tertiary = rgb(0, 120, 50),       // Deep Jade
            accent = rgb(180, 255, 160)       // White Green
        )

        val SYNTHWAVE_SUNSET = ColorPalette(
            name = "Synthwave Sunset",
            background = rgb(18, 5, 28),
            primary = rgb(255, 110, 0),       // Solar Orange
            secondary = rgb(255, 0, 90),      // Sunset Red
            tertiary = rgb(140, 20, 252),     // Twilight Violet
            accent = rgb(255, 220, 40)        // Gold
        )

        val LIQUID_CHROME = ColorPalette(
            name = "Liquid Chrome",
            background = rgb(8, 10, 14),
            primary = rgb(220, 230, 245),     // Platinum Silver
            secondary = rgb(160, 180, 205),   // Steel Blue
            tertiary = rgb(90, 110, 135),     // Deep Pewter
            accent = rgb(255, 255, 255)       // Specular White
        )

        val DEEP_NEBULA = ColorPalette(
            name = "Deep Nebula",
            background = rgb(3, 3, 10),
            primary = rgb(147, 51, 234),      // Nebula Purple
            secondary = rgb(59, 130, 246),    // Cosmic Blue
            tertiary = rgb(236, 72, 153),     // Starlight Pink
            accent = rgb(100, 220, 255)       // Pulsar Cyan
        )

        val SOLAR_CORONA = ColorPalette(
            name = "Solar Corona",
            background = rgb(12, 4, 2),
            primary = rgb(255, 140, 0),       // Plasma Orange
            secondary = rgb(255, 60, 20),     // Flare Crimson
            tertiary = rgb(255, 215, 0),      // Solar Gold
            accent = rgb(255, 245, 180)       // Core White
        )

        val SACRED_GOLD = ColorPalette(
            name = "Sacred Gold",
            background = rgb(10, 8, 12),
            primary = rgb(234, 179, 8),       // Pure Gold
            secondary = rgb(202, 138, 4),     // Antique Bronze
            tertiary = rgb(254, 240, 138),    // Shimmer Champagne
            accent = rgb(217, 70, 239)        // Mystic Amethyst
        )

        val SAKURA_MIST = ColorPalette(
            name = "Sakura Mist",
            background = rgb(16, 10, 18),
            primary = rgb(244, 114, 182),     // Petal Pink
            secondary = rgb(251, 207, 232),   // Blossom White
            tertiary = rgb(192, 132, 252),    // Twilight Lavender
            accent = rgb(255, 182, 193)       // Soft Rose
        )

        val ENCHANTED_FOREST = ColorPalette(
            name = "Enchanted Forest",
            background = rgb(4, 12, 8),
            primary = rgb(34, 197, 94),       // Firefly Green
            secondary = rgb(16, 185, 129),    // Emerald
            tertiary = rgb(250, 204, 21),     // Glowing Amber
            accent = rgb(134, 239, 172)       // Spore Light
        )

        val ABYSS_JELLYFISH = ColorPalette(
            name = "Abyss Jellyfish",
            background = rgb(2, 6, 16),
            primary = rgb(6, 182, 212),       // Bioluminescent Cyan
            secondary = rgb(99, 102, 241),    // Indigo
            tertiary = rgb(217, 70, 239),     // Fuchsia Glow
            accent = rgb(165, 243, 252)       // Electric White
        )

        val ZEN_TWILIGHT = ColorPalette(
            name = "Zen Twilight",
            background = rgb(12, 14, 20),
            primary = rgb(148, 163, 184),     // Zen Slate
            secondary = rgb(94, 234, 212),    // Water Teal
            tertiary = rgb(167, 139, 250),    // Harmonic Lilac
            accent = rgb(248, 250, 252)       // Pure Ripple White
        )

        val PRISMATIC_OP = ColorPalette(
            name = "Prismatic Op-Art",
            background = rgb(6, 6, 8),
            primary = rgb(255, 255, 255),     // Optical White
            secondary = rgb(239, 68, 68),     // Chromatic Red
            tertiary = rgb(59, 130, 246),     // Chromatic Blue
            accent = rgb(34, 197, 94)         // Chromatic Green
        )

        // ---- Curated palettes for the Signature designs (one look per design) ----
        val AMBER_TERMINAL = ColorPalette(
            name = "Amber Terminal",
            background = rgb(8, 6, 2),
            primary = rgb(255, 176, 0),
            secondary = rgb(255, 120, 20),
            tertiary = rgb(120, 80, 10),
            accent = rgb(255, 236, 170)
        )

        val ARCTIC_ICE = ColorPalette(
            name = "Arctic Ice",
            background = rgb(4, 10, 20),
            primary = rgb(186, 230, 253),
            secondary = rgb(125, 211, 252),
            tertiary = rgb(30, 58, 95),
            accent = rgb(255, 255, 255)
        )

        val MIDNIGHT_OCEAN = ColorPalette(
            name = "Midnight Ocean",
            background = rgb(2, 6, 18),
            primary = rgb(30, 64, 175),
            secondary = rgb(14, 116, 144),
            tertiary = rgb(8, 30, 70),
            accent = rgb(254, 243, 199)
        )

        val BAMBOO_MIST = ColorPalette(
            name = "Bamboo Mist",
            background = rgb(6, 14, 10),
            primary = rgb(74, 160, 90),
            secondary = rgb(132, 204, 22),
            tertiary = rgb(20, 70, 40),
            accent = rgb(220, 252, 231)
        )

        val LANTERN_GLOW = ColorPalette(
            name = "Lantern Glow",
            background = rgb(10, 6, 20),
            primary = rgb(255, 150, 50),
            secondary = rgb(239, 68, 68),
            tertiary = rgb(60, 30, 90),
            accent = rgb(255, 230, 160)
        )

        val LAVA_LAMP = ColorPalette(
            name = "Lava Lamp",
            background = rgb(16, 4, 12),
            primary = rgb(255, 64, 129),
            secondary = rgb(255, 145, 0),
            tertiary = rgb(124, 58, 237),
            accent = rgb(255, 214, 170)
        )

        val INK_WASH = ColorPalette(
            name = "Ink Wash",
            background = rgb(6, 6, 12),
            primary = rgb(99, 102, 241),
            secondary = rgb(236, 72, 153),
            tertiary = rgb(20, 184, 166),
            accent = rgb(250, 204, 21)
        )

        val ALL_PALETTES = listOf(
            CYBER_NEON, TOKYO_NIGHT, MATRIX_EMERALD, SYNTHWAVE_SUNSET,
            LIQUID_CHROME, DEEP_NEBULA, SOLAR_CORONA, SACRED_GOLD,
            SAKURA_MIST, ENCHANTED_FOREST, ABYSS_JELLYFISH, ZEN_TWILIGHT,
            PRISMATIC_OP
        )

        fun forIndex(index: Int): ColorPalette = ALL_PALETTES[Math.floorMod(index, ALL_PALETTES.size)]
    }
}
