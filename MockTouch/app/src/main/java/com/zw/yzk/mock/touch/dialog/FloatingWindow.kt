package com.zw.yzk.mock.touch.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Outline
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewOutlineProvider
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.zw.yzk.mock.touch.R
import com.zw.yzk.mock.touch.data.GestureLiveData
import com.zw.yzk.mock.touch.databinding.DialogMenuBinding
import com.zw.yzk.mock.touch.dialog.addTask.AddTaskDialogHelper
import com.zw.yzk.mock.touch.dialog.startTask.StartTaskDialogHelper
import com.zw.yzk.mock.touch.extention.dpInt
import com.zw.yzk.mock.touch.extention.screenWidth
import com.zw.yzk.mock.touch.service.GestureAccessibilityService
import com.zw.yzk.mock.touch.utils.DebounceClickListener
import com.zw.yzk.mock.touch.widget.FloatingImageView


class FloatingWindow private constructor() {

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            FloatingWindow()
        }
    }

    private val gestureDialog = AddGestureDialogHelper()
    private val taskDialog = AddTaskDialogHelper()
    private val startDialog = StartTaskDialogHelper()
    private lateinit var mainDialog: AlertDialog
    private var isFloating = false

    fun showFloatingWindow(context: Context) {
        isFloating = true
        val size = 80.dpInt()
        val params = newWmParams(size, size).apply {
            x = context.screenWidth() - size
            y = 0
        }
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val floatingView: FloatingImageView = createFloatingView(context)
        floatingView.setMotionListener { dx, dy, isMoving ->
            if (isMoving) {
                params.x += dx
                params.y += dy
                windowManager.updateViewLayout(floatingView, params)
            } else {
                if (GestureLiveData.instance.isRecording()) {
                    // 如果此时正在录制操作，点击悬浮框就结束录制
                    GestureLiveData.instance.stopRecording()
                    RecorderTaskDialog().showDialog(context)
                } else {
                    // 显示对话框
                    showDialog(context)
                }
            }
        }
        windowManager.addView(floatingView, params)
        ShowDialogHelper.setTopFloatingView(floatingView, createFloatingView(context))
    }

    private fun quit() {
        isFloating = false
        ShowDialogHelper.removeTopFloatingView()
        GestureLiveData.instance.setState(GestureLiveData.EXECUTE_STATE_PAUSE)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createFloatingView(context: Context): FloatingImageView {
        val floating = FloatingImageView(context)
        floating.setImageResource(R.mipmap.ic_launcher_logo)
        floating.clipToOutline = true
        floating.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(p0: View, p1: Outline) {
                p1.setOval(Rect(0, 0, p0.width, p0.height))
            }
        }
        return floating
    }

    private fun newWmParams(width: Int, height: Int): WindowManager.LayoutParams {
        val params = WindowManager.LayoutParams()
        params.flags =
            (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_SCALED or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        if (Build.VERSION.SDK_INT >= 26) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        params.width = width
        params.height = height
        params.format = PixelFormat.TRANSLUCENT
        return params
    }

    private fun initDialogView(ctx: Context) {
        val rootView = LayoutInflater.from(ctx).inflate(R.layout.dialog_menu, null)
        val binding = DialogMenuBinding.bind(rootView)
        mainDialog = DialogBuilder.Builder(ctx, rootView).setSize(240.dpInt(), 240.dpInt())
            .setSystemAlert(true).setDim(0f).setCancelable(true).build()

        binding.addEvent.setOnClickListener(ClickedListener(ctx))
        binding.addTask.setOnClickListener(ClickedListener(ctx))
        binding.capture.setOnClickListener(ClickedListener(ctx))
        binding.start.setOnClickListener(ClickedListener(ctx))
        binding.stop.setOnClickListener(ClickedListener(ctx))
        binding.preview.setOnClickListener(ClickedListener(ctx))
        binding.manage.setOnClickListener(ClickedListener(ctx))
        binding.quit.setOnClickListener(ClickedListener(ctx))
        binding.back.setOnClickListener(ClickedListener(ctx))

    }

    private inner class ClickedListener(val ctx: Context) : DebounceClickListener() {
        override fun onDebounceClick(view: View) {
            when (view.id) {
                R.id.addEvent -> {
                    mainDialog.dismiss()
                    gestureDialog.showDialog(ctx) {
                        showDialog(ctx)
                    }
                }

                R.id.addTask -> {
                    mainDialog.dismiss()
                    taskDialog.showDialog(ctx) {
                        showDialog(ctx)
                    }
                }

                R.id.capture -> {
                    mainDialog.dismiss()
                    GestureLiveData.instance.startRecording()
                }

                R.id.start -> {
                    mainDialog.dismiss()
                    startDialog.showDialog(ctx)
                }

                R.id.stop -> {
                    GestureLiveData.instance.setState(GestureLiveData.EXECUTE_STATE_PAUSE)
                }

                R.id.preview -> {
                    mainDialog.dismiss()
                    GesturePreviewDialogHelper(ctx).show()
                }

                R.id.manage -> {
                }

                R.id.quit -> {
                    mainDialog.dismiss()
                    gestureDialog.dismiss()
                    taskDialog.dismiss()
                    startDialog.dismiss()
                    GestureLiveData.instance.setState(GestureLiveData.EXECUTE_STATE_PAUSE)
                    quit()
                }

                R.id.back -> {
                    mainDialog.dismiss()
                    // 发送返回按键按下事件
                    view.context.startService(Intent(
                        view.context, GestureAccessibilityService::class.java
                    ).apply {
                        putExtra(GestureAccessibilityService.EXECUTE_BACK, true)
                    })
                }
            }
        }
    }

    private fun showDialog(context: Context) {
        if (!this::mainDialog.isInitialized) {
            initDialogView(context)
        }
        if (!mainDialog.isShowing) {
            ShowDialogHelper.showDialog(mainDialog)
        } else {
            mainDialog.dismiss()
        }
    }
}