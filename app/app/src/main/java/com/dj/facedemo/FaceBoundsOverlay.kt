package com.dj.facedemo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * Create by ChenLei on 2021/7/13
 * Describe:
 */
class FaceBoundsOverlay constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val faceBoundPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.RED
        strokeWidth = 10f
    }

    private val descPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.RED
        strokeWidth = 3f
        textSize = 40f
    }

    var faceBound: List<RectF> = ArrayList()
        set(value) {
            field = value
            invalidate()
        }
    var descriptions: List<String> = ArrayList()
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        for (index in faceBound.indices) {
            val f = faceBound[index]
            canvas?.drawRect(f, faceBoundPaint)
            val desc = descriptions.getOrNull(index) ?: return
            canvas?.drawText(desc, f.right, f.bottom + 50, descPaint)
        }
    }
}