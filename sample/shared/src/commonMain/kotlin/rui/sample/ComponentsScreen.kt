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
                title = "Search bar",
                subtitle = "Filterable query field with cancel",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Search) },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Selection controls",
                subtitle = "Switch, checkbox, slider",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Toggles) },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Pickers",
                subtitle = "Wheel drums for time and date",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Pickers) },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Segmented tabs",
                subtitle = "Roving focus, keyboard navigation",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Tabs) },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Disclosure",
                subtitle = "Expandable sections in place",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Disclosure) },
            )
        }

        SectionLabel("Identity")
        RuiListGroup {
            RuiListItem(
                title = "Avatar",
                subtitle = "Images, initials, sizes",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Avatar) },
            )
        }

        SectionLabel("Indicators")
        RuiListGroup {
            RuiListItem(
                title = "Progress",
                subtitle = "Linear and circular, determinate and indeterminate",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Progress) },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Skeleton",
                subtitle = "Breathing placeholders shaped like content",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Skeleton) },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Empty state",
                subtitle = "Quiet placeholder with optional action",
                showChevron = true,
                onClick = { onOpen(CatalogPage.EmptyState) },
            )
        }

        SectionLabel("Feedback")
        RuiListGroup {
            RuiListItem(
                title = "Snackbar",
                subtitle = "Queued transient messages, optional action",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Snackbar) },
            )
        }

        SectionLabel("Gestures")
        RuiListGroup {
            RuiListItem(
                title = "Pull to refresh",
                subtitle = "Overscroll gesture on a scrollable",
                showChevron = true,
                onClick = { onOpen(CatalogPage.PullToRefresh) },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Swipe actions",
                subtitle = "Rows reveal actions behind them",
                showChevron = true,
                onClick = { onOpen(CatalogPage.SwipeActions) },
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
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Dropdown menu",
                subtitle = "Anchored panels, icons, destructive rows",
                showChevron = true,
                onClick = { onOpen(CatalogPage.DropdownMenu) },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Bottom sheet",
                subtitle = "Grabber, detents, scrim",
                showChevron = true,
                onClick = { onOpen(CatalogPage.BottomSheet) },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Tooltip",
                subtitle = "Anchored explanation bubbles",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Tooltip) },
            )
        }

        RuiText(
            text = "Rui · Compose Unstyled",
            style = RuiTheme.typography.labelSm,
            color = RuiTheme.colors.contentDisabled,
        )
    }
}
