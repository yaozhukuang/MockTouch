package com.zw.yzk.mock.touch.dialog.startTask

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zw.yzk.mock.touch.R
import com.zw.yzk.mock.touch.data.ExecuteTask
import com.zw.yzk.mock.touch.utils.DebounceClickListener

class TaskListAdapter : RecyclerView.Adapter<TaskListAdapter.Holder>() {

    private val list = mutableListOf<ExecuteTask>()
    private var listener: OnItemClickedListener? = null

    fun setOnItemClickedListener(listener: OnItemClickedListener) {
        this.listener = listener
    }

    fun getList() = list

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<ExecuteTask>) {
        list.clear()
        list.addAll(data)
        notifyDataSetChanged()
    }

    fun delete(index: Int) {
        list.removeAt(index)
        notifyItemRangeRemoved(index, 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.li_task_list, parent, false)
        return Holder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.text.text = list[position].name
        holder.itemView.setOnClickListener(object : DebounceClickListener() {
            override fun onDebounceClick(view: View?) {
                listener?.onItemClicked(holder.adapterPosition)
            }

        })
        holder.delete.setOnClickListener(object : DebounceClickListener() {
            override fun onDebounceClick(view: View?) {
                listener?.onDeleteClicked(holder.adapterPosition)
            }

        })
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.task)
        val delete: View = view.findViewById(R.id.delete)
    }

    interface OnItemClickedListener {
        fun onItemClicked(index: Int)
        fun onDeleteClicked(index: Int)
    }
}