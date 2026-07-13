package com.xiao.wordshow.ui.display

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.xiao.wordshow.ui.display.components.StaticText
import com.xiao.wordshow.ui.input.InputViewModel

@Composable
fun DisplayScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    inputViewModel: InputViewModel
) {
    val text by inputViewModel.text.collectAsState()

    if (text.isBlank()) {
        // 无内容时的占位提示
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "无显示内容\n请返回输入页输入文字",
                textAlign = TextAlign.Center
            )
        }
    } else {
        // 静止大字显示
        StaticText(
            text = text,
            modifier = modifier
        )
    }
}
