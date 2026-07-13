package com.xiao.wordshow.ui.display

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 显示页 ViewModel — 管理显示模式（静止/滚动）、全屏状态
 */
class DisplayViewModel : ViewModel() {

    private val _isScrolling = MutableStateFlow(false)
    val isScrolling: StateFlow<Boolean> = _isScrolling.asStateFlow()

    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

    private val _fontSize = MutableStateFlow(64f)
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()

    fun toggleScrolling() {
        _isScrolling.value = !_isScrolling.value
    }

    fun toggleFullscreen() {
        _isFullscreen.value = !_isFullscreen.value
    }

    fun setFontSize(size: Float) {
        _fontSize.value = size.coerceIn(20f, 200f)
    }
}
