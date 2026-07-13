package com.xiao.wordshow.data.voice

import com.iflytek.sparkchain.core.asr.ASR
import com.iflytek.sparkchain.core.asr.AsrCallbacks
import com.iflytek.sparkchain.core.asr.AudioAttributes

/**
 * 语音识别封装 — 基于讯飞 SparkChain ASR
 *
 * 用法：
 * 1. create() 创建实例并注册回调
 * 2. start() 开启会话
 * 3. write(bytes) 持续送入 PCM 音频（16kHz, 16bit, mono）
 * 4. stop() 结束会话
 */
class VoiceRecognizer private constructor() {

    private var asr: ASR? = null
    private var resultCallback: ((text: String, isFinal: Boolean) -> Unit)? = null
    private var errorCallback: ((code: Int, msg: String) -> Unit)? = null

    companion object {
        fun create(
            onResult: (text: String, isFinal: Boolean) -> Unit,
            onError: (code: Int, msg: String) -> Unit
        ): VoiceRecognizer {
            val r = VoiceRecognizer()
            r.resultCallback = onResult
            r.errorCallback = onError
            return r
        }
    }

    fun start(): Boolean {
        asr = ASR().apply {
            language("zh_cn")
            domain("iat")
            accent("mandarin")
            vadEos(1000)
            ptt(true)

            registerCallbacks(object : AsrCallbacks {
                override fun onResult(result: ASR.ASRResult?, usrTag: Any?) {
                    val text = result?.bestMatchText ?: return
                    val isFinal = result.status == 2
                    resultCallback?.invoke(text, isFinal)
                }

                override fun onError(error: ASR.ASRError?, usrTag: Any?) {
                    errorCallback?.invoke(
                        error?.code ?: -1,
                        error?.errMsg ?: "未知错误"
                    )
                }
            })
        }

        val attr = AudioAttributes()
        attr.sampleRate = 16000
        attr.encoding = "raw"
        attr.channels = 1
        attr.bitdepth = 16
        attr.frameSize = 0

        val ret = asr!!.start(attr, null)
        return ret == 0
    }

    fun write(data: ByteArray): Boolean {
        val ret = asr?.write(data) ?: return false
        return ret == 0
    }

    fun stop() {
        asr?.stop(false)
        asr = null
    }
}
