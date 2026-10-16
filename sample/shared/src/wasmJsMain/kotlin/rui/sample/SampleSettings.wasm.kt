package rui.sample

import kotlinx.browser.window

public actual object SampleSettings {
    private val storage
        get() = window.localStorage

    public actual fun getBoolean(key: String, default: Boolean): Boolean =
        storage.getItem(key)?.toBooleanStrictOrNull() ?: default

    public actual fun putBoolean(key: String, value: Boolean) {
        storage.setItem(key, value.toString())
    }

    public actual fun getString(key: String, default: String): String =
        storage.getItem(key) ?: default

    public actual fun putString(key: String, value: String) {
        storage.setItem(key, value)
    }
}
