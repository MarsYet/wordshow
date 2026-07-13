package com.xiao.wordshow.ui.input

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
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
import kotlinx.coroutines.withContext
import kotlin.math.log10
import kotlin.math.sqrt

private const val SAMPLE_RATE = 16000

@Composable
fun InputScreen(
    onNavigateToDisplay: () -> Unit,
    modifier: Modifier = Modifier,
    inputViewModel: InputViewModel,
    adaptive: com.xiao.wordshow.util.AdaptiveParams
) {
    val text by inputViewModel.text.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isRecording by remember { mutableStateOf(false) }
    var currentAmplitude by remember { mutableFloatStateOf(0f) }
    var audioRecord by remember { mutableStateOf<AudioRecord?>(null) }
    var voiceRecognizer by remember { mutableStateOf<VoiceRecognizer?>(null) }
    // 语音开始前的原始文字，用于部分结果替换
    var baseText by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        onDispose {
            audioRecord?.let { try { it.release() } catch (_: Exception) {} }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(context, "语音输入需要录音权限，请在系统设置中开启", Toast.LENGTH_LONG).show()
        }
    }

    fun startRecording() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        baseText = inputViewModel.text.value
        isRecording = true

        // 创建讯飞 ASR
        val recognizer = VoiceRecognizer.create(
            onResult = { resultText, isFinal ->
                if (isFinal) {
                    // 最终结果：追加到原始文字后面
                    val sep = if (baseText.isNotBlank() && !baseText.endsWith(" ")) " " else ""
                    inputViewModel.updateText(baseText + sep + resultText)
                } else {
                    // 部分结果：实时预览
                    val sep = if (baseText.isNotBlank() && !baseText.endsWith(" ")) " " else ""
                    inputViewModel.updateText(baseText + sep + resultText)
                }
            },
            onError = { code, msg ->
                isRecording = false
                currentAmplitude = 0f
                Toast.makeText(context, "识别失败($code): $msg", Toast.LENGTH_SHORT).show()
            }
        )

        if (!recognizer.start()) {
            isRecording = false
            Toast.makeText(context, "语音识别启动失败", Toast.LENGTH_SHORT).show()
            return
        }
        voiceRecognizer = recognizer

        // 启动 AudioRecord
        val rec = buildAudioRecorder(
            scope = scope,
            onAmplitude = { amp -> currentAmplitude = amp },
            onAudioData = { bytes ->
                voiceRecognizer?.write(bytes)
            },
            onError = {
                isRecording = false
                currentAmplitude = 0f
                Toast.makeText(context, "麦克风不可用", Toast.LENGTH_SHORT).show()
            }
        )
        audioRecord = rec
        if (rec == null) {
            isRecording = false
            voiceRecognizer?.stop()
            voiceRecognizer = null
        }
    }

    fun stopRecording() {
        audioRecord?.let {
            try { it.stop() } catch (_: Exception) {}
            try { it.release() } catch (_: Exception) {}
        }
        audioRecord = null
        isRecording = false
        currentAmplitude = 0f

        voiceRecognizer?.stop()
        voiceRecognizer = null
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("输入文字", style = MaterialTheme.typography.headlineMedium)
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

                MicButton(
                    isRecording = isRecording,
                    onPress = { startRecording() },
                    onRelease = { stopRecording() },
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onNavigateToDisplay,
                modifier = Modifier.size(width = 200.dp, height = 56.dp),
                enabled = text.isNotBlank()
            ) {
                Text("进入显示", style = MaterialTheme.typography.titleMedium)
            }
        }

        // 声纹
        if (isRecording) {
            VoiceWaveBars(
                amplitude = currentAmplitude,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            )
        }
    }
}

// ---------- 麦克风按钮 ----------

@Composable
private fun MicButton(
    isRecording: Boolean,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed && !isRecording) onPress()
    }
    LaunchedEffect(isPressed, isRecording) {
        if (!isPressed && isRecording) onRelease()
    }

    val pulseAlpha by if (isRecording) {
        rememberInfiniteTransition(label = "mic").animateFloat(
            1f, 0.3f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "a"
        )
    } else remember { mutableStateOf(1f) }

    val bg = when {
        isRecording -> MaterialTheme.colorScheme.errorContainer
        isPressed   -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        else        -> MaterialTheme.colorScheme.primaryContainer
    }
    val fg = when {
        isRecording -> MaterialTheme.colorScheme.onErrorContainer
        else        -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    Box(
        modifier = modifier
            .size(56.dp).clip(CircleShape).background(bg)
            .clickable(interactionSource, indication = null, onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Filled.KeyboardVoice, "按住说话",
            Modifier.size(28.dp).alpha(pulseAlpha), tint = fg
        )
    }
}

// ---------- 声纹 ----------

@Composable
private fun VoiceWaveBars(amplitude: Float, modifier: Modifier = Modifier, barCount: Int = 7) {
    Row(modifier, Arrangement.Center, Alignment.Bottom) {
        for (i in 0 until barCount) {
            VoiceBar(i, amplitude)
            if (i < barCount - 1) Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
private fun VoiceBar(idx: Int, amp: Float) {
    val t = rememberInfiniteTransition(label = "vb$idx")
    val j by t.animateFloat(0f, 1f,
        infiniteRepeatable(tween(300 + idx * 70, easing = LinearEasing), RepeatMode.Reverse),
        label = "j$idx"
    )
    val base = 0.06f + j * 0.04f
    val voice = amp.coerceIn(0f, 1f) * 0.85f
    val h = (base + voice).coerceIn(0f, 1f)
    val color = if (amp > 0.12f) MaterialTheme.colorScheme.primary
               else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

    Box(Modifier.width(5.dp).height((8 + (56 - 8) * h).dp)
        .clip(RoundedCornerShape(3.dp)).background(color))
}

// ---------- AudioRecord ----------

private fun buildAudioRecorder(
    scope: kotlinx.coroutines.CoroutineScope,
    onAmplitude: (Float) -> Unit,
    onAudioData: (ByteArray) -> Unit,
    onError: () -> Unit
): AudioRecord? {
    return try {
        val bufSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(1280) // 40ms frame

        val rec = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT, bufSize
        )

        if (rec.state != AudioRecord.STATE_INITIALIZED) { rec.release(); onError(); return null }
        rec.startRecording()

        scope.launch(Dispatchers.IO) {
            val buf = ShortArray(bufSize)
            try {
                while (isActive && rec.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    val n = rec.read(buf, 0, buf.size)
                    if (n > 0) {
                        // 振幅
                        var sum = 0.0
                        for (i in 0 until n) sum += buf[i].toDouble().let { it * it }
                        val rms = sqrt(sum / n)
                        val norm = (log10(rms.coerceAtLeast(1.0)) / log10(32768.0))
                            .coerceIn(0.0, 1.0).toFloat()
                        withContext(Dispatchers.Main) { onAmplitude(norm) }

                        // 转 byte[] 送入 ASR
                        val bytes = ShortArrayToByteArray(buf, n)
                        withContext(Dispatchers.Main) { onAudioData(bytes) }
                    }
                }
            } catch (_: Exception) {}
        }
        rec
    } catch (_: Exception) { onError(); null }
}

private fun ShortArrayToByteArray(shorts: ShortArray, len: Int): ByteArray {
    val bytes = ByteArray(len * 2)
    for (i in 0 until len) {
        val v = shorts[i].toInt()
        bytes[i * 2] = (v and 0xFF).toByte()
        bytes[i * 2 + 1] = ((v shr 8) and 0xFF).toByte()
    }
    return bytes
}
