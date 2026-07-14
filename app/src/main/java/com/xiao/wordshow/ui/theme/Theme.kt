package com.xiao.wordshow.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = WarmBrown, secondary = WarmGray, tertiary = WarmAccent,
    background = IvoryDark, surface = IvoryPearl, surfaceVariant = Color(0xFFE8DDD0),
    onPrimary = Color.White, onBackground = WarmText, onSurface = WarmText, onSurfaceVariant = WarmGray,
)

private val LightColorScheme = lightColorScheme(
    primary = WarmBrown, secondary = WarmGray, tertiary = WarmAccent,
    background = IvoryBase, surface = PearlWhite, surfaceVariant = IvoryWarm,
    onPrimary = Color.White, onBackground = WarmText, onSurface = WarmText, onSurfaceVariant = WarmGray,
)

@Composable
fun WordshowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}