package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.auth.AuthRepository
import com.example.model.SensitivityCurveType
import com.example.model.SensitivityProfile
import com.example.nativecore.SensitivityBridge
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AuthAndSensitivityTest {

    private lateinit var context: Context
    private lateinit var authRepository: AuthRepository
    private lateinit var sensitivityRepository: SensitivityRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        // Clear shared preferences for clean test runs
        context.getSharedPreferences("paseo_auth_storage_v2", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("paseo_sensitivity_prefs", Context.MODE_PRIVATE).edit().clear().commit()

        authRepository = AuthRepository(context)
        sensitivityRepository = SensitivityRepository(context)
    }

    @Test
    fun testPinStrengthRules() {
        // Trivial or short/long PINs
        assertTrue(authRepository.validatePinStrength("12").isFailure)
        assertTrue(authRepository.validatePinStrength("123456789").isFailure)

        // Repetitive numbers
        assertTrue(authRepository.validatePinStrength("1111").isFailure)
        assertTrue(authRepository.validatePinStrength("000000").isFailure)

        // Sequential numbers
        assertTrue(authRepository.validatePinStrength("1234").isFailure)
        assertTrue(authRepository.validatePinStrength("4321").isFailure)
        assertTrue(authRepository.validatePinStrength("987654").isFailure)

        // Valid strong PINs
        assertTrue(authRepository.validatePinStrength("8294").isSuccess)
        assertTrue(authRepository.validatePinStrength("931842").isSuccess)
    }

    @Test
    fun testAccountRegistrationAndDuplicatePinRejection() {
        val user1 = authRepository.registerAccount(
            username = "GamerOne",
            pin = "8294",
            pinConfirm = "8294",
            securityQuestion = "Perangkat favorit?",
            securityAnswer = "ROG Phone 8"
        )
        assertTrue(user1.isSuccess)
        val acc1 = user1.getOrThrow()

        // Plaintext PIN must never be stored anywhere
        assertFalse(acc1.pinHashBase64.contains("8294"))
        assertNotNull(acc1.pinSaltBase64)
        assertNotNull(acc1.pinHmacBase64)

        // Duplicate PIN registration must be rejected via HMAC check
        val duplicatePinUser = authRepository.registerAccount(
            username = "GamerTwo",
            pin = "8294",
            pinConfirm = "8294"
        )
        assertTrue(duplicatePinUser.isFailure)
        assertTrue(duplicatePinUser.exceptionOrNull()?.message?.contains("sudah terdaftar") == true)
    }

    @Test
    fun testLoginSuccessAndLockoutRateLimiting() {
        authRepository.registerAccount(
            username = "PlayerZX",
            pin = "9318",
            pinConfirm = "9318"
        )

        // Valid PIN login
        val loginSuccess = authRepository.loginWithPin("9318")
        assertTrue(loginSuccess.isSuccess)

        // 5 consecutive wrong PIN failures
        for (i in 1..4) {
            val fail = authRepository.loginWithPin("7721")
            assertTrue(fail.isFailure)
            assertFalse(authRepository.isLockedOut())
        }

        // 5th failure triggers lockout
        val fifthFail = authRepository.loginWithPin("7721")
        assertTrue(fifthFail.isFailure)
        assertTrue(authRepository.isLockedOut())
        assertTrue(authRepository.getRemainingLockoutSeconds() > 0)

        // Locked out state blocks subsequent attempts
        val blockedAttempt = authRepository.loginWithPin("9318")
        assertTrue(blockedAttempt.isFailure)
        assertTrue(blockedAttempt.exceptionOrNull()?.message?.contains("terkunci") == true)
    }

    @Test
    fun testPatternValidationAndLogin() {
        val pattern = listOf(0, 4, 8, 5, 2) // Valid non-trivial pattern
        val reg = authRepository.registerAccount(
            username = "PatternMaster",
            pin = "8492",
            pinConfirm = "8492",
            patternNodes = pattern
        )
        assertTrue(reg.isSuccess)

        val patternLoginSuccess = authRepository.loginWithPattern(pattern)
        assertTrue(patternLoginSuccess.isSuccess)

        val wrongPattern = listOf(0, 1, 2, 5, 8)
        val wrongLogin = authRepository.loginWithPattern(wrongPattern)
        assertTrue(wrongLogin.isFailure)
    }

    @Test
    fun testAccountRecovery() {
        authRepository.registerAccount(
            username = "RecoveryUser",
            pin = "4829",
            pinConfirm = "4829",
            securityQuestion = "Kota lahir?",
            securityAnswer = "Jakarta"
        )

        // Wrong answer fails
        val wrongRecovery = authRepository.recoverAccount("RecoveryUser", "Bandung", "7391", "7391")
        assertTrue(wrongRecovery.isFailure)

        // Correct answer resets PIN
        val successRecovery = authRepository.recoverAccount("RecoveryUser", "Jakarta", "7391", "7391")
        assertTrue(successRecovery.isSuccess)

        // Old PIN no longer works
        assertTrue(authRepository.loginWithPin("4829").isFailure)

        // New PIN works
        assertTrue(authRepository.loginWithPin("7391").isSuccess)
    }

    @Test
    fun testSensitivityMathCoreCalculations() {
        val bridge = SensitivityBridge()

        val testProfile = SensitivityProfile(
            id = "test_profile",
            name = "Test Spec",
            description = "Test Description",
            sensiX = 1.50f,
            sensiY = 1.00f,
            deadzone = 0.02f,
            smoothing = 0.10f,
            acceleration = 1.10f,
            curveType = SensitivityCurveType.DYNAMIC
        )
        bridge.setProfile(testProfile)

        // 1. Sub-deadzone movement should be suppressed to 0
        val tinyDelta = bridge.processDelta(0.1f, 0.1f, 0.016f)
        assertEquals(0.0f, tinyDelta.first, 0.001f)
        assertEquals(0.0f, tinyDelta.second, 0.001f)

        // 2. Clear movement above deadzone
        bridge.reset()
        val delta = bridge.processDelta(10.0f, 5.0f, 0.016f)
        assertTrue(delta.first > 0.0f)
        assertTrue(delta.second > 0.0f)

        // SensiX is 1.5x and SensiY is 1.0x, so transformed X:Y ratio should be higher than raw 10:5
        val rawRatio = 10.0f / 5.0f
        val transformedRatio = delta.first / delta.second
        assertTrue(transformedRatio > rawRatio)
    }

    @Test
    fun testSensitivityRepositoryProfileManagement() {
        val presets = sensitivityRepository.allProfiles.value
        assertTrue(presets.isNotEmpty())

        val base = presets.first()
        val custom = base.copy(name = "My Custom Aim", sensiX = 1.85f, sensiY = 1.45f)
        val saved = sensitivityRepository.saveCustomProfile(custom)

        assertEquals("My Custom Aim", saved.name)
        assertTrue(saved.isCustom)

        // Check persistence in repository
        val found = sensitivityRepository.allProfiles.value.find { it.id == saved.id }
        assertNotNull(found)
        assertEquals(1.85f, found?.sensiX ?: 0f, 0.01f)

        // Export and import JSON
        val exportedJson = sensitivityRepository.exportProfilesJson()
        assertTrue(exportedJson.contains("My Custom Aim"))

        val importRes = sensitivityRepository.importProfilesJson(exportedJson)
        assertTrue(importRes.isSuccess)
        assertTrue((importRes.getOrNull() ?: 0) > 0)

        // Deletion of custom profile
        val deleted = sensitivityRepository.deleteProfile(saved.id)
        assertTrue(deleted)
    }
}
