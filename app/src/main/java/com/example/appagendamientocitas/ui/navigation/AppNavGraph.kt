package com.example.appagendamientocitas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.appagendamientocitas.data.local.entity.UserRole
import com.example.appagendamientocitas.ui.screens.client.ClientScreen
import com.example.appagendamientocitas.ui.screens.login.LoginScreen
import com.example.appagendamientocitas.ui.screens.owner.OwnerScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: Screen = Screen.Login
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable<Screen.Login> {
            LoginScreen(
                onLoginSuccess = { role ->
                    val target: Screen = when (role) {
                        UserRole.ADMIN -> Screen.OwnerDashboard
                        UserRole.CLIENT -> Screen.ClientHome
                    }
                    navController.navigate(target) {
                        popUpTo(Screen.Login) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<Screen.ClientHome> {
            ClientScreen(
                onLogout = { navigateToLogin(navController) }
            )
        }

        composable<Screen.OwnerDashboard> {
            OwnerScreen(
                onLogout = { navigateToLogin(navController) }
            )
        }
    }
}

private fun navigateToLogin(navController: NavHostController) {
    navController.navigate(Screen.Login) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}