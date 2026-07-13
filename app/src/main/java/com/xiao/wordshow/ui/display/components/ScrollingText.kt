package com.xiao.wordshow.ui.display.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 滚动文字显示组件（P0）— 跑马灯效果
 */
@Composable
fun ScrollingText(
    text: String,
    speed: Float = 1f,
    modifier: Modifier = Modifier
) {
    // TODO: M1 Step 2 实现跑马灯滚动动画
    StaticText(text = text, modifier = modifier)
}
