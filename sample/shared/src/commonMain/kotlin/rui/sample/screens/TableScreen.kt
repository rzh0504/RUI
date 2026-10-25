package rui.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import rui.components.RuiTable
import rui.components.RuiTableColumnWidth
import rui.components.RuiTableSort
import rui.components.RuiTableSortDirection
import rui.foundation.RuiIcon
import rui.foundation.RuiText
import rui.foundation.RuiSurface
import rui.icons.RuiCommonIcons
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

private data class CatalogTrack(
    val id: Int,
    val title: String,
    val artist: String,
    val year: Int,
    val minutes: Int,
    val available: Boolean,
)

private val Tracks = listOf(
    CatalogTrack(1, "Aurora", "Nils Kern", 2019, 43, true),
    CatalogTrack(2, "Basalt", "Vera Molnár", 2021, 51, true),
    CatalogTrack(3, "Cinder", "The Fold", 2017, 38, false),
    CatalogTrack(4, "Driftwood", "Nils Kern", 2023, 47, true),
    CatalogTrack(5, "Emberline", "Sao Mai", 2020, 42, true),
    CatalogTrack(6, "Fernweh", "The Fold", 2022, 55, false),
    CatalogTrack(7, "Glacier", "Vera Molnár", 2018, 36, true),
    CatalogTrack(8, "Halcyon", "Sao Mai", 2024, 49, true),
)

/**
 * RuiTable across its archetypes: fixed and weighted columns with per-column
 * alignment, hoisted sorting the caller applies to the data itself, and the
 * row slots — composable cells, a custom header, zebra stripes and selection
 * through the rowBackground hook.
 */
@Composable
public fun TableScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    ContentColumn(contentPadding = contentPadding) {
        SectionLabel("Columns")
        RuiSurface(modifier = Modifier.fillMaxWidth().height(232.dp)) {
            RuiTable(
                items = Tracks,
                modifier = Modifier.fillMaxWidth(),
            ) {
                column(title = "Title", width = RuiTableColumnWidth.Weighted(2f)) { track ->
                    RuiText(text = track.title, style = RuiTheme.typography.bodyMd)
                }
                column(title = "Artist", width = RuiTableColumnWidth.Weighted(1f)) { track ->
                    RuiText(
                        text = track.artist,
                        style = RuiTheme.typography.bodyMd,
                        color = RuiTheme.colors.contentMuted,
                    )
                }
                column(
                    title = "Year",
                    width = RuiTableColumnWidth.Fixed(56.dp),
                    alignment = Alignment.End,
                ) { track ->
                    RuiText(
                        text = "${track.year}",
                        style = RuiTheme.typography.bodyMd,
                        color = RuiTheme.colors.contentMuted,
                    )
                }
            }
        }

        SectionLabel("Sortable headers")
        SortableTracksTable()

        SectionLabel("Row selection")
        SelectableTracksTable()
    }
}

@Composable
private fun SortableTracksTable() {
    var sort by remember { mutableStateOf<RuiTableSort?>(null) }
    val sorted = remember(sort) {
        val direction = sort?.direction
        val comparator = when (sort?.column) {
            "title" -> compareBy<CatalogTrack> { it.title }
            "artist" -> compareBy<CatalogTrack> { it.artist }
            "minutes" -> compareBy<CatalogTrack> { it.minutes }
            else -> null
        }
        when {
            comparator == null || direction == null -> Tracks
            direction == RuiTableSortDirection.Ascending -> Tracks.sortedWith(comparator)
            else -> Tracks.sortedWith(comparator.reversed())
        }
    }

    RuiSurface(modifier = Modifier.fillMaxWidth().height(232.dp)) {
        RuiTable(
            items = sorted,
            modifier = Modifier.fillMaxWidth(),
            sort = sort,
            onSortChange = { sort = it },
        ) {
            column(key = "title", title = "Title", sortable = true, width = RuiTableColumnWidth.Weighted(2f)) { track ->
                RuiText(text = track.title, style = RuiTheme.typography.bodyMd)
            }
            column(key = "artist", title = "Artist", sortable = true, width = RuiTableColumnWidth.Weighted(1f)) { track ->
                RuiText(
                    text = track.artist,
                    style = RuiTheme.typography.bodyMd,
                    color = RuiTheme.colors.contentMuted,
                )
            }
            column(
                key = "minutes",
                width = RuiTableColumnWidth.Fixed(72.dp),
                alignment = Alignment.End,
                sortable = true,
                header = {
                    Row(horizontalArrangement = Arrangement.spacedBy(RuiTheme.spacing.xs)) {
                        RuiText(
                            text = "Length",
                            style = RuiTheme.typography.labelMd,
                            color = RuiTheme.colors.contentMuted,
                        )
                        RuiIcon(
                            imageVector = RuiCommonIcons.Clock,
                            contentDescription = null,
                            size = 14.dp,
                            tint = RuiTheme.colors.contentMuted,
                        )
                    }
                },
            ) { track ->
                RuiText(
                    text = "${track.minutes} min",
                    style = RuiTheme.typography.bodyMd,
                    color = RuiTheme.colors.contentMuted,
                )
            }
        }
    }
}

@Composable
private fun SelectableTracksTable() {
    var selectedId by remember { mutableStateOf<Int?>(null) }
    val colors = RuiTheme.colors

    RuiSurface(modifier = Modifier.fillMaxWidth().height(232.dp)) {
        RuiTable(
            items = Tracks,
            modifier = Modifier.fillMaxWidth(),
            showRowDividers = false,
            onRowClick = { track -> selectedId = track.id },
            rowBackground = { track, index ->
                when {
                    track.id == selectedId -> colors.primary.copy(alpha = 0.12f)
                    index % 2 == 1 -> colors.surfaceMuted.copy(alpha = 0.45f)
                    else -> Color.Transparent
                }
            },
        ) {
            column(title = "Title", width = RuiTableColumnWidth.Weighted(2f)) { track ->
                Column {
                    RuiText(text = track.title, style = RuiTheme.typography.bodyMd)
                    RuiText(
                        text = track.artist,
                        style = RuiTheme.typography.bodySm,
                        color = RuiTheme.colors.contentMuted,
                    )
                }
            }
            column(title = "Year", width = RuiTableColumnWidth.Fixed(56.dp), alignment = Alignment.End) { track ->
                RuiText(
                    text = "${track.year}",
                    style = RuiTheme.typography.bodyMd,
                    color = RuiTheme.colors.contentMuted,
                )
            }
            column(
                title = "Available",
                width = RuiTableColumnWidth.Fixed(72.dp),
                alignment = Alignment.End,
            ) { track ->
                RuiIcon(
                    imageVector = RuiCommonIcons.Check,
                    contentDescription = if (track.available) "Available" else "Unavailable",
                    size = 14.dp,
                    tint = if (track.available) RuiTheme.colors.success else RuiTheme.colors.contentDisabled,
                )
            }
        }
    }
}
