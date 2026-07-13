package com.xiao.wordshow.ui.display.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.xiao.wordshow.data.model.TextEffect
import kotlinx.coroutines.launch

/**
 * 滚动文字显示组件 — 自动跑马灯 + 左右滑动手动拖拽
 */
@Composable
fun ScrollingText(
    text: String,
    speed: Float = 1f,
    fontSize: TextUnit = 64.sp,
    effectType: TextEffect = TextEffect.NONE,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val containerWidthPx = constraints.maxWidth.toFloat()
        var textWidthPx by remember { mutableFloatStateOf(0f) }
        val totalScrollDistance = textWidthPx + containerWidthPx

        // 动画偏移量
        val animOffset = remember { Animatable(containerWidthPx) }
        var isDragging by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        // 自动滚动动画
        LaunchedEffect(text, speed, isDragging, totalScrollDistance) {
            if (!isDragging && totalScrollDistance > 0f) {
                val baseDuration = (totalScrollDistance / (100f * speed) * 1000).toInt().coerceAtLeast(3000)
                // 从当前位置继续滚动到末尾
                val remaining = (1f - (animOffset.value - containerWidthPx) / (-textWidthPx - containerWidthPx))
                    .coerceIn(0f, 1f)
                val remainingMs = (baseDuration * remaining).toInt().coerceAtLeast(1000)

                animOffset.animateTo(
                    targetValue = -textWidthPx,
                    animationSpec = tween(remainingMs, easing = LinearEasing)
                )
                // 循环：从右侧重新开始
                animOffset.snapTo(containerWidthPx)
                while (!isDragging) {
                    animOffset.animateTo(
                        targetValue = -textWidthPx,
                        animationSpec = tween(baseDuration, easing = LinearEasing)
                    )
                    if (!isDragging) animOffset.snapTo(containerWidthPx)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .wrapContentWidth(unbounded = true)
                .graphicsLayer { translationX = animOffset.value }
                .pointerInput(textWidthPx) {
                    if (textWidthPx <= 0f) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                val newValue = (animOffset.value + dragAmount)
                                    .coerceIn(-textWidthPx, containerWidthPx)
                                animOffset.snapTo(newValue)
                            }
                        }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {
            EffectText(
                text = text,
                effectType = effectType,
                fontSize = fontSize,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .wrapContentWidth(unbounded = true)
                    .onSizeChanged { textWidthPx = it.width.toFloat() }
            )
        }
    }
}
