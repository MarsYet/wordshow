package com.xiao.wordshow.ui.display.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * 静止文字显示组件（P0）
 * 居中大字展示输入的文字内容
 */
@Composable
fun StaticText(
    text: String,
    modifier: Modifier = Modifier
) {
    // TODO: M1 Step 2 实现实际的大字显示逻辑
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.ifEmpty { "预览文字" },
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center
        )
    }
}
