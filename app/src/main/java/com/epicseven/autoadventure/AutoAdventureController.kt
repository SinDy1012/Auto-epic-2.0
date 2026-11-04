package com.epicseven.autoadventure

import android.os.Handler
import android.os.Looper
import android.os.SystemClock

class AutoAdventureController(
    private val analyzer: ScreenAnalyzer
) {

    private val handler = Handler(Looper.getMainLooper())

    private var lastActionTime = 0L
    private var lastState = ScreenAnalyzer.GameState.UNKNOWN
    private var sameStateCount = 0
    private var busy = false

    private val minimumDelay = 1500L

    data class TapPoint(
        val x: Float,
        val y: Float
    )

    // Tọa độ theo tỷ lệ màn hình 1536 x 709
    private val referenceWidth = 1536f
    private val referenceHeight = 709f

    fun process(
        result: ScreenAnalyzer.AnalysisResult,
        screenWidth: Int,
        screenHeight: Int
    ) {
        if (!AutoAccessibilityService.isAutoRunning) {
            reset()
            return
        }

        if (busy) return

        val state = result.state

        if (state == ScreenAnalyzer.GameState.UNKNOWN) {
            lastState = state
            sameStateCount = 0
            return
        }

        if (result.confidence < 0.85f) return

        if (state == lastState) {
            sameStateCount++
        } else {
            lastState = state
            sameStateCount = 1
        }

        // Cần nhận diện cùng trạng thái ít nhất 2 lần
        if (sameStateCount < 2) return

        val now = SystemClock.elapsedRealtime()

        if (now - lastActionTime < minimumDelay) {
            return
        }

        val point = getTapPoint(state) ?: return

        val x = point.x / referenceWidth * screenWidth
        val y = point.y / referenceHeight * screenHeight

        val service = AutoAccessibilityService.instance
            ?: return

        busy = true
        lastActionTime = now

        service.tap(x, y) { success ->
            handler.post {
                busy = false

                if (success) {
                    sameStateCount = 0
                }
            }
        }
    }

    private fun getTapPoint(
        state: ScreenAnalyzer.GameState
    ): TapPoint? {

        return when (state) {

            ScreenAnalyzer.GameState.READY ->
                TapPoint(1385f, 652f)

            ScreenAnalyzer.GameState.SELECT_SUPPORTER ->
                TapPoint(1350f, 654f)

            ScreenAnalyzer.GameState.SELECT_TEAM ->
                TapPoint(1380f, 655f)

            ScreenAnalyzer.GameState.STORY_SKIP ->
                TapPoint(1480f, 38f)

            ScreenAnalyzer.GameState.SKIP_CONFIRM ->
                TapPoint(875f, 454f)

            ScreenAnalyzer.GameState.CLEAR ->
                TapPoint(1498f, 665f)

            ScreenAnalyzer.GameState.REWARDS ->
                TapPoint(1425f, 653f)

            ScreenAnalyzer.GameState.STORY_CLEAR ->
                TapPoint(768f, 475f)

            // Chưa tự bật Auto để tránh vô tình tắt Auto
            ScreenAnalyzer.GameState.BATTLE ->
                null

            ScreenAnalyzer.GameState.UNKNOWN ->
                null
        }
    }

    fun reset() {
        busy = false
        sameStateCount = 0
        lastState = ScreenAnalyzer.GameState.UNKNOWN
        lastActionTime = 0L
    }
}
