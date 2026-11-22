package com.aura.wallpapers

import com.aura.wallpapers.engine.interactive.EngineFamily
import com.aura.wallpapers.engine.interactive.InteractiveWallpaperConfig
import com.aura.wallpapers.engine.interactive.InteractiveWallpaperRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InteractiveLiveWallpaperEngineTest {

    @Test
    fun `verify registry resolves deterministic config for preset`() {
        val config1 = InteractiveWallpaperRegistry.getConfig("cyber_matrix_1")
        val config2 = InteractiveWallpaperRegistry.getConfig("cyber_matrix_1")

        assertNotNull(config1)
        assertEquals(config1.presetId, config2.presetId)
        assertEquals(config1.family, EngineFamily.CYBERPUNK_NEON)
        assertEquals(config1.archetype, "data_matrix")
        assertEquals(config1.particleCount, config2.particleCount)
        assertEquals(config1.speed, config2.speed, 0.001f)
        assertEquals(config1.palette.background, config2.palette.background)
    }

    @Test
    fun `verify all seven engine families and distinct archetypes are correctly identified`() {
        val cyber = InteractiveWallpaperRegistry.getConfig("cyber_matrix_1")
        val geom = InteractiveWallpaperRegistry.getConfig("geom_polyhedra_1")
        val fluid = InteractiveWallpaperRegistry.getConfig("fluid_chrome_1")
        val space = InteractiveWallpaperRegistry.getConfig("space_blackhole_1")
        val nature = InteractiveWallpaperRegistry.getConfig("nature_fireflies_1")
        val zen = InteractiveWallpaperRegistry.getConfig("zen_mesh_1")
        val chroma = InteractiveWallpaperRegistry.getConfig("chroma_opart_1")

        assertEquals(EngineFamily.CYBERPUNK_NEON, cyber.family)
        assertEquals("data_matrix", cyber.archetype)

        assertEquals(EngineFamily.ABSTRACT_GEOMETRY, geom.family)
        assertEquals("morphing_polyhedra", geom.archetype)

        assertEquals(EngineFamily.FLUID_LIQUID, fluid.family)
        assertEquals("liquid_chrome_waves", fluid.archetype)

        assertEquals(EngineFamily.CELESTIAL_SPACE, space.family)
        assertEquals("black_hole_lens", space.archetype)

        assertEquals(EngineFamily.BIOLUMINESCENT_NATURE, nature.family)
        assertEquals("enchanted_fireflies", nature.archetype)

        assertEquals(EngineFamily.MINIMAL_ZEN, zen.family)
        assertEquals("breathing_mesh", zen.archetype)

        assertEquals(EngineFamily.BOLD_CHROMATIC, chroma.family)
        assertEquals("kinetic_op_art", chroma.archetype)
    }

    @Test
    fun `verify interactive uri extraction and validation`() {
        val uri = "engine://interactive/cyber_matrix_42"
        assertTrue(InteractiveWallpaperConfig.isInteractiveUri(uri))
        assertEquals("cyber_matrix_42", InteractiveWallpaperConfig.extractPresetId(uri))
    }

    @Test
    fun `verify null and blank preset id extraction are safe and provide valid default`() {
        val nullUri: String? = null
        val blankUri = "   "
        val emptyUri = ""

        assertEquals("cyber_neon_1", InteractiveWallpaperConfig.extractPresetId(nullUri))
        assertEquals("cyber_neon_1", InteractiveWallpaperConfig.extractPresetId(blankUri))
        assertEquals("cyber_neon_1", InteractiveWallpaperConfig.extractPresetId(emptyUri))

        val configForBlank = InteractiveWallpaperRegistry.getConfig("")
        assertNotNull(configForBlank)
        assertTrue(configForBlank.speed > 0f)
        assertTrue(configForBlank.particleCount > 0)
    }

    @Test
    fun `verify all 53 motion loop titles exist in title registry map`() {
        assertEquals(53, InteractiveWallpaperRegistry.MOTION_LOOP_TITLES.size)
        assertTrue(InteractiveWallpaperRegistry.MOTION_LOOP_TITLES.containsKey("ml_01_aurora_drift"))
        assertTrue(InteractiveWallpaperRegistry.MOTION_LOOP_TITLES.containsKey("ml_25_vector_sweep"))
        assertTrue(InteractiveWallpaperRegistry.MOTION_LOOP_TITLES.containsKey("ml_53_fountain_ripple"))
    }
}
