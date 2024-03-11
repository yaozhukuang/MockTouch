package com.zw.yzk.mock.touch.utils

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.text.TextUtils.SimpleStringSplitter
import android.widget.Toast
import com.zw.yzk.mock.touch.R


object AccessibilityPermissionUtils {

    fun hasPermission(ctx: Context, service: Class<*>): Boolean {
        try {
            val enable = Settings.Secure.getInt(
                ctx.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED, 0
            )
            if (enable != 1) return false
            val services = Settings.Secure.getString(
                ctx.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (!TextUtils.isEmpty(services)) {
                val split = SimpleStringSplitter(':')
                split.setString(services)
                // 遍历所有已开启的辅助服务名
                while (split.hasNext()) {
                    if (split.next()
                            .equals(ctx.packageName + "/" + service.name, ignoreCase = true)
                    ) {
                        return true
                    }
                }
            }
        } catch (e: Throwable) { //若出现异常，则说明该手机设置被厂商篡改了,需要适配
            Toast.makeText(ctx, R.string.check_accessibility_failed, Toast.LENGTH_SHORT).show()
        }
        return false
    }

    /**
     * 跳转到系统设置：开启辅助服务
     */
    fun jumpToSetting(cxt: Context) {
        try {
            cxt.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        } catch (e: Throwable) { //若出现异常，则说明该手机设置被厂商篡改了,需要适配
            try {
                val intent: Intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                cxt.startActivity(intent)
            } catch (e2: Throwable) {
                Toast.makeText(cxt, R.string.open_accessibility_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }
}