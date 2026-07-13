package com.xiao.wordshow.data.voice

import android.content.Intent
import android.speech.RecognizerIntent

/**
 * 语音识别封装（P1）
 * 使用系统 RecognizerIntent，无需三方 SDK
 */
object VoiceRecognizer {

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
