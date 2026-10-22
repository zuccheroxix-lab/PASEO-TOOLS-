package com.example.auth

enum class AuthMethod {
    PIN,
    PATTERN,
    BOTH
}

data class UserAccount(
    val id: String,
    val username: String,
    val pinSaltBase64: String,
    val pinHashBase64: String,
    val pinHmacBase64: String,
    val patternSaltBase64: String? = null,
    val patternHashBase64: String? = null,
    val securityQuestion: String = "Apa nama perangkat gaming favorit Anda?",
    val securityAnswerSaltBase64: String = "",
    val securityAnswerHashBase64: String = "",
    val preferredMethod: AuthMethod = AuthMethod.PIN,
    val createdAt: Long = System.currentTimeMillis()
)
