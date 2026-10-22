package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.auth.AuthMode
import com.example.auth.AuthRepository
import com.example.auth.AuthResult
import com.example.auth.UserAccount
import com.example.auth.ValidationResult
import com.example.model.CrosshairConfig
import com.example.model.CrosshairPreset
import com.example.model.CrosshairShape
import com.example.model.SensitivityCurveType
import com.example.model.SensitivityProfile
import com.example.nativecore.SensitivityBridge
import com.example.repository.PreferencesRepository
import com.example.repository.SensitivityRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AuthAndSensitivityTest {

    private lateinit var context: Context
    private lateinit var authRepository: AuthRepository
    private lateinit var sensitivityRepository: SensitivityRepository
    private lateinit var preferencesRepository: PreferencesRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        authRepository = AuthRepository(context)
        sensitivityRepository = SensitivityRepository(context)
        preferencesRepository = PreferencesRepository(context)
    }

    // ==========================================
    // 1. PIN VALIDATION & WEAK PIN REJECTION
    // ==========================================
    @Test
    fun `test weak PIN rejection for repeated and sequential numbers`() {
        // Rejects repeated digits
        val repeated = authRepository.validatePin("1111")
        assertTrue(repeated is ValidationResult.Invalid)
        assertEquals("PIN terlalu lemah: tidak boleh angka berulang yang sama.", (repeated as ValidationResult.Invalid).reason)

        val repeatedZeroes = authRepository.validatePin("000000")
        assertTrue(repeatedZeroes is ValidationResult.Invalid)

        // Rejects sequential numbers
        val seqAsc = authRepository.validatePin("1234")
        assertTrue(seqAsc is ValidationResult.Invalid)
        assertEquals("PIN terlalu lemah: tidak boleh urutan angka berurutan sederhana.", (seqAsc as ValidationResult.Invalid).reason)

        val seqDesc = authRepository.validatePin("4321")
        assertTrue(seqDesc is ValidationResult.Invalid)

        // Rejects too short or too long
        assertTrue(authRepository.validatePin("12") is ValidationResult.Invalid)
        assertTrue(authRepository.validatePin("123456789") is ValidationResult.Invalid)

        // Accepts valid secure PIN
        val validPin = authRepository.validatePin("7391")
        assertEquals(ValidationResult.Valid, validPin)

        val validPin6 = authRepository.validatePin("849201")
        assertEquals(ValidationResult.Valid, validPin6)
    }

    // ==========================================
    // 2. PIN REGISTRATION, HASHING & LOGIN VERIFICATION
    // ==========================================
    @Test
    fun `test PIN registration with salted PBKDF2 hash and duplicate rejection`() {
        val testPin = "7492"
        val salt = authRepository.generateSalt()
        assertTrue(salt.isNotEmpty())
        assertEquals(32, salt.length) // 16 bytes in hex = 32 chars

        val pinHash = authRepository.hashCredential(testPin, salt)
        val hmacLookup = authRepository.computeHmacPinLookup(testPin)
        val recoveryHash = authRepository.hashCredential("free fire", salt)

        val account = UserAccount(
            id = UUID.randomUUID().toString(),
            username = "ZX_Master",
            saltHex = salt,
            pinHash = pinHash,
            hmacPinLookup = hmacLookup,
            patternHash = null,
            recoveryQuestion = "Game favorit?",
            recoveryAnswerHash = recoveryHash,
            preferredAuthMode = AuthMode.PIN
        )

        authRepository.saveAccount(account)

        // Verify account exists
        assertTrue(authRepository.hasAnyAccount())
        val saved = authRepository.getActiveAccount()
        assertNotNull(saved)
        assertEquals("ZX_Master", saved?.username)

        // Verify correct PIN authenticates
        val successResult = authRepository.verifyPin(saved!!, testPin)
        assertTrue(successResult is AuthResult.Success)

        // Verify incorrect PIN fails
        val failResult = authRepository.verifyPin(saved, "9999")
        assertTrue(failResult is AuthResult.Failure)

        // Verify duplicate PIN check flags duplicate on local database
        assertTrue(authRepository.isPinRegisteredInLocalDatabase(testPin))
        assertFalse(authRepository.isPinRegisteredInLocalDatabase("5821"))

        // Verify credentials are NEVER stored in plaintext in SharedPreferences
        val rawPrefs = context.getSharedPreferences("paseo_auth_secure_prefs", Context.MODE_PRIVATE)
        val allPrefsString = rawPrefs.all.toString()
        assertFalse("Raw PIN must never appear in preferences!", allPrefsString.contains(testPin))
    }

    // ==========================================
    // 3. PATTERN LOCK REGISTRATION & VALIDATION
    // ==========================================
    @Test
    fun `test Pattern lock validation and authentication`() {
        // Rejects pattern that is too short
        val shortPattern = listOf(0, 1, 2)
        val shortResult = authRepository.validatePattern(shortPattern)
        assertTrue(shortResult is ValidationResult.Invalid)

        // Valid pattern
        val validPattern = listOf(0, 4, 8, 5)
        val validResult = authRepository.validatePattern(validPattern)
        assertEquals(ValidationResult.Valid, validResult)

        val salt = authRepository.generateSalt()
        val patternString = validPattern.joinToString("-")
        val patternHash = authRepository.hashCredential(patternString, salt)

        val account = UserAccount(
            id = UUID.randomUUID().toString(),
            username = "ZX_Gamer",
            saltHex = salt,
            pinHash = null,
            hmacPinLookup = null,
            patternHash = patternHash,
            recoveryQuestion = "Game favorit?",
            recoveryAnswerHash = authRepository.hashCredential("pubg", salt),
            preferredAuthMode = AuthMode.PATTERN
        )
        authRepository.saveAccount(account)

        // Verification matches
        val success = authRepository.verifyPattern(account, listOf(0, 4, 8, 5))
        assertTrue(success is AuthResult.Success)

        // Wrong pattern fails
        val failure = authRepository.verifyPattern(account, listOf(0, 1, 2, 5))
        assertTrue(failure is AuthResult.Failure)
    }

    // ==========================================
    // 4. RATE LIMITING & LOCKOUT
    // ==========================================
    @Test
    fun `test authentication rate limiting and lockout after 5 failed attempts`() {
        val salt = authRepository.generateSalt()
        val account = UserAccount(
            id = UUID.randomUUID().toString(),
            username = "TestUser",
            saltHex = salt,
            pinHash = authRepository.hashCredential("8371", salt),
            hmacPinLookup = authRepository.computeHmacPinLookup("8371"),
            patternHash = null,
            recoveryQuestion = "Pet name?",
            recoveryAnswerHash = authRepository.hashCredential("cat", salt)
        )
        authRepository.saveAccount(account)

        var current = authRepository.getActiveAccount()!!
        for (i in 1..4) {
            val res = authRepository.verifyPin(current, "0000")
            assertTrue(res is AuthResult.Failure)
            current = authRepository.getActiveAccount()!!
        }

        // 5th failed attempt must trigger lockout
        val fifthRes = authRepository.verifyPin(current, "0000")
        assertTrue("5th failed attempt must trigger LockedOut", fifthRes is AuthResult.LockedOut)
        assertTrue((fifthRes as AuthResult.LockedOut).waitSeconds > 0)
    }

    // ==========================================
    // 5. CROSSHAIR PRESET & JSON IMPORT/EXPORT
    // ==========================================
    @Test
    fun `test CrosshairConfig JSON serialization and preset repository`() {
        val config = CrosshairConfig(
            isEnabled = true,
            shape = CrosshairShape.DIAMOND,
            colorValue = 0xFF00E676,
            sizeDp = 30,
            thicknessDp = 3.5f,
            gapDp = 6.0f,
            opacity = 0.90f,
            offsetX = 10,
            offsetY = -15
        )

        // Test JSON Export
        val json = config.toJson()
        assertTrue(json.contains("DIAMOND"))
        assertTrue(json.contains("\"sizeDp\": 30"))

        // Test JSON Import
        val imported = CrosshairConfig.fromJson(json)
        assertNotNull(imported)
        assertEquals(CrosshairShape.DIAMOND, imported?.shape)
        assertEquals(30, imported?.sizeDp)
        assertEquals(0xFF00E676, imported?.colorValue)
        assertEquals(10, imported?.offsetX)
        assertEquals(-15, imported?.offsetY)

        // Test Custom Preset saving in repository
        val preset = CrosshairPreset(
            id = "test_custom_preset_1",
            name = "My Elite Scope",
            config = config,
            isCustom = true
        )
        preferencesRepository.saveCustomCrosshairPreset(preset)

        val allPresets = preferencesRepository.getAllCrosshairPresets()
        assertTrue(allPresets.any { it.id == "test_custom_preset_1" && it.name == "My Elite Scope" })

        // Test deletion of custom preset
        val deleted = preferencesRepository.deleteCrosshairPreset("test_custom_preset_1")
        assertTrue(deleted)
    }

    // ==========================================
    // 6. SENSITIVITY ENGINE C++ ALGORITHMIC PARITY & X/Y TRANSFORMATION
    // ==========================================
    @Test
    fun `test SensitivityBridge mathematical coordinate calculation and curves`() {
        val rawDeltaX = 15.0f
        val rawDeltaY = 10.0f
        val sensiX = 1.35f
        val sensiY = 1.20f

        val resultDynamic = SensitivityBridge.calculateTransformedPoint(
            rawDeltaX = rawDeltaX,
            rawDeltaY = rawDeltaY,
            sensiMultiplierX = sensiX,
            sensiMultiplierY = sensiY,
            curveType = SensitivityCurveType.DYNAMIC,
            smoothingFactor = 0.10f,
            deadzoneThreshold = 0.02f,
            accelerationFactor = 1.10f,
            deltaTimeMs = 16.6f
        )

        assertTrue(resultDynamic.x > 0)
        assertTrue(resultDynamic.y > 0)
        assertTrue(resultDynamic.velocity > 0)
        assertTrue(resultDynamic.appliedMultiplierX >= sensiX)
        assertTrue(resultDynamic.appliedMultiplierY >= sensiY)

        // Test deadzone filtering
        val resultDeadzone = SensitivityBridge.calculateTransformedPoint(
            rawDeltaX = 0.005f,
            rawDeltaY = 0.005f,
            sensiMultiplierX = sensiX,
            sensiMultiplierY = sensiY,
            curveType = SensitivityCurveType.LINEAR,
            deadzoneThreshold = 0.05f
        )
        assertEquals(0.0f, resultDeadzone.x, 0.0001f)
        assertEquals(0.0f, resultDeadzone.y, 0.0001f)
    }

    // ==========================================
    // 7. SENSITIVITY REPOSITORY CRUD & EXPORT/IMPORT
    // ==========================================
    @Test
    fun `test SensitivityRepository profile CRUD, duplicate, and JSON import export`() {
        val baseProfile = SensitivityProfile.PRESETS.first()
        val duplicated = sensitivityRepository.duplicateProfile(baseProfile.id, "Headshot FF 2026")
        assertNotNull(duplicated)
        assertEquals("Headshot FF 2026", duplicated?.name)
        assertTrue(duplicated!!.isCustom)

        // Test Export
        val json = sensitivityRepository.exportProfileJson(duplicated)
        assertTrue(json.contains("Headshot FF 2026"))

        // Test Import
        val imported = sensitivityRepository.importProfileJson(json)
        assertNotNull(imported)
        assertTrue(imported!!.name.contains("Headshot FF 2026"))

        // Test Game Assignment
        sensitivityRepository.assignProfileToGame(imported.id, "com.dts.freefireth")
        val mapped = sensitivityRepository.getProfileForGame("com.dts.freefireth")
        assertNotNull(mapped)
        assertEquals(imported.id, mapped?.id)

        // Test Deletion
        val deleted = sensitivityRepository.deleteProfile(duplicated.id)
        assertTrue(deleted)
    }
}
