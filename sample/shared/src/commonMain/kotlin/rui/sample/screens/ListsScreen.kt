package rui.sample.screens

import androidx.compose.foundation.layout.Column
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
import rui.components.RuiRadioGroup
import rui.components.RuiRadioRow
import rui.components.RuiSelectionOption
import rui.components.RuiSelectionRow
import rui.components.RuiSwitch
import rui.foundation.BlurBackdrop
import rui.foundation.RuiGlassMaterial
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

private enum class Appearance {
    Automatic, Light, Dark,
}

private enum class Network {
    Studio5G, Studio24G, Guest,
}

private enum class Language {
    English, Chinese, French,
}

@Composable
public fun ListsScreen(
    backdrop: BlurBackdrop?,
    material: RuiGlassMaterial,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var wifi by rememberSaveable { mutableStateOf(true) }
    var bluetooth by rememberSaveable { mutableStateOf(false) }
    var cellular by rememberSaveable { mutableStateOf(true) }
    var appearance by rememberSaveable { mutableStateOf(Appearance.Automatic) }
    var network by rememberSaveable { mutableStateOf(Network.Studio5G) }
    var language by rememberSaveable { mutableStateOf(Language.English) }

    ContentColumn(contentPadding = contentPadding) {
        RuiText(
            text = "The settings page skeleton: a muted header, the grouped " +
                "card of rows and an optional muted footer note. Rows come in " +
                "the archetypes a settings page needs — toggles, exclusive " +
                "selection, menu-driven value rows, and value rows that lead " +
                "somewhere.",
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
            modifier = Modifier.fillMaxWidth(),
        )

        SectionLabel("Toggles")
        RuiListSection(
            header = "Connectivity",
            footer = "Turn off Wi-Fi to download over cellular data.",
        ) {
            RuiListItem(
                title = "Wi-Fi",
                subtitle = "Studio 5G",
                trailing = { RuiSwitch(checked = wifi, onCheckedChange = { wifi = it }) },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Bluetooth",
                subtitle = "Discoverable as \"Rui Catalog\"",
                trailing = { RuiSwitch(checked = bluetooth, onCheckedChange = { bluetooth = it }) },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Cellular data",
                trailing = { RuiSwitch(checked = cellular, onCheckedChange = { cellular = it }) },
            )
        }

        SectionLabel("Exclusive selection")
        RuiListSection(
            header = "Appearance",
            footer = "Automatic follows the system's light or dark mode.",
        ) {
            // The group itself lays out as a Box — the column keeps the rows
            // stacked inside the section's card.
            RuiRadioGroup(
                value = appearance,
                onValueChange = { appearance = it },
            ) {
                Column {
                    RuiRadioRow(Appearance.Automatic, "Automatic")
                    RuiDivider(inset = RuiTheme.spacing.lg)
                    RuiRadioRow(Appearance.Light, "Light", subtitle = "Always the light appearance")
                    RuiDivider(inset = RuiTheme.spacing.lg)
                    RuiRadioRow(Appearance.Dark, "Dark", subtitle = "Always the dark appearance")
                }
            }
        }

        SectionLabel("Value rows")
        RuiListSection(
            header = null,
            footer = "Menu rows pick their value in place; the last row leads " +
                "to a detail page. Deleted drafts are removed from every " +
                "device signed in to this account.",
        ) {
            RuiSelectionRow(
                title = "Wi-Fi network",
                options = listOf(
                    RuiSelectionOption(Network.Studio5G, "Studio 5G"),
                    RuiSelectionOption(Network.Studio24G, "Studio 2.4G"),
                    RuiSelectionOption(Network.Guest, "Guest"),
                ),
                selected = network,
                onSelect = { network = it },
                backdrop = backdrop,
                material = material,
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiSelectionRow(
                title = "Language",
                options = listOf(
                    RuiSelectionOption(Language.English, "English"),
                    RuiSelectionOption(Language.Chinese, "简体中文"),
                    RuiSelectionOption(Language.French, "Français"),
                ),
                selected = language,
                onSelect = { language = it },
                backdrop = backdrop,
                material = material,
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Clear drafts",
                showChevron = true,
            )
        }
    }
}
