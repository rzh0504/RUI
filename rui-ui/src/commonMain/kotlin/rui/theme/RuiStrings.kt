package rui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The strings Rui renders itself: accessibility labels of internal
 * affordances (a top app bar's back chevron, a stepper's segments) and the
 * default name sets of the date and time pickers. Components resolve them
 * through [RuiTheme.strings]; an explicit string or name parameter on a
 * component always overrides the value from here.
 *
 * English and Simplified Chinese ship built in. To add a language, implement
 * the interface — interface delegation keeps a partial override a one-liner:
 *
 * ```
 * class German : RuiStrings by RuiStringsDefaults.English {
 *     override val monthNames = listOf(
 *         "Januar", "Februar", "März", "April", "Mai", "Juni",
 *         "Juli", "August", "September", "Oktober", "November", "Dezember",
 *     )
 *     override fun monthHeader(year: Int, month: Int) = "$month. $year"
 * }
 * ```
 *
 * One delegation caveat: a default like `monthHeader` reads [monthNames]
 * through the *delegate*, so overriding `monthNames` alone leaves the header
 * formatting the delegate's names — override `monthHeader` alongside it.
 *
 * Which language an app runs in is the app's decision (usually the system
 * locale) — Rui never reads platform locale APIs itself. Provide the
 * implementation at the [RuiTheme] level, or scope a different one to a
 * subtree with [ProvideRuiStrings].
 */
public interface RuiStrings {
    /** Label of the top app bar's back affordance. */
    public val back: String

    /** Label of the search bar's clear affordance. */
    public val clear: String

    /** Label of a banner's dismiss affordance. */
    public val dismiss: String

    /** Label of a chip's remove affordance. */
    public val remove: String

    /** Label of the stepper's decrease segment. */
    public val decrease: String

    /** Label of the stepper's increase segment. */
    public val increase: String

    /** Label of the calendar grid's previous-month chevron. */
    public val previousMonth: String

    /** Label of the calendar grid's next-month chevron. */
    public val nextMonth: String

    /** Twelve month labels, January first. */
    public val monthNames: List<String>

    /** Seven weekday labels, Monday first. */
    public val weekdayNames: List<String>

    /** The two 12-hour period labels: ante meridiem, then post meridiem. */
    public val periodLabels: List<String>

    /**
     * Header of a calendar month grid, e.g. "January 2026". The default
     * renders [monthNames] before the year; languages with the year-first
     * convention override this.
     */
    public fun monthHeader(year: Int, month: Int): String = "${monthNames[month - 1]} $year"
}

/** The built-in [RuiStrings] implementations. */
public object RuiStringsDefaults {

    /** English strings. */
    public val English: RuiStrings = object : RuiStrings {
        override val back = "Back"
        override val clear = "Clear"
        override val dismiss = "Dismiss"
        override val remove = "Remove"
        override val decrease = "Decrease"
        override val increase = "Increase"
        override val previousMonth = "Previous month"
        override val nextMonth = "Next month"
        override val monthNames = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December",
        )
        override val weekdayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        override val periodLabels = listOf("AM", "PM")
    }

    /** Simplified Chinese strings (简体中文). */
    public val SimplifiedChinese: RuiStrings = object : RuiStrings {
        override val back = "返回"
        override val clear = "清除"
        override val dismiss = "关闭"
        override val remove = "移除"
        override val decrease = "减少"
        override val increase = "增大"
        override val previousMonth = "上个月"
        override val nextMonth = "下个月"
        override val monthNames = listOf(
            "1月", "2月", "3月", "4月", "5月", "6月",
            "7月", "8月", "9月", "10月", "11月", "12月",
        )
        override val weekdayNames = listOf("一", "二", "三", "四", "五", "六", "日")
        override val periodLabels = listOf("上午", "下午")

        override fun monthHeader(year: Int, month: Int): String = "${year}年${month}月"
    }
}

internal val LocalRuiStrings = staticCompositionLocalOf<RuiStrings> { RuiStringsDefaults.English }

/**
 * Overrides [RuiStrings] for [content]'s subtree — the scoped alternative to
 * passing `strings` into [RuiTheme]. The value resolves through
 * [RuiTheme.strings] inside [content].
 */
@Composable
public fun ProvideRuiStrings(
    strings: RuiStrings,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalRuiStrings provides strings, content = content)
}
