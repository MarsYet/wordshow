package com.xiao.wordshow.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.xiao.wordshow.ui.display.DisplayScreen
import com.xiao.wordshow.ui.input.InputScreen
import com.xiao.wordshow.ui.input.InputViewModel

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    // ViewModel 作用域在 Activity，确保 InputScreen 和 DisplayScreen 共享同一实例
    val activity = LocalContext.current as ComponentActivity
    val inputViewModel: InputViewModel = viewModel(viewModelStoreOwner = activity)

    NavHost(
        navController = navController,
        startDestination = Routes.INPUT
    ) {
        composable(Routes.INPUT) {
            InputScreen(
                onNavigateToDisplay = { navController.navigate(Routes.DISPLAY) },
                inputViewModel = inputViewModel
            )
        }
        composable(Routes.DISPLAY) {
            DisplayScreen(
                onNavigateBack = { navController.popBackStack() },
                inputViewModel = inputViewModel
            )
        }
    }
}
