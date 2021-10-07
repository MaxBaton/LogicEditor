package com.example.logiceditor.tools.elements

import android.graphics.*
import java.io.Serializable

class NOT(override val image: Int, override var coordinates: Pair<Float, Float> = 200f to 200f):
        Tool(image, coordinates), LogicCalculation, Serializable {
   @Transient private var input: Rect? = null
   @Transient private var output: Rect? = null

    init {
        ioBoxes = arrayOfNulls(2)
        sizes = 350 to 150
    }

    //this method calls the parent draw method and updates the port hitboxes
    override fun drawTool(canvas: Canvas, paint: Paint?, image: Int) {
        super.drawTool(canvas, paint, this.image)

        updatePorts(paint!!)
        ioBoxes!!.forEachIndexed { index, it ->
            paint.color = if (isClickPort.first && isClickPort.second == index) Color.RED else Color.GREEN
            canvas.drawRect(it!!.first!!, paint)
        }
    }

    //updates location of port hit boxes on the tool
    private fun updatePorts(paint: Paint) {
        paint.apply {
            style = Paint.Style.STROKE
            strokeWidth = 3.5f
        }

        output = Rect(coordinates.first.toInt() + 125, coordinates.second.toInt() - 40,
                coordinates.first.toInt() + 125 + 50, coordinates.second.toInt() - 40 + 60)
        input = Rect(coordinates.first.toInt() - 165, coordinates.second.toInt() - 40,
                coordinates.first.toInt() - 165 + 50, coordinates.second.toInt() - 40 + 60)

        ioBoxes!![0] = output!! to null
        ioBoxes!![1] = input!! to null
    }

    override fun calculate(input1: Boolean, input2: Boolean) = !input1
}