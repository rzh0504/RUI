package rui.sample

import android.content.Context
import android.content.SharedPreferences

public actual object SampleSettings {
    private lateinit var preferences: SharedPreferences

    /**
     * Call once from the host activity before the first read; reads before
     * that fall back to the defaults instead of crashing.
     */
    public fun init(context: Context) {
        preferences = context.getSharedPreferences("rui_sample_settings", Context.MODE_PRIVATE)
    }

    public actual fun getBoolean(key: String, default: Boolean): Boolean =
        if (::preferences.isInitialized) preferences.getBoolean(key, default) else default

    public actual fun putBoolean(key: String, value: Boolean) {
        if (::preferences.isInitialized) preferences.edit().putBoolean(key, value).apply()
    }

    public actual fun getString(key: String, default: String): String =
        if (::preferences.isInitialized) preferences.getString(key, default) ?: default else default

    public actual fun putString(key: String, value: String) {
        if (::preferences.isInitialized) preferences.edit().putString(key, value).apply()
    }
}
