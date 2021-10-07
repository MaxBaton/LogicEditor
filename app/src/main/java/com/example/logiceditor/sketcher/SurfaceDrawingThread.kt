package com.example.logiceditor.sketcher

import android.graphics.Color
import android.graphics.Paint
import android.view.SurfaceHolder
import com.example.logiceditor.game.CurrentGame

class SurfaceDrawingThread(private val surfaceHolder: SurfaceHolder): Thread() {

    private var mBackgroundPaint: Paint? = null
    var game: CurrentGame? = null

    init {
        mBackgroundPaint = Paint()
        mBackgroundPaint!!.color = Color.WHITE
    }

    override fun run() {
        val canvas = surfaceHolder.lockCanvas()
        val width = canvas.width
        val height = canvas.height
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), mBackgroundPaint!!)
        if (game != null) {
            game!!.draw(canvas)
        }

        surfaceHolder.unlockCanvasAndPost(canvas)
    }
}