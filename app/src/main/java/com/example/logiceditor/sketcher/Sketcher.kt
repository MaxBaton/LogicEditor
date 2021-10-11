package com.example.logiceditor.sketcher

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.logiceditor.game.CurrentGame
import com.example.logiceditor.tools.wire.WireState

class Sketcher(context: Context, atrs: AttributeSet): SurfaceView(context, atrs), SurfaceHolder.Callback {
    val game = CurrentGame()
    private var drawingEnabled = false
    private var wiringEnabled = false
    private var deleteEnabled = false
    private var inversionEnabled = false
    private var movingEnabled = false
    private var wireState: WireState = WireState()
    lateinit var drawingThread: SurfaceDrawingThread
    private val gestureDetector = GestureDetector(context, MyGestureListener())

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        game.sketcherRightBottom = this.right.toFloat() to this.bottom.toFloat()

        drawingThread = SurfaceDrawingThread(holder)
        drawingThread.start()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        try {
            drawingThread.join()
        }catch (e: InterruptedException) {
            Log.d("PaintException", e.toString())
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        drawingThread.game = game
        drawingThread.run()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (drawingEnabled) {
            game.handleMoveEvent(event)
            drawingThread.run()
        }
        if (wiringEnabled) {
            if (event.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_UP) {
                wireState = game.handleWireEvent(event, wireState)
                drawingThread.run()
            }
            if (wireState.secondRectFound) {
                drawingThread.run()
                wireState = WireState()
            }
        }
        if (deleteEnabled) {
            if (event.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_UP) {
                game.handleDeleteEvent(event)
                drawingThread.run()
            }
        }
        if (inversionEnabled) {
            if (event.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_UP) {
                game.handleInversionEvent(event)
                drawingThread.run()
            }
        }
        if (movingEnabled) {
            gestureDetector.onTouchEvent(event)
            drawingThread.run()
        }
        return true
    }

    fun enableDrawing() {
        disableWiring()
        disableDeleting()
        disableInversion()
        disableMoving()
        drawingEnabled = true
    }

    fun enableWiring() {
        disableDrawing()
        disableDeleting()
        disableInversion()
        disableMoving()
        wiringEnabled = true
    }

    fun enableDeleting() {
        disableDrawing()
        disableWiring()
        disableInversion()
        disableMoving()
        deleteEnabled = true
    }

    fun enableInversion() {
        disableDrawing()
        disableWiring()
        disableDeleting()
        disableMoving()
        inversionEnabled = true
    }

    fun enableMoving() {
        disableDrawing()
        disableWiring()
        disableDeleting()
        disableInversion()
        movingEnabled = true
    }

    private fun disableDrawing() {
        drawingEnabled = false
    }

    private fun disableWiring() {
        wiringEnabled = false
    }

    private fun disableDeleting() {
        deleteEnabled = false
    }

    private fun disableInversion() {
        inversionEnabled = false
    }

    private fun disableMoving() {
        movingEnabled = false
    }

    private inner class MyGestureListener: GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            drawingThread.scrollCoordinates = distanceX to distanceY
            scrollBy(distanceX.toInt(), distanceY.toInt())
            return true
        }
    }
}