package com.xiao.wordshow.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "wordshow")

class HistoryRepository(private val context: Context) {

    companion object {
        private val KEY_HISTORY = stringSetPreferencesKey("history")
        private val KEY_PRESETS = stringSetPreferencesKey("presets")
        private val KEY_LIGHT_BG = booleanPreferencesKey("light_background")

        val DEFAULT_PRESETS = setOf(
            "生日快乐", "欢迎光临", "安静", "加油",
            "我爱你", "谢谢", "再见", "晚安", "早安",
            "恭喜发财", "一路顺风", "干杯"
        )
    }

    // 历史记录
    val history: Flow<List<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_HISTORY]?.toList() ?: emptyList()
    }

    suspend fun addHistory(text: String) {
        if (text.isBlank()) return
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_HISTORY]?.toMutableList() ?: mutableListOf()
            current.remove(text) // 去重
            current.add(0, text) // 新记录放最前
            if (current.size > 50) current.removeAt(current.lastIndex)
            prefs[KEY_HISTORY] = current.toSet()
        }
    }

    suspend fun removeHistory(text: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_HISTORY]?.toMutableSet() ?: mutableSetOf()
            current.remove(text)
            prefs[KEY_HISTORY] = current
        }
    }

    // 预设短语
    val presets: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val saved = prefs[KEY_PRESETS]
        if (saved == null) DEFAULT_PRESETS.toList()
        else saved.toList()
    }

    suspend fun addPreset(text: String) {
        if (text.isBlank()) return
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_PRESETS]?.toMutableSet() ?: DEFAULT_PRESETS.toMutableSet()
            current.add(text)
            prefs[KEY_PRESETS] = current
        }
    }

    suspend fun removePreset(text: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_PRESETS]?.toMutableSet() ?: DEFAULT_PRESETS.toMutableSet()
            current.remove(text)
            prefs[KEY_PRESETS] = current
        }
    }

    suspend fun resetPresets() {
        context.dataStore.edit { it.remove(KEY_PRESETS) }
    }

    // 深浅背景
    val isLightBackground: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_LIGHT_BG] ?: false
    }

    suspend fun setLightBackground(light: Boolean) {
        context.dataStore.edit { it[KEY_LIGHT_BG] = light }
    }

    // 颜色模式：system/dark/light
    private val KEY_COLOR_MODE = stringPreferencesKey("color_mode")

    val colorMode: Flow<String> = context.dataStore.data.map { it[KEY_COLOR_MODE] ?: "system" }

    suspend fun setColorMode(mode: String) {
        context.dataStore.edit { it[KEY_COLOR_MODE] = mode }
    }

    // 预设配置
    private val KEY_PRESET_NAMES = stringSetPreferencesKey("preset_names")
    private fun presetKey(name: String) = stringPreferencesKey("preset_$name")

    val presetNames: Flow<List<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_PRESET_NAMES]?.toList() ?: emptyList()
    }

    suspend fun savePreset(name: String, config: String) {
        context.dataStore.edit { prefs ->
            val names = prefs[KEY_PRESET_NAMES]?.toMutableSet() ?: mutableSetOf()
            names.add(name)
            prefs[KEY_PRESET_NAMES] = names
            prefs[presetKey(name)] = config
        }
    }

    suspend fun loadPreset(name: String): String? {
        return context.dataStore.data.map { it[presetKey(name)] }.let { flow ->
            var result: String? = null
            flow.collect { result = it }
            result
        }
    }

    suspend fun deletePreset(name: String) {
        context.dataStore.edit { prefs ->
            val names = prefs[KEY_PRESET_NAMES]?.toMutableSet() ?: mutableSetOf()
            names.remove(name)
            prefs[KEY_PRESET_NAMES] = names
            prefs.remove(presetKey(name))
        }
    }
}
