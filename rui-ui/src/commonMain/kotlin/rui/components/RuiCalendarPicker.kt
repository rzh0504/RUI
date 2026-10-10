package rui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiText
import rui.foundation.rememberRuiIndication
import rui.theme.RuiTheme

/** One rendered day of the six-week grid; adjacent-month days fill the edges. */
internal data class DayCell(val date: RuiDate, val inMonth: Boolean)

/**
 * A date picker on a month grid — the calendar counterpart of
 * [RuiDatePicker]'s wheels, for scanning a month rather than spinning to a
 * known day. Six fixed weeks render per month, edges filled with
 * adjacent-month days (tappable, muted); weeks start on Monday.
 *
 * Like the wheels, the picker is pure: no calendar platform API, no clock —
 * pass [today] to have the real current day marked in the accent color when
 * it is not the selection.
 *
 * @param value The selected date; `null` shows no selection. Picking a day
 *   reports through [onValueChange]; the view follows external value changes.
 * @param today The real current date, or `null` for no marker.
 * @param monthNames Twelve month labels for the header. Defaults to
 *   [RuiTheme.strings]'s month names and header format; passing this
 *   explicitly composes the header as `"<name> <year>"` from the given names.
 * @param weekdayNames Seven weekday labels, Monday first, overriding
 *   [RuiTheme.strings]'s `weekdayNames`.
 * @param yearRange Months outside the range are unreachable — the chevrons
 *   disable at the bounds and days outside the range render disabled. An
 *   external [value] outside the range stays the caller's (the picker never
 *   rewrites it); the grid opens on the nearest in-range year of the same
 *   calendar month.
 */
