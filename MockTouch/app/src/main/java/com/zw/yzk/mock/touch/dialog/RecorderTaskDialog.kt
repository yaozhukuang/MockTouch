package com.zw.yzk.mock.touch.dialog

import android.content.Context
import android.graphics.Outline
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.zw.yzk.mock.touch.R
import com.zw.yzk.mock.touch.data.ExecuteTask
import com.zw.yzk.mock.touch.data.GestureDelay
import com.zw.yzk.mock.touch.data.RecorderRepository
import com.zw.yzk.mock.touch.data.TaskItem
import com.zw.yzk.mock.touch.data.TaskRepository
import com.zw.yzk.mock.touch.databinding.DialogRecorderTaskBinding
import com.zw.yzk.mock.touch.extention.dp
import com.zw.yzk.mock.touch.extention.dpInt
import com.zw.yzk.mock.touch.utils.DebounceClickListener

class RecorderTaskDialog {

    private lateinit var dialog: AlertDialog

    fun showDialog(context: Context) {
        if (!this::dialog.isInitialized) {
            initDialogView(context)
        }
        if (!dialog.isShowing) {
            ShowDialogHelper.showDialog(dialog)
        }
    }

    private fun initDialogView(context: Context) {
        val rootView = LayoutInflater.from(context).inflate(R.layout.dialog_recorder_task, null)
        rootView.clipToOutline = true
        rootView.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, 5.dp())
            }
        }
        val binding = DialogRecorderTaskBinding.bind(rootView)
        dialog = DialogBuilder.Builder(context, rootView).setSystemAlert(true).setCancelable(true)
            .setSize(280.dpInt(), ViewGroup.LayoutParams.WRAP_CONTENT).setDim(0f).build()
        dialog.setOnKeyListener { dialog, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                dialog.dismiss()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        initView(context, binding)
    }

    private fun initView(ctx: Context, binding: DialogRecorderTaskBinding) {
        binding.cancel.setOnClickListener(object : DebounceClickListener() {
            override fun onDebounceClick(view: View) {
                dialog.dismiss()
            }

        })
        binding.save.setOnClickListener(object : DebounceClickListener() {
            override fun onDebounceClick(view: View) {
                val text = binding.taskName.text.toString()
                val name = if (TextUtils.isEmpty(text)) binding.taskName.hint.toString() else text
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(ctx, R.string.input_task_name, Toast.LENGTH_SHORT).show()
                    return
                }
                val repeat = try {
                    val str = binding.repeatCount.text.toString()
                    if (TextUtils.isEmpty(str)) -1 else str.toInt()
                } catch (e: Exception) {
                    -1
                }
                val list = mutableListOf<TaskItem>()
                list.addAll(RecorderRepository.getRecords().last().getEventList())
                try {
                    val str = binding.cycleInterval.text.toString()
                    val interval = if (TextUtils.isEmpty(str)) 0 else str.toLong()
                    val delayItem =
                        GestureDelay(interval, ctx.getString(R.string.delay_duration, interval))
                    list.add(delayItem)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                TaskRepository.saveTask(ExecuteTask(name, repeat, list))
                dialog.dismiss()
            }

        })
    }

}