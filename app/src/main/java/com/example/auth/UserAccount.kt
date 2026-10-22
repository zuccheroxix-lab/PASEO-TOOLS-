package com.example.auth

import java.io.Serializable

data class UserAccount(
    val id: String,
    val username: String,
    val saltHex: String,
    val pinHash: String?,
    val hmacPinLookup: String?,
    val patternHash: String?,
    val recoveryQuestion: String,
    val recoveryAnswerHash: String,
    val failedAttempts: Int = 0,
    val lockUntilTimestamp: Long = 0L,
    val preferredAuthMode: AuthMode = AuthMode.PIN
) : Serializable

enum class AuthMode {
    PIN,
    PATTERN
}

enum class AuthScreenState {
    LOGIN_PIN,
    LOGIN_PATTERN,
    REGISTER_STEP1_PROFILE,
    REGISTER_STEP2_PIN,
    REGISTER_STEP3_PATTERN,
    REGISTER_STEP4_RECOVERY,
    FORGOT_CREDENTIALS,
    RESET_CREDENTIALS
}

sealed class AuthResult {
    object Success : AuthResult()
    data class Failure(val message: String, val remainingAttempts: Int? = null) : AuthResult()
    data class LockedOut(val waitSeconds: Long) : AuthResult()
}
