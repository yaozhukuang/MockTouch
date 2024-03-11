package com.zw.yzk.mock.touch.dialog

import android.content.Context
import android.gesture.Gesture
import android.gesture.GestureOverlayView
import android.os.SystemClock
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.zw.yzk.mock.touch.R
import com.zw.yzk.mock.touch.data.GestureRepository
import com.zw.yzk.mock.touch.databinding.DialogAddEventBinding
import com.zw.yzk.mock.touch.extention.screenHeight
import com.zw.yzk.mock.touch.extention.screenWidth
import com.zw.yzk.mock.touch.utils.DebounceClickListener

class AddGestureDialogHelper {

    private lateinit var rootView: View
    private lateinit var binding: DialogAddEventBinding
    private lateinit var dialog: AlertDialog
    private var currentGesture: Gesture? = null
    private var gestureDuration = 0L

    fun showDialog(context: Context, addEventComplete: () -> Unit) {
        if (!this::dialog.isInitialized) {
            initDialogView(context)
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
        rootView = LayoutInflater.from(context).inflate(R.layout.dialog_add_event, null)
        binding = DialogAddEventBinding.bind(rootView)
        dialog = DialogBuilder.Builder(context, rootView).setSystemAlert(true)
            .setSize(context.screenWidth(), context.screenHeight()).setDim(0f).build()
        dialog.setOnKeyListener { dialog, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                dialog.dismiss()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        initGestureOverlay()
        initConfirmGestureView()
    }

    private fun initConfirmGestureView() {
        binding.cancel.setOnClickListener(object : DebounceClickListener() {
            override fun onDebounceClick(view: View?) {
                showCaptureGesture()
            }

        })
        binding.confirm.setOnClickListener(object : DebounceClickListener() {
            override fun onDebounceClick(view: View) {
                val name = binding.gestureName.text.trim().toString()
                val gesture = currentGesture
                if (gesture != null) {
                    GestureRepository.addGesture(name, gestureDuration, gesture)
                } else {
                    Toast.makeText(view.context, R.string.invalid_gesture, Toast.LENGTH_SHORT)
                        .show()
                }
                showCaptureGesture()
            }

        })
        binding.complete.setOnClickListener(object : DebounceClickListener() {
            override fun onDebounceClick(view: View) {
                val name = binding.gestureName.text.trim().toString()
                val gesture = currentGesture
                if (gesture != null) {
                    GestureRepository.addGesture(name, gestureDuration, gesture)
                    dialog.dismiss()
                } else {
                    Toast.makeText(view.context, R.string.invalid_gesture, Toast.LENGTH_SHORT)
                        .show()
                }
                showCaptureGesture()
            }

        })

    }

    private fun initGestureOverlay() {
        binding.gestureOverlay.addOnGestureListener(object : GestureOverlayView.OnGestureListener {
            private var gestureStartTime = 0L

            override fun onGestureStarted(overlay: GestureOverlayView?, event: MotionEvent?) {
                gestureStartTime = SystemClock.elapsedRealtime()
            }

            override fun onGesture(overlay: GestureOverlayView?, event: MotionEvent?) {
            }

            override fun onGestureEnded(overlay: GestureOverlayView, event: MotionEvent?) {
                gestureDuration = SystemClock.elapsedRealtime() - gestureStartTime
                currentGesture = overlay.gesture
                showConfirmView()
            }

            override fun onGestureCancelled(overlay: GestureOverlayView?, event: MotionEvent?) {
            }
        })
    }

    private fun showConfirmView() {
        binding.gestureOverlay.isEnabled = false
        binding.confirmGesture.isVisible = true
        binding.hint.isVisible = false
        val name = generateDefaultGestureName(binding.gestureOverlay.context)
        binding.gestureName.setText(name)
        binding.gestureName.setSelection(name.length)
    }

    private fun showCaptureGesture() {
        binding.gestureOverlay.isEnabled = true
        binding.confirmGesture.isVisible = false
        binding.hint.isVisible = true
    }

    private fun generateDefaultGestureName(context: Context): String {
        val name = context.getString(R.string.default_gesture_name)
        val builder = StringBuilder()
        GestureRepository.getGesture().forEach {
            builder.append(it.name).append(",")
        }
        val names = builder.toString()
        var count = 1
        while (count < 100) {
            val newName = name.plus(count)
            if (names.contains(newName)) {
                count++
            } else {
                return newName
            }
        }
        return name
    }
}