package com.example.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MoneyGreen
import com.example.ui.theme.NeutralSlate
import com.example.ui.theme.SpendRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isRegisterMode by remember { mutableStateOf(false) }

    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // Access the shared preferences for credentials database simulation
    val sharedPrefs = remember {
        context.getSharedPreferences("student_finance_prefs", Context.MODE_PRIVATE)
    }

    // Default Demo user initialization (seeded into SharedPreferences if not exists)
    LaunchedEffect(Unit) {
        val defaultEmail = "budi.raharjo@mahasiswa.ac.id"
        if (!sharedPrefs.contains("reg_pwd_$defaultEmail")) {
            sharedPrefs.edit()
                .putString("reg_pwd_$defaultEmail", "budi123")
                .putString("reg_name_$defaultEmail", "Budi Raharjo")
                .putString("reg_initials_$defaultEmail", "BR")
                .apply()
        }
    }

    // Solid custom gradient background
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4F46E5), // Deep Indigo
                        Color(0xFF1E1B4B)  // Very Deep Dark Navy Indigo
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // App Icon Graphic
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .drawBehind {
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.25f),
                            size = size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx())
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Welcome Text
            Text(
                text = "Saku Mahasiswa",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Kelola Anggaran & Tagihan Anak Kost Lebih Hemat",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Form Card Container with Smooth Height Animation
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 450.dp)
                    .animateContentSize(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Modern Tab Toggle between Login & Register
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!isRegisterMode) Color.White else Color.Transparent)
                                .clickable {
                                    isRegisterMode = false
                                    isError = false
                                    successMessage = ""
                                }
                                .testTag("toggle_login_tab"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Masuk",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (!isRegisterMode) Color(0xFF4F46E5) else NeutralSlate
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isRegisterMode) Color.White else Color.Transparent)
                                .clickable {
                                    isRegisterMode = true
                                    isError = false
                                    successMessage = ""
                                }
                                .testTag("toggle_register_tab"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Daftar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isRegisterMode) Color(0xFF4F46E5) else NeutralSlate
                            )
                        }
                    }

                    Text(
                        text = if (isRegisterMode) "Daftar Akun Baru" else "Silakan Masuk",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B) // Dark Slate
                    )

                    // Error Message Banner
                    AnimatedVisibility(visible = isError) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = errorMessage,
                                color = SpendRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Success Message Banner
                    AnimatedVisibility(visible = successMessage.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = successMessage,
                                color = MoneyGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // 1. Nama Lengkap (Only shown in Register Mode)
                    AnimatedVisibility(visible = isRegisterMode) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "NAMA LENGKAP MAHASISWA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeutralSlate,
                                letterSpacing = 0.5.sp
                            )
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = {
                                    nameInput = it
                                    isError = false
                                },
                                placeholder = { Text("Contoh: Budi Raharjo", color = NeutralSlate.copy(alpha = 0.6f)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color(0xFF4F46E5)
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_name_input"),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF4F46E5),
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC)
                                )
                            )
                        }
                    }

                    // 2. Alamat Email
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "ALAMAT EMAIL MAHASISWA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeutralSlate,
                            letterSpacing = 0.5.sp
                        )
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = {
                                emailInput = it
                                isError = false
                            },
                            placeholder = { Text("Contoh: budi@mahasiswa.ac.id", color = NeutralSlate.copy(alpha = 0.6f)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = Color(0xFF4F46E5)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4F46E5),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            )
                        )
                    }

                    // 3. Kata Sandi (Password)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "KATA SANDI",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeutralSlate,
                            letterSpacing = 0.5.sp
                        )
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                passwordInput = it
                                isError = false
                            },
                            placeholder = { Text("Masukkan kata sandi", color = NeutralSlate.copy(alpha = 0.6f)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF4F46E5)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Sembunyikan sandi" else "Tampilkan sandi"
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = if (isRegisterMode) ImeAction.Next else ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                onDone = { focusManager.clearFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4F46E5),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            )
                        )
                    }

                    // 4. Konfirmasi Kata Sandi (Only shown in Register Mode)
                    AnimatedVisibility(visible = isRegisterMode) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "KONFIRMASI KATA SANDI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeutralSlate,
                                letterSpacing = 0.5.sp
                            )
                            OutlinedTextField(
                                value = confirmPasswordInput,
                                onValueChange = {
                                    confirmPasswordInput = it
                                    isError = false
                                },
                                placeholder = { Text("Masukkan ulang kata sandi", color = NeutralSlate.copy(alpha = 0.6f)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFF4F46E5)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                        Icon(
                                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (confirmPasswordVisible) "Sembunyikan sandi" else "Tampilkan sandi"
                                        )
                                    }
                                },
                                singleLine = true,
                                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_confirm_password_input"),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF4F46E5),
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Dynamic Submit Button
                    Button(
                        onClick = {
                            val emailTrimmed = emailInput.trim().lowercase()
                            if (isRegisterMode) {
                                // Pendaftaran Mode
                                if (nameInput.isBlank()) {
                                    isError = true
                                    errorMessage = "Nama lengkap tidak boleh kosong!"
                                } else if (emailInput.isBlank() || !emailInput.contains("@")) {
                                    isError = true
                                    errorMessage = "Harap masukkan alamat email yang valid!"
                                } else if (passwordInput.length < 6) {
                                    isError = true
                                    errorMessage = "Kata sandi minimal harus 6 karakter!"
                                } else if (passwordInput != confirmPasswordInput) {
                                    isError = true
                                    errorMessage = "Kata sandi dan konfirmasi tidak cocok!"
                                } else {
                                    // Simpan ke SharedPreferences
                                    val words = nameInput.trim().split("\\s+".toRegex())
                                    val initials = when {
                                        words.isEmpty() -> "U"
                                        words.size == 1 -> words[0].take(2).uppercase()
                                        else -> (words[0].take(1) + words[1].take(1)).uppercase()
                                    }

                                    sharedPrefs.edit()
                                        .putString("reg_pwd_$emailTrimmed", passwordInput)
                                        .putString("reg_name_$emailTrimmed", nameInput.trim())
                                        .putString("reg_initials_$emailTrimmed", initials)
                                        .apply()

                                    successMessage = "Registrasi Berhasil! Mengalihkan ke Dashboard..."
                                    
                                    // Delay briefly then log in
                                    onLoginSuccess(nameInput.trim(), emailInput.trim(), initials)
                                }
                            } else {
                                // Masuk Mode
                                if (emailInput.isBlank() || !emailInput.contains("@")) {
                                    isError = true
                                    errorMessage = "Harap masukkan alamat email yang valid!"
                                } else if (passwordInput.isBlank()) {
                                    isError = true
                                    errorMessage = "Kata sandi tidak boleh kosong!"
                                } else {
                                    val savedPwd = sharedPrefs.getString("reg_pwd_$emailTrimmed", null)
                                    val savedName = sharedPrefs.getString("reg_name_$emailTrimmed", null)
                                    val savedInitials = sharedPrefs.getString("reg_initials_$emailTrimmed", null)

                                    if (savedPwd == null) {
                                        isError = true
                                        errorMessage = "Email tidak terdaftar! Silakan klik tab 'Daftar' untuk pendaftaran."
                                    } else if (savedPwd != passwordInput) {
                                        isError = true
                                        errorMessage = "Kata sandi salah! Silakan periksa kembali."
                                    } else {
                                        onLoginSuccess(savedName ?: "Pengguna", emailInput.trim(), savedInitials ?: "U")
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4F46E5),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_submit_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isRegisterMode) Icons.Default.AppRegistration else Icons.Default.Wallet,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isRegisterMode) "Daftar Akun Saku" else "Masuk ke Dompet Saku",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    // Demo / Tutorial Hints
                    AnimatedVisibility(visible = !isRegisterMode) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEEF2FF))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Akun Demo Default:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4F46E5)
                            )
                            Text(
                                text = "Email: budi.raharjo@mahasiswa.ac.id\nSandi: budi123",
                                fontSize = 10.sp,
                                color = Color(0xFF312E81),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer info
            Text(
                text = "Saku Mahasiswa v1.2.0 • Aman & Privat Lokal",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}
