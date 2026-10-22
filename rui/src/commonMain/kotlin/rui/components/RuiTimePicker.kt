package rui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import rui.foundation.RuiText
import rui.theme.RuiTheme

/** A wall-clock time in 24-hour terms: [hour] 0..23, [minute] 0..59. */
public data class RuiTimeOfDay(
    public val hour: Int,
    public val minute: Int,
) {
    init {
        require(hour in 0..23) { "hour must be in 0..23, was $hour" }
        require(minute in 0..59) { "minute must be in 0..59, was $minute" }
    }
}

/**
 * A time picker on two (24-hour) or three (12-hour) wheels. The value is
 * canonical [RuiTimeOfDay] — the 12-hour presentation maps to and from it,
 * so callers never handle AM/PM bookkeeping.
 *
 * @param is24Hour Renders a 0–23 hour wheel instead of 1–12 plus a period wheel.
 * @param periodLabels Labels of the period wheel in 12-hour mode, overriding
 *   [RuiTheme.strings]'s `periodLabels`.
 */
@Composable
public fun RuiTimePicker(
    value: RuiTimeOfDay,
    onValueChange: (RuiTimeOfDay) -> Unit,
    modifier: Modifier = Modifier,
    is24Hour: Boolean = true,
    periodLabels: List<String>? = null,
) {
    val typography = RuiTheme.typography
    val spacing = RuiTheme.spacing

    val labels = periodLabels ?: RuiTheme.strings.periodLabels

    val hourItems = if (is24Hour) (0..23).toList() else (1..12).toList()
    val minuteItems = (0..59).toList()
    val isPm = value.hour >= 12
    val displayedHour = if (is24Hour) {
        value.hour
    } else {
        val h = value.hour % 12
        if (h == 0) 12 else h
    }

    // 12-hour display hour back to 0..23, honoring the current period.
    fun hour24(displayHour: Int): Int =
        if (is24Hour) displayHour else (displayHour % 12) + if (isPm) 12 else 0

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RuiWheelPicker(
            items = hourItems,
            selectedIndex = hourItems.indexOf(displayedHour),
            onSelectedIndexChange = { index ->
                onValueChange(RuiTimeOfDay(hour24(hourItems[index]), value.minute))
            },
            modifier = Modifier.weight(1f),
        ) { hour ->
            RuiText(text = hour.toString(), style = typography.bodyLg)
        }
        RuiWheelPicker(
            items = minuteItems,
            selectedIndex = value.minute,
            onSelectedIndexChange = { index ->
                onValueChange(RuiTimeOfDay(value.hour, minuteItems[index]))
            },
            modifier = Modifier.weight(1f),
        ) { minute ->
            RuiText(
                text = minute.toString().padStart(2, '0'),
                style = typography.bodyLg,
            )
        }
        if (!is24Hour) {
            RuiWheelPicker(
                items = labels,
                selectedIndex = if (isPm) 1 else 0,
                onSelectedIndexChange = { index ->
                    val hour = (value.hour % 12) + if (index == 1) 12 else 0
                    onValueChange(RuiTimeOfDay(hour, value.minute))
                },
                modifier = Modifier.weight(1f),
            ) { label ->
                RuiText(text = label, style = typography.bodyLg)
            }
        }
    }
}
