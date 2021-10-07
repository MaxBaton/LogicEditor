package com.example.logiceditor.game

import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent
import com.example.logiceditor.tools.elements.Tool
import com.example.logiceditor.tools.elements.*
import com.example.logiceditor.tools.wire.Wire
import com.example.logiceditor.tools.wire.WireState
import java.util.*
import kotlin.collections.LinkedHashSet
import kotlin.math.abs

class CurrentGame {
    var usedTools = mutableListOf<Tool?>()
    var savedWire = mutableListOf<Wire>()
    val paint = Paint()
    private var currentTool: Tool? = null
    private var currentWire: Wire? = null
    private var isToolSelected = false
    private var isWireSelected = false
    private lateinit var firstToolCountWire: Pair<Tool, Int>
    var isSimulate = false
    var isDelete = false
    private var isInversion = false
    var sketcherRightBottom: Pair<Float, Float>? = null


    fun draw(canvas: Canvas?) {
        if (usedTools.size > 0) {
            drawTools(canvas)
            drawWires(canvas)
        }
    }

    private fun drawTools(canvas: Canvas?) {
        var deleteIndex = -1
        usedTools.forEachIndexed { index, it ->
            if (it == currentTool && isDelete) {
                deleteIndex = index
            }else {
                it!!.drawTool(canvas!!, paint, it.image)
            }
        }

        if (deleteIndex != -1) {
            val wires = getToolWires(usedTools[deleteIndex]!!)
            usedTools.removeAt(deleteIndex)

            if (wires.isNotEmpty()) {
                wires.forEach {
                    savedWire.remove(it)
                }
            }
        }
    }

    private fun drawWires(canvas: Canvas?) {
        if (savedWire.size > 0) {
            var deleteIndex = -1

            savedWire.forEachIndexed { index, it ->
                if (it == currentWire && isDelete) {
                    deleteIndex = index
                }else {
                    it.drawLine(canvas!!, paint)
                }
            }

            if (deleteIndex != -1) {
                savedWire.removeAt(deleteIndex)
            }
        }
    }

    fun addNOT(image: Int) {
        currentTool = NOT(image, getCoordinates())
        usedTools.add(currentTool)
    }

    fun addAND(image: Int) {
        currentTool = AND(image, getCoordinates())
        usedTools.add(currentTool)
    }

    fun addOR(image: Int) {
        currentTool = OR(image, getCoordinates())
        usedTools.add(currentTool)
    }

    fun addXOR(image: Int) {
        currentTool = XOR(image, getCoordinates())
        usedTools.add(currentTool)
    }

    fun addButton(image: Int) {
        currentTool = Button(image, getCoordinates())
        usedTools.add(currentTool)
    }

    fun addLED(image: Int) {
        currentTool = LED(image, getCoordinates())
        usedTools.add(currentTool)
    }

    private fun getCoordinates(): Pair<Float, Float> {
        if (usedTools.size == 0) return 300f to 300f

        val previous = usedTools.elementAt(usedTools.size - 1)!!

        val previousRight = previous.coordinates.first + previous.bitmap!!.width/2
        val previousBottom = previous.coordinates.second + previous.bitmap!!.height/2

        val x = if ((previousRight) < (0.7*sketcherRightBottom!!.first)) (previous.coordinates.first + 100f) else
                                                                                (previous.coordinates.first - 100f)
        val y = if ((previousBottom) < (0.7*sketcherRightBottom!!.second)) (previous.coordinates.second + 100f) else
                                                                                (previous.coordinates.second - 100f)

        return x to y
    }

