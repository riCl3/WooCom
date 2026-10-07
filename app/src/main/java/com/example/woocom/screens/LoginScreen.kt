package com.example.woocom.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.woocom.AppUtil
import com.example.woocom.R
import com.example.woocom.Routes
import com.example.woocom.components.PremiumBackground
import com.example.woocom.ui.theme.CardSurface
import com.example.woocom.ui.theme.DarkText
import com.example.woocom.ui.theme.GreenPrimary
import com.example.woocom.ui.theme.NeonBorder
import com.example.woocom.ui.theme.PrimaryText
import com.example.woocom.ui.theme.SecondaryText
import com.example.woocom.viewmodel.AuthUiState
import com.example.woocom.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(),
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var emailError by rememberSaveable { mutableStateOf(false) }
    var passwordError by rememberSaveable { mutableStateOf(false) }
    val authState by authViewModel.state.collectAsStateWithLifecycle()
    val isLoading = authState is AuthUiState.Submitting
    val context = LocalContext.current

    fun validate(): Boolean {
        emailError = email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        passwordError = password.length < 6
        return !emailError && !passwordError
    }

    LaunchedEffect(authState) {
        when (val current = authState) {
            AuthUiState.Success -> {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.AUTH) { inclusive = true }
                }
                authViewModel.consumeSuccess()
            }
            is AuthUiState.Failure -> {
                AppUtil.showToast(context, current.message)
                authViewModel.clearError()
            }
            else -> Unit
        }
    }

    PremiumBackground {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Image(
                painter = painterResource(id = R.drawable.icon_bg),
                contentDescription = "WooCom logo",
                modifier = Modifier.size(110.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Welcome back",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryText,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Log in to continue shopping",
                fontSize = 14.sp,
                color = SecondaryText,
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface.copy(alpha = 0.9f)),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, NeonBorder),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = GreenPrimary,
                            )
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth(),
                        colors = fieldColors(),
                        label = { Text("Email") },
                        singleLine = true,
                        isError = emailError,
                        supportingText =
                            if (emailError) {
                                { Text("Enter a valid email address") }
                            } else {
                                null
                            },
                        keyboardOptions =
                            androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                            ),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = GreenPrimary,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors(),
                        label = { Text("Password") },
                        singleLine = true,
                        isError = passwordError,
                        supportingText =
                            if (passwordError) {
                                { Text("Password must be at least 6 characters") }
                            } else {
                                null
                            },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions =
                            androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                            ),
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (validate()) authViewModel.login(email.trim(), password)
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = GreenPrimary,
                                contentColor = DarkText,
                            ),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isLoading,
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = DarkText,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(
                                text = "Log In",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Don't have an account? ", color = SecondaryText)
                        TextButton(onClick = {
                            navController.navigate(Routes.SIGNUP) { launchSingleTop = true }
                        }) {
                            Text("Sign Up", fontWeight = FontWeight.Bold, color = GreenPrimary)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun fieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = GreenPrimary,
        unfocusedBorderColor = GreenPrimary.copy(alpha = 0.35f),
        cursorColor = GreenPrimary,
        focusedTextColor = PrimaryText,
        unfocusedTextColor = PrimaryText,
        focusedLabelColor = GreenPrimary,
        unfocusedLabelColor = SecondaryText,
        errorBorderColor = Color(0xFFFF6B6B),
        errorLabelColor = Color(0xFFFF6B6B),
        errorSupportingTextColor = Color(0xFFFF6B6B),
    )
