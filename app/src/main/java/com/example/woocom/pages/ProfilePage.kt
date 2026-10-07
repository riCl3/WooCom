package com.example.woocom.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.woocom.Routes
import com.example.woocom.components.ErrorState
import com.example.woocom.components.LoadingState
import com.example.woocom.components.PremiumBackground
import com.example.woocom.data.ServiceLocator
import com.example.woocom.data.resourceOf
import com.example.woocom.model.UserModel
import com.example.woocom.ui.theme.CardSurface
import com.example.woocom.ui.theme.GreenPrimary
import com.example.woocom.ui.theme.NeonBorder
import com.example.woocom.ui.theme.PrimaryText
import com.example.woocom.ui.theme.SecondaryText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage(navController: NavHostController) {
    var userModel by remember { mutableStateOf<UserModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var attempt by remember { mutableStateOf(0) }
    var isLoggingOut by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(attempt) {
        isLoading = true
        loadError = null
        val result = resourceOf { ServiceLocator.userRepository.currentUser() }
        userModel = result.dataOrNull
        loadError = result.errorMessageOrNull
        isLoading = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Profile", color = PrimaryText) },
                colors =
                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                    ),
            )
        },
        containerColor = Color.Transparent,
    ) { paddingValues ->
        PremiumBackground {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
            ) {
                when {
                    isLoading -> LoadingState(modifier = Modifier.align(Alignment.Center))

                    loadError != null ->
                        ErrorState(
                            message = loadError!!,
                            onRetry = { attempt++ },
                            modifier = Modifier.align(Alignment.Center),
                        )

                    else ->
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            ProfileHeader(user = userModel)
                            Spacer(modifier = Modifier.height(24.dp))
                            ProfileMenu(
                                navController = navController,
                                isLoggingOut = isLoggingOut,
                                signedIn = userModel != null,
                                onLogoutClicked = {
                                    isLoggingOut = true
                                    scope.launch {
                                        runCatching { ServiceLocator.authRepository.signOut() }
                                        navController.navigate(Routes.AUTH) {
                                            popUpTo(Routes.HOME) { inclusive = true }
                                        }
                                    }
                                },
                            )
                        }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(user: UserModel?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            modifier =
                Modifier
                    .size(100.dp)
                    .background(GreenPrimary.copy(alpha = 0.12f), CircleShape)
                    .padding(12.dp),
            tint = GreenPrimary,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = user?.name ?: "Guest User",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryText,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = user?.email ?: "No email provided",
            fontSize = 16.sp,
            color = SecondaryText,
        )
    }
}

@Composable
private fun ProfileMenu(
    navController: NavHostController,
    isLoggingOut: Boolean,
    signedIn: Boolean,
    onLogoutClicked: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, NeonBorder),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Column {
            ProfileMenuItem(icon = Icons.AutoMirrored.Filled.ReceiptLong, text = "My Orders", onClick = {
                navController.navigate(Routes.ORDERS) { launchSingleTop = true }
            })
            HorizontalDivider(color = SecondaryText.copy(alpha = 0.2f))
            ProfileMenuItem(icon = Icons.Filled.LocationOn, text = "Shipping Addresses", onClick = {
                navController.navigate(Routes.ADDRESSES) { launchSingleTop = true }
            })
            HorizontalDivider(color = SecondaryText.copy(alpha = 0.2f))
            ProfileMenuItem(icon = Icons.Filled.Tune, text = "Settings", onClick = {
                navController.navigate(Routes.SETTINGS) { launchSingleTop = true }
            })
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    if (signedIn) {
        Button(
            onClick = onLogoutClicked,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    contentColor = Color(0xFF141414),
                ),
            shape = RoundedCornerShape(14.dp),
            enabled = !isLoggingOut,
        ) {
            if (isLoggingOut) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color(0xFF141414),
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = "Log Out",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    } else {
        Button(
            onClick = {
                navController.navigate(Routes.AUTH) {
                    popUpTo(Routes.HOME) { inclusive = true }
                }
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    contentColor = Color(0xFF141414),
                ),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(
                text = "Sign In",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit = {},
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GreenPrimary.copy(alpha = 0.8f),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            color = PrimaryText,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = SecondaryText,
        )
    }
}
