package com.example.logiceditor.tools.elements

import android.graphics.*
import com.example.logiceditor.R
import java.io.Serializable

class OR(override var image: Int, override var coordinates: Pair<Float, Float> = 200f to 200f):
        Tool(image, coordinates), LogicCalculation,  Serializable {
    @Transient private var inputX: Rect? = null
    @Transient private  var inputY: Rect? = null
    @Transient private var output: Rect? = null
    private var isInversion = false

    init {
        ioBoxes = arrayOfNulls(3)
        sizes = 350 to 180
    }

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

        output = Rect(coordinates.first.toInt() + 120, coordinates.second.toInt() - 40,
            coordinates.first.toInt() + 120 + 50, coordinates.second.toInt() - 40 + 60)
        inputY = Rect(coordinates.first.toInt() - 170, coordinates.second.toInt() + 10,
            coordinates.first.toInt() - 170 + 50, coordinates.second.toInt() + 10 + 60)
        inputX = Rect(coordinates.first.toInt() - 170, coordinates.second.toInt() - 80,
            coordinates.first.toInt() - 170 + 50, coordinates.second.toInt() - 80 + 60)

        ioBoxes!![0] = output to null
        ioBoxes!![1] = inputY to null
        ioBoxes!![2] = inputX to null
    }

    override fun calculate(input1: Boolean, input2: Boolean) = if (!isInversion) (input1 or input2) else !(input1 or input2)

    fun invert() {
        isInversion = !isInversion
        changeImage()
    }

    private fun changeImage() {
        image = if (!isInversion) R.drawable.or_element else R.drawable.or_element_not
    }
}