package com.xiao.wordshow.ui.display.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 文字特效渲染组件（P2）
 * 支持渐变、阴影、发光、跳动等效果
 */
@Composable
fun TextEffects(
    text: String,
    effectType: com.xiao.wordshow.data.model.TextEffect = com.xiao.wordshow.data.model.TextEffect.NONE,
    modifier: Modifier = Modifier
) {
    // TODO: M3 实现文字特效
    StaticText(text = text, modifier = modifier)
}
