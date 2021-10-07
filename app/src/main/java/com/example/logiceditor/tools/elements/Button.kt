package com.example.logiceditor.tools.elements

import android.graphics.*
import com.example.logiceditor.R
import java.io.Serializable

class Button(override var image: Int, override var coordinates: Pair<Float, Float> = 200f to 200f):
                Tool(image, coordinates), Serializable {
    var state = false
    @Transient private var output: Rect? = null

    init {
        ioBoxes = arrayOfNulls(1)
        sizes = 135 to 70
    }

    override fun drawTool(canvas: Canvas, paint: Paint?, image: Int) {
        super.drawTool(canvas, paint, this.image)

        updatePorts(paint!!)
        canvas.drawRect(output!!, paint)
    }

    private fun updatePorts(paint: Paint) {
        paint.apply {
            color = if (isClickPort.first && isClickPort.second != -1) Color.RED else Color.GREEN
            style = Paint.Style.STROKE
            strokeWidth = 3.5f
        }

        output = Rect(coordinates.first.toInt() + 50, coordinates.second.toInt() - 30,
            coordinates.first.toInt() + 50 + 50, coordinates.second.toInt() - 30 + 60)
        ioBoxes!![0] = output to null
    }

    fun toggle() {
        state = !state
        changeImage()
    }

    private fun changeImage() {
        image = if (state) R.drawable.button_in_element else R.drawable.button_off_element
    }
}