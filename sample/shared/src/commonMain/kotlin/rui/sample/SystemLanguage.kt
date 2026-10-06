package rui.sample

/**
 * The system's preferred language tag ("en", "zh", …), or `null` when it
 * cannot be read. Sample-side capability: Rui itself never touches platform
 * locale APIs — apps decide which [rui.theme.RuiStrings] to provide.
 */
internal expect fun systemLanguageTag(): String?
