package com.example.logiceditor.tools.wire

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.logiceditor.tools.elements.LED
import com.example.logiceditor.tools.elements.Tool
import java.io.Serializable
import kotlin.math.abs

class Wire(var tools: Pair<Pair<Tool, Int>, Pair<Tool, Int>>): Serializable {

    @Transient var linesWire = mutableListOf<Line>()

    fun drawLine(canvas: Canvas, paint: Paint) {
        paint.apply {
            color = Color.BLACK
            strokeWidth = 7.5f
        }

        val startX = tools.first.first.ioBoxes!![tools.first.second]!!.first!!.centerX().toFloat()
        val startY = tools.first.first.ioBoxes!![tools.first.second]!!.first!!.centerY().toFloat()
        val endX   = tools.second.first.ioBoxes!![tools.second.second]!!.first!!.centerX().toFloat()
        val endY   = tools.second.first.ioBoxes!![tools.second.second]!!.first!!.centerY().toFloat()
        val line = Line(startX, startY, endX, endY)
        val lines = getLines(line)
        if (lines.isEmpty()) {
            linesWire.add(line)
            canvas.drawLine(line.startX, line.startY, line.endX, line.endY, paint)
        }else {
            lines.forEach {
                canvas.drawLine(it.startX, it.startY, it.endX, it.endY, paint)
            }
            linesWire = lines.toMutableList()
        }
    }

    private fun getLines(startLine: Line): List<Line> {
        val list: MutableList<Line> = mutableListOf()
        val isWrongOrder = (tools.first.second != 0) || tools.first.first is LED

        if (startLine.startY == startLine.endY && startLine.startX < startLine.endX) return list

        if (startLine.startX < startLine.endX && isWrongOrder) {
            //5 линиий
            val startToolLeft = tools.first.first.coordinates.first - tools.first.first.bitmap!!.width/2
            val endToolTop = tools.second.first.coordinates.second - tools.second.first.bitmap!!.height / 2
            val endToolBottom = tools.second.first.coordinates.second + tools.second.first.bitmap!!.height / 2
            val endX1 = startToolLeft - 0.1*(startLine.endX - startLine.startX)
            val line1 = Line(startLine.startX, startLine.startY, endX1.toFloat(), startLine.startY)
            val endY2 =  if (startLine.startY > endToolBottom) {
                (endToolBottom + 0.3*(startLine.startY - endToolBottom))
            } else {
                (endToolTop - 0.3*(startLine.endY - endToolTop))
            }
            val line2 = Line(endX1.toFloat(), startLine.startY, endX1.toFloat(), endY2.toFloat())
            val endX3 = startLine.endX - 0.3*(startLine.startX - startLine.endX)
            val line3 = Line(endX1.toFloat(), endY2.toFloat(), endX3.toFloat(), endY2.toFloat())
            val line4 = Line(endX3.toFloat(), endY2.toFloat(), endX3.toFloat(), startLine.endY)
            val line5 = Line(endX3.toFloat(), startLine.endY, startLine.endX, startLine.endY)

            list.add(line1)
            list.add(line2)
            list.add(line3)
            list.add(line4)
            list.add(line5)
        }else if (startLine.startX < startLine.endX || isWrongOrder) {
            //3 линии
            val endX1 = startLine.startX + 0.3*(startLine.endX - startLine.startX)
            val line1 = Line(startLine.startX, startLine.startY, endX1.toFloat(), startLine.startY)
            val line2 = Line(endX1.toFloat(), startLine.startY, endX1.toFloat(), startLine.endY)
            val line3 = Line(endX1.toFloat(), startLine.endY, startLine.endX, startLine.endY)

            list.add(line1)
            list.add(line2)
            list.add(line3)
        }else {
            //5 линиий
            val (toolSecondTop, toolFirstBottom) = if (!isWrongOrder) {
                tools.second.first.coordinates.second - tools.second.first.bitmap!!.height / 2 to  tools.first.first.coordinates.second + tools.first.first.bitmap!!.height / 2
            }else {
                tools.first.first.coordinates.second - tools.first.first.bitmap!!.height / 2 to tools.second.first.coordinates.second - tools.second.first.bitmap!!.height / 2
            }

            val (toolSecondBotom, toolFirstTop) = if (!isWrongOrder) {
                tools.second.first.coordinates.second + tools.second.first.bitmap!!.height / 2 to  tools.first.first.coordinates.second - tools.first.first.bitmap!!.height / 2
            }else {
                tools.first.first.coordinates.second + tools.first.first.bitmap!!.height / 2 to tools.second.first.coordinates.second - tools.second.first.bitmap!!.height / 2
            }
            val toolSecondLeft = if (!isWrongOrder) tools.second.first.coordinates.first - tools.second.first.bitmap!!.width/2
                                                else tools.first.first.coordinates.first - tools.first.first.bitmap!!.width/2

            val endX1 = startLine.startX + 0.3*(abs(startLine.startX - startLine.endX))
            val line1 = Line(startLine.startX, startLine.startY, endX1.toFloat(), startLine.startY)
            val endY2 = if (startLine.endY > startLine.startY) {
                (toolFirstBottom + 0.3*(abs(toolSecondTop - toolFirstBottom)))
            }else {
                (toolFirstTop - 0.3*(abs(toolFirstTop - toolSecondBotom)))
            }
            val line2 = Line(endX1.toFloat(), startLine.startY, endX1.toFloat(), endY2.toFloat())
            val endX3 = toolSecondLeft - 0.3*(abs(startLine.startX - startLine.endX))
            val line3 = Line(endX1.toFloat(), endY2.toFloat(), endX3.toFloat(), endY2.toFloat())
            val line4 = Line(endX3.toFloat(), endY2.toFloat(), endX3.toFloat(), startLine.endY)
            val line5 = Line(endX3.toFloat(), startLine.endY, startLine.endX, startLine.endY)

            list.add(line1)
            list.add(line2)
            list.add(line3)
            list.add(line4)
            list.add(line5)
        }

        return list.toList()
    }
}