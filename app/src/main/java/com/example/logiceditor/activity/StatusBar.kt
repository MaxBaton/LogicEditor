package com.example.logiceditor.activity

import android.app.Activity
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.example.logiceditor.R

object StatusBar {
    fun updateStatusBar(activity: Activity) {
        val window = activity.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.background_circuit_color_round))
    }
}