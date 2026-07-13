package com.xiao.wordshow.ui.display

import androidx.lifecycle.ViewModel
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
        _fontSize.value = size.coerceIn(20f, 400f)
    }

    fun setScrollSpeed(speed: Float) {
        _scrollSpeed.value = speed.coerceIn(0.2f, 3f)
    }

    fun setEffect(effect: TextEffect) {
        _currentEffect.value = effect
    }

    fun cycleEffect() {
        val effects = TextEffect.entries
        val nextIndex = (effects.indexOf(_currentEffect.value) + 1) % effects.size
        _currentEffect.value = effects[nextIndex]
    }
}
