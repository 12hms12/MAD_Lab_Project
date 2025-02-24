package com.example.mad_lab_project

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")
    object CreateAlarmScreen : Screen("create_alarm_screen")
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.MainScreen.route
    ) {
        composable(Screen.MainScreen.route) {
            MainScreen(navController)
        }
        composable(Screen.CreateAlarmScreen.route) {
            CreateAlarmScreen(navController)
        }
    }
}
