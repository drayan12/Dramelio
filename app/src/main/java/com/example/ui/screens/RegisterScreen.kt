package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.BackendConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    currentEmail: String,
    config: BackendConfig,
    onRegisterSuccess: (String, String) -> Unit,
    primaryColor: Color
) {
    var isLoginMode by remember { mutableStateOf(false) } // False = Register (Daftar), True = Login (Masuk)
    
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(currentEmail) }
    var password by remember { mutableStateOf("") }
    
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var globalError by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current
    val darkBackground = Color(0xFF050505)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        // Aesthetic Top Decorative Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
        ) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1574375927938-d5a98e8edd86?w=1000",
                contentDescription = "Background Cinema",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                darkBackground.copy(alpha = 0.5f),
                                darkBackground
                            )
                        )
                    )
            )
        }

        // Main content wrapper
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(80.dp))

                // Brand Emblem
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(primaryColor)
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = "Dramelio Logo icon",
                        tint = Color.Black,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title headings
                Text(
                    text = if (isLoginMode) "Masuk ke Akun VIP" else "Buat Akun Dramelio VIP",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isLoginMode) 
                        "Masuk dengan email & password yang sudah terdaftar untuk melanjutkan streaming VIP eksklusif tanpa batasan." 
                        else "Daftar instan menggunakan email aktif Anda dan mulai nikmati serial drama lokal terlengkap dengan audio kualitas sinema.",
                    color = Color.LightGray.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Firebase Global Error Feedback Banner
                if (globalError != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = globalError!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Name Input Field (Only in Sign Up Mode!)
                if (!isLoginMode) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (nameError != null) nameError = null
                        },
                        label = { Text("Nama Lengkap") },
                        placeholder = { Text("Masukkan nama...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Icon user",
                                tint = if (nameError != null) MaterialTheme.colorScheme.error else Color.Gray
                            )
                        },
                        isError = nameError != null,
                        supportingText = {
                            if (nameError != null) {
                                Text(text = nameError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            focusedLabelColor = primaryColor,
                            unfocusedLabelColor = Color.LightGray,
                            focusedContainerColor = Color(0xFF101010),
                            unfocusedContainerColor = Color(0xFF101010),
                            errorContainerColor = Color(0xFF101010)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .testTag("register_name_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Email Input Field
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (emailError != null) emailError = null
                    },
                    label = { Text("Alamat Email") },
                    placeholder = { Text("Masukkan alamat email...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Icon email",
                            tint = if (emailError != null) MaterialTheme.colorScheme.error else Color.Gray
                        )
                    },
                    isError = emailError != null,
                    supportingText = {
                        if (emailError != null) {
                            Text(text = emailError!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        focusedLabelColor = primaryColor,
                        unfocusedLabelColor = Color.LightGray,
                        focusedContainerColor = Color(0xFF101010),
                        unfocusedContainerColor = Color(0xFF101010),
                        errorContainerColor = Color(0xFF101010)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .testTag("register_email_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Password Input Field with Visibility Toggle
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (passwordError != null) passwordError = null
                    },
                    label = { Text("Sandi (Password)") },
                    placeholder = { Text("Masukkan setidaknya 6 karakter...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Icon Lock",
                            tint = if (passwordError != null) MaterialTheme.colorScheme.error else Color.Gray
                        )
                    },
                    trailingIcon = {
                        val image = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (isPasswordVisible) "Sembunyikan sandi" else "Tampilkan sandi"
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = description, tint = Color.Gray)
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = passwordError != null,
                    supportingText = {
                        if (passwordError != null) {
                            Text(text = passwordError!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        focusedLabelColor = primaryColor,
                        unfocusedLabelColor = Color.LightGray,
                        focusedContainerColor = Color(0xFF101010),
                        unfocusedContainerColor = Color(0xFF101010),
                        errorContainerColor = Color(0xFF101010)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .testTag("register_password_input")
                )
            }

            // Bottom section containing CTA Buttons and sign-in mode toggle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = primaryColor,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    Button(
                        onClick = {
                            focusManager.clearFocus()

                            // Input validation
                            var hasError = false
                            if (!isLoginMode && name.trim().isBlank()) {
                                nameError = "Nama lengkap wajib diisi!"
                                hasError = true
                            }
                            if (email.trim().isBlank()) {
                                emailError = "Alamat email wajib diisi!"
                                hasError = true
                            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                                emailError = "Masukkan format email yang valid!"
                                hasError = true
                            }
                            if (password.length < 6) {
                                passwordError = "Sandi minimal harus 6 karakter!"
                                hasError = true
                            }

                            if (!hasError) {
                                isLoading = true
                                globalError = null
                                val firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()

                                if (isLoginMode) {
                                    // Login Mode execution
                                    firebaseAuth.signInWithEmailAndPassword(email.trim(), password)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val firebaseUser = firebaseAuth.currentUser
                                                val displayName = firebaseUser?.displayName ?: email.trim().substringBefore("@")
                                                isLoading = false
                                                onRegisterSuccess(displayName, email.trim())
                                            } else {
                                                isLoading = false
                                                val exc = task.exception
                                                globalError = when {
                                                    exc is com.google.firebase.auth.FirebaseAuthInvalidUserException -> {
                                                        "Akun email belum terdaftar! Silakan pilih daftar terlebih dahulu."
                                                    }
                                                    exc is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                                                        "Email atau password salah. Silakan periksa kembali."
                                                    }
                                                    else -> {
                                                        exc?.localizedMessage ?: "Gagal masuk. Kesalahan server."
                                                    }
                                                }
                                            }
                                        }
                                } else {
                                    // Register Mode execution
                                    firebaseAuth.createUserWithEmailAndPassword(email.trim(), password)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val firebaseUser = firebaseAuth.currentUser
                                                // Save full display name
                                                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                                    .setDisplayName(name.trim())
                                                    .build()
                                                firebaseUser?.updateProfile(profileUpdates)?.addOnCompleteListener {
                                                    isLoading = false
                                                    onRegisterSuccess(name.trim(), email.trim())
                                                } ?: run {
                                                    isLoading = false
                                                    onRegisterSuccess(name.trim(), email.trim())
                                                }
                                            } else {
                                                isLoading = false
                                                val exc = task.exception
                                                globalError = when {
                                                    exc is com.google.firebase.auth.FirebaseAuthUserCollisionException -> {
                                                        "Alamat email ini sudah terdaftar! Pilih masuk jika sudah punya akun."
                                                    }
                                                    else -> {
                                                        exc?.localizedMessage ?: "Pendaftaran gagal. Kesalahan jaringan."
                                                    }
                                                }
                                            }
                                        }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("register_submit_button")
                    ) {
                        Text(
                            text = if (isLoginMode) "MASUK SEKARANG" else "DAFTAR & MENONTON SEKARANG",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Log In / Sign Up Mode Switcher Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isLoginMode) "Belum memiliki akun?" else "Sudah memiliki akun?",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isLoginMode) "Daftar Disini" else "Masuk Disini",
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .clickable {
                                isLoginMode = !isLoginMode
                                globalError = null
                                nameError = null
                                emailError = null
                                passwordError = null
                            }
                            .testTag("auth_mode_toggle")
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Compliance & Free Trial Disclaimer
                Text(
                    text = "Dengan masuk atau mendaftar, Anda menyetujui Ketentuan Penggunaan dan Kebijakan Privasi Dramelio Entertainment.",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
