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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiao.wordshow.data.model.TextEffect
import kotlin.math.roundToInt

/**
 * 滚动文字显示组件（P0）— 跑马灯效果，支持特效
 */
@Composable
fun ScrollingText(
    text: String,
    speed: Float = 1f,
    fontSize: TextUnit = 64.sp,
    effectType: TextEffect = TextEffect.NONE,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
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

        Box(
            modifier = Modifier.fillMaxSize().padding(vertical = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // 滚动特效文字
            AppEffectText(
                text = text,
                fontSize = fontSize,
                effectType = effectType,
                modifier = Modifier.offset { IntOffset(offsetX.roundToInt(), 0) },
                onTextLayout = { textWidthPx = it.size.width.toFloat() }
            )
        }
    }
}

/**
 * 单行特效文字 — 用于滚动场景
 */
@Composable
private fun AppEffectText(
    text: String,
    fontSize: TextUnit,
    effectType: TextEffect,
    modifier: Modifier,
    onTextLayout: (androidx.compose.ui.text.TextLayoutResult) -> Unit
) {
    // For scrolling, render with effect styling applied
    val baseModifier = modifier

    when (effectType) {
        TextEffect.NONE, TextEffect.SHADOW -> {
            val textColor = if (effectType == TextEffect.SHADOW) {
                androidx.compose.ui.graphics.Color(0xFFFFEB3B)
            } else androidx.compose.ui.graphics.Color.Unspecified

            Text(
                text = text, fontSize = fontSize, fontWeight = FontWeight.Bold,
                maxLines = 1, softWrap = false, color = textColor,
                onTextLayout = onTextLayout, modifier = baseModifier
            )
        }
        TextEffect.GRADIENT -> {
            Text(
                text = text, fontSize = fontSize, fontWeight = FontWeight.Bold,
                maxLines = 1, softWrap = false, color = androidx.compose.ui.graphics.Color(0xFF4D96FF),
                onTextLayout = onTextLayout, modifier = baseModifier
            )
        }
        TextEffect.GLOW -> {
            Text(
                text = text, fontSize = fontSize, fontWeight = FontWeight.Bold,
                maxLines = 1, softWrap = false, color = androidx.compose.ui.graphics.Color.Cyan,
                onTextLayout = onTextLayout, modifier = baseModifier
            )
        }
        TextEffect.BOUNCE -> {
            Text(
                text = text, fontSize = fontSize, fontWeight = FontWeight.Bold,
                maxLines = 1, softWrap = false,
                color = androidx.compose.ui.graphics.Color.hsl(0f, 1f, 0.5f),
                onTextLayout = onTextLayout, modifier = baseModifier
            )
        }
    }
}
