package com.xiao.wordshow.ui.input

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    // 语音识别结果处理
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
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
            voiceLauncher.launch(VoiceRecognizer.createRecognizerIntent())
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "未找到语音识别服务\n请安装讯飞输入法或 Google 语音搜索",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // 录音权限请求（需要 tryLaunchVoice 已定义）
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

            // 语音按钮 — 始终可用，不预检，运行时捕获异常
            IconButton(
                onClick = { startVoiceInput() },
                modifier = Modifier
                    .size(56.dp)
                    .padding(top = 4.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
