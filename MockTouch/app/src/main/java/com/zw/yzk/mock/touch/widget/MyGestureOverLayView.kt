package com.zw.yzk.mock.touch.widget

import android.content.Context
import android.gesture.GestureOverlayView
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet

class MyGestureOverLayView : GestureOverlayView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context, attrs, defStyle
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = -0x100
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 12.0f
        isDither = true
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        drawTouchDownPoint(canvas)
    }

    private fun drawTouchDownPoint(canvas: Canvas) {
        if (currentStroke.size != 1) {
            return
        }
        val stroke = currentStroke[0]
        canvas.drawPoint(stroke.x, stroke.y, paint)
    }
}