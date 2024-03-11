package com.zw.yzk.mock.touch.dialog.addTask

import android.content.Context
import android.graphics.Outline
import android.text.TextUtils
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zw.yzk.mock.touch.R
import com.zw.yzk.mock.touch.data.ExecuteTask
import com.zw.yzk.mock.touch.data.GestureDelay
import com.zw.yzk.mock.touch.data.GestureItem
import com.zw.yzk.mock.touch.data.GestureRepository
import com.zw.yzk.mock.touch.data.TaskRepository
import com.zw.yzk.mock.touch.databinding.DialogAddTaskBinding
import com.zw.yzk.mock.touch.dialog.DialogBuilder
import com.zw.yzk.mock.touch.dialog.ShowDialogHelper
import com.zw.yzk.mock.touch.extention.dp
import com.zw.yzk.mock.touch.extention.dpInt
import com.zw.yzk.mock.touch.extention.screenHeight
import com.zw.yzk.mock.touch.extention.screenWidth
import com.zw.yzk.mock.touch.utils.DebounceClickListener
import com.zw.yzk.mock.touch.widget.ItemDivide

class AddTaskDialogHelper {

    private val stepAdapter = TaskStepAdapter()
    private lateinit var rootView: View
    private lateinit var binding: DialogAddTaskBinding
    private lateinit var dialog: AlertDialog

    fun showDialog(context: Context, addEventComplete: () -> Unit) {
        if (!this::dialog.isInitialized) {
            initDialogView(context)
        } else {
            stepAdapter.clear()
        }
        if (!dialog.isShowing) {
            ShowDialogHelper.showDialog(dialog)
            dialog.setOnDismissListener {
                addEventComplete()
            }
        }
    }

    fun dismiss() {
        if (!this::dialog.isInitialized) {
            return
        }
        dialog.setOnDismissListener(null)
        dialog.dismiss()
    }

    fun dismiss(callback: () -> Unit) {
        if (!this::dialog.isInitialized) {
            return
        }
        dialog.setOnDismissListener { callback.invoke() }
        dialog.dismiss()
    }

    private fun initDialogView(context: Context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.dialog_add_task, null)
        binding = DialogAddTaskBinding.bind(rootView)
        binding.back.setOnClickListener(object : DebounceClickListener() {
            override fun onDebounceClick(view: View) {
                dialog.dismiss()
            }
        })
        binding.save.setOnClickListener(object : DebounceClickListener() {
            override fun onDebounceClick(view: View) {
                val name = binding.taskName.text.toString()
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(context, R.string.input_task_name, Toast.LENGTH_SHORT).show()
                    return
                }
                val repeat = try {
                    val str = binding.repeatCount.text.toString()
                    if (TextUtils.isEmpty(str)) -1 else str.toInt()
                } catch (e: Exception) {
                    -1
                }
                TaskRepository.saveTask(ExecuteTask(name, repeat, stepAdapter.getList()))
                dialog.dismiss()
            }
        })
        dialog = DialogBuilder.Builder(context, rootView).setSystemAlert(true)
            .setSize(context.screenWidth(), context.screenHeight()).setDim(0f).build()
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
        binding.taskStep.layoutManager = LinearLayoutManager(ctx, RecyclerView.HORIZONTAL, false)
        binding.taskStep.adapter = stepAdapter
        val divide = ItemDivide(ItemDivide.HORIZONTAL).apply {
            setDrawable(AppCompatResources.getDrawable(ctx, R.drawable.shape_task_item_divide))
        }
        binding.taskStep.addItemDecoration(divide)
        stepAdapter.setOnItemClickedListener(object : TaskStepAdapter.OnItemClickedListener {
            override fun onItemClicked(index: Int) {
                val child = binding.taskStep.layoutManager!!.findViewByPosition(index)
                val anchor = child ?: binding.taskStep
                showGestureEventWindow(ctx, index, anchor)
            }

        })
    }

    // 显示所有已添加的手势事件
    private fun showGestureEventWindow(ctx: Context, gestureIndex: Int, anchor: View) {
        createPopupWindow(ctx, gestureIndex).showAsDropDown(anchor, 0, 5.dpInt())
    }

    private fun createPopupWindow(ctx: Context, gestureIndex: Int): PopupWindow {
        val root: View = LayoutInflater.from(ctx).inflate(R.layout.popup_gesture_event, null)
        root.clipToOutline = true
        root.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, 5.dp())
            }
        }
        root.post {
            val maxHeight = 190.dpInt()
            val height = if (root.height > maxHeight) maxHeight else root.height
            root.layoutParams.height = height
        }

        val gesturePopupWindow = PopupWindow(root, 100.dpInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        gesturePopupWindow.isOutsideTouchable = true

        val recyclerView = root as RecyclerView
        val divide = ItemDivide(ItemDivide.VERTICAL).apply {
            setDrawable(AppCompatResources.getDrawable(ctx, R.drawable.shape_gesture_event_divide))
        }
        recyclerView.addItemDecoration(divide)
        recyclerView.layoutManager = LinearLayoutManager(ctx)
        recyclerView.adapter = GestureEventAdapter(GestureRepository.getGesture()).apply {
            setOnItemClickedListener(object : GestureEventAdapter.OnItemClickedListener {
                override fun onItemClicked(index: Int, item: GestureItem?) {
                    if (index == 0 || item == null) {
                        showDelayDialog(ctx, gestureIndex)
                    } else {
                        stepAdapter.setItem(gestureIndex, item)
                    }
                    gesturePopupWindow.dismiss()
                }

            })
        }
        return gesturePopupWindow
    }

    // 新增延手势事件之间的延时时间
    private fun showDelayDialog(ctx: Context, index: Int) {
        val root = LayoutInflater.from(ctx).inflate(R.layout.dialog_add_delay, null)
        root.clipToOutline = true
        root.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, 5.dp())
            }
        }
        val editText = root.findViewById<EditText>(R.id.delay)
        val confirm = root.findViewById<View>(R.id.confirm)
        val dialog =
            DialogBuilder.Builder(ctx, root).setSystemAlert(true).setGravity(Gravity.CENTER)
                .setSize(350.dpInt(), ViewGroup.LayoutParams.WRAP_CONTENT).setCancelable(true)
                .setDim(0f).build()
        confirm.setOnClickListener(object : DebounceClickListener() {
            override fun onDebounceClick(view: View?) {
                val delay = try {
                    editText.text.toString().toLong()
                } catch (e: Exception) {
                    0
                }
                if (delay != 0L) {
                    val delayItem =
                        GestureDelay(delay, ctx.getString(R.string.delay_duration, delay))
                    stepAdapter.setItem(index, delayItem)
                    dialog.dismiss()
                }
            }

        })
        ShowDialogHelper.showDialog(dialog)
    }
}