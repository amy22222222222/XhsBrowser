package com.example.xhsbrowser.ui.fragments

import android.content.Context

object PreferenceManager {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_AUTO_SCROLL = "auto_scroll"
    private const val KEY_SCROLL_INTERVAL = "scroll_interval"

    fun setAutoScroll(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, 0)
            .edit().putBoolean(KEY_AUTO_SCROLL, enabled).apply()
    }

    fun isAutoScroll(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, 0)
            .getBoolean(KEY_AUTO_SCROLL, true)
    }

    fun setScrollInterval(context: Context, ms: Long) {
        context.getSharedPreferences(PREFS_NAME, 0)
            .edit().putLong(KEY_SCROLL_INTERVAL, ms).apply()
    }

    fun getScrollInterval(context: Context): Long {
        return context.getSharedPreferences(PREFS_NAME, 0)
            .getLong(KEY_SCROLL_INTERVAL, 2000L)
    }
}
