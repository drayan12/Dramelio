package com.example.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object TripayService {

    fun getPaymentMethods(): List<PaymentMethod> {
        return listOf(
            PaymentMethod(
                code = "BCAVA",
                name = "BCA Virtual Account",
                type = PaymentMethodType.VIRTUAL_ACCOUNT,
                logoUrl = "https://logos-world.net/wp-content/uploads/2021/11/BCA-Logo.png", // Fallback text logo will render when needed
                instructions = listOf(
                    "Pilih m-Transfer > BCA Virtual Account di aplikasi BCA mobile Anda.",
                    "Masukkan nomor Virtual Account Anda.",
                    "Lakukan konfirmasi pembayaran lalu masukkan PIN transaksi Anda."
                )
            ),
            PaymentMethod(
                code = "BRIVA",
                name = "BRI Virtual Account (BRIVA)",
                type = PaymentMethodType.VIRTUAL_ACCOUNT,
                logoUrl = "https://vectorise.net/vectorio/wp-content/uploads/2018/08/Logo-BRI.png",
                instructions = listOf(
                    "Pilih Pembayaran > BRIVA di aplikasi BRImo.",
                    "Masukkan nomor Virtual Account BRIVA Anda.",
                    "Konfirmasi rincian tagihan Anda, lalu masukkan kata sandi BRImo."
                )
            ),
            PaymentMethod(
                code = "MANDIRIVA",
                name = "Mandiri Virtual Account",
                type = PaymentMethodType.VIRTUAL_ACCOUNT,
                logoUrl = "https://vectorise.net/logo/wp-content/uploads/2018/08/Logo-Mandiri.png",
                instructions = listOf(
                    "Pilih menu Bayar > Multi Payment pada Livin' by Mandiri.",
                    "Masukkan nomor Virtual Account Mandiri Anda.",
                    "Periksa nominal tagihan rincian, lalu selesaikan transaksi."
                )
            ),
            PaymentMethod(
                code = "BNIVA",
                name = "BNI Virtual Account",
                type = PaymentMethodType.VIRTUAL_ACCOUNT,
                logoUrl = "https://vectorise.net/logo/wp-content/uploads/2018/08/Logo-BNI.png",
                instructions = listOf(
                    "Pilih menu Transfer > Virtual Account Billing di BNI Mobile Banking.",
                    "Pilih rekening debit Anda lalu masukkan nomor Virtual Account BNI.",
                    "Tagihan akan muncul otomatis, konfirmasi dengan password transaksi Anda."
                )
            ),
            PaymentMethod(
                code = "QRIS",
                name = "QRIS (GPN All Payment)",
                type = PaymentMethodType.EWALLET,
                logoUrl = "https://gopay.co.id/images/qris-logo.png",
                instructions = listOf(
                    "Pindai/Scan QR code di layar menggunakan aplikasi e-wallet pilihan Anda.",
                    "Pastikan total nominal pembayaran sesuai dengan tagihan.",
                    "Selesaikan pembayaran di dalam aplikasi e-wallet Anda."
                )
            ),
            PaymentMethod(
                code = "SHOPEEPAY",
                name = "ShopeePay",
                type = PaymentMethodType.EWALLET,
                logoUrl = "https://logos-download.com/wp-content/uploads/2020/06/ShopeePay_Logo.png",
                instructions = listOf(
                    "Anda akan dialihkan otomatis ke aplikasi Shopee.",
                    "Konfirmasi pembayaran di dalam menu pembayaran ShopeePay.",
                    "Masukkan PIN ShopeePay Anda untuk menyelesaikan verifikasi."
                )
            ),
            PaymentMethod(
                code = "OVO",
                name = "OVO",
                type = PaymentMethodType.EWALLET,
                logoUrl = "https://logos-download.com/wp-content/uploads/2020/06/OVO_Logo.png",
                instructions = listOf(
                    "Masukkan nomor telepon Anda yang aktif dan terdaftar di OVO.",
                    "Buka aplikasi OVO Anda di ponsel pintar.",
                    "Klik menu notifikasi lonceng atau inbox, konfirmasi pembayaran lalu bayar."
                )
            ),
            PaymentMethod(
                code = "DANA",
                name = "DANA",
                type = PaymentMethodType.EWALLET,
                logoUrl = "https://logos-download.com/wp-content/uploads/2020/06/DANA_Logo.png",
                instructions = listOf(
                    "Masukkan nomor telepon yang terdaftar di akun DANA Anda.",
                    "Selesaikan verifikasi OTP yang dikirimkan melalui SMS.",
                    "Masukkan PIN DANA Anda untuk melakukan otorisasi pembayaran."
                )
            ),
            PaymentMethod(
                code = "INDOMARET",
                name = "Indomaret",
                type = PaymentMethodType.RETAIL,
                logoUrl = "https://vectorise.net/logo/wp-content/uploads/2018/08/Logo-Indomaret.png",
                instructions = listOf(
                    "Datang ke gerai Indomaret terdekat dari lokasi Anda.",
                    "Katakan ke kasir untuk membayar tagihan Tripay Dramelio.",
                    "Tunjukkan kode pembayaran unik Anda kepada kasir dan lakukan pembayaran tunai."
                )
            ),
            PaymentMethod(
                code = "ALFAMART",
                name = "Alfamart",
                type = PaymentMethodType.RETAIL,
                logoUrl = "https://vectorise.net/logo/wp-content/uploads/2018/08/Logo-Alfamart.png",
                instructions = listOf(
                    "Datang ke gerai Alfamart atau Alfamidi terdekat.",
                    "Katakan ingin membayar transaksi Tripay Dramelio kepada kasir.",
                    "Sebutkan kode bayar unik Anda dan serahkan tunai sesuai jumlah tagihan."
                )
            )
        )
    }

    fun executeTripayTransaction(
        plan: SubscriptionPlan,
        paymentMethodCode: String,
        config: BackendConfig
    ): PaymentTransaction {
        val ref = "DML-" + UUID.randomUUID().toString().substring(0, 8).uppercase()
        val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("id", "ID")).format(Date())

        val vaNum = if (paymentMethodCode != "QRIS" && paymentMethodCode != "SHOPEEPAY" && paymentMethodCode != "OVO" && paymentMethodCode != "DANA") {
            // Generate random VA number / Retail store invoice code
            "20268578" + (10000000..99999999).random().toString()
        } else {
            null
        }

        val qrUrl = if (paymentMethodCode == "QRIS") {
            "https://upload.wikimedia.org/wikipedia/commons/d/d0/QR_code_for_mobile_English_Wikipedia.svg"
        } else {
            null
        }

        val paymentInstructions = "Silakan ikuti instruksi pembayaran di aplikasi untuk melunasi tagihan langganan Anda."

        return PaymentTransaction(
            id = UUID.randomUUID().toString(),
            reference = ref,
            planId = plan.id,
            amount = plan.price,
            paymentMethodCode = paymentMethodCode,
            status = "PENDING",
            paymentInstructions = paymentInstructions,
            vaNumber = vaNum,
            qrCodeUrl = qrUrl,
            date = formattedDate
        )
    }
}
