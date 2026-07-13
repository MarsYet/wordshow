package com.xiao.wordshow.data.model

/**
 * 显示设置数据类 — 字体大小、显示模式、特效等状态
 */
data class DisplaySettings(
    val text: String = "",
    val fontSize: Float = 64f,
    val isScrolling: Boolean = false,
    val scrollSpeed: Float = 1f,
    val effectType: TextEffect = TextEffect.NONE
)

enum class TextEffect {
    NONE, GRADIENT, SHADOW, GLOW, BOUNCE
}
