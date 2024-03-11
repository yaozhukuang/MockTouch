package com.zw.yzk.mock.touch.dialog.addTask

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zw.yzk.mock.touch.R
import com.zw.yzk.mock.touch.data.GestureItem
import com.zw.yzk.mock.touch.utils.DebounceClickListener

class GestureEventAdapter(private val list: List<GestureItem>) :
    RecyclerView.Adapter<GestureEventAdapter.Holder>() {

    private var listener: OnItemClickedListener? = null

    fun setOnItemClickedListener(listener: OnItemClickedListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.li_gesture_event, parent, false)
        return Holder(view as TextView)
    }

    override fun getItemCount() = list.size + 1

    override fun onBindViewHolder(holder: Holder, position: Int) {
        if (position != 0) {
            holder.text.text = list[position - 1].getShowContent()
        } else {
            holder.text.setText(R.string.click_to_add_delay)
        }
        holder.text.setOnClickListener(object : DebounceClickListener() {
            override fun onDebounceClick(view: View?) {
                val position = holder.adapterPosition
                val gesture = if (position == 0) null else list[holder.adapterPosition - 1]
                listener?.onItemClicked(position, gesture)
            }

        })
    }

    class Holder(val text: TextView) : RecyclerView.ViewHolder(text)

    interface OnItemClickedListener {
        fun onItemClicked(index: Int, item: GestureItem?)
    }
}