package com.example.logiceditor.tools.elements

import android.graphics.*
import com.example.logiceditor.R
import java.io.Serializable

class LED(override var image: Int, override var coordinates: Pair<Float, Float> = 200f to 200f):
        Tool(image, coordinates), Serializable {
    @Transient private var input: Rect? = null

    init {
        ioBoxes = arrayOfNulls(1)
        sizes = 131 to 220
    }

    override fun drawTool(canvas: Canvas, paint: Paint?, image: Int) {
        super.drawTool(canvas, paint, this.image)

        updatePorts(paint!!)
        canvas.drawRect(ioBoxes!![0]!!.first!!, paint)
    }

    private fun updatePorts(paint: Paint) {
        paint.apply {
            color = if (isClickPort.first && isClickPort.second != -1) Color.RED else Color.GREEN
            style = Paint.Style.STROKE
            strokeWidth = 3.5f
        }

        input = Rect(coordinates.first.toInt() - 30, coordinates.second.toInt() + 55,
            coordinates.first.toInt() - 30 + 60, coordinates.second.toInt() + 55 + 60)
        ioBoxes!![0] = input to null
    }

    fun getResult(res: Boolean) {
        image = if (res) R.drawable.light_element_in else R.drawable.light_element
    }
}