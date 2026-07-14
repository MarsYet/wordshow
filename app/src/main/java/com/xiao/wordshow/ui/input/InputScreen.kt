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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.xiao.wordshow.data.preferences.HistoryRepository
import com.xiao.wordshow.data.voice.VoiceRecognizer
import com.xiao.wordshow.util.AdaptiveParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.log10
import kotlin.math.sqrt

private const val SAMPLE_RATE = 16000

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(
    onNavigateToDisplay: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
    inputViewModel: InputViewModel,
    displayViewModel: com.xiao.wordshow.ui.display.DisplayViewModel,
    adaptive: AdaptiveParams,
    repo: HistoryRepository
) {
    val text by inputViewModel.text.collectAsState()
    val isBoardMode by displayViewModel.isBoardMode.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isRecording by remember { mutableStateOf(false) }
    var currentAmplitude by remember { mutableFloatStateOf(0f) }
    var audioRecord by remember { mutableStateOf<AudioRecord?>(null) }
    var voiceRecognizer by remember { mutableStateOf<VoiceRecognizer?>(null) }
    var baseText by remember { mutableStateOf("") }

    // DataStore 数据
    val history by repo.history.collectAsState(initial = emptyList())
    val presets by repo.presets.collectAsState(initial = HistoryRepository.DEFAULT_PRESETS.toList())

    // 历史弹窗
    var showHistory by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    DisposableEffect(Unit) {
        onDispose { audioRecord?.let { try { it.release() } catch (_: Exception) {} } }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) Toast.makeText(context, "语音输入需要录音权限，请在系统设置中开启", Toast.LENGTH_LONG).show()
    }

    fun startRecording() { /* ... same as before ... */
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO); return }
        baseText = inputViewModel.text.value; isRecording = true
        val r = VoiceRecognizer.create(
            onResult = { rt, fin ->
                val sep = if (baseText.isNotBlank() && !baseText.endsWith(" ")) " " else ""
                inputViewModel.updateText(baseText + sep + rt)
            },
            onError = { c, m -> isRecording = false; currentAmplitude = 0f; Toast.makeText(context, "识别失败($c): $m", Toast.LENGTH_SHORT).show() }
        )
        if (!r.start()) { isRecording = false; Toast.makeText(context, "语音识别启动失败", Toast.LENGTH_SHORT).show(); return }
        voiceRecognizer = r
        val rec = buildAudioRecorder(scope, { currentAmplitude = it }, { voiceRecognizer?.write(it) }, { isRecording = false; currentAmplitude = 0f; Toast.makeText(context, "麦克风不可用", Toast.LENGTH_SHORT).show() })
        audioRecord = rec
        if (rec == null) { isRecording = false; voiceRecognizer?.stop(); voiceRecognizer = null }
    }

    fun stopRecording() {
        audioRecord?.let { try { it.stop() } catch (_: Exception) {}; try { it.release() } catch (_: Exception) {} }
        audioRecord = null; isRecording = false; currentAmplitude = 0f
        voiceRecognizer?.stop(); voiceRecognizer = null
    }

    fun navigate() {
        scope.launch { repo.addHistory(text) }
        onNavigateToDisplay()
    }

    // 文件导入 + 导入状态
    val subtitleSentences by displayViewModel.subtitleSentences.collectAsState()
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val sentences = com.xiao.wordshow.util.WordParser.parseDocx(context, it)
            if (sentences.isNotEmpty()) {
                displayViewModel.loadSentences(sentences)
                Toast.makeText(context, "已导入 ${sentences.size} 句，点击进入显示查看", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "文件解析失败，请确认是 .docx 格式", Toast.LENGTH_LONG).show()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 左上角展板/字幕切换
        BoardSubtitleToggle(
            isBoard = isBoardMode,
            onToggle = { displayViewModel.setBoardMode(it) },
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 标题行
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box {
                    val title = if (isBoardMode) "输入文字" else "字幕播报"
                    Text(title, style = MaterialTheme.typography.headlineMedium.copy(drawStyle = androidx.compose.ui.graphics.drawscope.Stroke(3f)), color = Color.White)
                    Text(title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Filled.Folder, "预设配置", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = { showHistory = true }) {
                    Icon(Icons.AutoMirrored.Filled.List, "历史记录", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(8.dp))

            // 字幕模式 - 导入Word文件
            if (!isBoardMode) {
                Button(
                    onClick = { filePicker.launch(arrayOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) },
                    modifier = Modifier.fillMaxWidth().height(40.dp).shadow(14.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xEEFFFFFF), contentColor = Color(0xFF5B9BD5))
                ) {
                    Text(if (subtitleSentences.isEmpty()) "📄 导入 Word 文件" else "📄 已加载 ${subtitleSentences.size} 句 (点此重新导入)")
                }
                Spacer(Modifier.height(8.dp))
            }

            // 预设短语条（仅展板模式）
            if (isBoardMode) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(presets, key = { it }) { p ->
                        SuggestionChip(
                            onClick = { inputViewModel.updateText(p) },
                            label = { Text(p, style = MaterialTheme.typography.labelMedium) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 输入区
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                OutlinedTextField(
                    value = text, onValueChange = inputViewModel::updateText,
                    modifier = Modifier.weight(1f),
                    label = { Text(if (isBoardMode) "输入文字..." else "输入字幕...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    maxLines = 5,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    )
                )
                Spacer(Modifier.width(8.dp))
                MicButton(isRecording, { startRecording() }, { stopRecording() }, Modifier.padding(top = 4.dp))
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { navigate() },
                modifier = Modifier.fillMaxWidth().height(52.dp).shadow(14.dp, RoundedCornerShape(26.dp), spotColor = Color.Black.copy(alpha = 0.2f)),
                enabled = text.isNotBlank(),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xDDFFFFFF), contentColor = Color(0xFF2C3035),
                    disabledContainerColor = Color(0x99EEF0F2), disabledContentColor = Color(0x99A0A8B0),
                )
            ) { Text("进入显示", style = MaterialTheme.typography.titleMedium) }
        }

        // 声纹
        if (isRecording) VoiceWaveBars(currentAmplitude, Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp))
    }

    // 历史记录弹窗
    if (showHistory) {
        ModalBottomSheet(onDismissRequest = { showHistory = false }, sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                Text("历史记录", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
                if (history.isEmpty()) {
                    Text("暂无历史记录", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 32.dp))
                } else {
                    history.take(20).forEach { item ->
                        Row(Modifier.fillMaxWidth().clickable {
                            inputViewModel.updateText(item)
                            showHistory = false
                        }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(item, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f), maxLines = 2)
                            IconButton(onClick = { scope.launch { repo.removeHistory(item) } }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Filled.Delete, "删除", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---- MicButton ----
@Composable
private fun MicButton(isRecording: Boolean, onPress: () -> Unit, onRelease: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    LaunchedEffect(isPressed) { if (isPressed && !isRecording) onPress() }
    LaunchedEffect(isPressed, isRecording) { if (!isPressed && isRecording) onRelease() }
    val pulseAlpha by if (isRecording) rememberInfiniteTransition(label = "mic").animateFloat(1f, 0.3f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "a") else remember { mutableStateOf(1f) }
    val bg = when { isRecording -> Color(0xFFFFE8E0); isPressed -> Color(0xCCF0F2F4); else -> Color(0xEEFFFFFF) }
    val fg = when { isRecording -> Color(0xFFD84040); else -> Color(0xFF5B6A7A) }
    Box(modifier.size(56.dp).clip(CircleShape).shadow(7.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.2f)).background(bg).clickable(interactionSource, indication = null, onClick = {}), contentAlignment = Alignment.Center) {
        Icon(Icons.Filled.KeyboardVoice, "按住说话", Modifier.size(28.dp).alpha(pulseAlpha), tint = fg)
    }
}

// ---- 声纹 ----
@Composable
private fun VoiceWaveBars(amplitude: Float, modifier: Modifier = Modifier, barCount: Int = 7) {
    Row(modifier, Arrangement.Center, Alignment.Bottom) { for (i in 0 until barCount) { VoiceBar(i, amplitude); if (i < barCount - 1) Spacer(Modifier.width(8.dp)) } }
}

@Composable
private fun VoiceBar(idx: Int, amp: Float) {
    val t = rememberInfiniteTransition(label = "vb$idx")
    val j by t.animateFloat(0f, 1f, infiniteRepeatable(tween(300 + idx * 70, easing = LinearEasing), RepeatMode.Reverse), label = "j$idx")
    val base = 0.06f + j * 0.04f; val voice = amp.coerceIn(0f, 1f) * 0.85f; val h = (base + voice).coerceIn(0f, 1f)
    val color = if (amp > 0.12f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    Box(Modifier.width(5.dp).height((8 + (56 - 8) * h).dp).clip(RoundedCornerShape(3.dp)).background(color))
}

// ---- AudioRecord ----
private fun buildAudioRecorder(scope: kotlinx.coroutines.CoroutineScope, onAmplitude: (Float) -> Unit, onAudioData: (ByteArray) -> Unit, onError: () -> Unit): AudioRecord? {
    return try {
        val bufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT).coerceAtLeast(1280)
        val rec = AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufSize)
        if (rec.state != AudioRecord.STATE_INITIALIZED) { rec.release(); onError(); return null }
        rec.startRecording()
        scope.launch(Dispatchers.IO) {
            val buf = ShortArray(bufSize)
            try {
                while (isActive && rec.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    val n = rec.read(buf, 0, buf.size)
                    if (n > 0) {
                        var sum = 0.0; for (i in 0 until n) sum += buf[i].toDouble().let { it * it }
                        val rms = sqrt(sum / n); val norm = (log10(rms.coerceAtLeast(1.0)) / log10(32768.0)).coerceIn(0.0, 1.0).toFloat()
                        withContext(Dispatchers.Main) { onAmplitude(norm) }
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
    val bytes = ByteArray(len * 2); for (i in 0 until len) { val v = shorts[i].toInt(); bytes[i * 2] = (v and 0xFF).toByte(); bytes[i * 2 + 1] = ((v shr 8) and 0xFF).toByte() }; return bytes
}

// ---------- 展板/字幕 拟物滑块 ----------

@Composable
fun BoardSubtitleToggle(isBoard: Boolean, onToggle: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val thumbOffset by animateFloatAsState(if (isBoard) 0f else 1f, animationSpec = tween(300), label = "toggle")

    val trackWidth = 96.dp
    val thumbSize = 36.dp
    val trackPadding = 2.dp

    Box(
        modifier = modifier
            .width(trackWidth).height(thumbSize + trackPadding * 2)
            .shadow(14.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.2f))
            .background(
                Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0xCCF0F2F4))),
                RoundedCornerShape(20.dp)
            )
            .border(0.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
            .clickable { onToggle(!isBoard) },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("展板", style = MaterialTheme.typography.labelSmall,
                color = if (isBoard) Color(0xFF3E3640) else Color(0xFF3E3640).copy(alpha = 0.3f))
            Text("字幕", style = MaterialTheme.typography.labelSmall,
                color = if (!isBoard) Color(0xFF3E3640) else Color(0xFF3E3640).copy(alpha = 0.3f))
        }

        Box(
            Modifier
                .offset(x = trackPadding + ((thumbOffset * (trackWidth - thumbSize - trackPadding * 2).value).dp))
                .size(thumbSize)
                .shadow(14.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.2f))
                .background(
                    Brush.verticalGradient(listOf(Color.White, Color(0xFFF0F2F4))),
                    CircleShape
                )
                .border(0.5.dp, Color.White.copy(alpha = 0.9f), CircleShape)
        )
    }
}
