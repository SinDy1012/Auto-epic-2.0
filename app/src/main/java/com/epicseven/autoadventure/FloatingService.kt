package com.epicseven.autoadventure

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast

class FloatingService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingView: LinearLayout? = null
    private var isRunning = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        windowManager = getSystemService(
            WINDOW_SERVICE
        ) as WindowManager

        createFloatingView()
    }

    private fun createFloatingView() {

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 12, 12, 12)
            setBackgroundColor(0xDD222222.toInt())
        }

        val startButton = Button(this).apply {
            text = "Bắt đầu"
            setOnClickListener {
                isRunning = !isRunning

                if (isRunning) {
                    text = "Dừng"
                    AutoAccessibilityService.isAutoRunning = true
                    showMessage("Đã bật chế độ tự động")
                } else {
                    text = "Bắt đầu"
                    AutoAccessibilityService.isAutoRunning = false
                    showMessage("Đã dừng tự động")
                }
            }
        }

        layout.addView(startButton)

        val windowType =
            if (Build.VERSION.SDK_INT >= 26) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 30
            y = 200
        }

        windowManager.addView(layout, params)
        floatingView = layout
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroy() {
        AutoAccessibilityService.isAutoRunning = false
        isRunning = false

        floatingView?.let {
            windowManager.removeView(it)
        }

        floatingView = null
        super.onDestroy()
    }
}
