package rui.sample.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import rui.components.RuiCheckbox
import rui.components.RuiSwitch
import rui.sample.ui.DetailScaffold
import rui.sample.ui.GroupedCard
import rui.sample.ui.ListRow
import rui.sample.ui.RowSeparator
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

@Composable
public fun TogglesScreen(onBack: () -> Unit) {
    var wifi by remember { mutableStateOf(true) }
    var bluetooth by remember { mutableStateOf(false) }
    var sync by remember { mutableStateOf(true) }
    var stats by remember { mutableStateOf(false) }

    DetailScaffold(title = "Switch & checkbox", onBack = onBack) {
        SectionLabel("Switch")
        GroupedCard {
            ListRow(
                title = "Wi-Fi",
                trailing = { RuiSwitch(checked = wifi, onCheckedChange = { wifi = it }) },
            )
            RowSeparator()
            ListRow(
                title = "Bluetooth",
                trailing = { RuiSwitch(checked = bluetooth, onCheckedChange = { bluetooth = it }) },
            )
            RowSeparator()
            ListRow(
                title = "Unavailable",
                trailing = { RuiSwitch(checked = false, onCheckedChange = null) },
            )
        }

        SectionLabel("Checkbox")
        GroupedCard {
            ListRow(
                title = "Sync on Wi-Fi only",
                trailing = { RuiCheckbox(checked = sync, onCheckedChange = { sync = it }) },
            )
            RowSeparator()
            ListRow(
                title = "Usage statistics",
                trailing = { RuiCheckbox(checked = stats, onCheckedChange = { stats = it }) },
            )
            RowSeparator()
            ListRow(
                title = "Unavailable",
                trailing = { RuiCheckbox(checked = false, onCheckedChange = {}, enabled = false) },
            )
        }
    }
}
