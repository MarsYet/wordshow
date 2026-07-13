package com.xiao.wordshow.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 设备自适应参数
 */
data class AdaptiveParams(
    val defaultFontSize: Float,
    val maxFontSize: Float,
    val minFontSize: Float,
    val defaultScrollSpeed: Float,
    val maxScrollSpeed: Float,
    val voiceBarCount: Int,
)

@Composable
fun rememberAdaptiveParams(): AdaptiveParams {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    return when {
        screenWidthDp >= 840 -> {
            // 平板横屏
            AdaptiveParams(
                defaultFontSize = 180f,
                maxFontSize = 700f,
                minFontSize = 24f,
                defaultScrollSpeed = 2f,
                maxScrollSpeed = 20f,
                voiceBarCount = 13,
            )
        }
        screenWidthDp >= 600 -> {
            // 小平板 / 手机横屏
            AdaptiveParams(
                defaultFontSize = 120f,
                maxFontSize = 500f,
                minFontSize = 20f,
                defaultScrollSpeed = 1.5f,
                maxScrollSpeed = 15f,
                voiceBarCount = 9,
            )
        }
        else -> {
            // 手机竖屏
            AdaptiveParams(
                defaultFontSize = 64f,
                maxFontSize = 300f,
                minFontSize = 20f,
                defaultScrollSpeed = 1f,
                maxScrollSpeed = 10f,
                voiceBarCount = 7,
            )
        }
    }
}
