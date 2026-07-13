package com.xiao.wordshow.ui.display

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 深蓝紫径向渐变背景 + 3个缓慢漂移的彩色光晕
 */
@Composable
fun AnimatedBackground(modifier: Modifier = Modifier) {
    val config = LocalConfiguration.current
    val screenW = config.screenWidthDp.dp.value
    val screenH = config.screenHeightDp.dp.value
    val centerX = screenW / 2
    val centerY = screenH / 2

    val transition = rememberInfiniteTransition(label = "bg")

    // 光晕角度
    val orbit1 by transition.animateFloat(0f, 360f,
        infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart), "o1")
    val orbit2 by transition.animateFloat(120f, 480f,
        infiniteRepeatable(tween(25000, easing = LinearEasing), RepeatMode.Restart), "o2")
    val orbit3 by transition.animateFloat(240f, 600f,
        infiniteRepeatable(tween(30000, easing = LinearEasing), RepeatMode.Restart), "o3")

    val radius = maxOf(screenW, screenH) * 0.6f

    val orb1X = centerX + cos(orbit1 * PI / 180f) * radius * 0.6f
    val orb1Y = centerY + sin(orbit1 * PI / 180f) * radius * 0.5f
    val orb2X = centerX + cos(orbit2 * PI / 180f) * radius * 0.45f
    val orb2Y = centerY + sin(orbit2 * PI / 180f) * radius * 0.55f
    val orb3X = centerX + cos(orbit3 * PI / 180f) * radius * 0.5f
    val orb3Y = centerY + sin(orbit3 * PI / 180f) * radius * 0.4f

    Box(modifier = modifier.fillMaxSize()) {
        // 径向渐变底层
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2D1B69),
                        Color(0xFF1A1A2E),
                        Color(0xFF0F0F23),
                        Color(0xFF0A0A1A)
                    ),
                    center = Offset(size.width / 2, size.height / 2),
                    radius = size.maxDimension * 0.8f
                )
            )
        }

        // 光晕 1 - 紫色
        Box(
            Modifier
                .offset(x = orb1X.dp, y = orb1Y.dp)
                .size((screenW * 0.5f).dp)
                .graphicsLayer { alpha = 0.35f }
                .blur(60.dp)
                .clip(CircleShape)
                .background(Color(0xFF7B2FFF))
        )

        // 光晕 2 - 蓝色
        Box(
            Modifier
                .offset(x = orb2X.dp, y = orb2Y.dp)
                .size((screenW * 0.4f).dp)
                .graphicsLayer { alpha = 0.3f }
                .blur(50.dp)
                .clip(CircleShape)
                .background(Color(0xFF3366FF))
        )

        // 光晕 3 - 青粉
        Box(
            Modifier
                .offset(x = orb3X.dp, y = orb3Y.dp)
                .size((screenW * 0.35f).dp)
                .graphicsLayer { alpha = 0.25f }
                .blur(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF4488))
        )
    }
}
