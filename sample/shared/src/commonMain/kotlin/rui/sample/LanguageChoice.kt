package rui.sample

import rui.theme.RuiStrings
import rui.theme.RuiStringsDefaults

/** What the catalog feeds [rui.theme.RuiTheme]'s `strings` parameter. */
public enum class LanguageChoice {
    System,
    English,
    SimplifiedChinese,
}

/** Resolves the choice into the [RuiStrings] provided at the theme root. */
internal fun LanguageChoice.resolveStrings(): RuiStrings = when (this) {
    LanguageChoice.System -> when (systemLanguageTag()?.lowercase()) {
        "zh" -> RuiStringsDefaults.SimplifiedChinese
        else -> RuiStringsDefaults.English
    }
    LanguageChoice.English -> RuiStringsDefaults.English
    LanguageChoice.SimplifiedChinese -> RuiStringsDefaults.SimplifiedChinese
}
