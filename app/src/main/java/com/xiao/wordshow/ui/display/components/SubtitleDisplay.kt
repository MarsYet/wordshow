package com.xiao.wordshow.ui.display.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SubtitleDisplay(
    sentence: String,
    current: Int, total: Int,
    isPlaying: Boolean,
    hasNext: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    textColor: Color,
    fontSize: androidx.compose.ui.unit.TextUnit = 28.sp
) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(1f))

        // 字幕文本区
        Box(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                .shadow(14.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.2f))
                .background(Color(0xDD000000), RoundedCornerShape(16.dp))
                .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                .padding(24.dp)
                .clickable { onNext() }
        ) {
            Text(sentence, color = Color.White, fontSize = fontSize, fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), lineHeight = fontSize * 1.4f)
        }

        Spacer(Modifier.height(16.dp))

        // 控制栏
        Row(Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrev) {
                Icon(Icons.Filled.SkipPrevious, "上一句", tint = textColor, modifier = Modifier.size(36.dp))
            }
            IconButton(onClick = onPlayPause) {
                Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    if (isPlaying) "暂停" else "继续",
                    tint = textColor, modifier = Modifier.size(40.dp))
            }
            IconButton(onClick = onNext, enabled = hasNext) {
                Icon(Icons.Filled.SkipNext, "下一句", tint = if (hasNext) textColor else textColor.copy(alpha = 0.3f),
                    modifier = Modifier.size(36.dp))
            }
        }

        // 进度
        Text("$current / $total", color = textColor.copy(alpha = 0.5f), style = androidx.compose.material3.MaterialTheme.typography.labelSmall)

        Spacer(Modifier.height(32.dp))
    }
}
