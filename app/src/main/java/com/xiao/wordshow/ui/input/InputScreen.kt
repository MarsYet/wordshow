package com.xiao.wordshow.ui.input

import android.Manifest
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
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
    var voiceNotAvailable by remember { mutableStateOf(false) }

    // 录音权限请求
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(context, "语音输入需要录音权限，请在系统设置中开启", Toast.LENGTH_LONG).show()
        }
    }

    // 语音识别结果
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        val spokenText = matches?.firstOrNull()
        if (!spokenText.isNullOrBlank()) {
            val current = inputViewModel.text.value
            // 追加到现有文字后面
            val separator = if (current.isNotBlank() && !current.endsWith(" ")) " " else ""
            inputViewModel.updateText(current + separator + spokenText)
        }
    }

    fun startVoiceInput() {
        // 检查设备是否支持语音识别
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            voiceNotAvailable = true
            Toast.makeText(context, "此设备不支持语音识别", Toast.LENGTH_SHORT).show()
            return
        }
        voiceNotAvailable = false

        // 检查权限
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            voiceLauncher.launch(VoiceRecognizer.createRecognizerIntent())
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Column(
        modifier = modifier
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

            IconButton(
                onClick = { startVoiceInput() },
                modifier = Modifier
                    .size(56.dp)
                    .padding(top = 4.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (voiceNotAvailable)
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (voiceNotAvailable)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardVoice,
                    contentDescription = "语音输入",
                    modifier = Modifier.size(28.dp)
                )
            }
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
}