    //left off trying to figure out how to draw wiring
    fun handleWireEvent(me: MotionEvent, wireState: WireState): WireState {
        isDelete = false
        isInversion = false
        val touchedY = me.y.toInt()
        val touchedX = me.x.toInt()
        if (wireState.firstRectFound) {
            usedTools.forEach {
                val hitbox = it!!.checkHitBoxes(touchedX, touchedY)
                if (hitbox != null) {
                    if ((firstToolCountWire.second != 0 && hitbox != 0) ||
                            ((firstToolCountWire.first is LED || it is LED) && (firstToolCountWire.second != 0
                                    || hitbox != 0)) ||
                            ((firstToolCountWire.first is Button || it is Button) && (firstToolCountWire.second == 0
                                    && hitbox == 0)
                                    && (firstToolCountWire.first !is LED && it !is LED)) ||
                            ((firstToolCountWire.first !is LED && it !is LED) && (firstToolCountWire.second == 0
                                    && hitbox == 0)) ||
                            (isWireContains(firstToolCountWire.first to firstToolCountWire.second, it to hitbox))) {
                        firstToolCountWire.first.isClickPort = false to -1
                        wireState.firstRectFound = false
                        return wireState
                    }

                    val countHitbox = hitbox

                    savedWire.add(Wire((firstToolCountWire to (it to countHitbox))))

                    //second wire found
                    wireState.secondRectFound = true
                    wireState.firstRectFound = false

                    firstToolCountWire.first.isClickPort = false to -1
                    return wireState
                }
            }
            //second wire not found
        }else {
             usedTools.forEach {
                if (it!!.checkHitBoxes(touchedX, touchedY) != null) {
                    val count = it.checkHitBoxes(touchedX, touchedY)!!

                    firstToolCountWire = (it to count)

                    it.isClickPort = true to count
                    wireState.firstRectFound = true
                    return wireState
                }
            }
        }
        return wireState
    }

