package com.example.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrimsonBorder
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.PaseoCardBackground
import com.example.ui.theme.PaseoDarkBackground
import kotlinx.coroutines.delay

enum class AuthMode {
    LOGIN,
    REGISTER,
    RECOVER
}

enum class LoginType {
    PIN,
    PATTERN
}

@Composable
fun AuthScreen(
    authRepository: AuthRepository,
    onAuthSuccess: (UserAccount) -> Unit
) {
    val accounts = remember { authRepository.getAccounts() }
    val hasAccounts = accounts.isNotEmpty()

    var currentMode by remember {
        mutableStateOf(if (hasAccounts) AuthMode.LOGIN else AuthMode.REGISTER)
    }
    var loginType by remember { mutableStateOf(LoginType.PIN) }

    // Lockout countdown state
    var remainingLockout by remember { mutableLongStateOf(authRepository.getRemainingLockoutSeconds()) }
    LaunchedEffect(remainingLockout) {
        if (remainingLockout > 0) {
            delay(1000)
            remainingLockout = authRepository.getRemainingLockoutSeconds()
        }
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PaseoDarkBackground,
                        Color(0xFF15080E),
                        PaseoDarkBackground
                    )
                )
            )
            .testTag("auth_screen_root"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Logo & Branding
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(CrimsonContainer)
                    .border(2.dp, CrimsonGlow, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "PASEO Security Shield",
                    tint = CrimsonGlow,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "PASEO SENSI",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = Color.White
            )

            Text(
                text = "INTERNAL SECURITY & ACCESS CONTROL",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                color = CrimsonGlow
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Lockout Banner
            if (remainingLockout > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3B1015)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "APLIKASI TERKUNCI SEMENTARA",
                            color = Color(0xFFFF6666),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Terlalu banyak percobaan gagal. Silakan tunggu $remainingLockout detik.",
                            color = Color.White,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Error or Success Banner
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3D1414)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFFF7777),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (successMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F3018)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = successMessage!!,
                        color = Color(0xFF66FF88),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Main Content Card depending on mode
            when (currentMode) {
                AuthMode.LOGIN -> {
                    LoginContent(
                        authRepository = authRepository,
                        loginType = loginType,
                        isLockedOut = remainingLockout > 0,
                        onLoginTypeChange = { loginType = it },
                        onSuccess = { acc ->
                            errorMessage = null
                            onAuthSuccess(acc)
                        },
                        onError = {
                            errorMessage = it
                            remainingLockout = authRepository.getRemainingLockoutSeconds()
                        },
                        onNavigateRegister = {
                            errorMessage = null
                            successMessage = null
                            currentMode = AuthMode.REGISTER
                        },
                        onNavigateRecover = {
                            errorMessage = null
                            successMessage = null
                            currentMode = AuthMode.RECOVER
                        }
                    )
                }
                AuthMode.REGISTER -> {
                    RegisterContent(
                        authRepository = authRepository,
                        onSuccess = { newAcc ->
                            errorMessage = null
                            successMessage = "Akun '${newAcc.username}' berhasil didaftarkan!"
                            currentMode = AuthMode.LOGIN
                        },
                        onError = { errorMessage = it },
                        onCancel = if (hasAccounts) {
                            {
                                errorMessage = null
                                currentMode = AuthMode.LOGIN
                            }
                        } else null
                    )
                }
                AuthMode.RECOVER -> {
                    RecoverContent(
                        authRepository = authRepository,
                        onSuccess = {
                            errorMessage = null
                            successMessage = "PIN berhasil dipulihkan! Silakan login dengan PIN baru."
                            currentMode = AuthMode.LOGIN
                        },
                        onError = { errorMessage = it },
                        onCancel = {
                            errorMessage = null
                            currentMode = AuthMode.LOGIN
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginContent(
    authRepository: AuthRepository,
    loginType: LoginType,
    isLockedOut: Boolean,
    onLoginTypeChange: (LoginType) -> Unit,
    onSuccess: (UserAccount) -> Unit,
    onError: (String) -> Unit,
    onNavigateRegister: () -> Unit,
    onNavigateRecover: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PaseoCardBackground),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CrimsonBorder, Color(0xFF2A2026))))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode Selector Tabs (PIN vs Pattern)
            TabRow(
                selectedTabIndex = if (loginType == LoginType.PIN) 0 else 1,
                containerColor = Color(0xFF1E151A),
                contentColor = CrimsonGlow,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[if (loginType == LoginType.PIN) 0 else 1]),
                        color = CrimsonPrimary
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .padding(bottom = 18.dp)
            ) {
                Tab(
                    selected = loginType == LoginType.PIN,
                    onClick = { onLoginTypeChange(LoginType.PIN) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Pin, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("LOGIN PIN", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = loginType == LoginType.PATTERN,
                    onClick = { onLoginTypeChange(LoginType.PATTERN) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Gesture, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("LOGIN POLA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            if (loginType == LoginType.PIN) {
                PinLoginSection(
                    isLockedOut = isLockedOut,
                    onPinSubmitted = { enteredPin ->
                        val result = authRepository.loginWithPin(enteredPin)
                        result.onSuccess(onSuccess).onFailure { onError(it.localizedMessage ?: "Login gagal") }
                    }
                )
            } else {
                PatternLoginSection(
                    isLockedOut = isLockedOut,
                    onPatternSubmitted = { patternNodes ->
                        val result = authRepository.loginWithPattern(patternNodes)
                        result.onSuccess(onSuccess).onFailure { onError(it.localizedMessage ?: "Pola salah") }
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onNavigateRecover,
                    modifier = Modifier.testTag("forgot_pin_button")
                ) {
                    Text("Lupa PIN?", color = Color(0xFFB0B0C0), fontSize = 12.sp)
                }

                TextButton(
                    onClick = onNavigateRegister,
                    modifier = Modifier.testTag("register_new_account_button")
                ) {
                    Text("Daftar Akun Baru", color = CrimsonGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PinLoginSection(
    isLockedOut: Boolean,
    onPinSubmitted: (String) -> Unit
) {
    var pinValue by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Bullet Indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            val maxDisplay = 6
            for (i in 0 until maxDisplay) {
                val isFilled = i < pinValue.length
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (isFilled) CrimsonGlow else Color(0xFF2A2A38))
                        .border(1.dp, if (isFilled) CrimsonPrimary else Color(0xFF444455), CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Virtual Keypad
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("C", "0", "DEL")
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            for (row in keys) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (k in row) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E1E28))
                                .border(1.dp, Color(0xFF333344), RoundedCornerShape(12.dp))
                                .clickable(enabled = !isLockedOut) {
                                    when (k) {
                                        "C" -> pinValue = ""
                                        "DEL" -> if (pinValue.isNotEmpty()) pinValue = pinValue.dropLast(1)
                                        else -> {
                                            if (pinValue.length < 8) {
                                                val next = pinValue + k
                                                pinValue = next
                                                if (next.length in 4..8) {
                                                    // Trigger auto-submit or user press submit
                                                }
                                            }
                                        }
                                    }
                                }
                                .testTag("keypad_key_$k"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (k == "DEL") {
                                Icon(Icons.Default.Backspace, contentDescription = "Hapus", tint = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Text(
                                    text = k,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (k == "C") Color(0xFFFF6666) else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                if (pinValue.isNotBlank()) {
                    onPinSubmitted(pinValue)
                }
            },
            enabled = !isLockedOut && pinValue.length >= 4,
            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("submit_pin_login_button"),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("MASUK DENGAN PIN", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun PatternLoginSection(
    isLockedOut: Boolean,
    onPatternSubmitted: (List<Int>) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Hubungkan titik-titik pola untuk masuk",
            color = Color(0xFFAAAAAA),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )

        PatternLockView(
            enabled = !isLockedOut,
            onPatternComplete = onPatternSubmitted
        )
    }
}

@Composable
private fun RegisterContent(
    authRepository: AuthRepository,
    onSuccess: (UserAccount) -> Unit,
    onError: (String) -> Unit,
    onCancel: (() -> Unit)?
) {
    var username by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var pinConfirm by remember { mutableStateOf("") }
    var securityQuestion by remember { mutableStateOf("Apa nama perangkat gaming favorit Anda?") }
    var securityAnswer by remember { mutableStateOf("") }

    var recordPattern by remember { mutableStateOf(false) }
    var patternStep by remember { mutableIntStateOf(0) } // 0: not started, 1: draw first, 2: confirm
    var firstPattern by remember { mutableStateOf<List<Int>>(emptyList()) }
    var confirmedPattern by remember { mutableStateOf<List<Int>>(emptyList()) }

    var pinVisible by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PaseoCardBackground),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CrimsonBorder, Color(0xFF2A2026))))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "REGISTRASI AKUN BARU",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )

            Text(
                text = "Kredensial disimpan terenkripsi dengan PBKDF2 & HMAC",
                fontSize = 11.sp,
                color = Color(0xFFAAAAAA),
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
            )

            // Username Field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username Akun") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_username_input"),
                colors = outlinedTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // PIN Field
            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) pin = it },
                label = { Text("PIN Baru (4-8 Digit)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { pinVisible = !pinVisible }) {
                        Icon(if (pinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = Color.Gray)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_pin_input"),
                colors = outlinedTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // PIN Confirm Field
            OutlinedTextField(
                value = pinConfirm,
                onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) pinConfirm = it },
                label = { Text("Konfirmasi PIN") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_pin_confirm_input"),
                colors = outlinedTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Security Question & Answer
            OutlinedTextField(
                value = securityQuestion,
                onValueChange = { securityQuestion = it },
                label = { Text("Pertanyaan Pemulihan") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_security_question_input"),
                colors = outlinedTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = securityAnswer,
                onValueChange = { securityAnswer = it },
                label = { Text("Jawaban Pemulihan") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_security_answer_input"),
                colors = outlinedTextFieldColors()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Optional Pattern Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E1A22))
                    .clickable { recordPattern = !recordPattern }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Gesture, contentDescription = null, tint = CrimsonGlow, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Tambahkan Kunci Pola", color = Color.White, fontSize = 13.sp)
                }
                Icon(
                    imageVector = if (recordPattern) Icons.Default.Check else Icons.Default.Pin,
                    contentDescription = null,
                    tint = if (recordPattern) CrimsonGlow else Color.Gray
                )
            }

            AnimatedVisibility(visible = recordPattern) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    val statusText = when {
                        firstPattern.isEmpty() -> "Gambar pola pertama kali (min. 4 titik)"
                        confirmedPattern.isEmpty() -> "Ulangi pola untuk konfirmasi"
                        firstPattern == confirmedPattern -> "Pola cocok dan siap disimpan!"
                        else -> "Pola tidak cocok! Silakan coba lagi."
                    }

                    Text(statusText, color = CrimsonGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    PatternLockView(
                        onPatternComplete = { drawn ->
                            if (firstPattern.isEmpty()) {
                                firstPattern = drawn
                            } else if (confirmedPattern.isEmpty()) {
                                if (firstPattern == drawn) {
                                    confirmedPattern = drawn
                                } else {
                                    firstPattern = emptyList()
                                    confirmedPattern = emptyList()
                                }
                            }
                        }
                    )

                    if (firstPattern.isNotEmpty()) {
                        TextButton(onClick = {
                            firstPattern = emptyList()
                            confirmedPattern = emptyList()
                        }) {
                            Text("Reset Pola", color = Color(0xFFFF6666), fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons
            Button(
                onClick = {
                    if (recordPattern && confirmedPattern.isEmpty()) {
                        onError("Harap selesaikan pembuatan dan konfirmasi pola.")
                        return@Button
                    }
                    val patternToSave = if (recordPattern) confirmedPattern else null
                    val res = authRepository.registerAccount(
                        username = username,
                        pin = pin,
                        pinConfirm = pinConfirm,
                        patternNodes = patternToSave,
                        securityQuestion = securityQuestion,
                        securityAnswer = securityAnswer
                    )
                    res.onSuccess(onSuccess).onFailure { onError(it.localizedMessage ?: "Registrasi gagal") }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("submit_register_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("SIMPAN & DAFTARKAN AKUN", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            if (onCancel != null) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("cancel_register_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Batal", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun RecoverContent(
    authRepository: AuthRepository,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    onCancel: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var newPinConfirm by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PaseoCardBackground),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CrimsonBorder, Color(0xFF2A2026))))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PEMULIHAN AKUN & RESET PIN",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )

            Text(
                text = "Masukkan jawaban pertanyaan pemulihan yang telah didaftarkan",
                fontSize = 11.sp,
                color = Color(0xFFAAAAAA),
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username Akun") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recover_username_input"),
                colors = outlinedTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = answer,
                onValueChange = { answer = it },
                label = { Text("Jawaban Pertanyaan Pemulihan") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recover_answer_input"),
                colors = outlinedTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = newPin,
                onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) newPin = it },
                label = { Text("PIN Baru") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recover_new_pin_input"),
                colors = outlinedTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = newPinConfirm,
                onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) newPinConfirm = it },
                label = { Text("Konfirmasi PIN Baru") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recover_new_pin_confirm_input"),
                colors = outlinedTextFieldColors()
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    val res = authRepository.recoverAccount(username, answer, newPin, newPinConfirm)
                    res.onSuccess { onSuccess() }.onFailure { onError(it.localizedMessage ?: "Pemulihan gagal") }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("submit_recovery_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("RESET DAN PULIHKAN PIN", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("cancel_recovery_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Kembali ke Login", color = Color.White)
            }
        }
    }
}

@Composable
private fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CrimsonPrimary,
    unfocusedBorderColor = Color(0xFF3E323A),
    focusedLabelColor = CrimsonGlow,
    unfocusedLabelColor = Color(0xFFAAAAAA),
    cursorColor = CrimsonGlow,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)
