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
    floatingNavBarEnabled: Boolean,
    onFloatingNavBarEnabledChange: (Boolean) -> Unit,
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
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Floating navigation bar",
                trailing = {
                    RuiSwitch(
                        checked = floatingNavBarEnabled,
                        onCheckedChange = onFloatingNavBarEnabledChange,
                    )
                },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Localization",
                subtitle = "Built-in strings in English and Chinese",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Localization) },
            )
        }

        SectionLabel("Blocks")
        RuiListGroup {
            RuiListItem(
                title = "Real-world blocks",
                subtitle = "App sections composed from Rui components",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Blocks) },
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
                title = "Chips",
                subtitle = "Compact tags for filters and metadata",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Chips) },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Disclosure",
                subtitle = "Expandable sections in place",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Disclosure) },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Lists",
                subtitle = "Sections framing grouped rows",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Lists) },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Lazy list",
                subtitle = "Virtualized rows for long content",
                showChevron = true,
                onClick = { onOpen(CatalogPage.LazyList) },
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
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Async image",
                subtitle = "Network loading with skeleton and error slots",
                showChevron = true,
                onClick = { onOpen(CatalogPage.AsyncImage) },
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
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Page indicator",
                subtitle = "Dots for paged content, current page as pill",
                showChevron = true,
                onClick = { onOpen(CatalogPage.PageIndicator) },
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
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Inline banner",
                subtitle = "Persistent in-flow notices, dismissible",
                showChevron = true,
                onClick = { onOpen(CatalogPage.InlineBanner) },
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
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Reorder",
                subtitle = "Drag rows into a new order, live",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Reorder) },
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
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Drawer",
                subtitle = "Start-edge navigation panel over a scrim",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Drawer) },
            )
        }

        RuiText(
            text = "Rui · Compose Unstyled",
            style = RuiTheme.typography.labelSm,
            color = RuiTheme.colors.contentDisabled,
        )
    }
}
