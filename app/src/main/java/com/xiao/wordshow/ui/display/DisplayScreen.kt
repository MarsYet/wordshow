package com.xiao.wordshow.ui.display

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xiao.wordshow.ui.display.components.ScrollingText
import com.xiao.wordshow.ui.display.components.StaticText
import com.xiao.wordshow.ui.input.InputViewModel
import com.xiao.wordshow.util.FullscreenUtil

@Composable
fun DisplayScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    inputViewModel: InputViewModel,
    displayViewModel: DisplayViewModel
) {
    val text by inputViewModel.text.collectAsState()
    val isScrolling by displayViewModel.isScrolling.collectAsState()
    val isFullscreen by displayViewModel.isFullscreen.collectAsState()

    val activity = LocalContext.current as ComponentActivity

    // 全屏切换副作用
    DisposableEffect(isFullscreen) {
        if (isFullscreen) {
            FullscreenUtil.enterFullscreen(activity)
        } else {
            FullscreenUtil.exitFullscreen(activity)
        }
        onDispose {
            FullscreenUtil.exitFullscreen(activity)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 主显示区域
        if (text.isBlank()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "无显示内容\n请返回输入页输入文字",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (isScrolling) {
            ScrollingText(text = text)
        } else {
            StaticText(text = text)
        }

        // 底部控制栏（全屏时半透明悬浮）
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .then(
                    if (isFullscreen) {
                        Modifier.background(Color.Black.copy(alpha = 0.5f))
                    } else {
                        Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
                    }
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 返回按钮
            IconButton(onClick = {
                if (isFullscreen) displayViewModel.toggleFullscreen()
                onNavigateBack()
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = if (isFullscreen) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 模式提示文字
            Text(
                text = if (isScrolling) "滚动中" else "静止",
                style = MaterialTheme.typography.labelLarge,
                color = if (isFullscreen) Color.White.copy(alpha = 0.8f)
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 滚动/静止切换
            IconButton(onClick = { displayViewModel.toggleScrolling() }) {
                Icon(
                    imageVector = if (isScrolling) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                    contentDescription = if (isScrolling) "切换为静止" else "切换为滚动",
                    tint = if (isFullscreen) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }

            // 全屏切换
            IconButton(onClick = { displayViewModel.toggleFullscreen() }) {
                Icon(
                    imageVector = if (isFullscreen) Icons.Filled.FullscreenExit
                                  else Icons.Filled.Fullscreen,
                    contentDescription = if (isFullscreen) "退出全屏" else "全屏",
                    tint = if (isFullscreen) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
