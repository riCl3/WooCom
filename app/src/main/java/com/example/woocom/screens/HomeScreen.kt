package com.example.woocom.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.woocom.pages.CartPage
import com.example.woocom.pages.FavoritePage
import com.example.woocom.pages.HomePage
import com.example.woocom.pages.ProfilePage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth


@Composable
fun HomeScreen(modifier: Modifier=Modifier, navController: NavHostController) {
    var isLoading by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /*
        Text("Home Screen")
        Button(
            onClick = {
                Firebase.auth.signOut()
                navController.navigate("auth"){
                    popUpTo("home"){
                        inclusive = true
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal=16.dp) // Added padding to ensure text is inside the button
                .height(56.dp) // Set a height for the button
                .border(1.dp, Color.Black) // Border color
                .background(
                    Brush.horizontalGradient(
                        colors=listOf(
                            Color(0xFFB7FF00), // Green color
                            Color(0xFFB7FF00)  // Lighter green color
                        )
                    )
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent // Transparent background to let gradient show
            ), // Avoid excessive padding, set to 0
            enabled = !isLoading

        ){
            Text(
                text = if(isLoading) "Logging Out" else "Log Out",
                style = TextStyle(fontSize = 20.sp, color = Color.Black)
            )
        }
        */

        //NavItem List
        val navItemList = listOf(
            NavItem("Home", Icons.Default.Home),
            NavItem("Favorite", Icons.Default.Favorite),
            NavItem("Cart", Icons.Default.ShoppingCart),
            NavItem("Profile", Icons.Default.Person),
        )

        var selectedIndex = remember { mutableStateOf(0) }
        //Bottom Navigation Bar
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = Color.LightGray,
                    modifier = Modifier.clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)) // Rounded top corners
                ) {
                    navItemList.forEachIndexed { index, navItem ->
                        NavigationBarItem(
                            selected = index == selectedIndex.value,
                            onClick = {
                                selectedIndex.value = index
                            },
                            icon = {
                                Icon(
                                    imageVector = navItem.icon,
                                    contentDescription = navItem.label,
                                    tint = if (index == selectedIndex.value) Color.White else Color.Black // Selected: White, Unselected: Black
                                )
                            },
                            label = {
                                Text(
                                    text = navItem.label,
                                    color = Color.Black // Selected: White, Unselected: Black
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Gray // Black indicator when selected
                            )
                        )
                    }
                }
            }
        ) {
            ContentScreen(modifier = Modifier.padding(it), selectedIndex.value)
        }

    }
}

@Composable
fun ContentScreen(modifier: Modifier=Modifier, selectedIndex: Int) {
    when (selectedIndex) {
        0 -> HomePage(modifier)
        1 -> FavoritePage(modifier)
        2 -> CartPage(modifier)
        3 -> ProfilePage(modifier)
    }
}

data class NavItem(
    val label: String,
    val icon: ImageVector,
)