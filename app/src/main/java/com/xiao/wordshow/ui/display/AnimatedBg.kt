package com.xiao.wordshow.ui.display

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

/**
 * 象牙白珠光背景 — 暖色光池缓慢漂移
 */
@Composable
fun AnimatedBackground(modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "abg")
    val a1 by t.animateFloat(0f, 360f, infiniteRepeatable(tween(22000, easing = LinearEasing), RepeatMode.Restart), "a1")
    val a2 by t.animateFloat(120f, 480f, infiniteRepeatable(tween(28000, easing = LinearEasing), RepeatMode.Restart), "a2")
    val a3 by t.animateFloat(240f, 600f, infiniteRepeatable(tween(32000, easing = LinearEasing), RepeatMode.Restart), "a3")

    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height; val cx = w / 2f; val cy = h / 2f; val r = maxOf(w, h) * 0.7f

        // 象牙白基底
        drawRect(Color(0xFFFFF8F0))

        // 光池 1 — 暖金
        drawCircle(Brush.radialGradient(
            colors = listOf(Color(0x18F0DCC0), Color(0x0AE0C8A0), Color(0x00000000)),
            center = Offset(cx + cos(a1 * PI / 180.0).toFloat() * r * 0.3f, cy + sin(a1 * PI / 180.0).toFloat() * r * 0.25f),
            radius = r * 0.8f
        ), radius = r * 0.8f)

        // 光池 2 — 珍珠粉
        drawCircle(Brush.radialGradient(
            colors = listOf(Color(0x12F5E6D8), Color(0x08E8D4C0), Color(0x00000000)),
            center = Offset(cx + cos(a2 * PI / 180.0).toFloat() * r * 0.45f, cy + sin(a2 * PI / 180.0).toFloat() * r * 0.35f),
            radius = r * 0.6f
        ), radius = r * 0.6f)

        // 光池 3 — 奶油白
        drawCircle(Brush.radialGradient(
            colors = listOf(Color(0x10FFFFFF), Color(0x06F0E8D8), Color(0x00000000)),
            center = Offset(cx + cos(a3 * PI / 180.0).toFloat() * r * 0.35f, cy + sin(a3 * PI / 180.0).toFloat() * r * 0.4f),
            radius = r * 0.5f
        ), radius = r * 0.5f)
    }
}
