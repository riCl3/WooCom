package com.example.woocom.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.woocom.AppUtil
import com.example.woocom.R
import com.example.woocom.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(modifier: Modifier = Modifier, navController: NavHostController, authViewModel: AuthViewModel= viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome", style = TextStyle(
                fontSize = 60.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Left

            )
        )
        Text(
            text = "Back", style = TextStyle(
                fontSize = 60.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Left
            )
        )


        //Area For Image

        Image(
            painter = painterResource(id = R.drawable.thoughtbg),
            contentDescription = "Login Image"
        )


        //Area For Email

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email Icon",
                    tint = Color(0xFFB7FF00)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFB7FF00),
                unfocusedBorderColor = Color.Black,
                cursorColor = Color(0xFFB7FF00)
            ),
            label = {
                Text(
                    text = "Email",
                    style = TextStyle(color = Color.Black)
                )
            },
            singleLine = true
        )




        Spacer(modifier = Modifier.height(8.dp))


        //Area For Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock Icon",
                    tint = Color(0xFFB7FF00)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .onFocusChanged {},
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFB7FF00),
                unfocusedBorderColor = Color.Black,
                cursorColor = Color(0xFFB7FF00)
            ),
            label = {
                Text(
                    text = "Password",
                    style = TextStyle(color = Color.Black)
                )
            },
            singleLine = true
        )


        Spacer(modifier = Modifier.height(8.dp))


        //Area for Login Button

        Button(
            onClick = {

                isLoading = true
                authViewModel.login(email,password){
                        success, errorMessage ->
                    if (success ){
                        isLoading = false
                        navController.navigate("home"){
                            popUpTo("auth"){
                                inclusive = true
                            }
                        }
                    }else {
                        isLoading = false
                        AppUtil.showToast(context = context, errorMessage?:"Something went wrong")
                    }
                }

            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp) // Added padding to ensure text is inside the button
                .height(56.dp) // Set a height for the button
                .border(1.dp, Color.Black) // Border color
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFB7FF00), // Green color
                            Color(0xFFB7FF00)  // Lighter green color
                        )
                    )
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent // Transparent background to let gradient show
            ), // Avoid excessive padding, set to 0
            enabled = !isLoading
        ) {
            Text(
                text = if(isLoading) "Loading..." else "Log In",
                style = TextStyle(fontSize = 20.sp, color = Color.Black)
            )
        }

    }
}