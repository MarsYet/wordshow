package com.xiao.wordshow.ui.display.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
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
import kotlin.math.sin

/**
 * 文字特效渲染（P2）
 * 根据 effectType 应用不同视觉效果，包裹 StaticText 渲染
 */
@Composable
fun TextEffects(
    text: String,
    effectType: TextEffect = TextEffect.NONE,
    fontSize: TextUnit = 64.sp,
    modifier: Modifier = Modifier
) {
    when (effectType) {
        TextEffect.NONE    -> PlainText(text, fontSize, modifier)
        TextEffect.GRADIENT -> GradientText(text, fontSize, modifier)
        TextEffect.SHADOW   -> ShadowText(text, fontSize, modifier)
        TextEffect.GLOW    -> GlowText(text, fontSize, modifier)
        TextEffect.BOUNCE  -> BounceText(text, fontSize, modifier)
    }
}

// ---------- 无特效 ----------

@Composable
private fun PlainText(text: String, fontSize: TextUnit, modifier: Modifier) {
    Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(
            text = text, fontSize = fontSize, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center, maxLines = 10,
            overflow = TextOverflow.Ellipsis, lineHeight = fontSize * 1.3f
        )
    }
}

// ---------- 渐变 ----------

@Composable
private fun GradientText(text: String, fontSize: TextUnit, modifier: Modifier) {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFF6B6B),
            Color(0xFFFFD93D),
            Color(0xFF6BCB77),
            Color(0xFF4D96FF)
        )
    )

    Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 10,
            overflow = TextOverflow.Ellipsis,
            lineHeight = fontSize * 1.3f,
            style = TextStyle(brush = gradientBrush)
        )
    }
}

// ---------- 阴影 ----------

@Composable
private fun ShadowText(text: String, fontSize: TextUnit, modifier: Modifier) {
    Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 10,
            overflow = TextOverflow.Ellipsis,
            lineHeight = fontSize * 1.3f,
            color = Color(0xFFFFEB3B), // 亮黄字
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.6f),
                    offset = Offset(6f, 6f),
                    blurRadius = 12f
                )
            )
        )
    }
}

// ---------- 发光 ----------

@Composable
private fun GlowText(text: String, fontSize: TextUnit, modifier: Modifier) {
    Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        // 发光层（模糊背景）
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 10,
            overflow = TextOverflow.Ellipsis,
            lineHeight = fontSize * 1.3f,
            color = Color.Cyan.copy(alpha = 0.3f),
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Cyan,
                    offset = Offset.Zero,
                    blurRadius = 40f
                )
            )
        )
        // 前景文字
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 10,
            overflow = TextOverflow.Ellipsis,
            lineHeight = fontSize * 1.3f,
            color = Color.White
        )
    }
}

// ---------- 跳动 ----------

@Composable
private fun BounceText(text: String, fontSize: TextUnit, modifier: Modifier) {
    val infinite = rememberInfiniteTransition(label = "bounce")

    val scale by infinite.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "bScale"
    )

    val hueShift by infinite.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "bHue"
    )

    val bounceColor = Color.hsl(hueShift, 1f, 0.5f)

    Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 10,
            overflow = TextOverflow.Ellipsis,
            lineHeight = fontSize * 1.3f,
            color = bounceColor,
            modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
        )
    }
}
