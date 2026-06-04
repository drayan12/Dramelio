package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    transactions: List<PaymentTransaction>,
    config: BackendConfig,
    onSaveProfile: (String, String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToCheckout: () -> Unit,
    onResetData: () -> Unit,
    primaryColor: Color
) {
    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(userProfile.name) }
    var editEmail by remember { mutableStateOf(userProfile.email) }

    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Profil Dramelio", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User Avatar representation / VIP status card
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .border(2.dp, if (userProfile.isPremium) Color(0xFFEAB308) else Color.Gray, CircleShape)
            ) {
                AsyncImage(
                    model = userProfile.avatarUrl,
                    contentDescription = "Profil Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Premium status badge label
            if (userProfile.isPremium) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFACC15), Color(0xFFEAB308))
                            )
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ANGGOTA VIP",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "AKSES GRATIS",
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Profile Input Box (Editable)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Detail Akun Pengguna",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        
                        TextButton(
                            onClick = {
                                if (isEditing) {
                                    // Validating data
                                    if (editName.isBlank() || !editEmail.contains("@")) {
                                        Toast.makeText(context, "Nama dan Email tidak valid!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        onSaveProfile(editName, editEmail)
                                        isEditing = false
                                        Toast.makeText(context, "Profil diperbarui!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    isEditing = true
                                }
                            }
                        ) {
                            Text(
                                text = if (isEditing) "Simpan" else "Ubah Profil",
                                color = primaryColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isEditing) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Nama Lengkap", color = Color.Gray) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth().testTag("edit_name_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editEmail,
                            onValueChange = { editEmail = it },
                            label = { Text("Alamat Email", color = Color.Gray) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth().testTag("edit_email_input")
                        )
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Nama", color = Color.Gray, fontSize = 12.sp)
                            Text(text = userProfile.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Email", color = Color.Gray, fontSize = 12.sp)
                            Text(text = userProfile.email, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Plan Info details (VIP Expire, upgrade trigger)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Rincian Langganan Aktif", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (userProfile.isPremium) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Paket Saat Ini", color = Color.Gray, fontSize = 12.sp)
                            Text(
                                text = if (userProfile.activePlanId == "premium") "Premium VIP Tahunan (4K Ultra HD)" else "Basic VIP Bulanan (1080p)",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Berakhir Pada", color = Color.Gray, fontSize = 12.sp)
                            Text(text = userProfile.planExpiryDate ?: "30 Hari", color = Color.White, fontSize = 12.sp)
                        }
                    } else {
                        Text(
                            text = "Anda belum berlangganan VIP. Dapatkan benefit penuh nonton tanpa iklan sekarang!",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onNavigateToCheckout,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth().testTag("upgrade_plan_cta")
                        ) {
                            Text("Beli Paket VIP Sekarang", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Transaction History Log Title
            Text(
                text = "Riwayat Pembayaran (Tripay)",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
            )

            if (transactions.isEmpty()) {
                BorderStroke(1.dp, Color.DarkGray)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Belum Ada Transaksi Pembayaran", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
                transactions.forEach { tx ->
                    val methodCode = tx.paymentMethodCode
                    val statusText = if (tx.status == "PAID") "PROSES SUKSES" else "PENDING BAYAR"
                    val statusColor = if (tx.status == "PAID") Color(0xFF2E7D32) else Color(0xFFEF6C00)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF262626)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Ref: ${tx.reference}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(text = "Metode: $methodCode | Rp ${currencyFormatter.format(tx.amount).replace("Rp", "")}", color = Color.LightGray, fontSize = 11.sp)
                                Text(text = tx.date, color = Color.Gray, fontSize = 10.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(statusColor)
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(text = statusText, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // -------------------------------------------------------------
            // Remote-Controlled Support / Help desk section
            // -------------------------------------------------------------
            if (config.isSupportActive) {
                val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF13110C)),
                    border = BorderStroke(1.dp, Color(0xFFEAB308).copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .testTag("help_support_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "Bantuan",
                                tint = Color(0xFFEAB308),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Layanan Bantuan Dramelio",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Mengalami kendala saat aktivasi VIP atau butuh bantuan lainnya? Administrator kami siap melayani Anda secara langsung.",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                try {
                                    uriHandler.openUri(config.supportUrl)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Membuka link bantuan...", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF25D366), // Whatsapp green
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(
                                    imageVector = Icons.Default.SupportAgent,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("HUBUNGI ADMIN VIA WHATSAPP", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Play Console Review Compliances & Privacy policy items
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
                border = BorderStroke(1.dp, Color.DarkGray),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Informasi Legalitas & Kepatuhan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aplikasi Dramelio mematuhi regulasi lisensi konten, tidak mengandung materi hak cipta ilegal, dan seluruh data pengguna dienkripsi secara aman sesuai UU ITE dan regulasi Play Store.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Kebijakan Privasi",
                            color = primaryColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                Toast.makeText(context, "Membuka Kebijakan Privasi (dramelio.com/privacy)", Toast.LENGTH_SHORT).show()
                            }
                        )
                        Text(
                            text = "Ketentuan Layanan",
                            color = primaryColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                Toast.makeText(context, "Membuka Syarat Layanan (dramelio.com/tos)", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // Developer options resetButton
            Button(
                onClick = {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    onResetData()
                    Toast.makeText(context, "Anda telah berhasil keluar dari akun!", Toast.LENGTH_LONG).show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = primaryColor
                ),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("logout_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("KELUAR DARI AKUN", fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            TextButton(
                onClick = {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    onResetData()
                    Toast.makeText(context, "Simpanan Prefs setelan di-reset!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Reset Seluruh Pengaturan Sistem (Simulasi)", color = Color.Gray, fontSize = 11.sp)
            }
        }
    }
}
