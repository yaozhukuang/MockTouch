package com.zw.yzk.mock.touch.dialog.addTask

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.zw.yzk.mock.touch.R
import com.zw.yzk.mock.touch.data.TaskItem
import com.zw.yzk.mock.touch.utils.DebounceClickListener

class TaskStepAdapter : RecyclerView.Adapter<TaskStepAdapter.Holder>() {

    private val list = mutableListOf<TaskItem>()
    private var listener: OnItemClickedListener? = null

    fun setOnItemClickedListener(listener: OnItemClickedListener) {
        this.listener = listener
    }

    fun getList() = list

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }

    fun setItem(index: Int, taskItem: TaskItem) {
        if (index >= list.size) {
            add(taskItem)
        } else {
            refresh(index, taskItem)
        }
    }

    private fun add(taskItem: TaskItem) {
        list.add(taskItem)
        notifyItemInserted(list.lastIndex)
    }

    private fun refresh(index: Int, taskItem: TaskItem) {
        list.removeAt(index)
        list.add(index, taskItem)
        notifyItemChanged(index)
    }

    private fun delete(index: Int) {
        list.removeAt(index)
        notifyItemRangeRemoved(index,   1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.li_task_step, parent, false)
        return Holder(view)
    }

    override fun getItemCount() = list.size + 1

    override fun onBindViewHolder(holder: Holder, position: Int) {
        if (position < list.size) {
            holder.text.text = list[position].getShowContent()
            holder.delete.isVisible = true
        } else {
            holder.text.setText(R.string.click_to_add_step)
            holder.delete.isVisible = false
        }
        holder.itemView.setOnClickListener(object : DebounceClickListener() {
            override fun onDebounceClick(view: View?) {
                listener?.onItemClicked(holder.adapterPosition)
            }

        })
        holder.delete.setOnClickListener(object : DebounceClickListener() {
            override fun onDebounceClick(view: View?) {
                delete(holder.adapterPosition)
            }

        })
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.add_step)
        val delete: View = view.findViewById(R.id.delete)
    }

    interface OnItemClickedListener {
        fun onItemClicked(index: Int)
    }
}