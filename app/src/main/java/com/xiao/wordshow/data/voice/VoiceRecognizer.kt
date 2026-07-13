package com.xiao.wordshow.data.voice

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent

/**
 * 语音识别封装（P1）
 * 使用系统 RecognizerIntent，兼容国产设备（小米/华为/OPPO 等）
 */
object VoiceRecognizer {

    /**
     * 检测设备是否支持语音识别
     * 不依赖 Google 服务，通过 PackageManager 检测任意语音识别 App
     */
    fun isAvailable(context: Context): Boolean {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        val resolveInfoList = context.packageManager.queryIntentActivities(intent, 0)
        return resolveInfoList.isNotEmpty()
    }

    /**
     * 创建系统语音识别 Intent
     * @return 可直接启动的 RecognizerIntent
     */
    fun createRecognizerIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "说出要显示的文字")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
    }
}
