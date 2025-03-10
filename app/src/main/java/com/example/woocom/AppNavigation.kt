package com.example.woocom

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.woocom.screens.AuthScreen
import com.example.woocom.screens.LoginScreen
import com.example.woocom.screens.SignUp

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "auth" ) {
        composable("auth"){
            AuthScreen(modifier,navController)
        }
        composable("login"){
            LoginScreen(modifier,navController)
        }
        composable("signup"){
            SignUp(modifier,navController)
        }
    }
}