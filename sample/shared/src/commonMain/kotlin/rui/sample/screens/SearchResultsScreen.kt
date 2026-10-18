package rui.sample.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rui.components.RuiAvatar
import rui.components.RuiEmptyState
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.foundation.RuiIcons
import rui.foundation.RuiSurface
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.theme.RuiTheme

/** Search results page: an empty state whose action populates result rows. */
@Composable
public fun SearchResultsScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    var hasResults by remember { mutableStateOf(false) }

    ContentColumn(contentPadding = contentPadding) {
        if (!hasResults) {
            RuiSurface(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                    RuiEmptyState(
                        title = "No results",
                        message = "Nothing matches the current filters.",
                        icon = RuiIcons.Search,
                        actionLabel = "Load sample results",
                        onAction = { hasResults = true },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        } else {
            RuiListGroup {
                ResultRow("Squircle radii, revisited", "Design notes · 6 min read", "RL")
                ResultRow("Wheel picker physics", "Engineering · 11 min read", "JT")
                ResultRow("Tokens on a 4dp grid", "Foundations · 4 min read", "MK")
            }
            RuiText(
                text = "Clear the section to see the empty state again.",
                style = RuiTheme.typography.labelSm,
                color = RuiTheme.colors.contentMuted,
            )
        }
    }
}

@Composable
private fun ResultRow(title: String, subtitle: String, initials: String) {
    RuiListItem(
        title = title,
        subtitle = subtitle,
        leading = { RuiAvatar(initials = initials) },
        showChevron = true,
        onClick = {},
    )
}
