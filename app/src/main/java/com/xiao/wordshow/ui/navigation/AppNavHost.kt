package com.xiao.wordshow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.xiao.wordshow.ui.display.DisplayScreen
import com.xiao.wordshow.ui.input.InputScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.INPUT
    ) {
        composable(Routes.INPUT) {
            InputScreen(
                onNavigateToDisplay = { navController.navigate(Routes.DISPLAY) }
            )
        }
        composable(Routes.DISPLAY) {
            DisplayScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
