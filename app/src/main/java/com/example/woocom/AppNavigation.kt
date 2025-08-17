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
import com.example.woocom.screens.HomeScreen
import com.example.woocom.screens.LoginScreen
import com.example.woocom.screens.SignUp
import com.example.woocom.screens.SplashScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    GlobalNavigation.navController = navController

    NavHost(navController = navController, startDestination = "splash" ) {
        composable("splash") {
            SplashScreen(modifier, navController)
        }
        composable("auth"){
            AuthScreen(modifier,navController)
        }
        composable("login"){
            LoginScreen(modifier,navController)
        }
        composable("signup"){
            SignUp(modifier,navController)
        }
        composable("home"){
            HomeScreen(modifier,navController)
        }
        composable("category-products/{categoryId}"){
            var categoryId = it.arguments?.getString("categoryId")
            CategoryProductsPage(modifier,navController,categoryId?:"")
        }
        composable("product-details/{productId}"){
            var productId = it.arguments?.getString("productId")
            ProductDetailsPage(modifier,productId?:"")
        }
        composable("checkout"){
            CheckoutPage()
        }

    }
}


object GlobalNavigation{
    lateinit var navController : NavHostController
}