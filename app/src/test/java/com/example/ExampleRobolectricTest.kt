package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.CrosshairConfig
import com.example.model.CrosshairShape
import com.example.model.SensitivityProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PASEO SENSI", appName)
    }

    @Test
    fun `sensitivity presets are valid and non empty`() {
        val presets = SensitivityProfile.PRESETS
        assertTrue(presets.isNotEmpty())
        val proPreset = presets.firstOrNull { it.id == "preset_pro_tournament" }
        assertNotNull(proPreset)
        assertEquals("ZX Pro Tournament Spec", proPreset?.name)
        assertTrue((proPreset?.generalSensi ?: 0) > 0)
    }

    @Test
    fun `crosshair default config has classic cross`() {
        val config = CrosshairConfig()
        assertEquals(CrosshairShape.CLASSIC_CROSS, config.shape)
        assertTrue(config.sizeDp > 0)
        assertTrue(config.thicknessDp > 0)
    }
}
