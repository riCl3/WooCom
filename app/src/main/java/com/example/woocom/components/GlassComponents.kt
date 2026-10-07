package com.example.woocom.components

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.woocom.ui.theme.DarkSurface
import com.example.woocom.ui.theme.NeonBorder

/**
 * Frosted "glass" surface: a translucent dark fill with a subtle top-light edge,
 * mimicking frosted glass without a hardware backdrop blur (which Compose does
 * not yet support cross-version).
 */
fun Modifier.glassEffect(
    shape: Shape = RoundedCornerShape(24.dp)
): Modifier = this
    .clip(shape)
    .background(DarkSurface.copy(alpha = 0.55f))
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.22f),
                Color.White.copy(alpha = 0.04f)
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
    Box(modifier = modifier.glassEffect(shape = shape)) {
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