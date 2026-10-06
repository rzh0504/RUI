package rui.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import rui.components.RuiListItem
import rui.components.RuiReorderableColumn
import rui.foundation.RuiSurface
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiText
import rui.foundation.utils.overScrollVertical
import rui.foundation.utils.scrollEndHaptic
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

private data class Track(val id: Int, val title: String, val artist: String)

private val Playlist = listOf(
    Track(1, "Glasswork", "Mara Ito"),
    Track(2, "Northbound", "The Quiet Set"),
    Track(3, "Paper Lanterns", "Akira Sun"),
    Track(4, "Slow Tide", "Nadia Field"),
    Track(5, "Copper Lines", "Vermilion"),
    Track(6, "Half Light", "Jonah Reyes"),
    Track(7, "Winter Waltz", "Ensemble Neu"),
    Track(8, "Salt and Pine", "Old Coast"),
)

/**
 * One playlist, two triggers: the first group reorders from a trailing grip,
 * the second from a long press anywhere on the row. Both report moves live,
 * so the two views (and the playlist) never disagree. Drag a row towards the
 * screen edges while the list is taller than the viewport to see autoscroll.
 */
@Composable
public fun ReorderScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    val spacing = RuiTheme.spacing
    val layoutDirection = LocalLayoutDirection.current
    val tracks = remember { mutableStateListOf<Track>().apply { addAll(Playlist) } }
    val scrollState = rememberScrollState()
    val move: (Int, Int) -> Unit = { from, to -> tracks.add(to, tracks.removeAt(from)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .overScrollVertical()
            .scrollEndHaptic()
            .verticalScroll(scrollState)
            .padding(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding(),
                start = contentPadding.calculateStartPadding(layoutDirection),
                end = contentPadding.calculateEndPadding(layoutDirection),
            )
            .padding(horizontal = spacing.xl, vertical = spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        RuiText(
            text = "RuiReorderableColumn reorders over a plain column: moves " +
                "report live while the finger is down, neighbors part with a " +
                "spring, and the lifted row scales up with a shadow. Grab the " +
                "grip — or long-press a row — and pull it through the others.",
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
            modifier = Modifier.fillMaxWidth(),
        )

        SectionLabel("Grip drag")
        RuiSurface {
            RuiReorderableColumn(
                items = tracks.toList(),
                onMove = move,
                key = { it.id },
                autoscrollState = scrollState,
            ) { track ->
                val handle = Modifier.dragHandle()
                RuiListItem(
                    title = track.title,
                    subtitle = track.artist,
                    trailing = {
                        RuiIcon(
                            imageVector = RuiIcons.Menu,
                            contentDescription = null,
                            modifier = handle,
                            tint = RuiTheme.colors.contentMuted,
                        )
                    },
                )
            }
        }

        SectionLabel("Long-press drag")
        RuiSurface {
            RuiReorderableColumn(
                items = tracks.toList(),
                onMove = move,
                key = { it.id },
                autoscrollState = scrollState,
            ) { track ->
                RuiListItem(
                    title = track.title,
                    subtitle = track.artist,
                    modifier = Modifier.draggableItem(),
                )
            }
        }
    }
}
