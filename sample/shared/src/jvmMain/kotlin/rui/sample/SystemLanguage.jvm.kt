package rui.sample

import java.util.Locale

internal actual fun systemLanguageTag(): String? =
    Locale.getDefault().language.takeIf { it.isNotEmpty() }
