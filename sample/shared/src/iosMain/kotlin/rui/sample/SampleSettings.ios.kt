package rui.sample

import platform.Foundation.NSUserDefaults

public actual object SampleSettings {
    private val defaults = NSUserDefaults.standardUserDefaults

    public actual fun getBoolean(key: String, default: Boolean): Boolean =
        if (defaults.objectForKey(key) != null) defaults.boolForKey(key) else default

    public actual fun putBoolean(key: String, value: Boolean) {
        defaults.setBool(value, forKey = key)
    }

    public actual fun getString(key: String, default: String): String =
        defaults.stringForKey(key) ?: default

    public actual fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }
}
