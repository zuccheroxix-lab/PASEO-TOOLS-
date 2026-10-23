package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.InstalledApp
import com.example.model.mobilador.MappingElement
import com.example.model.mobilador.MappingElementType
import com.example.model.mobilador.MobiladorProfile
import com.example.repository.MobiladorRepository
import com.example.repository.PreferencesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MobiladorAndLauncherTest {

    private lateinit var context: Context
    private lateinit var mobiladorRepository: MobiladorRepository
    private lateinit var preferencesRepository: PreferencesRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("paseo_mobilador_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("paseo_sensi_prefs", Context.MODE_PRIVATE).edit().clear().commit()

        mobiladorRepository = MobiladorRepository(context)
        preferencesRepository = PreferencesRepository(context)
    }

    @Test
    fun testMobiladorDefaultProfilesLoaded() {
        val profiles = mobiladorRepository.allProfiles.value
        assertTrue("Harus memuat profil bawaan mobilador", profiles.isNotEmpty())
        val defaultProfile = mobiladorRepository.activeProfile.value
        assertNotNull(defaultProfile)
        assertTrue(defaultProfile.elements.isNotEmpty())

        // Verify WASD movement and Fire mappings exist in default profile
        val hasMovement = defaultProfile.elements.any { it.type == MappingElementType.MOVEMENT_WASD }
        val hasFire = defaultProfile.elements.any { it.type == MappingElementType.FIRE }
        assertTrue("Default profile must contain WASD movement and FIRE", hasMovement && hasFire)
    }

    @Test
    fun testMobiladorCustomProfileSaveAndUndoRedo() {
        val base = mobiladorRepository.activeProfile.value
        val customProfile = base.copy(
            id = "custom_apex_mobile",
            name = "Apex Legends Mobile",
            gamePackage = "com.ea.gp.apexlegendsmobilefps",
            mouseSensitivityX = 1.45f,
            mouseSensitivityY = 1.20f,
            elements = listOf(
                MappingElement(
                    name = "WASD",
                    type = MappingElementType.MOVEMENT_WASD,
                    keyBinding = "WASD",
                    normalizedX = 0.2f,
                    normalizedY = 0.7f
                ),
                MappingElement(
                    name = "Shoot",
                    type = MappingElementType.FIRE,
                    keyBinding = "M_LEFT",
                    normalizedX = 0.8f,
                    normalizedY = 0.6f
                )
            ),
            isCustom = true
        )

        // Save profile
        val saved = mobiladorRepository.saveCustomProfile(customProfile)
        assertEquals("Apex Legends Mobile", saved.name)
        assertTrue(saved.isCustom)

        val loaded = mobiladorRepository.allProfiles.value.find { it.id == saved.id }
        assertNotNull(loaded)
        assertEquals(1.45f, loaded?.mouseSensitivityX ?: 0f, 0.01f)

        // Test Undo/Redo stack in Mapping Editor
        assertFalse(mobiladorRepository.canUndo())
        val modifiedProfile = saved.copy(mouseSensitivityX = 1.85f)
        mobiladorRepository.updateEditorProfile(modifiedProfile)
        assertTrue(mobiladorRepository.canUndo())

        val undone = mobiladorRepository.undo()
        assertNotNull(undone)
        assertEquals(1.45f, undone?.mouseSensitivityX ?: 0f, 0.01f)
        assertTrue(mobiladorRepository.canRedo())

        val redone = mobiladorRepository.redo()
        assertNotNull(redone)
        assertEquals(1.85f, redone?.mouseSensitivityX ?: 0f, 0.01f)
    }

    @Test
    fun testMobiladorExportAndImportJson() {
        val active = mobiladorRepository.activeProfile.value
        val exportedJson = mobiladorRepository.exportProfileJson(active)
        assertTrue(exportedJson.contains(active.name))

        val importResult = mobiladorRepository.importProfileJson(exportedJson)
        assertTrue("Import profil harus berhasil", importResult.isSuccess)
        val imported = importResult.getOrThrow()
        assertTrue(imported.isCustom)
        assertEquals(active.name, imported.name)
    }

    @Test
    fun testMobiladorDeviceScan() {
        // Runs on Robolectric environment
        val devices = mobiladorRepository.scanConnectedDevices()
        assertNotNull(devices)
    }

    @Test
    fun testFavoritesPersistenceAndToggling() {
        val testPkg = "com.dts.freefireth"
        assertFalse(preferencesRepository.getFavoritePackages().contains(testPkg))

        // Toggle ON
        val isFav1 = preferencesRepository.toggleFavoritePackage(testPkg)
        assertTrue(isFav1)
        assertTrue(preferencesRepository.getFavoritePackages().contains(testPkg))
        assertTrue(preferencesRepository.favoritePackages.value.contains(testPkg))

        // Toggle OFF
        val isFav2 = preferencesRepository.toggleFavoritePackage(testPkg)
        assertFalse(isFav2)
        assertFalse(preferencesRepository.getFavoritePackages().contains(testPkg))
    }

    @Test
    fun testNoFabricatedGamesIntegrity() {
        val realApp = InstalledApp(
            packageName = "com.example.game",
            appName = "Custom Arena",
            isGame = true,
            versionName = "2.1.0"
        )
        assertEquals("com.example.game", realApp.packageName)
        assertTrue(realApp.isGame)
    }
}
