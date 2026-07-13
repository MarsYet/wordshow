package com.xiao.wordshow.ui.display.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.xiao.wordshow.data.model.TextEffect

/**
 * 滚动文字显示组件（P0）— 跑马灯效果 + 特效
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

        val duration = remember(textWidthPx, containerWidthPx, speed) {
            if (textWidthPx > 0f && containerWidthPx > 0f) {
                val totalDistance = textWidthPx + containerWidthPx
                ((totalDistance / (100f * speed)) * 1000).toInt().coerceAtLeast(3000)
            } else 5000
        }

        val infiniteTransition = rememberInfiniteTransition(label = "marquee")
        val offsetX by infiniteTransition.animateFloat(
            initialValue = containerWidthPx,
            targetValue = -textWidthPx,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = duration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "marqueeOffset"
        )

        // 使用 graphicsLayer 而非 offset，避免被父容器裁剪
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .wrapContentWidth(unbounded = true)
                .graphicsLayer { translationX = offsetX },
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
