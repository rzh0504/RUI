package rui.sample.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rui.components.RuiDivider
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.components.RuiSearchBar
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

private data class TokenEntry(val name: String, val kind: String)

@Composable
public fun SearchBarScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    val query = rememberTextFieldState()
    val entries = remember {
        listOf(
            TokenEntry("md", "Shape · 10dp continuous corners"),
            TokenEntry("xl", "Shape · 18dp continuous corners"),
            TokenEntry("full", "Shape · stadium"),
            TokenEntry("titleXl", "Type · 34sp bold, iOS large title"),
            TokenEntry("bodySm", "Type · 13sp secondary text"),
            TokenEntry("labelMd", "Type · button text"),
            TokenEntry("primary", "Color · #3C87FF accent"),
            TokenEntry("danger", "Color · destructive red"),
        )
    }
    val matches by remember {
        derivedStateOf {
            val needle = query.text.toString().trim()
            if (needle.isEmpty()) {
                entries
            } else {
                entries.filter {
                    it.name.contains(needle, ignoreCase = true) ||
                        it.kind.contains(needle, ignoreCase = true)
                }
            }
        }
    }

    ContentColumn(contentPadding = contentPadding) {
        RuiText(
            text = "Type to filter the list below — the state is hoisted, the IME " +
                "action is Search, and a clear button appears once text is entered. " +
                "Cancel empties the query and drops focus.",
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
            modifier = Modifier.fillMaxWidth(),
        )

        RuiSearchBar(
            state = query,
            placeholder = "Search tokens",
            cancelLabel = "Cancel",
        )

        SectionLabel(if (query.text.isEmpty()) "All tokens" else "Matches")
        RuiListGroup {
            matches.forEachIndexed { index, entry ->
                RuiListItem(
                    title = entry.name,
                    subtitle = entry.kind,
                )
                if (index < matches.lastIndex) {
                    RuiDivider(inset = RuiTheme.spacing.lg)
                }
            }
            if (matches.isEmpty()) {
                RuiListItem(title = "No matches", subtitle = "Try another query")
            }
        }
    }
}
