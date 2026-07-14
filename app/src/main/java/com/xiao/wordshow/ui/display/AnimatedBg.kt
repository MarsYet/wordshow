package com.xiao.wordshow.ui.display

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.*

@Composable
fun AnimatedBackground(modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "bg")
    val a1 by t.animateFloat(0f, 360f, infiniteRepeatable(tween(25000, easing = LinearEasing), RepeatMode.Restart), "a1")
    val a2 by t.animateFloat(120f, 480f, infiniteRepeatable(tween(30000, easing = LinearEasing), RepeatMode.Restart), "a2")
    val a3 by t.animateFloat(240f, 600f, infiniteRepeatable(tween(35000, easing = LinearEasing), RepeatMode.Restart), "a3")

    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height; val cx = w / 2f; val cy = h / 2f; val r = maxOf(w, h) * 0.7f
        drawRect(Color(0xFFE8EBED))
        drawCircle(Brush.radialGradient(
            colors = listOf(Color(0x14FFFFFF), Color(0x08CCCCCC), Color(0x00000000)),
            center = Offset(cx + cos(a1 * PI / 180.0).toFloat() * r * 0.3f, cy + sin(a1 * PI / 180.0).toFloat() * r * 0.25f),
            radius = r * 0.8f), radius = r * 0.8f)
        drawCircle(Brush.radialGradient(
            colors = listOf(Color(0x0CFFFFFF), Color(0x06BBBBBB), Color(0x00000000)),
            center = Offset(cx + cos(a2 * PI / 180.0).toFloat() * r * 0.45f, cy + sin(a2 * PI / 180.0).toFloat() * r * 0.35f),
            radius = r * 0.6f), radius = r * 0.6f)
        drawCircle(Brush.radialGradient(
            colors = listOf(Color(0x10DDDDDD), Color(0x06AAAAAA), Color(0x00000000)),
            center = Offset(cx + cos(a3 * PI / 180.0).toFloat() * r * 0.35f, cy + sin(a3 * PI / 180.0).toFloat() * r * 0.4f),
            radius = r * 0.5f), radius = r * 0.5f)
    }
}
