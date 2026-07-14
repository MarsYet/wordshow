package com.xiao.wordshow.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.xiao.wordshow.data.preferences.HistoryRepository
import com.xiao.wordshow.ui.display.DisplayScreen
import com.xiao.wordshow.ui.display.DisplayViewModel
import com.xiao.wordshow.ui.input.InputScreen
import com.xiao.wordshow.ui.input.InputViewModel
import com.xiao.wordshow.ui.settings.SettingsScreen
import com.xiao.wordshow.util.rememberAdaptiveParams

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val activity = LocalContext.current as ComponentActivity
    val inputViewModel: InputViewModel = viewModel(viewModelStoreOwner = activity)
    val displayViewModel: DisplayViewModel = viewModel(viewModelStoreOwner = activity)
    val adaptive = rememberAdaptiveParams()
    val repo = remember { HistoryRepository(activity) }

    NavHost(navController = navController, startDestination = Routes.INPUT) {
        composable(
            route = Routes.INPUT,
            enterTransition = { fadeIn(spring()) },
            exitTransition = { fadeOut(spring()) },
        ) {
            InputScreen(
                onNavigateToDisplay = { navController.navigate(Routes.DISPLAY) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                inputViewModel = inputViewModel,
                adaptive = adaptive,
                repo = repo
            )
        }
        composable(
            route = Routes.DISPLAY,
            enterTransition = { fadeIn(spring()) },
            exitTransition = { fadeOut(spring()) },
        ) {
            DisplayScreen(
                onNavigateBack = { navController.popBackStack() },
                inputViewModel = inputViewModel,
                displayViewModel = displayViewModel,
                adaptive = adaptive,
                repo = repo
            )
        }
        composable(
            route = Routes.SETTINGS,
            enterTransition = { fadeIn(spring()) },
            exitTransition = { fadeOut(spring()) },
        ) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                repo = repo,
                displayViewModel = displayViewModel
            )
        }
    }
}
