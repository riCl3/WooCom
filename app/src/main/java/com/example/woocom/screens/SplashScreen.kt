package com.example.woocom.screens


import android.widget.Space
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.woocom.R
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay


@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    // Check authentication status
    // Check authentication status
    val isLoggedIn = com.google.firebase.Firebase.auth.currentUser != null
    val nextDestination = if (isLoggedIn) "home" else "auth"

    // Splash screen content
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.icon_bg),
                contentDescription = "App Logo",
                modifier = Modifier.size(300.dp)
            )

            Spacer(modifier = Modifier.height(8.dp)) //
            Text(
                text = "WooCom",
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 30.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
    }

    // Auto-navigate after 3 seconds
    LaunchedEffect(key1 = true) {
        delay(2000) // 5 seconds delay
        navController.navigate(nextDestination) {
            // Remove splash from back stack
            popUpTo("splash") {
                inclusive = true
            }
        }
    }
}