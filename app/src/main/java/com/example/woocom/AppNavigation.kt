package com.example.woocom

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.woocom.pages.CategoryProductsPage
import com.example.woocom.pages.ProductDetailsPage
import com.example.woocom.screens.AuthScreen
import com.example.woocom.screens.HomeScreen
import com.example.woocom.screens.LoginScreen
import com.example.woocom.screens.SignUp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val isLoggeIn = Firebase.auth.currentUser!=null
    val firstPage = if(isLoggeIn) "home" else "auth"
    GlobalNavigation.navController = navController

    NavHost(navController = navController, startDestination = firstPage ) {
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
    }
}


object GlobalNavigation{
    lateinit var navController : NavHostController
}