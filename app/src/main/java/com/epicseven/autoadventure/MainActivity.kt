package com.epicseven.autoadventure

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

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

        fun addButton(
            label: String,
            action: () -> Unit
        ) {
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

        addButton("3. Bắt đầu") {
            if (!Settings.canDrawOverlays(this)) {
                showMessage("Hãy cấp quyền nút nổi trước")
            } else {
                startService(
                    Intent(this, FloatingService::class.java)
                )
                statusText.text = "Trạng thái: Đã yêu cầu mở nút nổi"
            }
        }

        addButton("4. Dừng") {
            stopService(
                Intent(this, FloatingService::class.java)
            )
            statusText.text = "Trạng thái: Đã dừng nút nổi"
        }

        val note = TextView(this).apply {
            text = "Cần bật quyền Trợ năng và cho phép " +
                "ghi hình màn hình trước khi sử dụng " +
                "chức năng tự động."
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, 35, 0, 0)
        }
        layout.addView(note)

        setContentView(layout)
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}
