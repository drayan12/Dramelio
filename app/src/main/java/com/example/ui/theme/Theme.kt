package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.data.BackendConfig

// Helper to safely parse hex colors
fun String.toColor(fallback: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(this))
    } catch (e: Exception) {
        fallback
    }
}

@Composable
fun DramelioTheme(
    config: BackendConfig,
    darkTheme: Boolean = true, // We enforce dark theme as streaming default
    content: @Composable () -> Unit,
) {
    // Dynamically derive color scheme from BackendConfig configuration
    val primaryColor = config.appThemePrimaryHex.toColor(Color(0xFFE50914))
    val bgColor = config.appThemeBgHex.toColor(Color(0xFF141414))
    val accentColor = config.appThemeAccentHex.toColor(Color(0xFF1CD260))

    val customDarkColorScheme = darkColorScheme(
        primary = primaryColor,
        secondary = accentColor,
        tertiary = accentColor,
        background = bgColor,
        surface = Color(0xFF1F1F1F),
        onPrimary = Color.White,
        onSecondary = Color.Black,
        onTertiary = Color.White,
        onBackground = Color(0xFFF5F5F5),
        onSurface = Color(0xFFE5E5E5)
    )

    MaterialTheme(
        colorScheme = customDarkColorScheme,
        typography = Typography,
        content = content
    )
}
