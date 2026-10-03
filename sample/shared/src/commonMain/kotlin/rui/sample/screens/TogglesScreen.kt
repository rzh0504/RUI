package rui.sample.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import rui.components.RuiCheckbox
import rui.components.RuiDivider
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.components.RuiSwitch
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

@Composable
public fun TogglesScreen(modifier: Modifier = Modifier) {
    var wifi by remember { mutableStateOf(true) }
    var bluetooth by remember { mutableStateOf(false) }
    var sync by remember { mutableStateOf(true) }
    var stats by remember { mutableStateOf(false) }

    ContentColumn(modifier = modifier) {
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
    }
}
