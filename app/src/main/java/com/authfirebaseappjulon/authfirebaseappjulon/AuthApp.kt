package com.authfirebaseappjulon.authfirebaseappjulon

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.authfirebaseappjulon.authfirebaseappjulon.ui.screens.HomeScreen
import com.authfirebaseappjulon.authfirebaseappjulon.ui.screens.LoginScreen
import com.authfirebaseappjulon.authfirebaseappjulon.ui.screens.RegisterScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthApp() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val inicio = if (auth.currentUser != null) "home" else "login"

    NavHost(navController = navController, startDestination = inicio) {
        composable("login")    { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("home")     { HomeScreen(navController) }
    }
}
