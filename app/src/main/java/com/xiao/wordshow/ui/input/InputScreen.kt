package com.xiao.wordshow.ui.input

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.xiao.wordshow.data.voice.VoiceRecognizer

@Composable
fun InputScreen(
    onNavigateToDisplay: () -> Unit,
    modifier: Modifier = Modifier,
    inputViewModel: InputViewModel
) {
    val text by inputViewModel.text.collectAsState()
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }

    // 语音识别结果处理
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        val spokenText = matches?.firstOrNull()
        if (!spokenText.isNullOrBlank()) {
            val current = inputViewModel.text.value
            val separator = if (current.isNotBlank() && !current.endsWith(" ")) " " else ""
            inputViewModel.updateText(current + separator + spokenText)
        }
    }

    fun tryLaunchVoice() {
        try {
            isListening = true
            voiceLauncher.launch(VoiceRecognizer.createRecognizerIntent())
        } catch (e: ActivityNotFoundException) {
            isListening = false
            Toast.makeText(
                context,
                "未找到语音识别服务\n请安装讯飞输入法或 Google 语音搜索",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // 录音权限请求
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            tryLaunchVoice()
        } else {
            Toast.makeText(context, "语音输入需要录音权限，请在系统设置中开启", Toast.LENGTH_LONG).show()
        }
    }

    fun startVoiceInput() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            tryLaunchVoice()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "输入文字",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 输入区域 + 语音按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = inputViewModel::updateText,
                    modifier = Modifier.weight(1f),
                    label = { Text("请输入要显示的文字") },
                    maxLines = 5,
                    textStyle = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.width(8.dp))

                HoldToTalkButton(
                    isListening = isListening,
                    onPress = { startVoiceInput() },
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onNavigateToDisplay,
                modifier = Modifier.size(width = 200.dp, height = 56.dp),
                enabled = text.isNotBlank()
            ) {
                Text(
                    text = "进入显示",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // 底部声纹面板
        AnimatedVisibility(
            visible = isListening,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            VoiceWavePanel()
        }
    }
}

/**
 * 按住说话按钮 — 手指按下触发录音
 */
@Composable
private fun HoldToTalkButton(
    isListening: Boolean,
    onPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed && !isListening) {
            onPress()
        }
    }

    val pulseAlpha by if (isListening) {
        rememberInfiniteTransition(label = "pulse").animateFloat(
            initialValue = 1f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(600),
                repeatMode = RepeatMode.Reverse
            ),
            label = "micAlpha"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    val containerColor = if (isListening)
        MaterialTheme.colorScheme.errorContainer
    else if (isPressed)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
    else
        MaterialTheme.colorScheme.primaryContainer

    val iconColor = if (isListening)
        MaterialTheme.colorScheme.onErrorContainer
    else
        MaterialTheme.colorScheme.onPrimaryContainer

    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.KeyboardVoice,
            contentDescription = "按住说话",
            modifier = Modifier
                .size(28.dp)
                .alpha(pulseAlpha),
            tint = iconColor
        )
    }
}

/**
 * 底部声纹检测面板 — 模拟音频波形，让用户知道麦克风正在工作
 */
@Composable
private fun VoiceWavePanel() {
    val barCount = 5

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 声纹动画条
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            for (i in 0 until barCount) {
                VoiceBar(
                    index = i,
                    totalBars = barCount,
                    isActive = true
                )
                if (i < barCount - 1) {
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "正在聆听...",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "说出要显示的文字，系统会自动识别",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
        )
    }
}

/**
 * 单根声纹柱 — 随机振幅上下跳动，模拟麦克风拾音
 */
@Composable
private fun VoiceBar(
    index: Int,
    totalBars: Int,
    isActive: Boolean
) {
    // 每根柱子独立动画周期，错开相位看起来更自然
    val duration = 400 + index * 120
    val delay = index * 80

    val infiniteTransition = rememberInfiniteTransition(label = "voiceBar$index")
    val heightFraction by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = duration,
                delayMillis = delay,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "barHeight$index"
    )

    val minHeight = 16
    val maxHeight = 56

    Box(
        modifier = Modifier
            .width(6.dp)
            .height((minHeight + (maxHeight - minHeight) * heightFraction).dp)
            .clip(RoundedCornerShape(3.dp))
            .background(
                if (isActive)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
    )
}
