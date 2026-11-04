package com.epicseven.autoadventure

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs

class ScreenAnalyzer {

    enum class GameState {
        READY,
        SELECT_SUPPORTER,
        SELECT_TEAM,
        STORY_SKIP,
        SKIP_CONFIRM,
        BATTLE,
        CLEAR,
        REWARDS,
        STORY_CLEAR,
        UNKNOWN
    }

    data class ScreenTemplate(
        val state: GameState,
        val bitmap: Bitmap
    )

    data class AnalysisResult(
        val state: GameState,
        val confidence: Float
    )

    private val templates =
        mutableListOf<ScreenTemplate>()

    fun addTemplate(
        state: GameState,
        bitmap: Bitmap
    ) {
        templates.add(
            ScreenTemplate(
                state,
                Bitmap.createScaledBitmap(
                    bitmap,
                    64,
                    36,
                    true
                )
            )
        )
    }

    fun analyze(
        screenshot: Bitmap
    ): AnalysisResult {

        if (templates.isEmpty()) {
            return AnalysisResult(
                GameState.UNKNOWN,
                0f
            )
        }

        val scaled = Bitmap.createScaledBitmap(
            screenshot,
            64,
            36,
            true
        )

        var bestState = GameState.UNKNOWN
        var bestScore = 0f

        for (template in templates) {
            val score = compare(
                scaled,
                template.bitmap
            )

            if (score > bestScore) {
                bestScore = score
                bestState = template.state
            }
        }

        if (scaled !== screenshot) {
            scaled.recycle()
        }

        return if (bestScore >= 0.85f) {
            AnalysisResult(bestState, bestScore)
        } else {
            AnalysisResult(
                GameState.UNKNOWN,
                bestScore
            )
        }
    }

    private fun compare(
        first: Bitmap,
        second: Bitmap
    ): Float {

        var difference = 0.0

        val width = 64
        val height = 36

        for (y in 0 until height) {
            for (x in 0 until width) {

                val a = first.getPixel(x, y)
                val b = second.getPixel(x, y)

                difference += abs(
                    Color.red(a) - Color.red(b)
                )

                difference += abs(
                    Color.green(a) - Color.green(b)
                )

                difference += abs(
                    Color.blue(a) - Color.blue(b)
                )
            }
        }

        val maximum =
            width * height * 3.0 * 255.0

        return (
            1.0 - difference / maximum
        ).toFloat().coerceIn(0f, 1f)
    }

    fun clearTemplates() {
        templates.forEach {
            it.bitmap.recycle()
        }
        templates.clear()
    }
}
