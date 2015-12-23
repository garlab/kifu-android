package fr.narwhals.go

import android.content.Context
import android.preference.PreferenceManager

class Config(context: Context) {
    val sp = PreferenceManager.getDefaultSharedPreferences(context)

    fun fullscreen(): Boolean {
        return sp.getBoolean("fullscreen", false)
    }

    fun showLastMove(): Boolean {
        return sp.getBoolean("show_last_move", true)
    }

    fun numberMoves(): Boolean {
        return sp.getBoolean("number_moves", false)
    }

    fun tagVariations(): Boolean {
        return sp.getBoolean("tag_variations", true)
    }

    fun aiPass(): Boolean {
        return sp.getBoolean("ai_pass", true)
    }
}
