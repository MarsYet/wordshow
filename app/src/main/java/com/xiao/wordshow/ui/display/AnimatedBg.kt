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
 * 纯 Canvas 绘制的深色星空背景 — 3个柔和光池缓慢漂移，无硬边无条纹
 */
@Composable
fun AnimatedBackground(modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "abg")

    val a1 by t.animateFloat(0f, 360f,
        infiniteRepeatable(tween(18000, easing = LinearEasing), RepeatMode.Restart), "a1")
    val a2 by t.animateFloat(120f, 480f,
        infiniteRepeatable(tween(22000, easing = LinearEasing), RepeatMode.Restart), "a2")
    val a3 by t.animateFloat(240f, 600f,
        infiniteRepeatable(tween(26000, easing = LinearEasing), RepeatMode.Restart), "a3")

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = maxOf(w, h) * 0.7f

        // 基底渐变
        drawRect(
            Brush.radialGradient(
                colors = listOf(
                    Color(0xFF1C1C2E),
                    Color(0xFF141428),
                    Color(0xFF0C0C1E),
                    Color(0xFF060614)
                ),
                center = Offset(cx, cy),
                radius = r * 1.4f
            )
        )

        // 光池 1 — 靛蓝，极大半径 = 没有圆形边界感
        drawCircle(
            Brush.radialGradient(
                colors = listOf(
                    Color(0x1E6688FF),
                    Color(0x0E3366CC),
                    Color(0x00000000)
                ),
                center = Offset(
                    cx + cos(a1 * PI / 180.0).toFloat() * r * 0.3f,
                    cy + sin(a1 * PI / 180.0).toFloat() * r * 0.25f
                ),
                radius = r * 0.8f
            ),
            radius = r * 0.8f
        )

        // 光池 2 — 紫罗兰
        drawCircle(
            Brush.radialGradient(
                colors = listOf(
                    Color(0x168855EE),
                    Color(0x084422AA),
                    Color(0x00000000)
                ),
                center = Offset(
                    cx + cos(a2 * PI / 180.0).toFloat() * r * 0.45f,
                    cy + sin(a2 * PI / 180.0).toFloat() * r * 0.35f
                ),
                radius = r * 0.65f
            ),
            radius = r * 0.65f
        )

        // 光池 3 — 青蓝
        drawCircle(
            Brush.radialGradient(
                colors = listOf(
                    Color(0x12228899),
                    Color(0x06115577),
                    Color(0x00000000)
                ),
                center = Offset(
                    cx + cos(a3 * PI / 180.0).toFloat() * r * 0.35f,
                    cy + sin(a3 * PI / 180.0).toFloat() * r * 0.4f
                ),
                radius = r * 0.55f
            ),
            radius = r * 0.55f
        )

        // 暗角
        drawRect(
            Brush.radialGradient(
                colors = listOf(Color(0x00000000), Color(0x00000000), Color(0x2A000000)),
                center = Offset(cx, cy),
                radius = r * 1.2f
            )
        )
    }
}
