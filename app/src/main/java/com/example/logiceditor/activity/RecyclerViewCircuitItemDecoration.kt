package com.example.logiceditor.activity

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class RecyclerViewCircuitItemDecoration(private val spaceInPixel: Int): RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.left = spaceInPixel
        outRect.right = spaceInPixel
        outRect.bottom = spaceInPixel
        outRect.top = spaceInPixel
    }
}