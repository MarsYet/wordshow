package com.xiao.wordshow

import android.app.Application
import com.iflytek.sparkchain.core.SparkChain
import com.iflytek.sparkchain.core.SparkChainConfig
import com.xiao.wordshow.BuildConfig

class WordShowApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val config = SparkChainConfig.builder()
            .appID(BuildConfig.SPARKCHAIN_APP_ID)
            .apiKey(BuildConfig.SPARKCHAIN_API_KEY)
            .apiSecret(BuildConfig.SPARKCHAIN_API_SECRET)
            .workDir(filesDir.absolutePath + "/sparkchain")
            .logLevel(100) // OFF

        val ret = SparkChain.getInst().init(this, config)
        if (ret != 0) {
            android.util.Log.e("WordShow", "SparkChain init failed: $ret")
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        SparkChain.getInst().unInit()
    }
}
