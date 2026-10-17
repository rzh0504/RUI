package rui.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import rui.components.RuiCheckbox
import rui.components.RuiCheckboxState
import rui.components.RuiTriStateCheckbox
import rui.components.RuiDivider
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.components.RuiRadioButton
import rui.components.RuiRadioGroup
import rui.components.RuiRadioRow
import rui.components.RuiRangeSlider
import rui.components.RuiSlider
import rui.components.RuiStepper
import rui.components.RuiSwitch
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

@Composable
public fun TogglesScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    var wifi by remember { mutableStateOf(true) }
    var bluetooth by remember { mutableStateOf(false) }
    var sync by remember { mutableStateOf(true) }
    var stats by remember { mutableStateOf(false) }
    var volume by remember { mutableStateOf(0.6f) }
    var brightness by remember { mutableStateOf(0.25f) }
    var priceRange by remember { mutableStateOf(0.25f..0.75f) }
    var guests by rememberSaveable { mutableStateOf(2) }

    ContentColumn(contentPadding = contentPadding) {
        SectionLabel("Switch")
        RuiListGroup {
            RuiListItem(
                title = "Wi-Fi",
                trailing = { RuiSwitch(checked = wifi, onCheckedChange = { wifi = it }) },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Bluetooth",
                trailing = { RuiSwitch(checked = bluetooth, onCheckedChange = { bluetooth = it }) },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Unavailable",
                trailing = { RuiSwitch(checked = false, onCheckedChange = null) },
            )
        }

        SectionLabel("Checkbox")
        RuiListGroup {
            // The parent governs the two enabled rows below; the unavailable
            // row stays out of the aggregation.
            RuiListItem(
                title = "Select all",
                trailing = {
                    RuiTriStateCheckbox(
                        value = when {
                            sync && stats -> RuiCheckboxState.On
                            !sync && !stats -> RuiCheckboxState.Off
                            else -> RuiCheckboxState.Indeterminate
                        },
                        onValueChange = { next ->
                            val all = next == RuiCheckboxState.On
                            sync = all
                            stats = all
                        },
                    )
                },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Sync on Wi-Fi only",
                trailing = { RuiCheckbox(checked = sync, onCheckedChange = { sync = it }) },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Usage statistics",
                trailing = { RuiCheckbox(checked = stats, onCheckedChange = { stats = it }) },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Unavailable",
                trailing = { RuiCheckbox(checked = false, onCheckedChange = {}, enabled = false) },
            )
        }

        SectionLabel("Radio")
        var theme by remember { mutableStateOf("system") }
        RuiRadioGroup(
            value = theme,
            onValueChange = { theme = it },
        ) {
            RuiListGroup {
                RuiRadioRow(
                    value = "system",
                    title = "Follow system",
                    subtitle = "Match the device appearance",
                )
                RuiDivider(inset = RuiTheme.spacing.lg)
                RuiRadioRow(value = "light", title = "Light")
                RuiDivider(inset = RuiTheme.spacing.lg)
                RuiRadioRow(value = "dark", title = "Dark")
            }
        }
        RuiListGroup {
            var portion by remember { mutableStateOf("M") }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = RuiTheme.spacing.lg, vertical = RuiTheme.spacing.md),
                horizontalArrangement = Arrangement.spacedBy(RuiTheme.spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RuiText("Portion", style = RuiTheme.typography.titleSm, modifier = Modifier.weight(1f))
                listOf("S", "M", "L").forEach { size ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RuiRadioButton(
                            selected = portion == size,
                            onSelected = { portion = size },
                        )
                        Spacer(Modifier.width(RuiTheme.spacing.xs))
                        RuiText(size, style = RuiTheme.typography.bodyMd)
                    }
                }
            }
        }

        SectionLabel("Stepper")
        RuiListGroup {
            RuiListItem(
                title = "Guests",
                subtitle = "The value reads in the middle; segments disable at the bounds",
                trailing = {
                    RuiStepper(
                        value = guests,
                        onValueChange = { guests = it },
                        range = 1..6,
                    )
                },
            )
        }

        SectionLabel("Slider")
        RuiListGroup {
            Column(Modifier.padding(horizontal = RuiTheme.spacing.lg, vertical = RuiTheme.spacing.md)) {
                Row(Modifier.fillMaxWidth()) {
                    RuiText("Volume", style = RuiTheme.typography.titleSm)
                    Spacer(Modifier.weight(1f))
                    RuiText(
                        "${(volume * 100).roundToInt()}%",
                        style = RuiTheme.typography.bodySm,
                        color = RuiTheme.colors.contentMuted,
                    )
                }
                Spacer(Modifier.height(RuiTheme.spacing.md))
                RuiSlider(value = volume, onValueChange = { volume = it })
            }
            RuiDivider(inset = RuiTheme.spacing.lg)
            Column(Modifier.padding(horizontal = RuiTheme.spacing.lg, vertical = RuiTheme.spacing.md)) {
                RuiText("Brightness (4 steps)", style = RuiTheme.typography.titleSm)
                Spacer(Modifier.height(RuiTheme.spacing.md))
                RuiSlider(value = brightness, onValueChange = { brightness = it }, steps = 4)
            }
            RuiDivider(inset = RuiTheme.spacing.lg)
            Column(Modifier.padding(horizontal = RuiTheme.spacing.lg, vertical = RuiTheme.spacing.md)) {
                Row(Modifier.fillMaxWidth()) {
                    RuiText("Price range", style = RuiTheme.typography.titleSm)
                    Spacer(Modifier.weight(1f))
                    RuiText(
                        "${(priceRange.start * 100).roundToInt()}–${(priceRange.endInclusive * 100).roundToInt()}%",
                        style = RuiTheme.typography.bodySm,
                        color = RuiTheme.colors.contentMuted,
                    )
                }
                Spacer(Modifier.height(RuiTheme.spacing.md))
                // Push the thumbs together and keep pushing — the drag hands
                // over to the far thumb so the range spreads again.
                RuiRangeSlider(value = priceRange, onValueChange = { priceRange = it })
            }
        }
    }
}
