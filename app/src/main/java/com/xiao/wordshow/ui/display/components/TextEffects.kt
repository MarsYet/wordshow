package com.xiao.wordshow.ui.display.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiao.wordshow.data.model.TextEffect

/**
 * 文字特效渲染（P2）
 * 统一入口：静态和滚动模式均通过此函数渲染特效文字
 */
@Composable
fun EffectText(
    text: String,
    effectType: TextEffect,
    fontSize: TextUnit = 64.sp,
    maxLines: Int = 1,
    softWrap: Boolean = false,
    textAlign: TextAlign = TextAlign.Center,
    modifier: Modifier = Modifier
) {
    val baseStyle = TextStyle(
        fontSize = fontSize,
        fontWeight = FontWeight.Bold
    )

    when (effectType) {
        TextEffect.NONE -> {
            Text(
                text = text,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                maxLines = maxLines,
                softWrap = softWrap,
                textAlign = textAlign,
                modifier = modifier
            )
        }
        TextEffect.GRADIENT -> {
            Text(
                text = text,
                maxLines = maxLines,
                softWrap = softWrap,
                textAlign = textAlign,
                modifier = modifier,
                style = baseStyle.copy(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(0xFFFF6B6B),
                            Color(0xFFFFD93D),
                            Color(0xFF6BCB77),
                            Color(0xFF4D96FF)
                        )
                    )
                )
            )
        }
        TextEffect.SHADOW -> {
            Text(
                text = text,
                maxLines = maxLines,
                softWrap = softWrap,
                textAlign = textAlign,
                modifier = modifier,
                style = baseStyle.copy(
                    color = Color(0xFFFFEB3B),
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.7f),
                        offset = Offset(5f, 5f),
                        blurRadius = 10f
                    )
                )
            )
        }
        TextEffect.GLOW -> {
            // 双层：背景发光层 + 前景白色文字
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                Text(
                    text = text,
                    maxLines = maxLines,
                    softWrap = softWrap,
                    textAlign = textAlign,
                    style = baseStyle.copy(
                        color = Color.Cyan.copy(alpha = 0.35f),
                        shadow = Shadow(
                            color = Color.Cyan,
                            offset = Offset.Zero,
                            blurRadius = 50f
                        )
                    )
                )
                Text(
                    text = text,
                    maxLines = maxLines,
                    softWrap = softWrap,
                    textAlign = textAlign,
                    style = baseStyle.copy(color = Color.White)
                )
            }
        }
        TextEffect.BOUNCE -> {
            val infinite = rememberInfiniteTransition(label = "bounce")
            val scale by infinite.animateFloat(
                1f, 1.07f,
                infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse),
                label = "bs"
            )
            val hue by infinite.animateFloat(
                0f, 360f,
                infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart),
                label = "bh"
            )
            Text(
                text = text,
                maxLines = maxLines,
                softWrap = softWrap,
                textAlign = textAlign,
                modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale },
                style = baseStyle.copy(color = Color.hsl(hue, 1f, 0.5f))
            )
        }
    }
}

/**
 * 静态显示专用 — 居中 + 多行 + padding
 */
@Composable
fun TextEffects(
    text: String,
    effectType: TextEffect = TextEffect.NONE,
    fontSize: TextUnit = 64.sp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        EffectText(
            text = text,
            effectType = effectType,
            fontSize = fontSize,
            maxLines = 10,
            softWrap = true,
            textAlign = TextAlign.Center
        )
    }
}
