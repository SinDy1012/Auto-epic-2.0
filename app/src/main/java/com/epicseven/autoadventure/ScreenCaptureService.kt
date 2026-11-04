package com.epicseven.autoadventure

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import android.view.WindowManager

class ScreenCaptureService : Service() {

    companion object {
        const val ACTION_START = "capture.START"
        const val ACTION_STOP = "capture.STOP"
        const val EXTRA_RESULT_CODE = "resultCode"
        const val EXTRA_RESULT_DATA = "resultData"

        private const val CHANNEL_ID = "e7_capture"
        private const val NOTIFICATION_ID = 1001
        private const val TAG = "E7ScreenCapture"
    }

    private var projection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    private var captureThread: HandlerThread? = null
    private var captureHandler: Handler? = null

    private var screenWidth = 0
    private var screenHeight = 0
    private var screenDensity = 0

    private var lastCaptureTime = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Epic Seven - Chụp màn hình",
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
            .setContentText("Đang chụp màn hình để nhận diện")
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
            startForeground(
                NOTIFICATION_ID,
                notification
            )
        }

        val resultCode = intent.getIntExtra(
            EXTRA_RESULT_CODE,
            0
        )

        @Suppress("DEPRECATION")
        val resultData: Intent? =
            if (Build.VERSION.SDK_INT >= 33) {
                intent.getParcelableExtra(
                    EXTRA_RESULT_DATA,
                    Intent::class.java
                )
            } else {
                intent.getParcelableExtra(
                    EXTRA_RESULT_DATA
                )
            }

        if (resultData == null) {
            Log.e(TAG, "Không có dữ liệu cấp quyền")
            stopSelf()
            return
        }

        val windowManager = getSystemService(
            WINDOW_SERVICE
        ) as WindowManager

        if (Build.VERSION.SDK_INT >= 30) {
            val bounds = windowManager.currentWindowMetrics.bounds
            screenWidth = bounds.width()
            screenHeight = bounds.height()
        } else {
            @Suppress("DEPRECATION")
            val display = windowManager.defaultDisplay

            val metrics = android.util.DisplayMetrics()

            @Suppress("DEPRECATION")
            display.getRealMetrics(metrics)

            screenWidth = metrics.widthPixels
            screenHeight = metrics.heightPixels
        }

        screenDensity = resources.displayMetrics.densityDpi

        captureThread = HandlerThread(
            "EpicSevenCaptureThread"
        ).apply {
            start()
        }

        captureHandler = Handler(
            captureThread!!.looper
        )

        imageReader = ImageReader.newInstance(
            screenWidth,
            screenHeight,
            PixelFormat.RGBA_8888,
            2
        )

        imageReader?.setOnImageAvailableListener(
            { reader ->

                val image = reader.acquireLatestImage()
                    ?: return@setOnImageAvailableListener

                try {
                    val now = android.os.SystemClock.elapsedRealtime()

                    if (now - lastCaptureTime < 1000L) {
                        return@setOnImageAvailableListener
                    }

                    lastCaptureTime = now

                    val plane = image.planes[0]
                    val buffer = plane.buffer

                    val pixelStride = plane.pixelStride
                    val rowStride = plane.rowStride

                    val rowPadding =
                        rowStride - pixelStride * image.width

                    val paddedWidth =
                        image.width + rowPadding / pixelStride

                    val paddedBitmap = Bitmap.createBitmap(
                        paddedWidth,
                        image.height,
                        Bitmap.Config.ARGB_8888
                    )

                    try {
                        paddedBitmap.copyPixelsFromBuffer(buffer)

                        val bitmap = Bitmap.createBitmap(
                            paddedBitmap,
                            0,
                            0,
                            image.width,
                            image.height
                        )

                        try {
                            onScreenshotCaptured(bitmap)
                        } finally {
                            bitmap.recycle()
                        }
                    } finally {
                        paddedBitmap.recycle()
                    }

                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "Lỗi đọc ảnh màn hình",
                        e
                    )
                } finally {
                    image.close()
                }
            },
            captureHandler
        )

        val manager = getSystemService(
            MEDIA_PROJECTION_SERVICE
        ) as MediaProjectionManager

        try {
            projection = manager.getMediaProjection(
                resultCode,
                resultData
            )

            projection?.registerCallback(
                object : MediaProjection.Callback() {
                    override fun onStop() {
                        Log.d(TAG, "Phiên ghi hình đã dừng")
                        stopSelf()
                    }
                },
                captureHandler
            )

            virtualDisplay = projection?.createVirtualDisplay(
                "EpicSevenScreen",
                screenWidth,
                screenHeight,
                screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader!!.surface,
                null,
                captureHandler
            )

            Log.d(TAG, "Đã tạo VirtualDisplay")

        } catch (e: Exception) {
            Log.e(TAG, "Không thể bắt đầu ghi hình", e)
            stopSelf()
        }
    }

    private fun onScreenshotCaptured(bitmap: Bitmap) {

        Log.d(
            TAG,
            "Đã chụp ảnh: ${bitmap.width} x ${bitmap.height}"
        )

        // Bước 19 sẽ kết nối ảnh này với
        // ScreenAnalyzer và AutoAdventureController.
    }

    override fun onDestroy() {

        imageReader?.setOnImageAvailableListener(
            null,
            null
        )

        virtualDisplay?.release()
        virtualDisplay = null

        imageReader?.close()
        imageReader = null

        projection?.stop()
        projection = null

        captureThread?.quitSafely()
        captureThread = null
        captureHandler = null

        super.onDestroy()
    }
}
