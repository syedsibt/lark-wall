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
        val config1 = InteractiveWallpaperRegistry.getConfig("cyber_neon_1")
        val config2 = InteractiveWallpaperRegistry.getConfig("cyber_neon_1")

        assertNotNull(config1)
        assertEquals(config1.presetId, config2.presetId)
        assertEquals(config1.family, EngineFamily.CYBERPUNK_NEON)
        assertEquals(config1.particleCount, config2.particleCount)
        assertEquals(config1.speed, config2.speed, 0.001f)
        assertEquals(config1.palette.background, config2.palette.background)
    }

    @Test
    fun `verify all seven engine families are correctly identified`() {
        val cyber = InteractiveWallpaperRegistry.getConfig("cyber_neon_5")
        val geom = InteractiveWallpaperRegistry.getConfig("geom_math_5")
        val fluid = InteractiveWallpaperRegistry.getConfig("fluid_motion_5")
        val space = InteractiveWallpaperRegistry.getConfig("space_cosmos_5")
        val nature = InteractiveWallpaperRegistry.getConfig("nature_biolum_5")
        val zen = InteractiveWallpaperRegistry.getConfig("zen_harmonic_5")
        val chroma = InteractiveWallpaperRegistry.getConfig("chroma_opart_5")

        assertEquals(EngineFamily.CYBERPUNK_NEON, cyber.family)
        assertEquals(EngineFamily.ABSTRACT_GEOMETRY, geom.family)
        assertEquals(EngineFamily.FLUID_LIQUID, fluid.family)
        assertEquals(EngineFamily.CELESTIAL_SPACE, space.family)
        assertEquals(EngineFamily.BIOLUMINESCENT_NATURE, nature.family)
        assertEquals(EngineFamily.MINIMAL_ZEN, zen.family)
        assertEquals(EngineFamily.BOLD_CHROMATIC, chroma.family)
    }

    @Test
    fun `verify interactive uri extraction and validation`() {
        val uri = "engine://interactive/cyber_neon_42"
        assertTrue(InteractiveWallpaperConfig.isInteractiveUri(uri))
        assertEquals("cyber_neon_42", InteractiveWallpaperConfig.extractPresetId(uri))
    }
}
