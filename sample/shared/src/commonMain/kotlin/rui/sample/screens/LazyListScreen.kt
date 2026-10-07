package rui.sample.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import rui.components.RuiDivider
import rui.components.RuiLazyColumn
import rui.components.RuiListItem
import rui.foundation.RuiText
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

private data class FeedRow(val id: Int, val title: String, val body: String)

private val LetterGroups = listOf(
    "A" to listOf("Aurora", "Atlas", "Amber", "Arbor"),
    "B" to listOf("Basalt", "Birch", "Breeze", "Brook"),
    "C" to listOf("Cinder", "Cobalt", "Crest", "Cairn"),
)

/**
 * RuiLazyColumn carrying a long feed: 1000 flat rows that only exist while
 * on screen, grouped sections composed the same way as on any static page,
 * and sticky section headers riding over the content.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun LazyListScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    val spacing = RuiTheme.spacing
    val layoutDirection = LocalLayoutDirection.current
    val feed = remember {
        List(1000) { i ->
            FeedRow(
                id = i,
                title = "Result #${i + 1}",
                body = "Snippet for query term ${i % 7} — this row only exists while on screen.",
            )
        }
    }

    RuiLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + spacing.lg,
            bottom = contentPadding.calculateBottomPadding() + spacing.lg,
            start = contentPadding.calculateStartPadding(layoutDirection) + spacing.xl,
            end = contentPadding.calculateEndPadding(layoutDirection) + spacing.xl,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        item { SectionLabel("Feed") }
        items(feed, key = { it.id }, contentType = { "feed" }) { row ->
            RuiListItem(title = row.title, subtitle = row.body)
            RuiDivider()
        }

        item { SectionLabel("Sections") }
        section(
            header = "Preferences",
            footer = "A section is one bounded item — the card's rows are not virtualized.",
        ) {
            RuiListItem(title = "Notifications", showChevron = true, onClick = {})
            RuiDivider(inset = spacing.lg)
            RuiListItem(title = "Sounds", showChevron = true, onClick = {})
            RuiDivider(inset = spacing.lg)
            RuiListItem(title = "Do not disturb", showChevron = true, onClick = {})
        }

        item { SectionLabel("Sticky headers") }
        LetterGroups.forEach { (letter, names) ->
            stickyHeader(key = "sticky-$letter") {
                RuiText(
                    text = letter,
                    style = RuiTheme.typography.labelMd,
                    color = RuiTheme.colors.contentMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RuiTheme.colors.background)
                        .padding(start = spacing.lg, bottom = spacing.sm),
                )
            }
            items(names, key = { "$letter-$it" }) { name ->
                RuiListItem(title = name)
            }
        }
    }
}
