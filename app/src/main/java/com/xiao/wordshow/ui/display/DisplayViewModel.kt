package com.xiao.wordshow.ui.display

import androidx.lifecycle.ViewModel
import androidx.compose.ui.graphics.Color
import com.xiao.wordshow.data.model.TextEffect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 显示页 ViewModel — 管理显示模式、全屏、字体、特效状态
 */
class DisplayViewModel : ViewModel() {

    private val _isScrolling = MutableStateFlow(false)
    val isScrolling: StateFlow<Boolean> = _isScrolling.asStateFlow()

    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

    private val _fontSize = MutableStateFlow(64f)
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()

    private val _scrollSpeed = MutableStateFlow(1f)
    val scrollSpeed: StateFlow<Float> = _scrollSpeed.asStateFlow()

    private val _currentEffect = MutableStateFlow(TextEffect.NONE)
    val currentEffect: StateFlow<TextEffect> = _currentEffect.asStateFlow()

    fun toggleScrolling() {
        _isScrolling.value = !_isScrolling.value
    }

    fun toggleFullscreen() {
        _isFullscreen.value = !_isFullscreen.value
    }

    fun setFontSize(size: Float) {
        _fontSize.value = size.coerceIn(20f, 700f)
    }

    fun setScrollSpeed(speed: Float) {
        _scrollSpeed.value = speed.coerceIn(0.2f, 20f)
    }

    fun setEffect(effect: TextEffect) {
        _currentEffect.value = effect
    }

    fun cycleEffect() {
        val effects = TextEffect.entries
        val nextIndex = (effects.indexOf(_currentEffect.value) + 1) % effects.size
        _currentEffect.value = effects[nextIndex]
    }

    // 浅色背景：默认跟随系统（深色系统=暗底白字，浅色系统=亮底黑字）
    private val _isLightBg = MutableStateFlow(true) // default light until system check
    val isLightBg: StateFlow<Boolean> = _isLightBg.asStateFlow()
    var lightBgInited = false

    fun toggleLightBg() {
        _isLightBg.value = !_isLightBg.value
    }

    fun setLightBg(light: Boolean) {
        _isLightBg.value = light
    }

    // 字体选择
    private val _fontIndex = MutableStateFlow(0)
    val fontIndex: StateFlow<Int> = _fontIndex.asStateFlow()

    fun setFont(index: Int) {
        _fontIndex.value = index.coerceIn(0, fontCount - 1)
    }

    companion object {
        val fontCount get() = 7
    }

    fun setColor(index: Int) {
        _colorIndex.value = index.coerceIn(0, presetTextColors.size)
    }

    // 文字颜色（0=自动跟随背景，1~N=预设色）
    private val _colorIndex = MutableStateFlow(0)
    val colorIndex: StateFlow<Int> = _colorIndex.asStateFlow()

    fun cycleTextColor() {
        _colorIndex.value = (_colorIndex.value + 1) % (presetTextColors.size + 1)
    }
}

val presetTextColors = listOf(
    Color(0xFFFFFFFF), // 白
    Color(0xFFFFD93D), // 黄
    Color(0xFFFF6B6B), // 红
    Color(0xFF6BCB77), // 绿
    Color(0xFF4D96FF), // 蓝
    Color(0xFFFF9224), // 橙
    Color(0xFFE040FB), // 紫
    Color(0xFF00E5FF), // 青
)

val colorNames = listOf("白", "黄", "红", "绿", "蓝", "橙", "紫", "青")
