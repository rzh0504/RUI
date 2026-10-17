package rui.sample.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
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
import rui.components.RuiNavigationRail
import rui.foundation.ExperimentalRuiApi
import rui.foundation.RuiIcons
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

private val RailDestinations = listOf(
    RuiNavigationBarItem("Library", RuiIcons.Grid),
    RuiNavigationBarItem("Search", RuiIcons.Search),
    RuiNavigationBarItem("Blocks", RuiIcons.Layers),
    RuiNavigationBarItem("Palette", RuiIcons.Swatch),
)

/**
 * RuiNavigationRail in both static forms: the 80dp icon strip and the 250dp
 * row form where labels sit beside their icons. Pick per window width class —
 * the item list is the bars', so a host swaps the shell and keeps the data.
 */
@OptIn(ExperimentalRuiApi::class)
@Composable
public fun NavigationRailScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    var collapsedTab by remember { mutableStateOf(0) }
    var expandedTab by remember { mutableStateOf(1) }

    ContentColumn(contentPadding = contentPadding) {
        SectionLabel("Collapsed")
        RuiListGroup {
            RailSample(selected = collapsedTab, onSelect = { collapsedTab = it })
        }

        SectionLabel("Expanded")
        RuiListGroup {
            RailSample(
                selected = expandedTab,
                onSelect = { expandedTab = it },
                expanded = true,
                header = {
                    RuiText(
                        text = "Header",
                        style = RuiTheme.typography.labelMd,
                        color = RuiTheme.colors.contentMuted,
                        modifier = Modifier.padding(start = RuiTheme.spacing.lg, bottom = RuiTheme.spacing.sm),
                    )
                },
            )
        }

        RuiText(
            text = "Pick the form per window width class — Medium widths take the " +
                "collapsed strip, Expanded takes the row form or a sidebar.",
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
        )
    }
}

@OptIn(ExperimentalRuiApi::class)
@Composable
private fun RailSample(
    selected: Int,
    onSelect: (Int) -> Unit,
    expanded: Boolean = false,
    header: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp),
    ) {
        RuiNavigationRail(
            items = RailDestinations,
            selected = selected,
            onSelect = onSelect,
            modifier = Modifier.fillMaxHeight(),
            expanded = expanded,
            header = header,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
            RuiText(
                text = RailDestinations[selected].label,
                style = RuiTheme.typography.titleMd,
                color = RuiTheme.colors.contentMuted,
            )
        }
    }
}
