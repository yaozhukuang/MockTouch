package com.zw.yzk.mock.touch.dialog

import android.content.Context
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.zw.yzk.mock.touch.R
import com.zw.yzk.mock.touch.data.GestureLiveData
import com.zw.yzk.mock.touch.databinding.DialogPreviewEventBinding
import com.zw.yzk.mock.touch.dialog.startTask.StartTaskDialogHelper
import com.zw.yzk.mock.touch.extention.screenHeight
import com.zw.yzk.mock.touch.extention.screenWidth
import com.zw.yzk.mock.touch.utils.DebounceClickListener


class GesturePreviewDialogHelper(ctx: Context) : LifecycleOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)

    private val dialog: AlertDialog

    init {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        dialog = createDialog(ctx)
        initLifecycleEvent()
    }

    fun show() {
        ShowDialogHelper.showDialog(dialog)
    }

    private fun createDialog(ctx: Context): AlertDialog {
        val root = LayoutInflater.from(ctx).inflate(R.layout.dialog_preview_event, null)
        val binding = DialogPreviewEventBinding.bind(root)


        binding.startPreview.setOnClickListener(object : DebounceClickListener() {
            override fun onDebounceClick(view: View) {
                StartTaskDialogHelper().showDialog(ctx)
            }
        })
        binding.stopPreview.setOnClickListener(object : DebounceClickListener() {
            override fun onDebounceClick(view: View) {
                GestureLiveData.instance.setState(GestureLiveData.EXECUTE_STATE_PAUSE)
            }
        })
        binding.quitPreview.setOnClickListener(object : DebounceClickListener() {
            override fun onDebounceClick(view: View) {
                dialog.dismiss()
            }
        })
        val dialog = DialogBuilder.Builder(ctx, root).setSystemAlert(true)
            .setSize(ctx.screenWidth(), ctx.screenHeight()).setDim(0f).build()
        dialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                dialog.dismiss()
                GestureLiveData.instance.setState(GestureLiveData.EXECUTE_STATE_PAUSE)
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        dialog.setOnShowListener {
            GestureLiveData.instance.currentTask.observe(this) {
                if (it == null) {
                    return@observe
                }
                binding.currentTask.text = ctx.getString(R.string.current_display_task, it.name)
            }
            GestureLiveData.instance.currentGestureName.observe(this) {
                binding.currentGesture.text = ctx.getString(R.string.current_display_gesture, it)
            }
            GestureLiveData.instance.currentTaskExecuteCount.observe(this) {
                binding.taskCount.text = ctx.getString(R.string.task_execute_count, it)
            }
        }
        dialog.setOnDismissListener {
            GestureLiveData.instance.setState(GestureLiveData.EXECUTE_STATE_PAUSE)
        }
        return dialog
    }

    private fun initLifecycleEvent() {
        dialog.setOnDismissListener {
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }
    }

    override val lifecycle = lifecycleRegistry
}