package com.example.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.PaseoBorderDark
import com.example.ui.theme.PaseoBorderGlow
import com.example.ui.theme.PaseoDarkBackground
import com.example.ui.theme.PaseoSurfaceCard
import com.example.ui.theme.PaseoSurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.util.UUID

@Composable
fun AuthScreen(
    authRepository: AuthRepository,
    onAuthSuccess: () -> Unit
) {
    val accounts = remember { authRepository.getAllAccounts() }
    val hasAccounts = accounts.isNotEmpty()

    var activeAccount by remember { mutableStateOf(authRepository.getActiveAccount()) }
    var currentScreenState by remember {
        mutableStateOf(
            if (!hasAccounts) AuthScreenState.REGISTER_STEP1_PROFILE
            else if (activeAccount?.preferredAuthMode == AuthMode.PATTERN && activeAccount?.patternHash != null)
                AuthScreenState.LOGIN_PATTERN
            else
                AuthScreenState.LOGIN_PIN
        )
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var lockoutSeconds by remember { mutableIntStateOf(0) }

    // Lockout countdown timer
    LaunchedEffect(lockoutSeconds) {
        if (lockoutSeconds > 0) {
            delay(1000L)
            lockoutSeconds -= 1
        }
    }

    // PIN State
    var enteredPin by remember { mutableStateOf("") }

    // Registration Temp State
    var regUsername by remember { mutableStateOf("") }
    var regPin by remember { mutableStateOf("") }
    var regPinConfirm by remember { mutableStateOf("") }
    var regPattern by remember { mutableStateOf<List<Int>>(emptyList()) }
    var regPatternConfirm by remember { mutableStateOf<List<Int>>(emptyList()) }
    var regQuestion by remember { mutableStateOf("Game favorit Anda?") }
    var regAnswer by remember { mutableStateOf("") }

    // Recovery State
    var recoveryAnswerInput by remember { mutableStateOf("") }
    var resetNewPin by remember { mutableStateOf("") }
    var resetNewPinConfirm by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PaseoDarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // App Branding & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(listOf(CrimsonPrimary, CrimsonContainer))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Shield Icon",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "PASEO SENSI",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "SECURITY GATEWAY • BY ZUCCHERO XANN",
                        fontSize = 10.sp,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error or Success Banner
            AnimatedVisibility(visible = errorMessage != null, enter = fadeIn(), exit = fadeOut()) {
                errorMessage?.let { msg ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0x33FF1744)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(1.dp, Color(0x66FF1744), RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = msg,
                            color = Color(0xFFFF8A80),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            AnimatedVisibility(visible = successMessage != null, enter = fadeIn(), exit = fadeOut()) {
                successMessage?.let { msg ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0x3300E676)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(1.dp, Color(0x6600E676), RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = msg,
                            color = EmeraldAccent,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (lockoutSeconds > 0) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x44FF3333)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .border(1.dp, Color(0xFFFF3333), RoundedCornerShape(14.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Terkunci",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Akses Ditangguhkan Sementara",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Terlalu banyak percobaan salah. Coba lagi dalam $lockoutSeconds detik.",
                            color = Color(0xFFFFCDD2),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            when (currentScreenState) {
                // ==========================================
                // 1. LOGIN DENGAN PIN
                // ==========================================
                AuthScreenState.LOGIN_PIN -> {
                    val user = activeAccount
                    Text(
                        text = "Masuk dengan PIN",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Pengguna: ${user?.username ?: "Paseo Master"}",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    // Masked PIN bullets (never shows numbers)
                    PinDotsRow(
                        pinLength = enteredPin.length,
                        maxDots = 6
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Numeric Keypad
                    NumericKeypad(
                        enabled = lockoutSeconds == 0,
                        onNumberClick = { num ->
                            errorMessage = null
                            if (enteredPin.length < 8) {
                                enteredPin += num
                                if (user != null && enteredPin.length >= 4) {
                                    // Automatic quick validation if matches 4-6 digits
                                    val result = authRepository.verifyPin(user, enteredPin)
                                    if (result is AuthResult.Success) {
                                        onAuthSuccess()
                                    } else if (enteredPin.length >= 6) {
                                        // Process explicit failure if 6 digits reached
                                        handleAuthResult(result) { err, lock ->
                                            errorMessage = err
                                            if (lock != null) lockoutSeconds = lock.toInt()
                                            enteredPin = ""
                                        }
                                    }
                                }
                            }
                        },
                        onBackspace = {
                            if (enteredPin.isNotEmpty()) {
                                enteredPin = enteredPin.dropLast(1)
                            }
                        },
                        onConfirm = {
                            if (user != null) {
                                val result = authRepository.verifyPin(user, enteredPin)
                                if (result is AuthResult.Success) {
                                    onAuthSuccess()
                                } else {
                                    handleAuthResult(result) { err, lock ->
                                        errorMessage = err
                                        if (lock != null) lockoutSeconds = lock.toInt()
                                        enteredPin = ""
                                    }
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (user?.patternHash != null) {
                            TextButton(
                                onClick = {
                                    errorMessage = null
                                    enteredPin = ""
                                    currentScreenState = AuthScreenState.LOGIN_PATTERN
                                }
                            ) {
                                Icon(Icons.Default.GridOn, contentDescription = null, tint = CrimsonGlow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gunakan Pola", color = CrimsonGlow, fontSize = 12.sp)
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        TextButton(
                            onClick = {
                                errorMessage = null
                                enteredPin = ""
                                currentScreenState = AuthScreenState.FORGOT_CREDENTIALS
                            }
                        ) {
                            Text("Lupa PIN?", color = TextMuted, fontSize = 12.sp)
                        }
                    }

                    TextButton(
                        onClick = {
                            errorMessage = null
                            enteredPin = ""
                            currentScreenState = AuthScreenState.REGISTER_STEP1_PROFILE
                        }
                    ) {
                        Text("Daftar Akun Baru", color = TextSecondary, fontSize = 12.sp)
                    }
                }

                // ==========================================
                // 2. LOGIN DENGAN POLA
                // ==========================================
                AuthScreenState.LOGIN_PATTERN -> {
                    val user = activeAccount
                    Text(
                        text = "Masuk dengan Pola",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Tarik garis pola penguncian Anda",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    PatternLockView(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(vertical = 8.dp),
                        enabled = lockoutSeconds == 0,
                        isError = errorMessage != null,
                        onPatternCompleted = { patternDots ->
                            errorMessage = null
                            if (user != null) {
                                val result = authRepository.verifyPattern(user, patternDots)
                                if (result is AuthResult.Success) {
                                    onAuthSuccess()
                                } else {
                                    handleAuthResult(result) { err, lock ->
                                        errorMessage = err
                                        if (lock != null) lockoutSeconds = lock.toInt()
                                    }
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = {
                                errorMessage = null
                                currentScreenState = AuthScreenState.LOGIN_PIN
                            }
                        ) {
                            Icon(Icons.Default.Pin, contentDescription = null, tint = CrimsonGlow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gunakan PIN", color = CrimsonGlow, fontSize = 12.sp)
                        }

                        TextButton(
                            onClick = {
                                errorMessage = null
                                currentScreenState = AuthScreenState.FORGOT_CREDENTIALS
                            }
                        ) {
                            Text("Lupa Pola?", color = TextMuted, fontSize = 12.sp)
                        }
                    }

                    TextButton(
                        onClick = {
                            errorMessage = null
                            currentScreenState = AuthScreenState.REGISTER_STEP1_PROFILE
                        }
                    ) {
                        Text("Daftar Akun Baru", color = TextSecondary, fontSize = 12.sp)
                    }
                }

                // ==========================================
                // 3. REGISTRASI — STEP 1: USERNAME
                // ==========================================
                AuthScreenState.REGISTER_STEP1_PROFILE -> {
                    Text(
                        text = "Registrasi Akun Baru",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Langkah 1 dari 4: Nama Pengguna",
                        fontSize = 12.sp,
                        color = CrimsonGlow,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = PaseoSurfaceCard),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PaseoBorderDark, RoundedCornerShape(16.dp))
                            .padding(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = regUsername,
                                onValueChange = { regUsername = it },
                                label = { Text("Nama Pengguna / Nickname") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reg_username_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CrimsonPrimary,
                                    unfocusedBorderColor = PaseoBorderDark,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Keamanan Kredensial:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "• PIN dan Pola disimpan dalam bentuk salted PBKDF2 hash, bukan teks biasa.\n• Keunikan PIN diperiksa melalui HMAC berkunci pada database lokal.\n• Sistem ini hanya untuk keamanan internal aplikasi PASEO SENSI dan tidak mengubah kunci lockscreen Android Anda.",
                                fontSize = 11.sp,
                                color = TextMuted,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (regUsername.trim().length < 3) {
                                errorMessage = "Nama pengguna minimal 3 karakter."
                            } else {
                                errorMessage = null
                                currentScreenState = AuthScreenState.REGISTER_STEP2_PIN
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("reg_step1_next_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Lanjutkan ke Pembuatan PIN", fontWeight = FontWeight.Bold)
                    }

                    if (hasAccounts) {
                        TextButton(
                            onClick = {
                                errorMessage = null
                                currentScreenState = AuthScreenState.LOGIN_PIN
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Sudah punya akun? Masuk", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }

                // ==========================================
                // 4. REGISTRASI — STEP 2: PIN & KONFIRMASI
                // ==========================================
                AuthScreenState.REGISTER_STEP2_PIN -> {
                    Text(
                        text = "Buat PIN Keamanan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Langkah 2 dari 4: PIN 4-8 Digit",
                        fontSize = 12.sp,
                        color = CrimsonGlow,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    OutlinedTextField(
                        value = regPin,
                        onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) regPin = it },
                        label = { Text("PIN Baru (4-8 Digit)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_pin_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonPrimary,
                            unfocusedBorderColor = PaseoBorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = regPinConfirm,
                        onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) regPinConfirm = it },
                        label = { Text("Konfirmasi PIN Baru") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_pin_confirm_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonPrimary,
                            unfocusedBorderColor = PaseoBorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Aturan Keamanan PIN:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Text(
                        text = "• Ditolak jika angka berulang (misal: 1111, 7777)\n• Ditolak jika urutan sederhana (misal: 1234, 4321)\n• Ditolak jika PIN sudah terdaftar pada database lokal",
                        fontSize = 11.sp,
                        color = TextMuted,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val validation = authRepository.validatePin(regPin)
                            if (validation is ValidationResult.Invalid) {
                                errorMessage = validation.reason
                            } else if (regPin != regPinConfirm) {
                                errorMessage = "PIN baru dan Konfirmasi PIN tidak cocok!"
                            } else if (authRepository.isPinRegisteredInLocalDatabase(regPin)) {
                                errorMessage = "PIN ini sudah terdaftar pada database lokal. Harap gunakan kombinasi lain demi keamanan."
                            } else {
                                errorMessage = null
                                currentScreenState = AuthScreenState.REGISTER_STEP3_PATTERN
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("reg_step2_next_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Lanjutkan ke Pola Penguncian", fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = { currentScreenState = AuthScreenState.REGISTER_STEP1_PROFILE }
                    ) {
                        Text("Kembali", color = TextMuted)
                    }
                }

                // ==========================================
                // 5. REGISTRASI — STEP 3: POLA (Pattern)
                // ==========================================
                AuthScreenState.REGISTER_STEP3_PATTERN -> {
                    val isFirstPattern = regPattern.isEmpty()
                    Text(
                        text = if (isFirstPattern) "Buat Pola Baru" else "Konfirmasi Pola",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (isFirstPattern)
                            "Langkah 3 dari 4: Hubungkan minimal 4 titik"
                        else
                            "Tarik kembali pola yang sama untuk konfirmasi",
                        fontSize = 12.sp,
                        color = CrimsonGlow,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    PatternLockView(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(vertical = 8.dp),
                        isError = errorMessage != null,
                        onPatternCompleted = { patternDots ->
                            errorMessage = null
                            if (isFirstPattern) {
                                val validation = authRepository.validatePattern(patternDots)
                                if (validation is ValidationResult.Invalid) {
                                    errorMessage = validation.reason
                                } else {
                                    regPattern = patternDots
                                    successMessage = "Pola pertama tersimpan. Ulangi untuk konfirmasi."
                                }
                            } else {
                                if (patternDots != regPattern) {
                                    errorMessage = "Pola konfirmasi tidak cocok. Ulangi dari awal."
                                    regPattern = emptyList()
                                } else {
                                    regPatternConfirm = patternDots
                                    successMessage = "Pola valid dan terkonfirmasi!"
                                    currentScreenState = AuthScreenState.REGISTER_STEP4_RECOVERY
                                }
                            }
                        }
                    )

                    if (!isFirstPattern) {
                        TextButton(
                            onClick = {
                                regPattern = emptyList()
                                regPatternConfirm = emptyList()
                                errorMessage = null
                                successMessage = null
                            }
                        ) {
                            Text("Reset dan Gambar Ulang Pola", color = CrimsonGlow, fontSize = 12.sp)
                        }
                    }

                    TextButton(
                        onClick = { currentScreenState = AuthScreenState.REGISTER_STEP2_PIN }
                    ) {
                        Text("Kembali ke PIN", color = TextMuted)
                    }
                }

                // ==========================================
                // 6. REGISTRASI — STEP 4: RECOVERY QUESTION
                // ==========================================
                AuthScreenState.REGISTER_STEP4_RECOVERY -> {
                    Text(
                        text = "Pertanyaan Pemulihan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Langkah 4 dari 4: Digunakan saat Anda lupa PIN/Pola",
                        fontSize = 12.sp,
                        color = CrimsonGlow,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = PaseoSurfaceCard),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PaseoBorderDark, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Pilih / Tulis Pertanyaan Keamanan:",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = regQuestion,
                                onValueChange = { regQuestion = it },
                                label = { Text("Pertanyaan Keamanan") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CrimsonPrimary,
                                    unfocusedBorderColor = PaseoBorderDark,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = regAnswer,
                                onValueChange = { regAnswer = it },
                                label = { Text("Jawaban Rahasia") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reg_answer_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CrimsonPrimary,
                                    unfocusedBorderColor = PaseoBorderDark,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Text(
                                text = "Jawaban akan di-hash secara aman bersama salt unik.",
                                fontSize = 10.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (regAnswer.trim().length < 2) {
                                errorMessage = "Jawaban keamanan minimal 2 karakter."
                            } else {
                                val salt = authRepository.generateSalt()
                                val pinHashed = authRepository.hashCredential(regPin, salt)
                                val hmacLookup = authRepository.computeHmacPinLookup(regPin)
                                val patternString = regPattern.joinToString("-")
                                val patternHashed = authRepository.hashCredential(patternString, salt)
                                val answerHashed = authRepository.hashCredential(regAnswer.trim().lowercase(), salt)

                                val newAccount = UserAccount(
                                    id = UUID.randomUUID().toString(),
                                    username = regUsername.trim(),
                                    saltHex = salt,
                                    pinHash = pinHashed,
                                    hmacPinLookup = hmacLookup,
                                    patternHash = patternHashed,
                                    recoveryQuestion = regQuestion.trim(),
                                    recoveryAnswerHash = answerHashed,
                                    preferredAuthMode = AuthMode.PIN
                                )

                                authRepository.saveAccount(newAccount)
                                authRepository.setAppLocked(false)
                                successMessage = "Akun berhasil didaftarkan!"
                                onAuthSuccess()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("reg_finish_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Selesaikan Registrasi", fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = { currentScreenState = AuthScreenState.REGISTER_STEP3_PATTERN }
                    ) {
                        Text("Kembali", color = TextMuted)
                    }
                }

                // ==========================================
                // 7. LUPA PIN / PEMULIHAN AKUN
                // ==========================================
                AuthScreenState.FORGOT_CREDENTIALS -> {
                    val user = activeAccount
                    Text(
                        text = "Pemulihan Akun PASEO SENSI",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Jawab pertanyaan keamanan akun Anda untuk reset",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = PaseoSurfaceCard),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PaseoBorderDark, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Pertanyaan:",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            Text(
                                text = user?.recoveryQuestion ?: "Game favorit Anda?",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = recoveryAnswerInput,
                                onValueChange = { recoveryAnswerInput = it },
                                label = { Text("Jawaban Anda") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("recovery_answer_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CrimsonPrimary,
                                    unfocusedBorderColor = PaseoBorderDark,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (user != null && authRepository.verifyRecoveryAnswer(user, recoveryAnswerInput)) {
                                errorMessage = null
                                successMessage = "Verifikasi berhasil! Silakan buat PIN baru."
                                currentScreenState = AuthScreenState.RESET_CREDENTIALS
                            } else {
                                errorMessage = "Jawaban keamanan salah. Akses pemulihan ditolak."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("verify_recovery_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.LockReset, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verifikasi Jawaban", fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = {
                            errorMessage = null
                            currentScreenState = AuthScreenState.LOGIN_PIN
                        }
                    ) {
                        Text("Batal & Kembali ke Login", color = TextMuted)
                    }
                }

                // ==========================================
                // 8. RESET KREDENSIAL BARU
                // ==========================================
                AuthScreenState.RESET_CREDENTIALS -> {
                    val user = activeAccount
                    Text(
                        text = "Setel Ulang PIN",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Masukkan PIN baru 4-8 digit angka",
                        fontSize = 12.sp,
                        color = CrimsonGlow,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    OutlinedTextField(
                        value = resetNewPin,
                        onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) resetNewPin = it },
                        label = { Text("PIN Baru (4-8 Digit)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonPrimary,
                            unfocusedBorderColor = PaseoBorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = resetNewPinConfirm,
                        onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) resetNewPinConfirm = it },
                        label = { Text("Konfirmasi PIN Baru") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonPrimary,
                            unfocusedBorderColor = PaseoBorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val validation = authRepository.validatePin(resetNewPin)
                            if (validation is ValidationResult.Invalid) {
                                errorMessage = validation.reason
                            } else if (resetNewPin != resetNewPinConfirm) {
                                errorMessage = "PIN baru dan Konfirmasi PIN tidak cocok!"
                            } else if (authRepository.isPinRegisteredInLocalDatabase(resetNewPin, excludeUserId = user?.id)) {
                                errorMessage = "PIN ini sudah terdaftar pada database lokal. Harap pilih kombinasi lain."
                            } else if (user != null) {
                                val newSalt = authRepository.generateSalt()
                                val pinHash = authRepository.hashCredential(resetNewPin, newSalt)
                                val hmacLookup = authRepository.computeHmacPinLookup(resetNewPin)
                                val updated = user.copy(
                                    saltHex = newSalt,
                                    pinHash = pinHash,
                                    hmacPinLookup = hmacLookup,
                                    failedAttempts = 0,
                                    lockUntilTimestamp = 0L
                                )
                                authRepository.saveAccount(updated)
                                authRepository.setAppLocked(false)
                                successMessage = "PIN berhasil diperbarui!"
                                onAuthSuccess()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simpan PIN Baru", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private inline fun handleAuthResult(
    result: AuthResult,
    crossinline onError: (String, Long?) -> Unit
) {
    when (result) {
        is AuthResult.Success -> {}
        is AuthResult.Failure -> {
            val note = if (result.remainingAttempts != null) {
                "${result.message} Sisa percobaan: ${result.remainingAttempts}"
            } else {
                result.message
            }
            onError(note, null)
        }
        is AuthResult.LockedOut -> {
            onError("Terlalu banyak percobaan gagal. Aplikasi dikunci sementara.", result.waitSeconds)
        }
    }
}

@Composable
private fun PinDotsRow(pinLength: Int, maxDots: Int = 6) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val count = maxDots.coerceAtLeast(4)
        for (i in 0 until count) {
            val isFilled = i < pinLength
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFilled) CrimsonPrimary else PaseoBorderDark
                    )
                    .border(
                        1.5.dp,
                        if (isFilled) CrimsonGlow else Color(0xFF3A3B4D),
                        CircleShape
                    )
            )
        }
    }
}

@Composable
private fun NumericKeypad(
    enabled: Boolean,
    onNumberClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onConfirm: () -> Unit
) {
    val keyRows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("C", "0", "DEL")
    )

    Column(
        modifier = Modifier.fillMaxWidth(0.85f),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keyRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (key == "C" || key == "DEL") PaseoSurfaceCard else PaseoSurfaceDark
                            )
                            .border(
                                1.dp,
                                if (key == "C" || key == "DEL") PaseoBorderGlow.copy(alpha = 0.5f) else PaseoBorderDark,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable(enabled = enabled) {
                                when (key) {
                                    "DEL" -> onBackspace()
                                    "C" -> onConfirm()
                                    else -> onNumberClick(key)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (key == "DEL") {
                            Icon(
                                imageVector = Icons.Default.Backspace,
                                contentDescription = "Hapus",
                                tint = if (enabled) Color(0xFFFF8A80) else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        } else if (key == "C") {
                            Text(
                                text = "OK",
                                color = if (enabled) CrimsonGlow else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        } else {
                            Text(
                                text = key,
                                color = if (enabled) Color.White else Color.Gray,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
