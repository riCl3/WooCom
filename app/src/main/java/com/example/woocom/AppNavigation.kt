package com.example.woocom

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.woocom.pages.AddressPage
import com.example.woocom.pages.CategoryProductsPage
import com.example.woocom.pages.CheckoutPage
import com.example.woocom.pages.OrdersPage
import com.example.woocom.pages.ProductDetailsPage
import com.example.woocom.pages.SearchPage
import com.example.woocom.pages.SettingsPage
import com.example.woocom.screens.AuthScreen
import com.example.woocom.screens.HomeScreen
import com.example.woocom.screens.LoginScreen
import com.example.woocom.screens.SignUpScreen
import com.example.woocom.screens.SplashScreen

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(navController)
        }
        composable(Routes.AUTH) {
            AuthScreen(navController)
        }
        composable(Routes.LOGIN) {
            LoginScreen(navController)
        }
        composable(Routes.SIGNUP) {
            SignUpScreen(navController)
        }
        composable(Routes.HOME) {
            HomeScreen(navController)
        }
        composable(Routes.CATEGORY_PRODUCTS) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId").orEmpty()
            CategoryProductsPage(navController, categoryId)
        }
        composable(Routes.PRODUCT_DETAILS) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId").orEmpty()
            ProductDetailsPage(navController, productId)
        }
        composable(Routes.CHECKOUT) {
            CheckoutPage(navController)
        }
        composable(Routes.SEARCH) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query").orEmpty()
            SearchPage(navController, query)
        }
        composable(Routes.ORDERS) {
            OrdersPage(navController)
        }
        composable(Routes.ADDRESSES) {
            AddressPage(navController)
        }
        composable(Routes.SETTINGS) {
            SettingsPage(navController)
        }
    }
}