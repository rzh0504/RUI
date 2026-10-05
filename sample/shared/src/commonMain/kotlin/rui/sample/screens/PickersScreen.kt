package rui.sample.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rui.components.RuiDatePicker
import rui.components.RuiDate
import rui.components.RuiDivider
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.components.RuiTimeOfDay
import rui.components.RuiTimePicker
import rui.foundation.RuiSurface
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

private val MonthNames = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)

@Composable
public fun PickersScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    var time12 by remember { mutableStateOf(RuiTimeOfDay(9, 41)) }
    var time24 by remember { mutableStateOf(RuiTimeOfDay(21, 7)) }
    var date by remember { mutableStateOf(RuiDate(2026, 10, 6)) }

    ContentColumn(contentPadding = contentPadding) {
        RuiText(
            text = "Wheel drums snap the centered row; rows fade towards the edges " +
                "and every row the drum passes reports with a selection tick. " +
                "The date picker clamps the day when the month shortens — try " +
                "January 31, then spin to February.",
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
            modifier = Modifier.fillMaxWidth(),
        )

        SectionLabel("Time")
        RuiSurface {
            RuiTimePicker(
                value = time12,
                onValueChange = { time12 = it },
                is24Hour = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = RuiTheme.spacing.sm),
            )
        }
        RuiListGroup {
            RuiListItem(
                title = "12-hour value",
                subtitle = formatTime12(time12),
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "24-hour wheel",
                subtitle = formatTime24(time24),
            )
        }
        RuiSurface {
            RuiTimePicker(
                value = time24,
                onValueChange = { time24 = it },
                is24Hour = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = RuiTheme.spacing.sm),
            )
        }

        SectionLabel("Date")
        RuiSurface {
            RuiDatePicker(
                value = date,
                onValueChange = { date = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = RuiTheme.spacing.sm),
            )
        }
        RuiListGroup {
            RuiListItem(
                title = "Selected date",
                subtitle = "${MonthNames[date.month - 1]} ${date.day}, ${date.year}",
            )
        }
    }
}

private fun formatTime12(time: RuiTimeOfDay): String {
    val h = time.hour % 12
    val hour = if (h == 0) 12 else h
    val period = if (time.hour >= 12) "PM" else "AM"
    return "$hour:${time.minute.toString().padStart(2, '0')} $period"
}

private fun formatTime24(time: RuiTimeOfDay): String =
    "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
