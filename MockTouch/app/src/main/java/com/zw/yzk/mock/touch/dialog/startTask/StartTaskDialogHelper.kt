package com.zw.yzk.mock.touch.dialog.startTask

import android.content.Context
import android.content.Intent
import android.graphics.Outline
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewOutlineProvider
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zw.yzk.mock.touch.R
import com.zw.yzk.mock.touch.data.ExecuteTask
import com.zw.yzk.mock.touch.data.GestureLiveData
import com.zw.yzk.mock.touch.data.TaskRepository
import com.zw.yzk.mock.touch.databinding.DialogStartTaskBinding
import com.zw.yzk.mock.touch.dialog.DialogBuilder
import com.zw.yzk.mock.touch.dialog.ShowDialogHelper
import com.zw.yzk.mock.touch.extention.dp
import com.zw.yzk.mock.touch.extention.dpInt
import com.zw.yzk.mock.touch.service.GestureAccessibilityService
import com.zw.yzk.mock.touch.utils.DebounceClickListener
import com.zw.yzk.mock.touch.widget.ItemDivide

class StartTaskDialogHelper {

    private val listAdapter = TaskListAdapter()
    private lateinit var rootView: View
    private lateinit var binding: DialogStartTaskBinding
    private lateinit var dialog: AlertDialog

    fun showDialog(context: Context) {
        if (!this::dialog.isInitialized) {
            initDialogView(context)
        } else {
            initRecyclerView(context)
        }
        if (!dialog.isShowing) {
            ShowDialogHelper.showDialog(dialog)
        }
    }

    fun dismiss() {
        if (this::dialog.isInitialized) {
            dialog.dismiss()
        }
    }

    private fun initDialogView(context: Context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.dialog_start_task, null)
        rootView.clipToOutline = true
        rootView.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, 5.dp())
            }
        }
        binding = DialogStartTaskBinding.bind(rootView)
        dialog = DialogBuilder.Builder(context, rootView).setSystemAlert(true).setCancelable(true)
            .setSize(280.dpInt(), 300.dpInt()).setDim(0f).build()
        dialog.setOnKeyListener { dialog, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                dialog.dismiss()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        initRecyclerView(context)
    }

    // 当前任务的执行步骤
    private fun initRecyclerView(ctx: Context) {
        binding.taskList.layoutManager = LinearLayoutManager(ctx, RecyclerView.VERTICAL, false)
        binding.taskList.adapter = listAdapter
        listAdapter.setData(TaskRepository.getAllTask())
        val divide = ItemDivide(ItemDivide.VERTICAL).apply {
            setDrawable(AppCompatResources.getDrawable(ctx, R.drawable.shape_start_task_divide))
        }
        binding.taskList.addItemDecoration(divide)
        listAdapter.setOnItemClickedListener(object : TaskListAdapter.OnItemClickedListener {
            override fun onItemClicked(index: Int) {
                showExecuteGroup(listAdapter.getList()[index])
            }

            override fun onDeleteClicked(index: Int) {
                showDeleteGroup(index)
            }

        })
    }

    private fun showDeleteGroup(index: Int) {
        binding.deleteGroup.isVisible = true
        binding.cancelDelete.setOnClickListener(object : DebounceClickListener() {
            override fun onDebounceClick(view: View) {
                binding.deleteGroup.isVisible = false
            }
        })
        binding.confirmDelete.setOnClickListener(object : DebounceClickListener() {
            override fun onDebounceClick(view: View) {
                binding.deleteGroup.isVisible = false
                listAdapter.delete(index)
                TaskRepository.refreshTask(listAdapter.getList())
            }

        })
    }

    private fun showExecuteGroup(task: ExecuteTask) {
        binding.executeGroup.isVisible = true
        binding.cancelExecute.setOnClickListener(object : DebounceClickListener() {
            override fun onDebounceClick(view: View) {
                binding.executeGroup.isVisible = false
            }
        })
        binding.confirmExecute.setOnClickListener(object : DebounceClickListener() {
            override fun onDebounceClick(view: View) {
                binding.executeGroup.isVisible = false
                dialog.dismiss()
                GestureLiveData.instance.currentTask.postValue(task)
                view.context.startService(
                    Intent(view.context, GestureAccessibilityService::class.java).apply {
                        putExtra(GestureAccessibilityService.EXECUTE_TASK, true)
                    }
                )
            }
        })
    }
}