package rui.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import rui.components.RuiSwitch
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.GroupedCard
import rui.sample.ui.ListRow
import rui.sample.ui.RowSeparator
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

@Composable
public fun HomeScreen(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    glassEnabled: Boolean,
    onGlassEnabledChange: (Boolean) -> Unit,
    onOpen: (Destination) -> Unit,
) {
    val colors = RuiTheme.colors
    val typography = RuiTheme.typography
    val spacing = RuiTheme.spacing

    Column(
        Modifier
            .fillMaxSize()
            .background(colors.background)
            .safeDrawingPadding(),
    ) {
        ContentColumn {
            Column {
                RuiText(text = "Rui", style = typography.titleLg)
                RuiText(
                    text = "A neutral design system on Compose Unstyled",
                    style = typography.bodySm,
                    color = colors.contentMuted,
                    modifier = Modifier.padding(top = spacing.xs),
                )
            }

            SectionLabel("Appearance")
            GroupedCard {
                ListRow(
                    title = "Dark appearance",
                    trailing = { RuiSwitch(checked = darkTheme, onCheckedChange = onDarkThemeChange) },
                )
                RowSeparator()
                ListRow(
                    title = "Glass material",
                    trailing = { RuiSwitch(checked = glassEnabled, onCheckedChange = onGlassEnabledChange) },
                )
            }

            SectionLabel("Foundation")
            GroupedCard {
                ListRow(
                    title = "Typography, color & surfaces",
                    subtitle = "Type scale, tokens, icons, glass",
                    showChevron = true,
                    onClick = { onOpen(Destination.Foundations) },
                )
            }

            SectionLabel("Controls")
            GroupedCard {
                ListRow(
                    title = "Buttons",
                    subtitle = "Variants, sizes, icons",
                    showChevron = true,
                    onClick = { onOpen(Destination.Buttons) },
                )
                RowSeparator()
                ListRow(
                    title = "Text field",
                    subtitle = "Filled input with error and disabled states",
                    showChevron = true,
                    onClick = { onOpen(Destination.TextField) },
                )
                RowSeparator()
                ListRow(
                    title = "Selection controls",
                    subtitle = "Switch and checkbox",
                    showChevron = true,
                    onClick = { onOpen(Destination.Toggles) },
                )
                RowSeparator()
                ListRow(
                    title = "Segmented tabs",
                    subtitle = "Roving focus, keyboard navigation",
                    showChevron = true,
                    onClick = { onOpen(Destination.Tabs) },
                )
            }

            SectionLabel("Overlays")
            GroupedCard {
                ListRow(
                    title = "Dialog",
                    subtitle = "Scrim, actions, portal hosting",
                    showChevron = true,
                    onClick = { onOpen(Destination.Dialog) },
                )
            }

            RuiText(
                text = "Rui · Compose Unstyled",
                style = typography.labelSm,
                color = colors.contentDisabled,
            )
        }
    }
}
