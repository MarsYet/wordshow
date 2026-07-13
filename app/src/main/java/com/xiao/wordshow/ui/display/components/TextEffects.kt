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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    modifier: Modifier = Modifier,
    onTextLayout: ((androidx.compose.ui.text.TextLayoutResult) -> Unit)? = null
) {
    // 行距跟随字号等比缩放，防止多行堆叠
    val lineHeightScale = 1.5f
    val baseStyle = TextStyle(
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        lineHeight = (fontSize.value * lineHeightScale).sp
    )

    when (effectType) {
        TextEffect.NONE -> {
            Text(
                text = text,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                lineHeight = (fontSize.value * 1.5f).sp,
                maxLines = maxLines,
                softWrap = softWrap,
                textAlign = textAlign,
                modifier = modifier,
                onTextLayout = onTextLayout
            )
        }
        TextEffect.GRADIENT -> {
            Text(
                text = text,
                maxLines = maxLines,
                softWrap = softWrap,
                textAlign = textAlign,
                modifier = modifier,
                onTextLayout = onTextLayout,
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
                onTextLayout = onTextLayout,
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
            // 只在前景文字上回调 onTextLayout（避免重复）
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
                    onTextLayout = onTextLayout,
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
                onTextLayout = onTextLayout,
                style = baseStyle.copy(color = Color.hsl(hue, 1f, 0.5f))
            )
        }
    }
}

/**
 * 静态显示专用 — 居中 + 实测溢出自动缩小
 */
@Composable
fun TextEffects(
    text: String,
    effectType: TextEffect = TextEffect.NONE,
    fontSize: TextUnit = 64.sp,
    modifier: Modifier = Modifier
) {
    val baseSp = fontSize.value
    // 溢出检测：从目标字号开始，每次溢出缩小 12%，直到不溢出或达下限
    var effectiveSp by remember(text, baseSp) { mutableFloatStateOf(baseSp) }
    // 每段文本只做一次缩放决策，避免无限循环
    var settled by remember(text, baseSp) { mutableStateOf(false) }

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
            fontSize = effectiveSp.sp,
            maxLines = 30,
            softWrap = true,
            textAlign = TextAlign.Center,
            onTextLayout = { result ->
                if (!settled && result.hasVisualOverflow && effectiveSp > 10f) {
                    effectiveSp = (effectiveSp * 0.82f).coerceAtLeast(10f)
                } else {
                    settled = true
                }
            }
        )
    }
}
