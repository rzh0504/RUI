package rui.sample

import kotlinx.browser.window

internal actual fun systemLanguageTag(): String? =
    window.navigator.language.substringBefore('-').takeIf { it.isNotEmpty() }
