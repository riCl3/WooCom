package com.example.woocom.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.woocom.components.GlassBottomNavigation
import com.example.woocom.pages.CartPage
import com.example.woocom.pages.FavoritePage
import com.example.woocom.pages.HomePage
import com.example.woocom.pages.ProfilePage
import com.example.woocom.ui.theme.GradientEnd
import com.example.woocom.ui.theme.GradientStart

data class NavItem(
    val label: String,
    val icon: ImageVector
)

@Composable
fun HomeScreen(navController: NavHostController) {
    val navItemList = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Favorite", Icons.Default.Favorite),
        NavItem("Cart", Icons.Default.ShoppingCart),
        NavItem("Profile", Icons.Default.Person)
    )

    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(GradientStart, GradientEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 88.dp)
        ) {
            ContentScreen(
                navController = navController,
                selectedIndex = selectedIndex,
                onSelectTab = { selectedIndex = it }
            )
        }

        GlassBottomNavigation(
            navItems = navItemList,
            selectedIndex = selectedIndex,
            onItemSelected = { selectedIndex = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun ContentScreen(
    navController: NavHostController,
    selectedIndex: Int,
    onSelectTab: (Int) -> Unit
) {
    when (selectedIndex) {
        0 -> HomePage(navController)
        1 -> FavoritePage()
        2 -> CartPage(navController, onGoHome = { onSelectTab(0) })
        3 -> ProfilePage(navController)
    }
}