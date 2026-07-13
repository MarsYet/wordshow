package com.xiao.wordshow.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.xiao.wordshow.ui.display.DisplayScreen
import com.xiao.wordshow.ui.display.DisplayViewModel
import com.xiao.wordshow.ui.input.InputScreen
import com.xiao.wordshow.ui.input.InputViewModel
import com.xiao.wordshow.util.rememberAdaptiveParams

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val activity = LocalContext.current as ComponentActivity
    val inputViewModel: InputViewModel = viewModel(viewModelStoreOwner = activity)
    val displayViewModel: DisplayViewModel = viewModel(viewModelStoreOwner = activity)
    val adaptive = rememberAdaptiveParams()

    NavHost(navController = navController, startDestination = Routes.INPUT) {
        composable(
            route = Routes.INPUT,
            exitTransition = { slideOutHorizontally { -it / 3 } },
            popEnterTransition = { slideInHorizontally { -it / 3 } }
        ) {
            InputScreen(
                onNavigateToDisplay = { navController.navigate(Routes.DISPLAY) },
                inputViewModel = inputViewModel,
                adaptive = adaptive
            )
        }
        composable(
            route = Routes.DISPLAY,
            enterTransition = { slideInHorizontally { it } },
            popExitTransition = { slideOutHorizontally { it } }
        ) {
            DisplayScreen(
                onNavigateBack = { navController.popBackStack() },
                inputViewModel = inputViewModel,
                displayViewModel = displayViewModel,
                adaptive = adaptive
            )
        }
    }
}
