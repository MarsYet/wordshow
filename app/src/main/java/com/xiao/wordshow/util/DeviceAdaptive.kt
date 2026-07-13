package com.xiao.wordshow.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

/**
 * 设备自适应参数 — 用屏幕短边判断设备类型，不受横竖屏影响
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
    val config = LocalConfiguration.current
    val shortSideDp = minOf(config.screenWidthDp, config.screenHeightDp)

    return if (shortSideDp >= 600) {
        // 平板（短边 ≥600dp）
        AdaptiveParams(
            defaultFontSize = 180f,
            maxFontSize = 700f,
            minFontSize = 24f,
            defaultScrollSpeed = 2f,
            maxScrollSpeed = 20f,
            voiceBarCount = 13,
        )
    } else {
        // 手机（短边 <600dp）
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
