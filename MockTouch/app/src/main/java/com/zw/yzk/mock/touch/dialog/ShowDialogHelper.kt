package com.zw.yzk.mock.touch.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog

@SuppressLint("StaticFieldLeak")
object ShowDialogHelper {

    // 需要始终保持在最顶层显示的悬浮窗
    private var topFloatingView: View? = null
    private var placeHolder: View? = null
    private lateinit var windowManager: WindowManager

    fun setTopFloatingView(view: View, holder: View) {
        topFloatingView = view
        placeHolder = holder
    }

    fun removeTopFloatingView() {
        removePlaceHolder()
        removeFloating()
    }

    fun showDialog(dialog: AppCompatDialog) {
        showUnderFloating(dialog.context) {
            dialog.show()
        }
    }

    fun showUnderFloating(context: Context, show: () -> Unit) {
        if (!this::windowManager.isInitialized) {
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        }
        show()
        showPlaceHolder()
    }

    private fun showPlaceHolder() {
        val floating = topFloatingView
        val holder = placeHolder
        if (floating != null && holder != null) {
            windowManager.addView(holder, floating.layoutParams)
            doOnViewShow(holder) { showTopFloatingView() }
        }
    }

    private fun doOnViewShow(view: View, block: () -> Unit) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                view.post { block() }
            }
        })
    }

    private fun removePlaceHolder() {
        placeHolder?.let {
            try {
                windowManager.removeViewImmediate(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun removeFloating() {
        topFloatingView?.let {
            try {
                windowManager.removeViewImmediate(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showTopFloatingView() {
        topFloatingView?.let {
            removeFloating()
            windowManager.addView(it, it.layoutParams)
            doOnViewShow(it) {
                removePlaceHolder()
            }
        }
    }

}