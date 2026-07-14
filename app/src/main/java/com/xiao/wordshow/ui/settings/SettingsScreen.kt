package com.xiao.wordshow.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.xiao.wordshow.data.preferences.HistoryRepository
import com.xiao.wordshow.ui.display.DisplayViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    repo: HistoryRepository,
    displayViewModel: DisplayViewModel
) {
    val scope = rememberCoroutineScope()
    val colorMode by repo.colorMode.collectAsState(initial = "system")
    val presetNames by repo.presetNames.collectAsState(initial = emptyList())

    var showSaveDialog by remember { mutableStateOf(false) }
    var presetNameInput by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // 顶栏
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text("设置", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        }

        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            // 颜色模式
            item {
                Text("默认颜色模式", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(bottom = 12.dp))
                val modes = listOf("system" to "跟随系统", "dark" to "深色模式", "light" to "浅色模式")
                modes.forEach { (key, label) ->
                    Row(Modifier.fillMaxWidth().clickable { scope.launch { repo.setColorMode(key) } }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = colorMode == key, onClick = { scope.launch { repo.setColorMode(key) } })
                        Spacer(Modifier.width(12.dp))
                        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }

            // 预设配置
            item {
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("预设配置", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { showSaveDialog = true }) {
                        Icon(Icons.Filled.Add, "保存当前配置", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (presetNames.isEmpty()) {
                item { Text("暂无预设，点 + 保存当前配置", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp)) }
            } else {
                items(presetNames, key = { it }) { name ->
                    Row(Modifier.fillMaxWidth().clickable {
                        scope.launch {
                            repo.loadPreset(name)?.let { applyPreset(it, displayViewModel) }
                        }
                    }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground)
                        IconButton(onClick = { scope.launch { repo.deletePreset(name) } }) {
                            Icon(Icons.Filled.Delete, "删除", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }

    // 保存预设对话框
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("保存预设") },
            text = {
                OutlinedTextField(
                    value = presetNameInput,
                    onValueChange = { presetNameInput = it },
                    label = { Text("预设名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (presetNameInput.isNotBlank()) {
                        scope.launch {
                            repo.savePreset(presetNameInput, buildPresetConfig(displayViewModel))
                            presetNameInput = ""
                            showSaveDialog = false
                        }
                    }
                }) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { showSaveDialog = false }) { Text("取消") } }
        )
    }
}

private fun buildPresetConfig(vm: DisplayViewModel): String {
    val f = vm.fontIndex.value
    val c = vm.colorIndex.value
    val e = vm.currentEffect.value.ordinal
    val s = vm.scrollSpeed.value
    val fs = vm.fontSize.value
    return "$f,$c,$e,$s,$fs"
}

private suspend fun applyPreset(config: String, vm: DisplayViewModel) {
    val parts = config.split(",")
    if (parts.size >= 5) {
        vm.setFont(parts[0].toIntOrNull() ?: 0)
        vm.setColor(parts[1].toIntOrNull() ?: 0)
        val effectOrdinal = parts[2].toIntOrNull() ?: 0
        vm.setEffect(com.xiao.wordshow.data.model.TextEffect.entries.getOrElse(effectOrdinal) { com.xiao.wordshow.data.model.TextEffect.NONE })
        vm.setScrollSpeed(parts[3].toFloatOrNull() ?: 1f)
        vm.setFontSize(parts[4].toFloatOrNull() ?: 64f)
    }
}