    fun handleMoveEvent(me: MotionEvent) {
        isDelete = false
        isInversion = false
        try {
            Thread.sleep(50)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        val action = me.action
        if (!isToolSelected) {
            currentTool = getCurrentTool(me.x to me.y)
        }
        if (currentTool != null) {
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    if (!isSimulate) {
                        currentTool!!.coordinates = checkBoundariesX(me.x) to checkBoundariesY(me.y)
                    }else if (isSimulate && (currentTool is Button)) {
                        (currentTool as Button).toggle()
                        simulate()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (!isSimulate){
                        currentTool!!.coordinates = checkBoundariesX(me.x) to checkBoundariesY(me.y)
                    }
                    isToolSelected = false
                    currentTool = null
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!isSimulate) {
                        currentTool!!.coordinates = checkBoundariesX(me.x) to checkBoundariesY(me.y)
                    }
                }
            }
        }else {
            isToolSelected = false
        }
    }


    fun handleDeleteEvent(me: MotionEvent) {
        isInversion = false
        isDelete = true
        val coordinates = me.x to me.y
        if (!isWireSelected && currentTool == null) {
            currentWire = getCurrentWire(coordinates)
        }
        if (!isToolSelected && currentWire == null) {
            currentTool = getCurrentTool(coordinates)
        }

        isWireSelected = false
        isToolSelected = false
    }

    fun handleInversionEvent(me: MotionEvent) {
        isDelete = false
        isInversion = true
        val coordinates = me.x to me.y
        if (!isToolSelected) {
            currentTool = getCurrentTool(coordinates)
        }

        if (currentTool != null) {
            if (currentTool!!.ioBoxes!!.size == 3) invertTool(currentTool!!)
        }

        isToolSelected = false
    }

    private fun checkBoundariesY(coordinateY: Float) = when {
        coordinateY < 70f -> 100f
        coordinateY > (sketcherRightBottom!!.second - 340f) -> (sketcherRightBottom!!.second - 360f)
        else -> coordinateY
    }

    private fun checkBoundariesX(coordinateX: Float) = when {
        coordinateX < 70f -> 100f
        coordinateX > (sketcherRightBottom!!.first - 470f) -> (sketcherRightBottom!!.first - 520f)
        else -> coordinateX
    }

    private fun invertTool(tool: Tool) {
        when(tool) {
            is AND -> tool.invert()
            is OR  -> tool.invert()
            is XOR -> tool.invert()
        }
    }

    private fun isWireContains(firstTool: Pair<Tool, Int>, secondTool: Pair<Tool, Int>): Boolean {
        savedWire.forEach {
            if ((it.tools.first.first == firstTool.first || it.tools.first.first == secondTool.first) ||
                 (it.tools.second.first == firstTool.first || it.tools.second.first == secondTool.first)) {
                    val toolWire = if ((it.tools.first.first == firstTool.first || it.tools.first.first ==
                                secondTool.first) && it.tools.first.second != 0)
                                    it.tools.first else it.tools.second
                    val tool = if (toolWire.first == firstTool.first) firstTool else secondTool
                    if ((toolWire.second == tool.second) && (toolWire.first == tool.first)) return true
            }
        }

        return false
    }

    private fun getCurrentTool(coordinates: Pair<Float, Float>): Tool? {
        usedTools.forEach {
            val left = it!!.coordinates.first - it.bitmap!!.width / 2
            val top  = it.coordinates.second - it.bitmap!!.height / 2
            val right = left + it.bitmap!!.width
            val bottom = top + it.bitmap!!.height


            if ((coordinates.first > left && coordinates.first < right) && (coordinates.second > top &&
                        coordinates.second < bottom)) {
                isToolSelected = true
                return it
            }
        }

        return null
    }

    private fun getCurrentWire(coordinates: Pair<Float, Float>): Wire? {
        savedWire.forEach {
            it.linesWire.forEach { line ->
                if (line.startY == line.endY) {
                    if (abs(coordinates.second - line.startY) < 15 &&
                            ((coordinates.first >= line.startX && coordinates.first <= line.endX) ||
                              (coordinates.first <= line.startX && coordinates.first >= line.endX))) {
                                isWireSelected = true
                                return it
                    }
                }else {
                    if (abs(coordinates.first - line.startX) < 15 &&
                            ((coordinates.second >= line.startY && coordinates.second <= line.endY) ||
                             (coordinates.second <= line.startY && coordinates.second >= line.endY) )) {
                                isWireSelected = true
                                return it
                    }
                }
            }
        }
        return null
    }

    fun checkConnections(): Boolean {
        var inputs = 0

        usedTools.forEach {
            if (it is AND || it is OR || it is XOR) {
                inputs += 2
            }else if (it is NOT || it is LED) inputs++
        }

        return inputs == savedWire.size
    }

    fun checkOutputs(): Boolean {
        val duplicateTools = usedTools.map { it }.toMutableList()
        var outputs = 0

        usedTools.forEach { tool ->
            if (tool !is LED) {
                savedWire.forEach {
                    if (((it.tools.first.first == tool && it.tools.first.second == 0) || (it.tools.second.first ==
                                tool && it.tools.second.second == 0)) &&
                            duplicateTools.contains(tool)) {
                        outputs++
                        duplicateTools.remove(tool)
                    }
                }
            }else {
                duplicateTools.remove(tool)
            }
        }

        return outputs == (usedTools.size - 1)
    }

    fun isLEDOne(): Boolean {
        var ledCount = 0
        usedTools.forEach {
            if (it is LED) {
                ledCount++
            }
        }

        return ledCount == 1
    }

    fun simulate() {
        usedTools = sortedTools().toMutableList()
        usedTools.reverse()
        usedTools = correctOrder(usedTools)

        var indexLED = -1

        for (i in usedTools.indices) {
            val tool = usedTools[i]

            if (tool is Button) {
                tool.ioBoxes!![0] = tool.ioBoxes!![0]!!.first to tool.state
            }else {
                var index: Int
                for (j in savedWire.indices) {
                    index = j
                    val wire = savedWire[j]
                    if ((wire.tools.second.first !is LED) && (wire.tools.first.first !is LED)) {
                        if ((wire.tools.second.first == tool) || (wire.tools.first.first == tool)) {
                            val (pairTool, _) = if (wire.tools.second.first == tool) wire.tools.first else
                                                                                                    wire.tools.second
                            val (currentTool, countHitboxCurrent) = if (wire.tools.second.first == tool)
                                                                                wire.tools.second else wire.tools.first
                            if (pairTool.ioBoxes!![0]!!.second != null) {
                                tool.ioBoxes!![countHitboxCurrent] =
                                        tool.ioBoxes!![countHitboxCurrent]!!.first to
                                                pairTool.ioBoxes!![0]!!.second

                                if (!isHitboxesNull(tool)!!) {
                                    val res = getRes(currentTool)

                                    tool.ioBoxes!![0] =
                                            tool.ioBoxes!![countHitboxCurrent]!!.first  to res!!
                                }
                            }
                        }
                    }else {
                        indexLED = index
                    }
                }
            }
        }

        val led = if (savedWire[indexLED].tools.second.first is LED) (savedWire[indexLED].tools.second.first as LED)
                else (savedWire[indexLED].tools.first.first as LED)
        val pairTool = if(savedWire[indexLED].tools.second.first == led) savedWire[indexLED].tools.first.first else
            savedWire[indexLED].tools.second.first
        led.ioBoxes!![0] = led.ioBoxes!![0]!!.first to pairTool.ioBoxes!![0]!!.second
        led.getResult(led.ioBoxes!![0]!!.second!!)
    }

    private fun isHitboxesNull(tool: Tool) =
        when (tool.ioBoxes!!.size) {
            3 -> {
                val input1 = tool.ioBoxes!![1]!!.second
                val input2 = tool.ioBoxes!![2]!!.second
                input1 == null || input2 == null
            }
            2 -> {
                val input1 = tool.ioBoxes!![1]!!.second
                input1 == null
            }
            else -> null
        }

    private fun getRes(tool: Tool): Boolean? {
        when (tool) {
            is Button -> return tool.state
            is AND -> {
                val input1 = tool.ioBoxes!![1]!!.second!!
                val input2 = tool.ioBoxes!![2]!!.second!!
                return tool.calculate(input1, input2)
            }
            is OR -> {
                val input1 = tool.ioBoxes!![1]!!.second!!
                val input2 = tool.ioBoxes!![2]!!.second!!
                return tool.calculate(input1, input2)
            }
            is NOT -> {
                val input = tool.ioBoxes!![1]!!.second!!
                return tool.calculate(input)
            }
            is XOR -> {
                val input1 = tool.ioBoxes!![1]!!.second!!
                val input2 = tool.ioBoxes!![2]!!.second!!
                return tool.calculate(input1, input2)
            }
            else -> return null
        }
    }

    private fun sortedTools(): MutableList<Tool> {
        val set: LinkedHashSet<Tool> = mutableSetOf<Tool>() as LinkedHashSet<Tool>
        val additionalSet: LinkedHashSet<Tool> = mutableSetOf<Tool>() as LinkedHashSet<Tool>
        var wireLED: Wire? = null
        var tool: Tool? = null

        while (set.size < usedTools.size) {
            if (additionalSet.isNotEmpty()) {
                tool = additionalSet.elementAt(0)
                additionalSet.remove(additionalSet.elementAt(0))
            }

            savedWire.forEachIndexed  { index, it ->
                if (wireLED != null) {
                    if ((it.tools.second.first == tool) || (it.tools.first.first == tool) &&
                        (!set.contains(it.tools.first.first) && !set.contains(it.tools.second.first))) {
                        set.add(tool!!)

                        if (tool!!.ioBoxes!!.size == 3) {
                            val set2 = getAdditionalList(tool!!)
                            set2.forEach { additionalSet.add(it) }
                        }else if (tool!!.ioBoxes!!.size == 2) {
                            val pairTool = if (it.tools.second.first == tool) it.tools.first.first else
                                                                                            it.tools.second.first

                            if (pairTool.ioBoxes!!.size > 1) {
                                if (additionalSet.isEmpty()) {
                                    tool = pairTool
                                }else {
                                    additionalSet.add(tool!!)
                                }
                            }else {
                                set.add(pairTool)
                            }
                        }
                    }else if (tool!!.ioBoxes!!.size == 1) {
                        set.add(tool!!)
                        return@forEachIndexed
                    }
                }

                if (((it.tools.second.first is LED) || (it.tools.first.first is LED)) && (set.size == 0)) {
                    val led = if (it.tools.second.first is LED) it.tools.second.first else it.tools.first.first
                    val pairTool = if (it.tools.second.first == led) it.tools.first.first else it.tools.second.first

                    if (pairTool !is Button) {
                        wireLED = it
                        tool = pairTool
                        set.add(led)
                    }else {
                        set.add(led)
                        set.add(pairTool)
                        return set.toMutableList()
                    }
                }
            }
        }

        return set.toMutableList()
    }

    private fun correctOrder(list: MutableList<Tool?>): MutableList<Tool?> {
        return if (list[0] is Button) {
            list
        }else {
            list.forEachIndexed { index, tool ->
                if (tool is Button) {
                    for (i in index downTo 1) {
                        val additionalTool = list[i]
                        list[i] = list[i-1]
                        list[i-1] = additionalTool
                    }
                    return@forEachIndexed
                }
            }
            list
        }
    }

    private fun getAdditionalList(tool: Tool): LinkedHashSet<Tool> {
        val list = mutableSetOf<Tool>() as LinkedHashSet<Tool>

        savedWire.forEach {
            if ((it.tools.second.first == tool) || (it.tools.first.first == tool)) {
                val pairTool = if (it.tools.second.first == tool) it.tools.first.first else it.tools.second.first
                list.add(pairTool)
            }
        }

        return list
    }

    private fun getToolWires(tool: Tool): List<Wire> {
        val list = mutableListOf<Wire>()
        savedWire.forEach {
            if (it.tools.first.first == tool || it.tools.second.first == tool) list.add(it)
        }
        return list
    }
}