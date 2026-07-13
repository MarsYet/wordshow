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
    inputViewModel: InputViewModel
) {
    val text by inputViewModel.text.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // null=空闲, false=录音中, true=识别中
    var recordingState by remember { mutableStateOf<Boolean?>(null) }
    val isRecording = recordingState == false
    var currentAmplitude by remember { mutableFloatStateOf(0f) }
    var audioRecord by remember { mutableStateOf<AudioRecord?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            audioRecord?.let {
                try { it.stop() } catch (_: Exception) {}
                try { it.release() } catch (_: Exception) {}
            }
        }
    }

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        recordingState = null
        currentAmplitude = 0f
        val matches = result.data?.getStringArrayListExtra(
            android.speech.RecognizerIntent.EXTRA_RESULTS
        )
        val spokenText = matches?.firstOrNull()
        if (!spokenText.isNullOrBlank()) {
            val cur = inputViewModel.text.value
            val sep = if (cur.isNotBlank() && !cur.endsWith(" ")) " " else ""
            inputViewModel.updateText(cur + sep + spokenText)
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

        recordingState = false
        val rec = buildAudioRecorder(
            scope = scope,
            onAmplitude = { amp -> currentAmplitude = amp },
            onError = {
                recordingState = null
                Toast.makeText(context, "麦克风不可用", Toast.LENGTH_SHORT).show()
            }
        )
        audioRecord = rec
        if (rec == null) recordingState = null
    }

    fun stopAndRecognize() {
        audioRecord?.let {
            try { it.stop() } catch (_: Exception) {}
            try { it.release() } catch (_: Exception) {}
        }
        audioRecord = null
        recordingState = true
        currentAmplitude = 0f

        try {
            voiceLauncher.launch(VoiceRecognizer.createRecognizerIntent())
        } catch (e: android.content.ActivityNotFoundException) {
            recordingState = null
            Toast.makeText(context, "未找到语音识别服务", Toast.LENGTH_LONG).show()
        }
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
                    isRecognizing = recordingState == true,
                    onPress = { startRecording() },
                    onRelease = { stopAndRecognize() },
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

        // 声纹 — 只在按住录音期间显示，松手即刻消失
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
    isRecognizing: Boolean,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 按下 → 录音
    LaunchedEffect(isPressed) {
        if (isPressed && !isRecording && !isRecognizing) {
            onPress()
        }
    }

    // 松手 → 识别（在录音中松手）
    LaunchedEffect(isPressed, isRecording) {
        if (!isPressed && isRecording) {
            onRelease()
        }
    }

    val active = isRecording || isRecognizing

    val pulseAlpha by if (active) {
        rememberInfiniteTransition(label = "mic").animateFloat(
            initialValue = 1f, targetValue = 0.3f,
            animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
            label = "a"
        )
    } else remember { mutableStateOf(1f) }

    val bg = when {
        isRecording   -> MaterialTheme.colorScheme.errorContainer
        isRecognizing -> MaterialTheme.colorScheme.tertiaryContainer
        isPressed     -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        else          -> MaterialTheme.colorScheme.primaryContainer
    }
    val fg = when {
        isRecording   -> MaterialTheme.colorScheme.onErrorContainer
        isRecognizing -> MaterialTheme.colorScheme.onTertiaryContainer
        else          -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(interactionSource = interactionSource, indication = null, onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.KeyboardVoice,
            contentDescription = "按住说话",
            modifier = Modifier.size(28.dp).alpha(pulseAlpha),
            tint = fg
        )
    }
}

// ---------- 声纹 ----------

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
            VoiceBar(idx = i, amp = amplitude)
            if (i < barCount - 1) Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
private fun VoiceBar(idx: Int, amp: Float) {
    val t = rememberInfiniteTransition(label = "vb$idx")
    val j by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(300 + idx * 70, easing = LinearEasing), RepeatMode.Reverse),
        label = "j$idx"
    )
    val base = 0.06f + j * 0.04f
    val voice = amp.coerceIn(0f, 1f) * 0.85f
    val h = (base + voice).coerceIn(0f, 1f)

    val color = if (amp > 0.12f) MaterialTheme.colorScheme.primary
               else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

    Box(
        Modifier
            .width(5.dp)
            .height((8 + (56 - 8) * h).dp)
            .clip(RoundedCornerShape(3.dp))
            .background(color)
    )
}

// ---------- AudioRecord ----------

private fun buildAudioRecorder(
    scope: kotlinx.coroutines.CoroutineScope,
    onAmplitude: (Float) -> Unit,
    onError: () -> Unit
): AudioRecord? {
    return try {
        val bufSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(1024)

        val rec = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT, bufSize
        )

        if (rec.state != AudioRecord.STATE_INITIALIZED) {
            rec.release(); onError(); return null
        }

        rec.startRecording()

        scope.launch(Dispatchers.IO) {
            val buf = ShortArray(bufSize)
            try {
                while (isActive && rec.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    val n = rec.read(buf, 0, buf.size)
                    if (n > 0) {
                        var sum = 0.0
                        for (i in 0 until n) sum += buf[i].toDouble().let { it * it }
                        val rms = sqrt(sum / n)
                        val norm = (log10(rms.coerceAtLeast(1.0)) / log10(32768.0))
                            .coerceIn(0.0, 1.0).toFloat()
                        withContext(Dispatchers.Main) { onAmplitude(norm) }
                    }
                }
            } catch (_: Exception) {}
        }

        rec
    } catch (_: Exception) { onError(); null }
}
