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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.woocom.GlobalNavigation.navController
import com.example.woocom.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage(modifier: Modifier = Modifier) {
    var userModel by remember { mutableStateOf<UserModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoggingOut by remember { mutableStateOf(false) }

    // Fetch user data from Firestore
    LaunchedEffect(Unit) {
        try {
            val userId = Firebase.auth.currentUser?.uid
            if (userId != null) {
                val userSnapshot = Firebase.firestore.collection("user").document(userId).get().await()
                userModel = userSnapshot.toObject(UserModel::class.java)
            }
        } catch (e: Exception) {
            // Handle error
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Profile", color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        com.example.woocom.components.PremiumBackground {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color(0xFF4CAF50))
            } else if (userModel == null) {
                Text("Could not load profile.", color = Color.Gray)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfileHeader(user = userModel)
                    Spacer(modifier = Modifier.height(24.dp))
                    ProfileMenu(
                        onLogoutClicked = {
                            isLoggingOut = true
                            Firebase.auth.signOut()
                            navController.navigate("auth") {
                                popUpTo("home") {
                                    inclusive = true
                                }
                            }
                        },
                        isLoggingOut = isLoggingOut
                    )
                }
            }
        }
    }
}
}

@Composable
fun ProfileHeader(user: UserModel?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.White),
            tint = Color(0xFF1A1A1A).copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = user?.name ?: "Guest User",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = user?.email ?: "No email provided",
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun ProfileMenu(onLogoutClicked: () -> Unit, isLoggingOut: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = com.example.woocom.ui.theme.CardSurface),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, com.example.woocom.ui.theme.NeonBorder),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            ProfileMenuItem(icon = Icons.Default.AccountCircle, text = "My Orders", onClick = { navController.navigate("orders") })
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            ProfileMenuItem(icon = Icons.Default.LocationOn, text = "Shipping Addresses", onClick = { navController.navigate("addresses") })
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            ProfileMenuItem(icon = Icons.Default.FavoriteBorder, text = "My Wishlist", onClick = { navController.navigate("favorites") })
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            ProfileMenuItem(icon = Icons.Default.Settings, text = "Settings", onClick = { navController.navigate("settings") })
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = onLogoutClicked,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFA5E800),
            contentColor = Color(0xFF1A1A1A)
        ),
        shape = RoundedCornerShape(12.dp),
        enabled = !isLoggingOut
    ) {
        if (isLoggingOut) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color(0xFF1A1A1A),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "Log Out",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, text: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = Color.White.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Go to $text",
            tint = Color.Gray
        )
    }
}