@Composable
public fun RuiCalendarPicker(
    value: RuiDate?,
    onValueChange: (RuiDate) -> Unit,
    modifier: Modifier = Modifier,
    today: RuiDate? = null,
    yearRange: IntRange = 1900..2100,
    monthNames: List<String>? = null,
    weekdayNames: List<String>? = null,
) {
    val colors = RuiTheme.colors
    val spacing = RuiTheme.spacing
    val strings = RuiTheme.strings

    val names = monthNames ?: strings.monthNames
    val weekdays = weekdayNames ?: strings.weekdayNames
    check(names.size == 12) { "monthNames must have 12 entries, was ${names.size}" }
    check(weekdays.size == 7) { "weekdayNames must have 7 entries (Monday first), was ${weekdays.size}" }
    check(!yearRange.isEmpty()) { "yearRange must not be empty" }

    val anchor = value ?: today ?: RuiDate(yearRange.first, 1, 1)
    var cursor by remember(yearRange) {
        mutableStateOf(calendarCursor(anchor, yearRange))
    }
    // The view follows the value when it changes from outside (including a
    // pick on an adjacent-month day); browsing months only moves the cursor.
    LaunchedEffect(value) {
        if (value != null) {
            cursor = calendarCursor(value, yearRange)
        }
    }

    // The chevrons' enabled state and the accepted shifts are the same
    // decision, so a disabled chevron and a rejected shift can never disagree.
    val canGoBack = shiftedCursor(cursor, -1, yearRange) != null
    val canGoForward = shiftedCursor(cursor, 1, yearRange) != null

    fun moveCursor(delta: Int) {
        shiftedCursor(cursor, delta, yearRange)?.let { cursor = it }
    }

    Column(modifier = modifier.padding(spacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RuiIconButton(
                onClick = { moveCursor(-1) },
                enabled = canGoBack,
                variant = RuiButtonVariant.Plain,
                size = RuiButtonSize.Sm,
            ) {
                RuiIcon(imageVector = RuiIcons.ChevronLeft, contentDescription = strings.previousMonth)
            }
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                RuiText(
                    text = if (monthNames == null) {
                        strings.monthHeader(cursor.year, cursor.month)
                    } else {
                        "${names[cursor.month - 1]} ${cursor.year}"
                    },
                    style = RuiTheme.typography.titleMd,
                    color = colors.content,
                )
            }
            RuiIconButton(
                onClick = { moveCursor(1) },
                enabled = canGoForward,
                variant = RuiButtonVariant.Plain,
                size = RuiButtonSize.Sm,
            ) {
                RuiIcon(imageVector = RuiIcons.ChevronRight, contentDescription = strings.nextMonth)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = spacing.sm, bottom = spacing.xs),
        ) {
            weekdays.forEach { label ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    RuiText(
                        text = label,
                        style = RuiTheme.typography.labelSm,
                        color = colors.contentMuted,
                    )
                }
            }
        }

        val cells = remember(cursor.year, cursor.month) { monthCells(cursor.year, cursor.month) }
        cells.chunked(7).forEach { week ->
            Row {
                week.forEach { cell ->
                    CalendarDayCell(
                        cell = cell,
                        selected = cell.date == value,
                        isToday = cell.date == today,
                        selectedContainer = colors.primary,
                        selectedContent = colors.onPrimary,
                        todayContent = colors.primary,
                        inMonthContent = colors.content,
                        outOfMonthContent = colors.contentDisabled,
                        enabled = cell.date.year in yearRange,
                        onSelect = onValueChange,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/** One tappable day: a circle filling its grid square when selected. */
@Composable
private fun CalendarDayCell(
    cell: DayCell,
    selected: Boolean,
    isToday: Boolean,
    selectedContainer: Color,
    selectedContent: Color,
    todayContent: Color,
    inMonthContent: Color,
    outOfMonthContent: Color,
    enabled: Boolean,
    onSelect: (RuiDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = RuiTheme.spacing
    val interactionSource = remember { MutableInteractionSource() }
    val contentColor = when {
        selected -> selectedContent
        isToday -> todayContent
        cell.inMonth -> inMonthContent
        else -> outOfMonthContent
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(spacing.xs)
            .clip(CircleShape)
            .background(if (selected) selectedContainer else Color.Transparent, CircleShape)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = rememberRuiIndication(),
                role = Role.Button,
                enabled = enabled,
                onClick = { onSelect(cell.date) },
            ),
        contentAlignment = Alignment.Center,
    ) {
        RuiText(
            text = cell.date.day.toString(),
            style = RuiTheme.typography.bodyMd,
            color = contentColor,
        )
    }
}

/**
 * The six-week grid of [year]/[month]: leading days of the previous month,
 * the month's own days, trailing days of the next — always exactly 42 cells.
 */
internal fun monthCells(year: Int, month: Int): List<DayCell> {
    val leading = weekdayFromMonday(year, month, 1)
    val previous = shiftMonth(RuiDate(year, month, 1), -1)
    val next = shiftMonth(RuiDate(year, month, 1), 1)
    val previousDays = daysInMonth(previous.year, previous.month)

    return buildList {
        for (offset in leading - 1 downTo 0) {
            add(DayCell(RuiDate(previous.year, previous.month, previousDays - offset), inMonth = false))
        }
        for (day in 1..daysInMonth(year, month)) {
            add(DayCell(RuiDate(year, month, day), inMonth = true))
        }
        var day = 1
        while (size < 42) {
            add(DayCell(RuiDate(next.year, next.month, day), inMonth = false))
            day++
        }
    }
}

private fun shiftMonth(date: RuiDate, delta: Int): RuiDate {
    val total = date.year * 12 + date.month - 1 + delta
    return RuiDate(year = total.floorDiv(12), month = total.mod(12) + 1, day = 1)
}

/**
 * The month the grid opens on for an external anchor: the year clamps into
 * [yearRange] (the grid can only present in-range months) while the calendar
 * month is preserved, and the cursor always sits on day one.
 */
internal fun calendarCursor(anchor: RuiDate, yearRange: IntRange): RuiDate =
    RuiDate(anchor.year.coerceIn(yearRange), anchor.month, 1)

/**
 * The cursor shifted by [delta] months, or `null` when the shift would leave
 * [yearRange] — the same decision that backs the chevrons' enabled state.
 */
internal fun shiftedCursor(cursor: RuiDate, delta: Int, yearRange: IntRange): RuiDate? {
    val candidate = shiftMonth(cursor, delta)
    return if (candidate.year in yearRange) candidate else null
}

/**
 * Days since 1970-01-01 of a Gregorian civil date (Howard Hinnant's
 * `days_from_civil`, pure integer math — no calendar platform API).
 */
internal fun daysFromCivil(year: Int, month: Int, day: Int): Long {
    val y = if (month <= 2) year - 1 else year
    val era = (if (y >= 0) y else y - 399) / 400
    val yearOfEra = y - era * 400
    val dayOfYear = (153 * (if (month > 2) month - 3 else month + 9) + 2) / 5 + day - 1
    val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
    return era * 146097L + dayOfEra - 719468L
}

/** Day of week of a civil date: Monday = 0 … Sunday = 6 (1970-01-01 = Thursday). */
internal fun weekdayFromMonday(year: Int, month: Int, day: Int): Int =
    (daysFromCivil(year, month, day) + 3L).mod(7L).toInt()
