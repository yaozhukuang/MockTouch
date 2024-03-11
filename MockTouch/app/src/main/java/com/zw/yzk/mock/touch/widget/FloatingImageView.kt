package com.zw.yzk.mock.touch.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.abs

class FloatingImageView : AppCompatImageView {

    private var x = 0
    private var y = 0
    private var isMoving = false
    private var block: ((dx: Int, dy: Int, isMoving: Boolean) -> Unit)? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context, attrs, defStyle
    )

    fun setMotionListener(block: (dx: Int, dy: Int, isMoving: Boolean) -> Unit) {
        this.block = block
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                x = event.rawX.toInt()
                y = event.rawY.toInt()
                isMoving = false
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val nowX = event.rawX.toInt()
                val nowY = event.rawY.toInt()
                val moveX = nowX - x
                val moveY = nowY - y
                if (abs(moveX) > 0 || abs(moveY) > 0) {
                    isMoving = true
                    block?.invoke(moveX, moveY, true)
                    x = nowX
                    y = nowY
                    return true
                }
            }

            MotionEvent.ACTION_UP -> if (!isMoving) {
                block?.invoke(0, 0, false)
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}