package com.zw.yzk.mock.touch.dialog

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.widget.TextView
import android.view.*
import androidx.appcompat.app.AlertDialog

/**
 * 示例用法
 *  DialogBuilder.Builder(this, R.layout.dialog_select_image)
 *  .setAnimation(R.style.bottom_animation)
 *  .addItem(DialogBuilder.Item(R.id.album, getString(R.string.album), View.OnClickListener {
 *  getCameraPermission()
 *  }))
 *  .addItem(DialogBuilder.Item(R.id.cancel, getString(R.string.cancel)))
 *  .setSize(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
 *  .setGravity(Gravity.BOTTOM)
 *  .setCancelable(true)
 *  .build().show()
 */
class DialogBuilder {

    class Item(id: Int, text: String? = null, listener: View.OnClickListener? = null) {
        val itemId: Int = id
        val itemText: String? = text
        val clickListener: View.OnClickListener? = listener
    }

    //对话框属性设置
    private class AlertParams(var context: Context, var view: View) {
        val itemList: MutableList<Item> = mutableListOf()
        //是否可点击区域外取消
        var cancelable: Boolean = false
        //动画
        var animationId: Int = 0
        //显示位置
        var gravity: Int = Gravity.NO_GRAVITY
        //设置对话框style
        var style: Int = 0
        //设置大小
        var size: Point? = null
        //是否是全局对话框
        var systemAlert = false
        //设置dim
        var dim: Float? = null
    }

    class Builder(context: Context, view: View) {
        private val params: AlertParams = AlertParams(context, view)

        constructor(context: Context, layoutId: Int) : this(context, LayoutInflater.from(context).inflate(layoutId, null))

        fun addItem(item: Item, param: View.OnClickListener): Builder {
            params.itemList.add(item)

            return this
        }

        fun setCancelable(cancelable: Boolean): Builder {
            params.cancelable = cancelable

            return this
        }

        fun setAnimation(animation: Int): Builder {
            params.animationId = animation

            return this
        }

        fun setGravity(gravity: Int): Builder {
            params.gravity = gravity

            return this
        }

        fun setStyle(style: Int): Builder {
            params.style = style

            return this
        }

        fun setDim(dim: Float): Builder {
            params.dim = dim

            return this
        }

        fun setSize(width: Int, height: Int): Builder {
            params.size = Point(width, height)

            return this
        }

        fun setSystemAlert(systemAlert: Boolean): Builder  {
            params.systemAlert = systemAlert

            return this
        }

        fun build(): AlertDialog {
            val dialog = AlertDialog.Builder(params.context, params.style)
                .setView(params.view).setCancelable(params.cancelable).create()
            //设置背景透明
            val window = dialog.window
            window?.let {
                window.setBackgroundDrawableResource(android.R.color.transparent)
                window.decorView.setPadding(0, 0, 0, 0)
                window.attributes.width = ViewGroup.LayoutParams.WRAP_CONTENT
                window.attributes.height = ViewGroup.LayoutParams.WRAP_CONTENT
                params.size?.let {
                    window.attributes.width = params.size!!.x
                    window.attributes.height = params.size!!.y
                }
                //设置展示位置
                window.attributes.gravity = params.gravity
                //设置动画
                window.setWindowAnimations(params.animationId)
                if (params.systemAlert) {
                    if (Build.VERSION.SDK_INT >= 26) {
                        window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                    } else {
                        window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    }
                }
                params.dim?.let {
                    window.setDimAmount(it)
                }
            }
            //设置item
            params.itemList.forEach { item ->
                params.view.findViewById<View>(item.itemId)?.apply {
                    setOnClickListener {
                        item.clickListener?.onClick(it)
                        dialog.dismiss()
                    }
                    item.itemText?.let {
                        (this as TextView).text = item.itemText
                    }
                }
            }

            return dialog
        }
    }
}