package com.xiao.wordshow.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.xiao.wordshow.data.model.TextEffect
import com.xiao.wordshow.data.preferences.HistoryRepository
import com.xiao.wordshow.ui.display.DisplayViewModel
import com.xiao.wordshow.ui.display.presetTextColors
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    repo: HistoryRepository,
    displayViewModel: DisplayViewModel
) {
    val scope = rememberCoroutineScope()
    val colorMode by displayViewModel.colorMode.collectAsState()
    val presetNames by repo.presetNames.collectAsState(initial = emptyList())
    // 预设详情缓存
    var presetDetails by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    LaunchedEffect(presetNames) {
        val map = mutableMapOf<String, String>()
        for (name in presetNames) {
            repo.loadPreset(name)?.let { map[name] = it }
        }
        presetDetails = map
    }

    var showSaveDialog by remember { mutableStateOf(false) }
    var presetNameInput by remember { mutableStateOf("") }

    val systemIsLight = !androidx.compose.foundation.isSystemInDarkTheme()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = MaterialTheme.colorScheme.onBackground) }
            Text("设置", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        }

        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            // 颜色模式
            item {
                Text("默认颜色模式", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(bottom = 12.dp))
                val modes = listOf("system" to "跟随系统", "dark" to "深色模式", "light" to "浅色模式")
                modes.forEach { (key, label) ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = colorMode == key,
                            onClick = {
                                scope.launch {
                                    repo.setColorMode(key)
                                    displayViewModel.setColorMode(key, systemIsLight)
                                }
                            }
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }

            // 预设
            item {
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("预设配置", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.weight(1f))
                    // 显示当前配置摘要
                    val summary = buildConfigSummary(displayViewModel)
                    Text(summary, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { showSaveDialog = true }) {
                        Icon(Icons.Filled.Add, "保存当前配置", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (presetNames.isEmpty()) {
                item { Text("暂无预设，调整好配置后点 + 保存", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp)) }
            } else {
                items(presetNames, key = { it }) { name ->
                    val detail = presetDetails[name] ?: ""
                    Card(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                            scope.launch {
                                repo.loadPreset(name)?.let { applyPreset(it, displayViewModel) }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                                IconButton(onClick = { scope.launch { repo.deletePreset(name); presetDetails = presetDetails - name } }) {
                                    Icon(Icons.Filled.Delete, "删除", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                }
                            }
                            if (detail.isNotBlank()) {
                                Text(presetSummary(detail), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }

    // 保存对话框
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("保存预设") },
            text = {
                Column {
                    Text("当前配置：${buildConfigSummary(displayViewModel)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = presetNameInput, onValueChange = { presetNameInput = it }, label = { Text("预设名称") }, singleLine = true)
                }
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

private fun buildConfigSummary(vm: DisplayViewModel): String {
    val eff = vm.currentEffect.value.let { listOf("无","渐变","阴影","发光","跳动","LED").getOrElse(it.ordinal) { "无" } }
    val clr = if (vm.colorIndex.value == 0) "自动" else listOf("白","黄","红","绿","蓝","橙","紫","青").getOrElse(vm.colorIndex.value - 1) { "自动" }
    return "${vm.fontSize.value.toInt()}sp · $clr · $eff · ${vm.scrollSpeed.value}x"
}

private fun presetSummary(config: String): String {
    val p = config.split(",")
    if (p.size < 5) return config
    val font = listOf("默认","默认粗","默认细","衬线","无衬线细","等宽","手写").getOrElse(p[0].toIntOrNull() ?: 0) { "默认" }
    val color = if (p[1] == "0") "自动" else listOf("白","黄","红","绿","蓝","橙","紫","青").getOrElse((p[1].toIntOrNull() ?: 1) - 1) { "?" }
    val effect = listOf("无","渐变","阴影","发光","跳动","LED").getOrElse(p[2].toIntOrNull() ?: 0) { "?" }
    return "$font · $color · $effect · ${p[3]}x · ${p[4]}sp"
}

private suspend fun applyPreset(config: String, vm: DisplayViewModel) {
    val parts = config.split(",")
    if (parts.size >= 5) {
        vm.setFont(parts[0].toIntOrNull() ?: 0)
        vm.setColor(parts[1].toIntOrNull() ?: 0)
        vm.setEffect(TextEffect.entries.getOrElse(parts[2].toIntOrNull() ?: 0) { TextEffect.NONE })
        vm.setScrollSpeed(parts[3].toFloatOrNull() ?: 1f)
        vm.setFontSize(parts[4].toFloatOrNull() ?: 64f)
    }
}
