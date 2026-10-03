package rui.sample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.components.RuiSwitch
import rui.components.RuiDivider
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

@Composable
public fun ComponentsScreen(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    frostedEnabled: Boolean,
    onFrostedEnabledChange: (Boolean) -> Unit,
    onOpen: (CatalogPage) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    ContentColumn(contentPadding = contentPadding) {
        SectionLabel("Appearance")
        RuiListGroup {
            RuiListItem(
                title = "Dark appearance",
                trailing = { RuiSwitch(checked = darkTheme, onCheckedChange = onDarkThemeChange) },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Frosted material",
                trailing = { RuiSwitch(checked = frostedEnabled, onCheckedChange = onFrostedEnabledChange) },
            )
        }

        SectionLabel("Controls")
        RuiListGroup {
            RuiListItem(
                title = "Buttons",
                subtitle = "Variants, sizes, icons",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Buttons) },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Text field",
                subtitle = "Filled input with error and disabled states",
                showChevron = true,
                onClick = { onOpen(CatalogPage.TextField) },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Selection controls",
                subtitle = "Switch and checkbox",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Toggles) },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Segmented tabs",
                subtitle = "Roving focus, keyboard navigation",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Tabs) },
            )
        }

        SectionLabel("Overlays")
        RuiListGroup {
            RuiListItem(
                title = "Dialog",
                subtitle = "Scrim, actions, portal hosting",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Dialog) },
            )
        }

        RuiText(
            text = "Rui · Compose Unstyled",
            style = RuiTheme.typography.labelSm,
            color = RuiTheme.colors.contentDisabled,
        )
    }
}
