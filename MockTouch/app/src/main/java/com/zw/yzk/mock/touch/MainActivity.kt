package com.zw.yzk.mock.touch

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.permissionx.guolindev.PermissionX
import com.zw.yzk.mock.touch.databinding.ActivityMainBinding
import com.zw.yzk.mock.touch.dialog.FloatingWindow
import com.zw.yzk.mock.touch.service.GestureAccessibilityService
import com.zw.yzk.mock.touch.utils.AccessibilityPermissionUtils
import com.zw.yzk.mock.touch.utils.DebounceClickListener


class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.start.setOnClickListener(object : DebounceClickListener() {
            override fun onDebounceClick(view: View) {
                if (AccessibilityPermissionUtils.hasPermission(
                        this@MainActivity, GestureAccessibilityService::class.java
                    )
                ) {
                    checkPermission()
                } else {
                    AccessibilityPermissionUtils.jumpToSetting(this@MainActivity)
                }
//                checkPermission()
            }

        })
    }

    private fun checkPermission() {
        if (!Settings.canDrawOverlays(this)) {
            PermissionX.init(this).permissions(android.Manifest.permission.SYSTEM_ALERT_WINDOW)
                .onExplainRequestReason { scope, deniedList ->
                    val message = getString(R.string.system_alert_window_permission_title)
                    val positive = getString(R.string.positive)
                    val negative = getString(R.string.negative)
                    scope.showRequestReasonDialog(deniedList, message, positive, negative)
                }.request { allGranted, _, _ ->
                    if (allGranted) {
                        start()
                    } else {
                        Toast.makeText(
                            this, R.string.missing_necessary_permissions, Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            start()
        }
    }

    private fun start() {
        val workRequest = OneTimeWorkRequest.Builder(FloatingWindowWork::class.java).build()
        WorkManager.getInstance(this).enqueue(workRequest)
        finish()
    }


    class FloatingWindowWork(ctx: Context, p: WorkerParameters) : Worker(ctx, p) {
        override fun doWork(): Result {
            Handler(Looper.getMainLooper()).post {
                val context =
                    ContextThemeWrapper(MockTouchApplication.instance, R.style.Theme_MockTouch)
                FloatingWindow.instance.showFloatingWindow(context)
            }
            return Result.success()
        }
    }

}