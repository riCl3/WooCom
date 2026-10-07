package com.example.woocom.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.woocom.R
import com.example.woocom.Routes
import com.example.woocom.data.ServiceLocator
import com.example.woocom.ui.theme.GradientEnd
import com.example.woocom.ui.theme.GradientStart
import com.example.woocom.ui.theme.NeonGreen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    // Gentle scale-in for the logo
    val transition = rememberInfiniteTransition()
    val logoScale by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.0f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 900),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "logo-pulse",
    )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(GradientStart, GradientEnd)),
                ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.icon_bg),
                contentDescription = "WooCom logo",
                modifier =
                    Modifier
                        .size(160.dp)
                        .scale(logoScale)
                        .alpha(0.95f),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "WooCom",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier =
                    Modifier
                        .size(6.dp)
                        .background(NeonGreen, CircleShape),
            )
        }
    }

    LaunchedEffect(Unit) {
        delay(2000)
        // Decided here rather than at composition time: Supabase restores its session
        // from storage asynchronously, so asking earlier could log a signed-in user out.
        val nextDestination =
            if (ServiceLocator.authRepository.isSignedIn()) Routes.HOME else Routes.AUTH
        navController.navigate(nextDestination) {
            popUpTo(Routes.SPLASH) { inclusive = true }
        }
    }
}
