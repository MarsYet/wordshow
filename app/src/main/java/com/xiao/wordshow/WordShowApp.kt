package com.xiao.wordshow

import android.app.Application
import com.iflytek.sparkchain.core.SparkChain
import com.iflytek.sparkchain.core.SparkChainConfig

class WordShowApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val config = SparkChainConfig.builder()
            .appID("7a06bdcf")
            .apiKey("642d26ae7b75787595c01fb40dc11c08")
            .apiSecret("ODk4OTdlYjcyZWRhODgwZTkzMjYzNzg2")
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
