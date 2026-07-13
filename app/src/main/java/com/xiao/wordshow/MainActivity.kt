package com.xiao.wordshow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.xiao.wordshow.ui.navigation.AppNavHost
import com.xiao.wordshow.ui.theme.WordshowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WordshowTheme {
                AppNavHost()
            }
        }
    }
}