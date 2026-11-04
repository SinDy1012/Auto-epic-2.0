package com.epicseven.autoadventure

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent

class AutoAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        var isAutoRunning = false

        @Volatile
        var instance: AutoAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {
        // Logic nhận diện màn hình sẽ được thêm sau.
    }

    override fun onInterrupt() {
        isAutoRunning = false
    }

    override fun onDestroy() {
        isAutoRunning = false
        instance = null
        super.onDestroy()
    }

    fun tap(
        x: Float,
        y: Float,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        if (!isAutoRunning) {
            onComplete?.invoke(false)
            return
        }

        val path = Path().apply {
            moveTo(x, y)
        }

        val stroke = GestureDescription.StrokeDescription(
            path,
            0L,
            100L
        )

        val gesture = GestureDescription.Builder()
            .addStroke(stroke)
            .build()

        dispatchGesture(
            gesture,
            object : GestureResultCallback() {
                override fun onCompleted(
                    gestureDescription: GestureDescription?
                ) {
                    onComplete?.invoke(true)
                }

                override fun onCancelled(
                    gestureDescription: GestureDescription?
                ) {
                    onComplete?.invoke(false)
                }
            },
            null
        )
    }

    fun swipe(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        duration: Long = 400L
    ) {
        if (!isAutoRunning) return

        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }

        val stroke = GestureDescription.StrokeDescription(
            path,
            0L,
            duration.coerceIn(100L, 2000L)
        )

        val gesture = GestureDescription.Builder()
            .addStroke(stroke)
            .build()

        dispatchGesture(gesture, null, null)
    }
}
