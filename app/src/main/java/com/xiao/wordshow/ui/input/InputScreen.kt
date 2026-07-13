package com.xiao.wordshow.ui.input

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.xiao.wordshow.data.voice.VoiceRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.log10

// 音频采样参数
private const val SAMPLE_RATE = 16000
private const val BUFFER_SIZE = 1024

@Composable
fun InputScreen(
    onNavigateToDisplay: () -> Unit,
    modifier: Modifier = Modifier,
    inputViewModel: InputViewModel
) {
    val text by inputViewModel.text.collectAsState()
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var currentAmplitude by remember { mutableFloatStateOf(0f) }
    var audioRecord by remember { mutableStateOf<AudioRecord?>(null) }

    // 用于 AudioRecord 采样协程，Dispose 时取消
    val samplingScope = rememberCoroutineScope()

    // 清理 AudioRecord
    DisposableEffect(Unit) {
        onDispose { audioRecord?.release() }
    }

    // 语音识别结果处理
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        currentAmplitude = 0f
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
            // 启动真实麦克风采样 → 声纹实时反映环境音量
            startAmplitudeSampling(samplingScope) { amp ->
                currentAmplitude = amp
            }?.let { record ->
                audioRecord?.release()
                audioRecord = record
            }
            // 短暂延迟让用户看到声纹反应，然后启动系统识别
            voiceLauncher.launch(VoiceRecognizer.createRecognizerIntent())
        } catch (e: ActivityNotFoundException) {
            isListening = false
            currentAmplitude = 0f
            audioRecord?.release()
            audioRecord = null
            Toast.makeText(
                context,
                "未找到语音识别服务\n请安装讯飞输入法或 Google 语音搜索",
                Toast.LENGTH_LONG
            ).show()
        }
    }

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

        // 底部声纹 — 真实麦克风振幅，无面板
        if (isListening) {
            VoiceWaveBars(
                amplitude = currentAmplitude,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )
        }
    }
}

/**
 * 启动 AudioRecord 并在后台协程中持续读取振幅
 * @return AudioRecord 实例，调用方负责 release
 */
private fun startAmplitudeSampling(
    scope: kotlinx.coroutines.CoroutineScope,
    onAmplitude: (Float) -> Unit
): AudioRecord? {
    return try {
        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(BUFFER_SIZE)

        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        recorder.startRecording()

        // 在协程中循环读取振幅（scope 由 InputScreen 的 rememberCoroutineScope 管理生命周期）
        scope.launch(Dispatchers.IO) {
            val buffer = ShortArray(bufferSize)
            try {
                while (isActive && recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    val read = recorder.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        // 计算 RMS 振幅并归一化到 0~1
                        var sum = 0.0
                        for (i in 0 until read) {
                            sum += (buffer[i] * buffer[i]).toDouble()
                        }
                        val rms = kotlin.math.sqrt(sum / read)
                        // 归一化：32768 是 16-bit PCM 最大值，取 log 压缩动态范围
                        val normalized = (log10(rms.coerceAtLeast(1.0)) / log10(32768.0))
                            .coerceIn(0.0, 1.0).toFloat()
                        onAmplitude(normalized)
                    }
                }
            } catch (_: Exception) {
                // AudioRecord 在 RecognizerIntent 启动后会因 mic 被抢占而抛异常，忽略
            } finally {
                try { recorder.stop() } catch (_: Exception) {}
                try { recorder.release() } catch (_: Exception) {}
            }
        }

        recorder
    } catch (e: Exception) {
        null
    }
}

/**
 * 按住说话按钮
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
 * 声纹柱状条 — 根据真实麦克风振幅实时变化，无背景面板
 */
@Composable
private fun VoiceWaveBars(
    amplitude: Float,
    modifier: Modifier = Modifier,
    barCount: Int = 7
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {
        for (i in 0 until barCount) {
            VoiceBar(
                index = i,
                totalBars = barCount,
                amplitude = amplitude
            )
            if (i < barCount - 1) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

/**
 * 单根声纹柱 — 高度由真实振幅驱动 + 微弱随机抖动
 */
@Composable
private fun VoiceBar(
    index: Int,
    totalBars: Int,
    amplitude: Float
) {
    // 每根柱子的微弱独立抖动（避免完全静止时看起来像卡住了）
    val jitter = rememberInfiniteTransition(label = "jitter$index")
    val jitterOffset by jitter.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(300 + index * 70, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "jitter$index"
    )

    val baseHeight = 0.06f + jitterOffset * 0.04f
    val voiceHeight = amplitude.coerceIn(0f, 1f) * 0.85f
    val totalHeight = (baseHeight + voiceHeight).coerceIn(0f, 1f)

    val minH = 8
    val maxH = 56

    val barColor = if (amplitude > 0.15f)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

    Box(
        modifier = Modifier
            .width(5.dp)
            .height((minH + (maxH - minH) * totalHeight).dp)
            .clip(RoundedCornerShape(3.dp))
            .background(barColor)
    )
}
