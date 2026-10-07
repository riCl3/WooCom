package com.example.woocom.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
    darkColorScheme(
        primary = NeonGreen,
        onPrimary = DarkText,
        primaryContainer = NeonGreen.copy(alpha = 0.16f),
        onPrimaryContainer = PrimaryText,
        secondary = Emerald,
        onSecondary = Color(0xFF00210F),
        tertiary = DeepSkyBlue,
        onTertiary = Color(0xFF001E2B),
        background = DeepCharcoal,
        onBackground = PrimaryText,
        surface = DarkSurface,
        onSurface = PrimaryText,
        surfaceVariant = GradientStart,
        onSurfaceVariant = SecondaryText,
        outline = NeonGreen.copy(alpha = 0.35f),
        error = Color(0xFFFF6B6B),
        onError = Color(0xFF5C0000),
    )

private val LightColorScheme =
    lightColorScheme(
        primary = NeonGreen,
        onPrimary = DarkText,
        primaryContainer = NeonGreen.copy(alpha = 0.25f),
        onPrimaryContainer = LightOnSurface,
        secondary = Color(0xFF0B8A4F),
        onSecondary = Color.White,
        background = LightBackground,
        onBackground = LightOnSurface,
        surface = LightSurface,
        onSurface = LightOnSurface,
        surfaceVariant = Color(0xFFE9EAE6),
        onSurfaceVariant = Color(0xFF5C5F56),
        outline = NeonGreen,
        error = Color(0xFFB00020),
        onError = Color.White,
    )

/**
 * Brand theme for WooCom.
 *
 * The shopping experience is designed as a dark, neon-accented storefront, so the
 * app always renders the dark palette to preserve the visual identity. Dynamic
 * colour is intentionally off to keep the brand accent consistent across devices.
 */
@Composable
fun WooComTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
