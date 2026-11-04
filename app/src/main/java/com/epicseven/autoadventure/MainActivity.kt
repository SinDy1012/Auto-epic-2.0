package com.epicseven.autoadventure

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    companion object {
        private const val REQUEST_CAPTURE = 1001
    }

    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(40, 60, 40, 40)
        }

        val title = TextView(this).apply {
            text = "EPIC SEVEN AUTO ADVENTURE"
            textSize = 22f
            gravity = Gravity.CENTER
        }
        layout.addView(title)

        statusText = TextView(this).apply {
            text = "Trạng thái: Sẵn sàng"
            textSize = 17f
            gravity = Gravity.CENTER
            setPadding(0, 30, 0, 30)
        }
        layout.addView(statusText)

        fun addButton(label: String, action: () -> Unit) {
            val button = Button(this).apply {
                text = label
                setOnClickListener { action() }
            }
            layout.addView(button)
        }

        addButton("1. Cấp quyền nút nổi") {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            } else {
                showMessage("Đã có quyền nút nổi")
            }
        }

        addButton("2. Bật quyền Trợ năng") {
            startActivity(
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            )
        }

        addButton("3. Cấp quyền ghi hình màn hình") {
            requestScreenCapture()
        }

        addButton("4. Hiển thị nút nổi") {
            if (!Settings.canDrawOverlays(this)) {
                showMessage("Hãy cấp quyền nút nổi trước")
            } else {
                startService(
                    Intent(this, FloatingService::class.java)
                )
                statusText.text =
                    "Trạng thái: Đã mở nút nổi"
            }
        }

        addButton("5. Dừng") {
            AutoAccessibilityService.isAutoRunning = false

            stopService(
                Intent(this, FloatingService::class.java)
            )

            stopService(
                Intent(this, ScreenCaptureService::class.java)
            )

            statusText.text = "Trạng thái: Đã dừng"
        }

        val note = TextView(this).apply {
            text = "Hướng dẫn:\n" +
                "1. Cấp quyền nút nổi\n" +
                "2. Bật quyền Trợ năng\n" +
                "3. Cho phép ghi hình màn hình\n" +
                "4. Hiển thị nút nổi\n" +
                "5. Mở Epic Seven để kiểm thử"
            textSize = 14f
            setPadding(0, 35, 0, 0)
        }
        layout.addView(note)

        setContentView(layout)
    }

    private fun requestScreenCapture() {
        val manager = getSystemService(
            MEDIA_PROJECTION_SERVICE
        ) as MediaProjectionManager

        val intent = manager.createScreenCaptureIntent()

        @Suppress("DEPRECATION")
        startActivityForResult(intent, REQUEST_CAPTURE)
    }

    @Deprecated("Uses legacy activity result callback")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (requestCode != REQUEST_CAPTURE) return

        if (resultCode != RESULT_OK || data == null) {
            statusText.text =
                "Trạng thái: Chưa cấp quyền ghi hình"
            showMessage("Bạn đã từ chối ghi hình")
            return
        }

        val serviceIntent = Intent(
            this,
            ScreenCaptureService::class.java
        ).apply {
            action = ScreenCaptureService.ACTION_START

            putExtra(
                ScreenCaptureService.EXTRA_RESULT_CODE,
                resultCode
            )

            putExtra(
                ScreenCaptureService.EXTRA_RESULT_DATA,
                data
            )
        }

        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        statusText.text =
            "Trạng thái: Đã yêu cầu bắt đầu ghi hình"

        showMessage("Đã chấp thuận quyền ghi hình")
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}
