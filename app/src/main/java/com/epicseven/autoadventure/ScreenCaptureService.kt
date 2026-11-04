package com.epicseven.autoadventure

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder

class ScreenCaptureService : Service() {

    companion object {
        const val ACTION_START = "capture.START"
        const val ACTION_STOP = "capture.STOP"
        const val EXTRA_RESULT_CODE = "resultCode"
        const val EXTRA_RESULT_DATA = "resultData"

        private const val CHANNEL_ID = "e7_capture"
        private const val NOTIFICATION_ID = 1001
    }

    private var projection: MediaProjection? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Epic Seven - Nhận diện màn hình",
            NotificationManager.IMPORTANCE_LOW
        )

        val manager = getSystemService(
            NotificationManager::class.java
        )
        manager.createNotificationChannel(channel)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        when (intent?.action) {
            ACTION_START -> startCapture(intent)
            ACTION_STOP -> stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun startCapture(intent: Intent) {

        val notification = Notification.Builder(
            this,
            CHANNEL_ID
        )
            .setContentTitle("Epic Seven Auto Adventure")
            .setContentText("Đang chuẩn bị nhận diện màn hình")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        val resultCode = intent.getIntExtra(
            EXTRA_RESULT_CODE,
            0
        )

        @Suppress("DEPRECATION")
        val resultData = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(
                EXTRA_RESULT_DATA,
                Intent::class.java
            )
        } else {
            intent.getParcelableExtra(EXTRA_RESULT_DATA)
        }

        if (resultData == null) {
            stopSelf()
            return
        }

        val manager = getSystemService(
            MEDIA_PROJECTION_SERVICE
        ) as MediaProjectionManager

        projection = manager.getMediaProjection(
            resultCode,
            resultData
        )

        projection?.registerCallback(
            object : MediaProjection.Callback() {
                override fun onStop() {
                    projection = null
                    stopSelf()
                }
            },
            null
        )
    }

    override fun onDestroy() {
        projection?.stop()
        projection = null
        super.onDestroy()
    }
}
