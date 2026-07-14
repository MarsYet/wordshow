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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.xiao.wordshow.data.model.TextEffect
import com.xiao.wordshow.ui.display.components.ScrollingText
import com.xiao.wordshow.ui.display.components.SubtitleDisplay
import com.xiao.wordshow.ui.display.components.TextEffects
import com.xiao.wordshow.ui.input.InputViewModel
import com.xiao.wordshow.util.AdaptiveParams
import com.xiao.wordshow.util.FullscreenUtil

private val fontOptions: List<Pair<FontFamily, FontWeight>> = listOf(
    FontFamily.Default to FontWeight.Normal,
    FontFamily.Default to FontWeight.Bold,
    FontFamily.Default to FontWeight.Light,
    FontFamily.Serif to FontWeight.Bold,
    FontFamily.SansSerif to FontWeight.Light,
    FontFamily.Monospace to FontWeight.Bold,
    FontFamily.Cursive to FontWeight.Bold,
)
private val fontNames = listOf("默认", "默认粗体", "默认细体", "衬线", "无衬线细", "等宽", "手写")

@Composable
fun DisplayScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    inputViewModel: InputViewModel,
    displayViewModel: DisplayViewModel,
    adaptive: AdaptiveParams,
    repo: com.xiao.wordshow.data.preferences.HistoryRepository
) {
    val text by inputViewModel.text.collectAsState()
    val isScrolling by displayViewModel.isScrolling.collectAsState()
    val isFullscreen by displayViewModel.isFullscreen.collectAsState()
    val fontSize by displayViewModel.fontSize.collectAsState()
    val currentEffect by displayViewModel.currentEffect.collectAsState()
    val scrollSpeed by displayViewModel.scrollSpeed.collectAsState()
    val isLightBg by displayViewModel.isLightBg.collectAsState()
    val fontIndex by displayViewModel.fontIndex.collectAsState()
    val colorIndex by displayViewModel.colorIndex.collectAsState()

    // 初始化颜色模式：从 DataStore 加载到 ViewModel
    val systemIsLight = !androidx.compose.foundation.isSystemInDarkTheme()
    LaunchedEffect(Unit) {
        repo.colorMode.collect { mode -> displayViewModel.setColorMode(mode, systemIsLight) }
    }
    val colorMode by displayViewModel.colorMode.collectAsState()
    val resolvedLight = when (colorMode) { "light" -> true; "dark" -> false; else -> systemIsLight }
    LaunchedEffect(resolvedLight) { displayViewModel.setLightBg(resolvedLight) }

    // 字幕模式
    val isBoardMode by displayViewModel.isBoardMode.collectAsState()
    val subtitleSentences by displayViewModel.subtitleSentences.collectAsState()
    val currentIndex by displayViewModel.currentSentenceIndex.collectAsState()
    val isPlaying by displayViewModel.isSubtitlePlaying.collectAsState()

    // 自动播放字幕（速度用 scrollSpeed 控制）
    LaunchedEffect(isPlaying, currentIndex, scrollSpeed) {
        if (!isBoardMode && isPlaying && subtitleSentences.isNotEmpty()) {
            val interval = (3000 / scrollSpeed).toLong().coerceIn(500, 10000)
            delay(interval)
            if (currentIndex < subtitleSentences.lastIndex) {
                displayViewModel.nextSentence()
            }
        }
    }

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
    val scope = rememberCoroutineScope()
    val contentColor = Color(0xFF2C3035)
    val textColor = if (colorIndex == 0) contentColor else com.xiao.wordshow.ui.display.presetTextColors[colorIndex - 1]
    val fontEntry: Pair<FontFamily, FontWeight> = fontOptions[fontIndex]
    val fontFamily: FontFamily = fontEntry.first
    val fontWeight: FontWeight = fontEntry.second
    val controlBg = Brush.verticalGradient(listOf(Color(0xDDFFFFFF), Color(0xBBF0F2F4), Color(0xAAE0E4E8)))
    val sliderBg = Brush.verticalGradient(listOf(Color(0xCCFFFFFF), Color(0xAAEEF0F2)))
    val sliderBorder = Color.White.copy(alpha = 0.5f)

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
        // 动态背景
        if (isLightBg) {
            Box(Modifier.fillMaxSize().background(Color(0xFFF2F2F2)))
        } else {
            AnimatedBackground(Modifier.fillMaxSize())
        }

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
            if (!isBoardMode && subtitleSentences.isNotEmpty()) {
                SubtitleDisplay(
                    sentence = subtitleSentences.getOrElse(currentIndex) { "" },
                    current = currentIndex + 1, total = subtitleSentences.size,
                    isPlaying = isPlaying, hasNext = currentIndex < subtitleSentences.lastIndex,
                    onPlayPause = { displayViewModel.togglePlayPause() },
                    onNext = { displayViewModel.nextSentence() },
                    onPrev = { displayViewModel.prevSentence() },
                    textColor = contentColor,
                    fontSize = fontSize.sp
                )
            } else if (!isBoardMode && subtitleSentences.isEmpty()) {
                Text("暂无字幕数据\n请返回输入页导入 Word 文件", textAlign = TextAlign.Center, color = contentColor.copy(alpha = 0.4f))
            } else if (text.isBlank()) {
                Text("无显示内容\n请返回输入页输入文字", textAlign = TextAlign.Center, color = contentColor.copy(alpha = 0.4f))
            } else if (isScrolling) {
                ScrollingText(
                    text = text, fontSize = fontSize.sp,
                    speed = scrollSpeed, effectType = currentEffect,
                    textColor = textColor, fontFamily = fontFamily, fontWeight = fontWeight
                )
            } else {
                TextEffects(
                    text = text, fontSize = fontSize.sp,
                    effectType = currentEffect,
                    textColor = textColor, fontFamily = fontFamily, fontWeight = fontWeight
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
                // 字幕模式：简化操控栏
                if (!isBoardMode && subtitleSentences.isNotEmpty()) {
                    // 字幕模式：字号+速度+导航
                    FontSizeSlider(fontSize = fontSize, onFontSizeChange = displayViewModel::setFontSize, isFullscreen = isFullscreen, range = adaptive.minFontSize..adaptive.maxFontSize, textColor = contentColor, sliderBg = sliderBg, sliderBorder = sliderBorder)
                    SpeedSlider(speed = scrollSpeed, onSpeedChange = displayViewModel::setScrollSpeed, isFullscreen = isFullscreen, maxSpeed = adaptive.maxScrollSpeed, textColor = contentColor, sliderBg = sliderBg, sliderBorder = sliderBorder)
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp)
                            .shadow(14.dp, RoundedCornerShape(18.dp), spotColor = Color.Black.copy(alpha = 0.2f))
                            .background(controlBg, RoundedCornerShape(18.dp)).border(0.5.dp, sliderBorder, RoundedCornerShape(18.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        Arrangement.SpaceEvenly, Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { if (isFullscreen) doToggleFullscreen(); onNavigateBack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = contentColor) }
                        IconButton(onClick = { displayViewModel.prevSentence() }) { Text("⏮", fontSize = 22.sp) }
                        Text("${currentIndex + 1}/${subtitleSentences.size}", style = MaterialTheme.typography.titleSmall, color = contentColor)
                        IconButton(onClick = { displayViewModel.togglePlayPause() }) { Text(if (isPlaying) "⏸" else "▶", fontSize = 22.sp) }
                        IconButton(onClick = { displayViewModel.nextSentence() }) { Text("⏭", fontSize = 22.sp) }
                    }
                } else {
                // 展板模式：完整控制
                FontSizeSlider(
                    fontSize = fontSize,
                    onFontSizeChange = displayViewModel::setFontSize,
                    isFullscreen = isFullscreen,
                    range = adaptive.minFontSize..adaptive.maxFontSize,
                    textColor = contentColor,
                    sliderBg = sliderBg,
                    sliderBorder = sliderBorder
                )
                if (isScrolling) {
                    SpeedSlider(
                        speed = scrollSpeed,
                        onSpeedChange = displayViewModel::setScrollSpeed,
                        isFullscreen = isFullscreen,
                        maxSpeed = adaptive.maxScrollSpeed,
                        textColor = contentColor,
                        sliderBg = sliderBg,
                        sliderBorder = sliderBorder
                    )
                }
                // 拟物化控制栏 — 凸起面板 + 哑光金属质感
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .shadow(14.dp, RoundedCornerShape(18.dp), spotColor = Color.Black.copy(alpha = 0.2f))
                        .background(
                            Brush.verticalGradient(listOf(Color(0xFF3A3A3A), Color(0xFF2A2A2A), Color(0xFF222222))),
                            RoundedCornerShape(18.dp)
                        )
                        .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (isFullscreen) doToggleFullscreen()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回",
                            tint = contentColor)
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        if (isScrolling) "滚动中" else "静止",
                        style = MaterialTheme.typography.labelLarge,
                        color = contentColor.copy(alpha = 0.85f)
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
                            tint = if (currentEffect != TextEffect.NONE) Color(0xFFFFD93D) else contentColor)
                    }
                    // 字体选择(弹出)
                    Box {
                        var fontMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { fontMenu = true }) {
                            Icon(Icons.Filled.FormatSize, "字体",
                                tint = if (fontIndex != 0) Color(0xFF4FC3F7) else contentColor)
                        }
                        DropdownMenu(expanded = fontMenu, onDismissRequest = { fontMenu = false },
                            offset = androidx.compose.ui.unit.DpOffset(0.dp, (-280).dp)) {
                            fontOptions.forEachIndexed { i, pair ->
                                val fam = pair.first
                                DropdownMenuItem(
                                    text = { Text("${fontNames[i]}  Aa", fontFamily = fam, color = if (i == fontIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) },
                                    onClick = { displayViewModel.setFont(i); fontMenu = false },
                                    leadingIcon = if (i == fontIndex) { { Icon(Icons.Filled.FormatSize, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) } } else null
                                )
                            }
                        }
                    }
                    // 文字颜色(弹出)
                    Box {
                        var colorMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { colorMenu = true }) {
                            Icon(Icons.Filled.ColorLens, "颜色",
                                tint = if (colorIndex != 0) textColor else contentColor)
                        }
                        DropdownMenu(expanded = colorMenu, onDismissRequest = { colorMenu = false },
                            offset = androidx.compose.ui.unit.DpOffset(0.dp, (-400).dp)) {
                            // 自动选项
                            DropdownMenuItem(
                                text = { Text("自动（跟背景）", color = if (colorIndex == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) },
                                onClick = { displayViewModel.setColor(0); colorMenu = false },
                                leadingIcon = {
                                    Box(Modifier.size(20.dp).background(contentColor, CircleShape).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.3f), CircleShape))
                                }
                            )
                            // 预设颜色
                            presetTextColors.forEachIndexed { i, c ->
                                DropdownMenuItem(
                                    text = { Text(colorNames[i], color = if (colorIndex == i + 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) },
                                    onClick = { displayViewModel.setColor(i + 1); colorMenu = false },
                                    leadingIcon = { Box(Modifier.size(20.dp).background(c, CircleShape).border(1.dp, Color.White.copy(0.3f), CircleShape)) }
                                )
                            }
                        }
                    }
                    // 浅色/深色背景切换
                    IconButton(onClick = {
                        val newLight = !isLightBg
                        displayViewModel.toggleLightBg()
                        scope.launch { repo.setLightBackground(newLight) }
                    }) {
                        Icon(Icons.Filled.LightMode, "切换背景",
                            tint = if (isLightBg) Color(0xFFFFD93D) else Color.White)
                    }
                    // 全屏
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
                } // else (board mode controls)
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
    range: ClosedFloatingPointRange<Float>,
    textColor: Color,
    sliderBg: Brush,
    sliderBorder: Color
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .shadow(14.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.2f))
            .background(sliderBg, RoundedCornerShape(12.dp))
            .border(0.5.dp, sliderBorder, RoundedCornerShape(12.dp))
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
                thumbColor = Color(0xEEFFFFFF),
                activeTrackColor = Color(0x88A0A8B0),
                inactiveTrackColor = Color(0x44D0D4D8),
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
    maxSpeed: Float,
    textColor: Color,
    sliderBg: Brush,
    sliderBorder: Color
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .shadow(14.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.2f))
            .background(sliderBg, RoundedCornerShape(12.dp))
            .border(0.5.dp, sliderBorder, RoundedCornerShape(12.dp))
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
                thumbColor = Color(0xEEFFFFFF),
                activeTrackColor = Color(0x88A0A8B0),
                inactiveTrackColor = Color(0x44D0D4D8),
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

