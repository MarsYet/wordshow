package com.xiao.wordshow.ui.display.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * 滚动文字显示组件（P0）— 跑马灯效果
 */
@Composable
fun ScrollingText(
    text: String,
    speed: Float = 1f,
    fontSize: TextUnit = 64.sp,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val containerWidthPx = constraints.maxWidth.toFloat()
        var textWidthPx by remember { mutableFloatStateOf(0f) }

        // 滚动时长基于文字宽度 + 容器宽度，speed 越高越快
        val duration = remember(textWidthPx, containerWidthPx, speed) {
            if (textWidthPx > 0f && containerWidthPx > 0f) {
                val totalDistance = textWidthPx + containerWidthPx
                // 基准：100px/s，速度倍率调整
                ((totalDistance / (100f * speed)) * 1000).toInt().coerceAtLeast(3000)
            } else {
                5000
            }
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = text,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                onTextLayout = { layoutResult ->
                    textWidthPx = layoutResult.size.width.toFloat()
                },
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), 0) }
            )
        }
    }
}
