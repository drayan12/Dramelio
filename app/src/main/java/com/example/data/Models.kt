package com.example.data

import androidx.compose.ui.graphics.Color

data class MovieOrSeries(
    val id: String,
    val title: String,
    val overview: String,
    val posterUrl: String,
    val backdropUrl: String,
    val rating: Double,
    val releaseDate: String,
    val isSeries: Boolean = false,
    val genres: List<String> = emptyList(),
    val seasons: List<Season> = emptyList(),
    val videoSources: List<VideoSource> = emptyList(),
    val ageRating: String = "13+",
    val duration: String = "2h 15m",
    val quality: String = "Ultra HD"
)

data class Season(
    val id: Int,
    val name: String,
    val episodes: List<Episode>
)

data class Episode(
    val id: String,
    val episodeNumber: Int,
    val title: String,
    val duration: String,
    val thumbnail: String,
    val overview: String,
    val videoUrl: String
)

data class VideoSource(
    val label: String, // 1080p, 720p, 480p, 360p, Auto
    val url: String
)

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val price: Long,
    val description: String,
    val durationDays: Int,
    val benefits: List<String>
)

data class UserProfile(
    val name: String,
    val email: String,
    val isPremium: Boolean,
    val activePlanId: String?,
    val planExpiryDate: String?,
    val avatarUrl: String = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150"
)

enum class PaymentMethodType {
    VIRTUAL_ACCOUNT, QRIS, EWALLET, RETAIL
}

data class PaymentMethod(
    val code: String,
    val name: String,
    val type: PaymentMethodType,
    val logoUrl: String,
    val instructions: List<String>
)

data class PaymentTransaction(
    val id: String,
    val reference: String,
    val planId: String,
    val amount: Long,
    val paymentMethodCode: String,
    val status: String, // PENDING, PAID, EXPIRED
    val paymentInstructions: String,
    val vaNumber: String? = null,
    val qrCodeUrl: String? = null,
    val date: String
)

// Admin/Backend configuration synced dynamically
data class BackendConfig(
    val domain: String = "dramelio.com",
    val tripayApiKey: String = "DEV-MOCK-TRIPAY-KEY-12345",
    val tripayPrivateKey: String = "DEV-MOCK-TRIPAY-PRIVATE-12345",
    val tripayMerchantCode: String = "T12345",
    val tmdbApiKey: String = "",
    val subscriptionBasicPrice: Long = 29000,
    val subscriptionPremiumPrice: Long = 59000,
    val appThemePrimaryHex: String = "#EAB308", // Dramelio Gold default
    val appThemeBgHex: String = "#050505", // Sophisticated Dark default
    val appThemeAccentHex: String = "#FACC15" // Accent Gold default
)
