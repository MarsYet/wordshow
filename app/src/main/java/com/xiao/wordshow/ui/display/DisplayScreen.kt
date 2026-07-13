package com.xiao.wordshow.ui.display

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.xiao.wordshow.data.model.TextEffect
import com.xiao.wordshow.ui.display.components.ScrollingText
import com.xiao.wordshow.ui.display.components.TextEffects
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
    val fontSize by displayViewModel.fontSize.collectAsState()
    val currentEffect by displayViewModel.currentEffect.collectAsState()
    val scrollSpeed by displayViewModel.scrollSpeed.collectAsState()

    val activity = LocalContext.current as ComponentActivity
    val density = LocalDensity.current

    // 全屏时控制栏显隐
    var showControls by remember { mutableStateOf(true) }
    var controlsVisible = !isFullscreen || showControls

    // 全屏自动隐藏定时器
    LaunchedEffect(isFullscreen, showControls) {
        if (isFullscreen && showControls) {
            delay(3000)
            showControls = false
        }
    }

    DisposableEffect(isFullscreen) {
        if (isFullscreen) FullscreenUtil.enterFullscreen(activity)
        else { FullscreenUtil.exitFullscreen(activity); showControls = true }
        onDispose { FullscreenUtil.exitFullscreen(activity) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 270sp以下保持正中，270以上线性上偏，350sp≈偏上64%，≥360sp=偏上72%
        val bias = -((fontSize - 270f) * 0.008f).coerceIn(0f, 0.72f)
        val contentAlign = remember(bias) {
            BiasAlignment(0f, bias)
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = contentAlign
        ) {
            if (text.isBlank()) {
                Text(
                    text = "无显示内容\n请返回输入页输入文字",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (isScrolling) {
                ScrollingText(
                    text = text, fontSize = fontSize.sp,
                    speed = scrollSpeed, effectType = currentEffect
                )
            } else {
                TextEffects(
                    text = text, fontSize = fontSize.sp,
                    effectType = currentEffect
                )
            }
        }

        // 触碰检测：全屏时点击显示控制栏
        if (isFullscreen && !showControls) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { showControls = true }
                    }
            )
        }

        // 底部控制区（全屏时淡入淡出）
        AnimatedVisibility(
            visible = controlsVisible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                FontSizeSlider(
                    fontSize = fontSize,
                    onFontSizeChange = displayViewModel::setFontSize,
                    isFullscreen = isFullscreen
                )
                if (isScrolling) {
                    SpeedSlider(
                        speed = scrollSpeed,
                        onSpeedChange = displayViewModel::setScrollSpeed,
                        isFullscreen = isFullscreen
                    )
                }
                // 控制按钮栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isFullscreen) Modifier.background(Color.Black.copy(alpha = 0.5f))
                            else Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (isFullscreen) displayViewModel.toggleFullscreen()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回",
                            tint = if (isFullscreen) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        if (isScrolling) "滚动中" else "静止",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isFullscreen) Color.White.copy(alpha = 0.8f)
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = { displayViewModel.toggleScrolling() }) {
                        Icon(
                            if (isScrolling) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                            if (isScrolling) "切换为静止" else "切换为滚动",
                            tint = if (isFullscreen) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { displayViewModel.cycleEffect() }) {
                        Icon(Icons.Filled.AutoFixHigh, "切换特效",
                            tint = if (currentEffect != TextEffect.NONE)
                                if (isFullscreen) Color(0xFFFFD93D) else MaterialTheme.colorScheme.tertiary
                            else if (isFullscreen) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = {
                        showControls = true
                        displayViewModel.toggleFullscreen()
                    }) {
                        Icon(
                            if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                            if (isFullscreen) "退出全屏" else "全屏",
                            tint = if (isFullscreen) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

/**
 * 字体大小调节滑块
 */
@Composable
private fun FontSizeSlider(
    fontSize: Float,
    onFontSizeChange: (Float) -> Unit,
    isFullscreen: Boolean
) {
    val textColor = if (isFullscreen) Color.White else MaterialTheme.colorScheme.onSurface
    val bgColor = if (isFullscreen) Color.Black.copy(alpha = 0.4f)
                  else MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.TextDecrease,
            contentDescription = "缩小字体",
            modifier = Modifier.size(20.dp),
            tint = textColor.copy(alpha = 0.7f)
        )

        Slider(
            value = fontSize,
            onValueChange = onFontSizeChange,
            valueRange = 20f..400f,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            colors = SliderDefaults.colors(
                thumbColor = if (isFullscreen) Color.White else MaterialTheme.colorScheme.primary,
                activeTrackColor = if (isFullscreen) Color.White.copy(alpha = 0.8f)
                                   else MaterialTheme.colorScheme.primary,
                inactiveTrackColor = if (isFullscreen) Color.White.copy(alpha = 0.3f)
                                     else MaterialTheme.colorScheme.surfaceContainerHighest
            )
        )

        Icon(
            imageVector = Icons.Filled.TextIncrease,
            contentDescription = "放大字体",
            modifier = Modifier.size(20.dp),
            tint = textColor.copy(alpha = 0.7f)
        )

        // 字号数值标签
        Text(
            text = "${fontSize.toInt()}",
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = 0.7f),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

/**
 * 滚动速度调节滑块
 */
@Composable
private fun SpeedSlider(
    speed: Float,
    onSpeedChange: (Float) -> Unit,
    isFullscreen: Boolean
) {
    val textColor = if (isFullscreen) Color.White else MaterialTheme.colorScheme.onSurface
    val bgColor = if (isFullscreen) Color.Black.copy(alpha = 0.4f)
                  else MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🐢",
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = 0.7f)
        )

        Slider(
            value = speed,
            onValueChange = onSpeedChange,
            valueRange = 0.2f..10f,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            colors = SliderDefaults.colors(
                thumbColor = if (isFullscreen) Color.White else MaterialTheme.colorScheme.primary,
                activeTrackColor = if (isFullscreen) Color.White.copy(alpha = 0.8f)
                                   else MaterialTheme.colorScheme.primary,
                inactiveTrackColor = if (isFullscreen) Color.White.copy(alpha = 0.3f)
                                     else MaterialTheme.colorScheme.surfaceContainerHighest
            )
        )

        Text(
            text = "🐇",
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = 0.7f)
        )

        Text(
            text = "×${String.format("%.1f", speed)}",
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = 0.7f),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
