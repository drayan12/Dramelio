package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.BackendConfig

@Composable
fun RegisterScreen(
    currentEmail: String,
    config: BackendConfig,
    onRegisterSuccess: (String, String) -> Unit,
    primaryColor: Color
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(currentEmail) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    // Background and accent styling from theme config
    val darkBackground = Color(0xFF050505)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        // Aesthetic Top Decorative Image with Blur Overlay Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.40f)
        ) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1574375927938-d5a98e8edd86?w=1000",
                contentDescription = "Background Cinema",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Premium Gradient overlay to dark color background
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

        // Main content card wrapper
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
                Spacer(modifier = Modifier.height(110.dp))

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

                // Title headings in display typography
                Text(
                    text = "Selamat Datang di DRAMELIO",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Streaming Drama Lokal Terlengkap & Eksklusif VIP. Masukkan nama dan email Anda untuk mulai membuat akun instan.",
                    color = Color.LightGray.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Name Input Field
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
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .testTag("register_email_input")
                )
            }

            // Bottom section containing Register CTA Button and safety labels
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Button(
                    onClick = {
                        focusManager.clearFocus()

                        // Simple Client validation step
                        var hasError = false
                        if (name.trim().isBlank()) {
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

                        if (!hasError) {
                            onRegisterSuccess(name.trim(), email.trim())
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
                        text = "MULAI MENONTON SEKARANG",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Compliance & Free Trial Disclaimer
                Text(
                    text = "Dengan mendaftar, Anda menyetujui Ketentuan Penggunaan dan Kebijakan Privasi Dramelio Entertainment.",
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
