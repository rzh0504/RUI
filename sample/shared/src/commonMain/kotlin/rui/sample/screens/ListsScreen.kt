package rui.sample.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rui.components.RuiDivider
import rui.components.RuiListItem
import rui.components.RuiListSection
import rui.components.RuiSwitch
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

@Composable
public fun ListsScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    var wifi by rememberSaveable { mutableStateOf(true) }
    var bluetooth by rememberSaveable { mutableStateOf(false) }
    var cellular by rememberSaveable { mutableStateOf(true) }

    ContentColumn(contentPadding = contentPadding) {
        RuiText(
            text = "The settings page skeleton: a muted header, the grouped " +
                "card of rows and an optional muted footer note.",
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
            modifier = Modifier.fillMaxWidth(),
        )

        SectionLabel("Header and footer")
        RuiListSection(
            header = "Connectivity",
            footer = "Turn off Wi-Fi to download over cellular data.",
        ) {
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
                title = "Cellular data",
                trailing = { RuiSwitch(checked = cellular, onCheckedChange = { cellular = it }) },
            )
        }

        SectionLabel("Footer only")
        RuiListSection(
            header = null,
            footer = "Deleted drafts are removed from every device signed in " +
                "to this account.",
        ) {
            RuiListItem(title = "Clear drafts", showChevron = true)
        }
    }
}
