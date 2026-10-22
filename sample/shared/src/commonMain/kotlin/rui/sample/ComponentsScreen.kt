package rui.sample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.components.RuiSwitch
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
    liquidNavBarEnabled: Boolean,
    onLiquidNavBarEnabledChange: (Boolean) -> Unit,
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
            RuiListItem(
                title = "Frosted material",
                trailing = { RuiSwitch(checked = frostedEnabled, onCheckedChange = onFrostedEnabledChange) },
            )
            RuiListItem(
                title = "Floating navigation bar",
                trailing = {
                    RuiSwitch(
                        checked = floatingNavBarEnabled,
                        onCheckedChange = onFloatingNavBarEnabledChange,
                    )
                },
            )
            RuiListItem(
                title = "Liquid navigation bar",
                subtitle = if (floatingNavBarEnabled) {
                    "The floating capsule renders in liquid glass"
                } else {
                    "Requires the floating navigation bar"
                },
                trailing = {
                    RuiSwitch(
                        checked = liquidNavBarEnabled,
                        onCheckedChange = onLiquidNavBarEnabledChange,
                        enabled = floatingNavBarEnabled,
                    )
                },
            )
            RuiListItem(
                title = "Localization",
                subtitle = "Built-in strings in English and Chinese",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Localization) },
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
            RuiListItem(
                title = "Text field",
                subtitle = "Filled input with error and disabled states",
                showChevron = true,
                onClick = { onOpen(CatalogPage.TextField) },
            )
            RuiListItem(
                title = "Search bar",
                subtitle = "Filterable query field with cancel",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Search) },
            )
            RuiListItem(
                title = "Selection controls",
                subtitle = "Switch, checkbox, slider",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Toggles) },
            )
            RuiListItem(
                title = "Pickers",
                subtitle = "Wheel drums for time and date",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Pickers) },
            )
            RuiListItem(
                title = "Segmented tabs",
                subtitle = "Roving focus, keyboard navigation",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Tabs) },
            )
            RuiListItem(
                title = "Chips",
                subtitle = "Compact tags for filters and metadata",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Chips) },
            )
            RuiListItem(
                title = "Disclosure",
                subtitle = "Expandable sections in place",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Disclosure) },
            )
            RuiListItem(
                title = "Lists",
                subtitle = "Sections framing grouped rows",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Lists) },
            )
            RuiListItem(
                title = "Lazy list",
                subtitle = "Virtualized rows for long content",
                showChevron = true,
                onClick = { onOpen(CatalogPage.LazyList) },
            )
            RuiListItem(
                title = "Scroll bar",
                subtitle = "Overlay indicator, draggable with a mouse",
                showChevron = true,
                onClick = { onOpen(CatalogPage.ScrollBar) },
            )
            RuiListItem(
                title = "Lyrics",
                subtitle = "Synced karaoke playback over artwork",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Lyrics) },
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
            RuiListItem(
                title = "Async image",
                subtitle = "Network loading with skeleton and error slots",
                showChevron = true,
                onClick = { onOpen(CatalogPage.AsyncImage) },
            )
        }

        SectionLabel("Navigation")
        RuiListGroup {
            RuiListItem(
                title = "Navigation rail",
                subtitle = "Collapsed strip and expanded rows",
                showChevron = true,
                onClick = { onOpen(CatalogPage.NavigationRail) },
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
            RuiListItem(
                title = "Skeleton",
                subtitle = "Breathing placeholders shaped like content",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Skeleton) },
            )
            RuiListItem(
                title = "Empty state",
                subtitle = "Quiet placeholder with optional action",
                showChevron = true,
                onClick = { onOpen(CatalogPage.EmptyState) },
            )
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
            RuiListItem(
                title = "Swipe actions",
                subtitle = "Rows reveal actions behind them",
                showChevron = true,
                onClick = { onOpen(CatalogPage.SwipeActions) },
            )
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
            RuiListItem(
                title = "Dropdown menu",
                subtitle = "Anchored panels, icons, destructive rows",
                showChevron = true,
                onClick = { onOpen(CatalogPage.DropdownMenu) },
            )
            RuiListItem(
                title = "Bottom sheet",
                subtitle = "Grabber, detents, scrim",
                showChevron = true,
                onClick = { onOpen(CatalogPage.BottomSheet) },
            )
            RuiListItem(
                title = "Tooltip",
                subtitle = "Anchored explanation bubbles",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Tooltip) },
            )
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
