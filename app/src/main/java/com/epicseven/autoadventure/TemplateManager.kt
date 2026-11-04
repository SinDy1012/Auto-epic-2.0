package com.epicseven.autoadventure

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log

class TemplateManager(
    private val context: Context,
    private val analyzer: ScreenAnalyzer
) {

    companion object {
        private const val TAG = "E7TemplateManager"
        private const val FOLDER = "templates"
    }

    private val templateFiles = mapOf(
        "ready.png" to ScreenAnalyzer.GameState.READY,

        "select_supporter.png" to
            ScreenAnalyzer.GameState.SELECT_SUPPORTER,

        "select_team.png" to
            ScreenAnalyzer.GameState.SELECT_TEAM,

        "story_skip.png" to
            ScreenAnalyzer.GameState.STORY_SKIP,

        "skip_confirm.png" to
            ScreenAnalyzer.GameState.SKIP_CONFIRM,

        "battle.png" to
            ScreenAnalyzer.GameState.BATTLE,

        "clear.png" to
            ScreenAnalyzer.GameState.CLEAR,

        "rewards.png" to
            ScreenAnalyzer.GameState.REWARDS,

        "story_clear.png" to
            ScreenAnalyzer.GameState.STORY_CLEAR
    )

    fun loadTemplates(): Int {

        analyzer.clearTemplates()

        var loaded = 0

        for ((fileName, state) in templateFiles) {

            val path = "$FOLDER/$fileName"

            try {
                context.assets.open(path).use { stream ->

                    val bitmap =
                        BitmapFactory.decodeStream(stream)

                    if (bitmap != null) {
                        analyzer.addTemplate(
                            state,
                            bitmap
                        )

                        bitmap.recycle()
                        loaded++

                        Log.d(
                            TAG,
                            "Loaded: $fileName"
                        )
                    }
                }

            } catch (e: Exception) {
                Log.w(
                    TAG,
                    "Cannot load: $path",
                    e
                )
            }
        }

        Log.d(
            TAG,
            "Total templates loaded: $loaded"
        )

        return loaded
    }
}
