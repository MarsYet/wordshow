package com.xiao.wordshow.ui.input

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 输入页 ViewModel — 管理用户输入的文字状态
 * 通过 Activity 作用域共享给 DisplayScreen
 */
class InputViewModel : ViewModel() {

    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text.asStateFlow()

    fun updateText(newText: String) {
        _text.value = newText
    }

    fun clearText() {
        _text.value = ""
    }
}
