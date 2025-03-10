package com.example.woocom.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.woocom.R


@Composable
fun AuthScreen(modifier: Modifier = Modifier, navController: NavHostController) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
      Image(painter = painterResource(id = R.drawable.loginbg),
          contentDescription = "Login",
          modifier = Modifier.fillMaxWidth()
              .height(300.dp))

        Text("Start Your Shopping Journey With WooCom",
            style = TextStyle(
                fontSize = 20.sp,
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            ))
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate("login")
            },
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Black)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFB7FF00), // Green color
                            Color(0xFFB7FF00)  // Lighter green color
                        )
                    )
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent // Makes the button's container transparent so the gradient shows through
            )
        ) {
            Text(
                text = "Login",
                style = TextStyle(fontSize = 20.sp, color = Color.Black)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp), // Padding for the row
            verticalAlignment = Alignment.CenterVertically // Align items in the center vertically
        ) {
            // Text before the button
            Text(
                text = "Don't Have An Account? ",
                style = TextStyle(fontSize = 16.sp, color = Color.Black) // Basic styling for the label
            )

            // TextButton with a darker neon green
            TextButton(
                onClick = {
                    navController.navigate("signup")
                },
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF89F8C7) // Slightly darker neon green color
                )
            ) {
                Text(
                    text = "Sign Up",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1ED927), // Slightly darker neon green
                    )
                )
            }
        }
    }


}


