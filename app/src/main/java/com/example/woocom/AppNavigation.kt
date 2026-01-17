package com.example.woocom

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.woocom.pages.CategoryProductsPage
import com.example.woocom.pages.CheckoutPage
import com.example.woocom.pages.ProductDetailsPage
import com.example.woocom.screens.AuthScreen
import com.example.woocom.pages.FavoritePage
import com.example.woocom.pages.OrdersPage
import com.example.woocom.pages.AddressPage
import com.example.woocom.pages.SettingsPage
import com.example.woocom.screens.HomeScreen
import com.example.woocom.screens.LoginScreen
import com.example.woocom.screens.SignUp
import com.example.woocom.pages.SearchPage
import com.example.woocom.screens.SplashScreen

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController=rememberNavController()
    GlobalNavigation.navController=navController

    NavHost(navController=navController, startDestination="splash") {
        composable("splash") {
            SplashScreen(modifier, navController)
        }
        composable("auth") {
            AuthScreen(modifier, navController)
        }
        composable("login") {
            LoginScreen(modifier, navController)
        }
        composable("signup") {
            SignUp(modifier, navController)
        }
        composable("home") {
            HomeScreen(modifier, navController)
        }
        composable("category-products/{categoryId}") {
            val categoryId=it.arguments?.getString("categoryId")
            CategoryProductsPage(modifier, navController, categoryId ?: "")
        }
        composable("product-details/{productId}") {
            val productId=it.arguments?.getString("productId")
            ProductDetailsPage(modifier, productId ?: "")
        }
        composable("checkout/{total}") { backStackEntry ->
            val total=backStackEntry.arguments?.getString("total")?.toFloatOrNull() ?: 0.0
            CheckoutPage(totalAmount=total)
        }
        composable("search/{query}") { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query") ?: ""
            SearchPage(modifier, navController, query)
        }
        composable("orders") {
            OrdersPage(modifier, navController)
        }
        composable("addresses") {
            AddressPage(modifier, navController)
        }
        composable("settings") {
            SettingsPage(modifier, navController)
        }
        composable("favorites") {
            FavoritePage(modifier)
        }
        }
    }


    object GlobalNavigation {
        lateinit var navController: NavHostController
    }
