package rui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import rui.foundation.RuiText
import rui.theme.RuiTheme

/**
 * A calendar date. [month] is 1..12 so it maps straight onto any calendar
 * API the caller uses; [day] 1..[daysInMonth].
 */
public data class RuiDate(
    public val year: Int,
    public val month: Int,
    public val day: Int,
) {
    init {
        require(month in 1..12) { "month must be in 1..12, was $month" }
        require(day in 1..daysInMonth(year, month)) {
            "day must be in 1..${daysInMonth(year, month)} for $year-$month, was $day"
        }
    }
}

/** Number of days in [month] (1..12) of [year], Gregorian rules. */
public fun daysInMonth(year: Int, month: Int): Int {
    require(month in 1..12) { "month must be in 1..12, was $month" }
    return when (month) {
    2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
    4, 6, 9, 11 -> 30
    else -> 31
    }
}

private fun clampToMonth(date: RuiDate): RuiDate =
    date.copy(day = date.day.coerceIn(1, daysInMonth(date.year, date.month)))

/**
 * A date picker on month, day and year wheels (fixed month-day-year column
 * order; month labels follow [RuiTheme.strings]). Changing the month or year
 * clamps the day to the new month — February 30 becomes February 28 — and
 * the day wheel animates to the clamped value.
 *
 * An external [value] whose year falls outside [yearRange] shows on the year
 * wheel clamped to the nearest boundary; the value is never rewritten on its
 * own — the first wheel turn reports from the clamped working date.
 *
 * @param monthNames Twelve month labels, overriding
 *   [RuiTheme.strings]'s `monthNames`.
 * @param yearRange Years offered by the year wheel, ascending.
 */
@Composable
public fun RuiDatePicker(
    value: RuiDate,
    onValueChange: (RuiDate) -> Unit,
    modifier: Modifier = Modifier,
    yearRange: IntRange = 1900..2100,
    monthNames: List<String>? = null,
) {
    val typography = RuiTheme.typography
    val spacing = RuiTheme.spacing

    val names = monthNames ?: RuiTheme.strings.monthNames
    check(names.size == 12) { "monthNames must have 12 entries, was ${names.size}" }
    check(!yearRange.isEmpty()) { "yearRange must not be empty" }

    // The year wheel can only present in-range years, so the working date
    // clamps its year to the nearest boundary; the value itself is never
    // rewritten until a wheel turns.
    val date = clampToMonth(value.copy(year = value.year.coerceIn(yearRange)))
    val years = yearRange.toList()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RuiWheelPicker(
            items = names,
            selectedIndex = date.month - 1,
            onSelectedIndexChange = { index ->
                onValueChange(clampToMonth(date.copy(month = index + 1)))
            },
            modifier = Modifier.weight(1.3f),
        ) { month ->
            RuiText(text = month, style = typography.bodyLg)
        }
        RuiWheelPicker(
            items = (1..daysInMonth(date.year, date.month)).toList(),
            selectedIndex = date.day - 1,
            onSelectedIndexChange = { index ->
                onValueChange(date.copy(day = index + 1))
            },
            modifier = Modifier.weight(1f),
        ) { day ->
            RuiText(text = day.toString(), style = typography.bodyLg)
        }
        RuiWheelPicker(
            items = years,
            selectedIndex = (date.year - yearRange.first).coerceIn(0, years.lastIndex),
            onSelectedIndexChange = { index ->
                onValueChange(clampToMonth(date.copy(year = yearRange.first + index)))
            },
            modifier = Modifier.weight(1f),
        ) { year ->
            RuiText(text = year.toString(), style = typography.bodyLg)
        }
    }
}
