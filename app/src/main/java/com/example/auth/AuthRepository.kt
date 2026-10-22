package com.example.auth

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class AuthRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_AUTH, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_AUTH = "paseo_auth_secure_prefs"
        private const val KEY_ACCOUNTS_JSON = "user_accounts_data"
        private const val KEY_ACTIVE_USER_ID = "active_user_id"
        private const val KEY_APP_HMAC_SECRET = "app_hmac_secret_key"
        private const val KEY_APP_LOCKED = "is_app_locked"

        private const val PBKDF2_ITERATIONS = 10000
        private const val HASH_KEY_LENGTH = 256
        const val MAX_FAILED_ATTEMPTS = 5
        const val LOCKOUT_DURATION_MS = 30_000L // 30 seconds
    }

    init {
        // Initialize an app-level secret for keyed HMAC duplicate checking if not present
        if (!prefs.contains(KEY_APP_HMAC_SECRET)) {
            val secretBytes = ByteArray(32)
            SecureRandom().nextBytes(secretBytes)
            prefs.edit().putString(KEY_APP_HMAC_SECRET, bytesToHex(secretBytes)).apply()
        }
    }

    private fun getHmacSecret(): ByteArray {
        val hex = prefs.getString(KEY_APP_HMAC_SECRET, null) ?: "0123456789abcdef0123456789abcdef"
        return hexToBytes(hex)
    }

    fun isAppLocked(): Boolean {
        return prefs.getBoolean(KEY_APP_LOCKED, true)
    }

    fun setAppLocked(locked: Boolean) {
        prefs.edit().putBoolean(KEY_APP_LOCKED, locked).apply()
    }

    fun hasAnyAccount(): Boolean {
        return getAllAccounts().isNotEmpty()
    }

    fun getActiveAccount(): UserAccount? {
        val activeId = prefs.getString(KEY_ACTIVE_USER_ID, null)
        val all = getAllAccounts()
        return if (activeId != null) {
            all.firstOrNull { it.id == activeId } ?: all.firstOrNull()
        } else {
            all.firstOrNull()
        }
    }

    fun setActiveAccount(id: String) {
        prefs.edit().putString(KEY_ACTIVE_USER_ID, id).apply()
    }

    fun getAllAccounts(): List<UserAccount> {
        val jsonStr = prefs.getString(KEY_ACCOUNTS_JSON, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<UserAccount>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    UserAccount(
                        id = obj.getString("id"),
                        username = obj.getString("username"),
                        saltHex = obj.getString("saltHex"),
                        pinHash = if (obj.has("pinHash")) obj.optString("pinHash", null) else null,
                        hmacPinLookup = if (obj.has("hmacPinLookup")) obj.optString("hmacPinLookup", null) else null,
                        patternHash = if (obj.has("patternHash")) obj.optString("patternHash", null) else null,
                        recoveryQuestion = obj.optString("recoveryQuestion", "Game favorit Anda?"),
                        recoveryAnswerHash = obj.getString("recoveryAnswerHash"),
                        failedAttempts = obj.optInt("failedAttempts", 0),
                        lockUntilTimestamp = obj.optLong("lockUntilTimestamp", 0L),
                        preferredAuthMode = try {
                            AuthMode.valueOf(obj.optString("preferredAuthMode", AuthMode.PIN.name))
                        } catch (_: Exception) {
                            AuthMode.PIN
                        }
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveAccount(account: UserAccount) {
        val all = getAllAccounts().toMutableList()
        val index = all.indexOfFirst { it.id == account.id }
        if (index >= 0) {
            all[index] = account
        } else {
            all.add(account)
        }
        persistAccounts(all)
        setActiveAccount(account.id)
    }

    fun deleteAccount(accountId: String): Boolean {
        val all = getAllAccounts().toMutableList()
        val removed = all.removeAll { it.id == accountId }
        if (removed) {
            persistAccounts(all)
            val activeId = prefs.getString(KEY_ACTIVE_USER_ID, null)
            if (activeId == accountId) {
                prefs.edit().remove(KEY_ACTIVE_USER_ID).apply()
            }
        }
        return removed
    }

    private fun persistAccounts(accounts: List<UserAccount>) {
        val array = JSONArray()
        for (acc in accounts) {
            val obj = JSONObject().apply {
                put("id", acc.id)
                put("username", acc.username)
                put("saltHex", acc.saltHex)
                put("pinHash", acc.pinHash)
                put("hmacPinLookup", acc.hmacPinLookup)
                put("patternHash", acc.patternHash)
                put("recoveryQuestion", acc.recoveryQuestion)
                put("recoveryAnswerHash", acc.recoveryAnswerHash)
                put("failedAttempts", acc.failedAttempts)
                put("lockUntilTimestamp", acc.lockUntilTimestamp)
                put("preferredAuthMode", acc.preferredAuthMode.name)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_ACCOUNTS_JSON, array.toString()).apply()
    }

    // --- Cryptographic Hash & HMAC helpers ---

    fun generateSalt(): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return bytesToHex(salt)
    }

    fun hashCredential(credential: String, saltHex: String): String {
        val salt = hexToBytes(saltHex)
        val spec = PBEKeySpec(credential.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return bytesToHex(hash)
    }

    fun computeHmacPinLookup(pin: String): String {
        val secret = getHmacSecret()
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret, "HmacSHA256"))
        val digest = mac.doFinal(pin.toByteArray(Charsets.UTF_8))
        return bytesToHex(digest)
    }

    /**
     * Checks if this PIN is already registered by any user in the local database
     * without comparing or storing plaintext PINs.
     */
    fun isPinRegisteredInLocalDatabase(pin: String, excludeUserId: String? = null): Boolean {
        val targetLookup = computeHmacPinLookup(pin)
        return getAllAccounts()
            .filter { it.id != excludeUserId }
            .any { it.hmacPinLookup == targetLookup }
    }

    // --- Validation Rules ---

    fun validatePin(pin: String): ValidationResult {
        if (pin.length < 4 || pin.length > 8) {
            return ValidationResult.Invalid("Panjang PIN harus antara 4 sampai 8 digit angka.")
        }
        if (!pin.all { it.isDigit() }) {
            return ValidationResult.Invalid("PIN hanya boleh mengandung angka (0-9).")
        }
        // Weak PIN check: all identical digits (e.g. 1111, 0000, 777777)
        if (pin.all { it == pin[0] }) {
            return ValidationResult.Invalid("PIN terlalu lemah: tidak boleh angka berulang yang sama.")
        }
        // Weak PIN check: simple sequential sequences (e.g. 1234, 4321, 0123, 9876)
        val ascending = "0123456789012345"
        val descending = "9876543210987654"
        if (ascending.contains(pin) || descending.contains(pin)) {
            return ValidationResult.Invalid("PIN terlalu lemah: tidak boleh urutan angka berurutan sederhana.")
        }
        return ValidationResult.Valid
    }

    fun validatePattern(patternDots: List<Int>): ValidationResult {
        if (patternDots.size < 4) {
            return ValidationResult.Invalid("Pola terlalu pendek. Hubungkan minimal 4 titik.")
        }
        if (patternDots.size > 9) {
            return ValidationResult.Invalid("Pola tidak valid.")
        }
        // Check for duplicates
        if (patternDots.toSet().size != patternDots.size) {
            return ValidationResult.Invalid("Titik pada pola tidak boleh berulang.")
        }
        return ValidationResult.Valid
    }

    // --- Authentication Verification ---

    fun verifyPin(account: UserAccount, inputPin: String): AuthResult {
        val now = System.currentTimeMillis()
        if (now < account.lockUntilTimestamp) {
            val remainingSec = ((account.lockUntilTimestamp - now) / 1000).coerceAtLeast(1)
            return AuthResult.LockedOut(remainingSec)
        }

        val computedHash = hashCredential(inputPin, account.saltHex)
        if (computedHash == account.pinHash) {
            // Reset failed counter
            saveAccount(account.copy(failedAttempts = 0, lockUntilTimestamp = 0L))
            setAppLocked(false)
            return AuthResult.Success
        } else {
            val newAttempts = account.failedAttempts + 1
            val lockUntil = if (newAttempts >= MAX_FAILED_ATTEMPTS) {
                now + LOCKOUT_DURATION_MS
            } else 0L
            val updated = account.copy(failedAttempts = newAttempts, lockUntilTimestamp = lockUntil)
            saveAccount(updated)

            return if (newAttempts >= MAX_FAILED_ATTEMPTS) {
                AuthResult.LockedOut(LOCKOUT_DURATION_MS / 1000)
            } else {
                val remaining = MAX_FAILED_ATTEMPTS - newAttempts
                AuthResult.Failure("PIN yang Anda masukkan salah.", remaining)
            }
        }
    }

    fun verifyPattern(account: UserAccount, patternDots: List<Int>): AuthResult {
        val now = System.currentTimeMillis()
        if (now < account.lockUntilTimestamp) {
            val remainingSec = ((account.lockUntilTimestamp - now) / 1000).coerceAtLeast(1)
            return AuthResult.LockedOut(remainingSec)
        }

        val patternString = patternDots.joinToString("-")
        val computedHash = hashCredential(patternString, account.saltHex)
        if (computedHash == account.patternHash) {
            saveAccount(account.copy(failedAttempts = 0, lockUntilTimestamp = 0L))
            setAppLocked(false)
            return AuthResult.Success
        } else {
            val newAttempts = account.failedAttempts + 1
            val lockUntil = if (newAttempts >= MAX_FAILED_ATTEMPTS) {
                now + LOCKOUT_DURATION_MS
            } else 0L
            val updated = account.copy(failedAttempts = newAttempts, lockUntilTimestamp = lockUntil)
            saveAccount(updated)

            return if (newAttempts >= MAX_FAILED_ATTEMPTS) {
                AuthResult.LockedOut(LOCKOUT_DURATION_MS / 1000)
            } else {
                val remaining = MAX_FAILED_ATTEMPTS - newAttempts
                AuthResult.Failure("Pola yang Anda masukkan tidak cocok.", remaining)
            }
        }
    }

    fun verifyRecoveryAnswer(account: UserAccount, inputAnswer: String): Boolean {
        val normalized = inputAnswer.trim().lowercase()
        val computedHash = hashCredential(normalized, account.saltHex)
        return computedHash == account.recoveryAnswerHash
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

    private fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val reason: String) : ValidationResult()
}
