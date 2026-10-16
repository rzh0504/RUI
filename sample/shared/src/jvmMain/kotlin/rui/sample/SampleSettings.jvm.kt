package rui.sample

import java.util.prefs.Preferences

public actual object SampleSettings {
    private val preferences = Preferences.userNodeForPackage(SampleSettings::class.java)

    public actual fun getBoolean(key: String, default: Boolean): Boolean =
        preferences.getBoolean(key, default)

    public actual fun putBoolean(key: String, value: Boolean) {
        preferences.putBoolean(key, value)
    }

    public actual fun getString(key: String, default: String): String =
        preferences.get(key, default)

    public actual fun putString(key: String, value: String) {
        preferences.put(key, value)
    }
}
