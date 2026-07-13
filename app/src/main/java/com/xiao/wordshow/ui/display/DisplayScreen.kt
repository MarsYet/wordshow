package com.xiao.wordshow.ui.display

import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.xiao.wordshow.data.model.TextEffect
import com.xiao.wordshow.ui.display.components.ScrollingText
import com.xiao.wordshow.ui.display.components.TextEffects
import com.xiao.wordshow.ui.input.InputViewModel
import com.xiao.wordshow.util.AdaptiveParams
import com.xiao.wordshow.util.FullscreenUtil

@Composable
fun DisplayScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    inputViewModel: InputViewModel,
    displayViewModel: DisplayViewModel,
    adaptive: AdaptiveParams
) {
    val text by inputViewModel.text.collectAsState()
    val isScrolling by displayViewModel.isScrolling.collectAsState()
    val isFullscreen by displayViewModel.isFullscreen.collectAsState()
    val fontSize by displayViewModel.fontSize.collectAsState()
    val currentEffect by displayViewModel.currentEffect.collectAsState()
    val scrollSpeed by displayViewModel.scrollSpeed.collectAsState()

    val activity = LocalContext.current as ComponentActivity
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

    val isPhone = remember { adaptive.maxFontSize <= 300f }

    fun doToggleFullscreen() {
        val willBeFullscreen = !isFullscreen
        if (willBeFullscreen) {
            FullscreenUtil.enterFullscreen(activity)
            if (isPhone) {
                // 先隐藏系统栏，再锁横屏，避免 hide() 触发 orientation reset
                activity.window?.decorView?.post {
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
            }
        } else {
            if (isPhone) activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            FullscreenUtil.exitFullscreen(activity)
            showControls = true
        }
        displayViewModel.toggleFullscreen()
    }

    // 离开此页面时恢复竖屏
    DisposableEffect(Unit) {
        onDispose {
            if (isPhone) activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 动态背景（必须给 fillMaxSize 否则 Canvas 尺寸为 0）
        AnimatedBackground(Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize()) {
        // 文字显示区 — 控制栏上方空间内居中，支持双指缩放字号
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        val newSize = fontSize * zoom
                        displayViewModel.setFontSize(newSize)
                    }
                }
                .then(
                    if (isFullscreen && !showControls) {
                        Modifier.pointerInput(Unit) { detectTapGestures { showControls = true } }
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (text.isBlank()) {
                Text(
                    text = "无显示内容\n请返回输入页输入文字",
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.5f)
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

        // 底部控制区（全屏时淡入淡出）
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                FontSizeSlider(
                    fontSize = fontSize,
                    onFontSizeChange = displayViewModel::setFontSize,
                    isFullscreen = isFullscreen,
                    range = adaptive.minFontSize..adaptive.maxFontSize
                )
                if (isScrolling) {
                    SpeedSlider(
                        speed = scrollSpeed,
                        onSpeedChange = displayViewModel::setScrollSpeed,
                        isFullscreen = isFullscreen,
                        maxSpeed = adaptive.maxScrollSpeed
                    )
                }
                // 液态玻璃控制栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .glassBg(isFullscreen, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (isFullscreen) doToggleFullscreen()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回",
                            tint = Color.White)
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        if (isScrolling) "滚动中" else "静止",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    IconButton(onClick = { displayViewModel.toggleScrolling() }) {
                        Icon(
                            if (isScrolling) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                            if (isScrolling) "切换为静止" else "切换为滚动",
                            tint = Color.White
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
                        doToggleFullscreen()
                    }) {
                        Icon(
                            if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                            if (isFullscreen) "退出全屏" else "全屏",
                            tint = Color.White
                        )
                    }
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
    isFullscreen: Boolean,
    range: ClosedFloatingPointRange<Float>
) {
    val textColor = Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .glassBg(isFullscreen, RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
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
            valueRange = range,
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
    isFullscreen: Boolean,
    maxSpeed: Float
) {
    val textColor = Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .glassBg(isFullscreen, RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
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
            valueRange = 0.2f..maxSpeed,
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

/**
 * 液态玻璃 Modifier — 半透明 + 细边框 + Shimmer 扫光
 */
@Composable
private fun Modifier.glassBg(isFullscreen: Boolean, shape: androidx.compose.ui.graphics.Shape): Modifier {
    val bgColor = if (isFullscreen) Color.Black.copy(alpha = 0.55f)
                  else Color.White.copy(alpha = 0.12f)
    val borderColor = Color.White.copy(alpha = if (isFullscreen) 0.1f else 0.25f)

    val shimmer = rememberInfiniteTransition(label = "shimmer")
    val sweep by shimmer.animateFloat(-1f, 2f,
        infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Restart), "sw")

    return this
        .clip(shape)
        .background(bgColor)
        .drawWithContent {
            drawContent()
            // 45° 倾斜扫光
            val shimmerBrush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.15f),
                    Color.White.copy(alpha = 0.25f),
                    Color.White.copy(alpha = 0.15f),
                    Color.Transparent,
                ),
                start = Offset(sweep * size.width, 0f),
                end = Offset(sweep * size.width + size.height, size.height)
            )
            drawRect(shimmerBrush)
        }
        .border(0.5.dp, borderColor, shape)
}
