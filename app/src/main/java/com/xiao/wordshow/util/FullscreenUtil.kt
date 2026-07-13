package com.xiao.wordshow.util

import android.app.Activity
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController

/**
 * 全屏模式工具类 — 隐藏/显示状态栏和导航栏
 */
object FullscreenUtil {

    fun enterFullscreen(activity: Activity) {
        val windowInsetsController =
            activity.window.decorView.windowInsetsController
        windowInsetsController?.let {
            it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            it.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    fun exitFullscreen(activity: Activity) {
        val windowInsetsController =
            activity.window.decorView.windowInsetsController
        windowInsetsController?.let {
            it.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        }
    }
}
