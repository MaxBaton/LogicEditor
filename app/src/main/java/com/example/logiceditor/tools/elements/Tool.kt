package com.example.logiceditor.tools.elements

import android.graphics.*
import com.example.logiceditor.activity.MainActivity
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

open class Tool(open val image: Int, open var coordinates: Pair<Float, Float>): Serializable {
    @Transient var bitmap: Bitmap? = null
    @Transient open var ioBoxes: Array<Pair<Rect?, Boolean?>?>? = null
    open var isClickPort = false to -1
    protected open var sizes: Pair<Int, Int> = 350 to 180

    // draws tool in the center of the touch location
    open fun drawTool(canvas: Canvas, paint: Paint?, image: Int) {
        val temp = BitmapFactory.decodeResource(MainActivity._resources, image)
        bitmap = Bitmap.createScaledBitmap(temp, sizes.first, sizes.second, true)

        val left = coordinates.first - bitmap!!.width / 2
        val top = coordinates.second - bitmap!!.height / 2
        canvas.drawBitmap(bitmap!!, left, top, null)
    }

    open fun checkHitBoxes(touchedX: Int, touchedY: Int): Int? {
        ioBoxes!!.forEachIndexed { index, rect ->
            if (rect!!.first!!.contains(touchedX, touchedY)) {
                //println("hitbox hit")
                return index
            }
        }
        return null
    }

    private fun writeObject(oos: ObjectOutputStream) {
        oos.defaultWriteObject()

        if (bitmap != null){
            val byteStream = ByteArrayOutputStream()
            val success = bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, byteStream)
            if (success) {
                oos.writeObject(byteStream.toByteArray())
            }
        }

        val listHitboxes = mutableListOf<Pair<IntArray, Boolean?>>()

        if (ioBoxes != null) {
            ioBoxes!!.forEach {
                val array = intArrayOf(it!!.first!!.left, it.first!!.top,
                                    it.first!!.right, it.first!!.bottom)
                listHitboxes.add(array to it.second)
            }
            oos.writeObject(listHitboxes.toTypedArray())
        }
    }

    private fun readObject(ois: ObjectInputStream) {
        ois.defaultReadObject()

        val image = ois.readObject() as ByteArray
        if (image != null && image.isNotEmpty()){
            bitmap = BitmapFactory.decodeByteArray(image, 0, image.size)
        }

        val _hitboxes = ois.readObject() as Array<Pair<IntArray, Boolean?>>?

        if (_hitboxes != null) {
            ioBoxes = arrayOfNulls(_hitboxes.size)
            _hitboxes.forEachIndexed { index, it ->
                val rect = Rect(it.first[0], it.first[1], it.first[2], it.first[3])
                ioBoxes!![index] = rect to it.second
            }
        }
    }
}