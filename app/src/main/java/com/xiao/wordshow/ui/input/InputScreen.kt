package com.xiao.wordshow.ui.input

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

                // 按住说话按钮
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

        // 聆听状态全屏提示
        if (isListening) {
            ListeningOverlay()
        }
    }
}

/**
 * 按住说话按钮 — 按下开始录音，松开无操作（系统识别自动结束）
 */
@Composable
private fun HoldToTalkButton(
    isListening: Boolean,
    onPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alpha by if (isListening) {
        rememberInfiniteTransition(label = "pulse").animateFloat(
            initialValue = 1f,
            targetValue = 0.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(600),
                repeatMode = RepeatMode.Reverse
            ),
            label = "micAlpha"
        )
    } else {
        remember { mutableStateOf(1f) }  // no need for animation
    }

    IconButton(
        onClick = {}, // 不用 onClick，用 pointerInput 处理按压
        modifier = modifier
            .size(56.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPress()
                        tryAwaitRelease()
                    }
                )
            },
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (isListening)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (isListening)
                MaterialTheme.colorScheme.onErrorContainer
            else
                MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Icon(
            imageVector = Icons.Filled.KeyboardVoice,
            contentDescription = "按住说话",
            modifier = Modifier
                .size(28.dp)
                .alpha(alpha)
        )
    }
}

/**
 * 聆听状态全屏遮罩 — 提示用户正在录音
 */
@Composable
private fun ListeningOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "listening")
    val dotCount = 3
    val animatedDots by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "dots"
    )
    val dots = ".".repeat(
        ((animatedDots * (dotCount + 1)).toInt() % (dotCount + 1)).coerceAtLeast(1)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            ,
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // 波形动画圈
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardVoice,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "正在聆听$dots",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "说话结束后自动识别",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}
