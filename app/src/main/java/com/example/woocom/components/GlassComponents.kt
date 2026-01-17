package com.example.woocom.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.woocom.ui.theme.GlassSurface
import com.example.woocom.ui.theme.NeonBorder
import com.example.woocom.ui.theme.DarkSurface

/**
 * Applies a premium glassmorphism effect.
 * Uses blur on Android 12+ (API 31+), and a sophisticated semi-transparent fallback on older versions.
 */
fun Modifier.glassEffect(
    shape: Shape = RoundedCornerShape(24.dp),
    blurRadius: Dp = 30.dp
): Modifier = this
    .clip(shape)
    .then(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Modifier.blur(blurRadius)
        } else {
            Modifier
        }
    )
    .background(GlassSurface)
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.15f),
                Color.White.copy(alpha = 0.05f)
            )
        ),
        shape = shape
    )

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .glassEffect(shape = shape)
            .padding(0.dp) // Reset padding, let caller handle internal padding
    ) {
        content()
    }
}

@Composable
fun NeonGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(DarkSurface.copy(alpha = 0.8f))
            .border(
                width = 0.5.dp,
                color = NeonBorder,
                shape = shape
            )
    ) {
        content()
    }
}
