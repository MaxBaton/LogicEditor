package com.example.logiceditor.sketcher

import android.graphics.Color
import android.graphics.Paint
import android.view.SurfaceHolder
import com.example.logiceditor.game.CurrentGame

class SurfaceDrawingThread(private val surfaceHolder: SurfaceHolder): Thread() {

    private var mBackgroundPaint: Paint? = null
    var game: CurrentGame? = null
    var scrollCoordinates = 0f to 0f

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
            if (scrollCoordinates.first != 0f || scrollCoordinates.second != 0f) {
                game!!.usedTools.forEach {
                    it!!.coordinates = (it.coordinates.first - scrollCoordinates.first) to
                            (it.coordinates.second - scrollCoordinates.second)
                }
            }
            game!!.draw(canvas)
        }

        surfaceHolder.unlockCanvasAndPost(canvas)
        scrollCoordinates = 0f to 0f
    }
}