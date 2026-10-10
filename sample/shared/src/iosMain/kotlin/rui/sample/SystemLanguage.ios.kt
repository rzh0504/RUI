package rui.sample

import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

internal actual fun systemLanguageTag(): String? =
    (NSLocale.preferredLanguages.firstOrNull() as? String)
        ?.substringBefore('-')
        ?.takeIf { it.isNotEmpty() }
