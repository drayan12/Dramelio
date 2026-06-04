package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.components.TripayCheckoutSheet
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    config: BackendConfig,
    onNavigateBack: () -> Unit,
    onSubmitTransaction: (PaymentTransaction) -> Unit,
    onPaymentSuccess: () -> Unit,
    primaryColor: Color
) {
    val context = LocalContext.current
    val systemMethods = TripayService.getPaymentMethods().filter { method ->
        when (method.type) {
            PaymentMethodType.QRIS -> config.isQrisActive
            PaymentMethodType.VIRTUAL_ACCOUNT -> config.isVaActive
            PaymentMethodType.EWALLET -> config.isEwalletActive
            PaymentMethodType.RETAIL -> config.isRetailActive
        }
    }

    val monthlyPlan = SubscriptionPlan(
        id = "basic",
        name = "VIP Bulanan",
        price = config.subscriptionBasicPrice,
        description = "Cocok untuk marathon drakor hemat sesaat.",
        durationDays = 30,
        benefits = listOf(
            "Nonton seluruh koleksi drakor & donghua",
            "Resolusi Full HD (1080p)",
            "Lanjutkan nonton di ponsel & tablet",
            "Bebas Iklan Pengganggu"
        )
    )

    val annualPlan = SubscriptionPlan(
        id = "premium",
        name = "VIP Tahunan (Hemat 50%)",
        price = config.subscriptionPremiumPrice,
        description = "Akses tanpa batas setahun penuh paling hemat.",
        durationDays = 365,
        benefits = listOf(
            "Nonton seluruh koleksi drakor & donghua",
            "Kualitas Ultra HD (4K HDR)",
            "Lanjutkan nonton di 4 Device sekaligus",
            "Bebas Iklan & Akses Rilis Eksklusif Tercepat"
        )
    )

    var selectedPlan by remember { mutableStateOf(monthlyPlan) }
    var selectedPaymentCode by remember(systemMethods) { mutableStateOf(systemMethods.firstOrNull()?.code ?: "") }
    var pendingTransaction by remember { mutableStateOf<PaymentTransaction?>(null) }
    var showCheckoutDialog by remember { mutableStateOf(false) }

    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Langganan Dramelio VIP", color = Color.White, fontWeight = FontWeight.Bold) },
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
            // Plan Cards Selection Title
            Text(
                text = "Pilih Paket Terbaik Anda",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )

            // 1. Monthly Plan
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .border(
                        width = if (selectedPlan.id == monthlyPlan.id) 2.dp else 0.dp,
                        color = if (selectedPlan.id == monthlyPlan.id) primaryColor else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { selectedPlan = monthlyPlan }
                    .testTag("plan_card_basic"),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedPlan.id == monthlyPlan.id) Color(0xFF1F1B12) else Color(0xFF1E1E1E)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = monthlyPlan.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = currencyFormatter.format(monthlyPlan.price),
                            color = primaryColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                    Text(text = monthlyPlan.description, color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(vertical = 4.dp))
                }
            }

            // 2. Annual Plan
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .border(
                        width = if (selectedPlan.id == annualPlan.id) 2.dp else 0.dp,
                        color = if (selectedPlan.id == annualPlan.id) primaryColor else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { selectedPlan = annualPlan }
                    .testTag("plan_card_premium"),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedPlan.id == annualPlan.id) Color(0xFF1F1B12) else Color(0xFF1E1E1E)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = annualPlan.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(primaryColor)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("HEMAT", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        Text(
                            text = currencyFormatter.format(annualPlan.price),
                            color = primaryColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                    Text(text = annualPlan.description, color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(vertical = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Box listing the selected benefits of the plan
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Keuntungan VIP Anda:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    selectedPlan.benefits.forEach { b ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = b, color = Color.LightGray, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Payment method list title
            Text(
                text = "Metode Pembayaran Tripay",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )

            // Render Indonesian Payment Channels in visual groupings
            if (systemMethods.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1010)),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color.Red, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Metode Pembayaran Dinonaktifkan",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Maaf, seluruh loket pembayaran saat ini dinonaktifkan sementara oleh Administrator dari server virtual kami. Silakan hubungi Layanan Pelanggan di halaman Profil jika butuh bantuan lebih lanjut.",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 15.sp
                        )
                    }
                }
            } else {
                val groupedMethods = systemMethods.groupBy { it.type }
                
                groupedMethods.entries.forEach { entry ->
                    val typeName = when(entry.key) {
                        PaymentMethodType.VIRTUAL_ACCOUNT -> "Virtual Account (VA Bank)"
                        PaymentMethodType.EWALLET -> "E-Wallet (Bayar Instan)"
                        PaymentMethodType.RETAIL -> "Retail Outlet (Minimarket)"
                        PaymentMethodType.QRIS -> "QRIS (GPN All Payment)"
                    }

                    Text(
                        text = typeName,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 6.dp)
                    )

                    entry.value.forEach { method ->
                        val isSelected = selectedPaymentCode == method.code
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0xFF2B2B2B) else Color(0xFF1E1E1E))
                                .border(
                                    width = if (isSelected) 1.dp else 0.dp,
                                    color = if (isSelected) primaryColor else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { selectedPaymentCode = method.code }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Radio selector visual representation
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedPaymentCode = method.code },
                                colors = RadioButtonDefaults.colors(selectedColor = primaryColor, unselectedColor = Color.Gray),
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Method icon / representation placeholder
                            Box(
                                modifier = Modifier
                                    .size(width = 54.dp, height = 24.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.White)
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = method.code.replace("VA", ""),
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = method.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Main CTA submit transaction payment
            Button(
                enabled = systemMethods.isNotEmpty() && selectedPaymentCode.isNotBlank(),
                onClick = {
                    val newTx = TripayService.executeTripayTransaction(
                        plan = selectedPlan,
                        paymentMethodCode = selectedPaymentCode,
                        config = config
                    )
                    onSubmitTransaction(newTx)
                    pendingTransaction = newTx
                    showCheckoutDialog = true
                    Toast.makeText(context, "Membuka Panel Tripay: ${selectedPaymentCode}", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_checkout_cta"),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Bayar Sekarang - ${currencyFormatter.format(selectedPlan.price)}",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }

    // Modal Tripay bottom-sheet invoice showing
    if (showCheckoutDialog) {
        pendingTransaction?.let { tx ->
            val methodDetail = systemMethods.find { it.code == tx.paymentMethodCode } ?: systemMethods.firstOrNull() ?: TripayService.getPaymentMethods().first()
            TripayCheckoutSheet(
                transaction = tx,
                paymentMethod = methodDetail,
                onVerifyPayment = {
                    showCheckoutDialog = false
                    onPaymentSuccess()
                    Toast.makeText(context, "VIP Berhasil Aktif - Selamat Menikmati!", Toast.LENGTH_LONG).show()
                },
                onClose = {
                    showCheckoutDialog = false
                    onNavigateBack()
                }
            )
        }
    }
}
