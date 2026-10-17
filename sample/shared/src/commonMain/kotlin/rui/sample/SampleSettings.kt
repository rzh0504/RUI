package rui.sample

/**
 * Tiny key-value persistence for the catalog's appearance settings:
 * SharedPreferences on Android, NSUserDefaults on iOS. Sample-side only —
 * Rui itself never persists anything.
 */
public expect object SampleSettings {
    public fun getBoolean(key: String, default: Boolean): Boolean
    public fun putBoolean(key: String, value: Boolean)
    public fun getString(key: String, default: String): String
    public fun putString(key: String, value: String)
}
