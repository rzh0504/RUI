package rui.sample.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rui.components.RuiListGroup
import rui.components.RuiNavigationBarItem
import rui.components.RuiSidebar
import rui.foundation.ExperimentalRuiApi
import rui.foundation.RuiIcons
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

private val SidebarDestinations = listOf(
    RuiNavigationBarItem("Library", RuiIcons.Grid),
    RuiNavigationBarItem("Search", RuiIcons.Search),
    RuiNavigationBarItem("Blocks", RuiIcons.Layers),
    RuiNavigationBarItem("Palette", RuiIcons.Swatch),
)

/**
 * RuiSidebar: the iPadOS-style persistent leading column — full height, 280dp,
 * destination rows at the list-row level on a stadium pill, with the pinned
 * header and footer slots carrying a screen title and a trailing row.
 */
@OptIn(ExperimentalRuiApi::class)
@Composable
public fun SidebarScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    var tab by remember { mutableStateOf(0) }

    ContentColumn(contentPadding = contentPadding) {
        SectionLabel("Destinations")
        RuiListGroup {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp),
            ) {
                RuiSidebar(
                    items = SidebarDestinations,
                    selected = tab,
                    onSelect = { tab = it },
                    modifier = Modifier.fillMaxHeight(),
                    header = {
                        RuiText(
                            text = "Header",
                            style = RuiTheme.typography.titleMd,
                            modifier = Modifier.padding(
                                horizontal = RuiTheme.spacing.lg,
                                vertical = RuiTheme.spacing.sm,
                            ),
                        )
                    },
                    footer = {
                        RuiText(
                            text = "Footer",
                            style = RuiTheme.typography.labelMd,
                            color = RuiTheme.colors.contentMuted,
                            modifier = Modifier.padding(
                                horizontal = RuiTheme.spacing.lg,
                                vertical = RuiTheme.spacing.sm,
                            ),
                        )
                    },
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center,
                ) {
                    RuiText(
                        text = SidebarDestinations[tab].label,
                        style = RuiTheme.typography.titleMd,
                        color = RuiTheme.colors.contentMuted,
                    )
                }
            }
        }

        RuiText(
            text = "The expanded window form: destination rows at the list-row " +
                "level beside the content column. Like the rail, it is a " +
                "page-background column — the glass stays with the bars.",
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
        )
    }
}
