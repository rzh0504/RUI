package rui.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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

private data class ScrollBarTrack(val id: Int, val title: String, val album: String)

/**
 * RuiLazyColumn with its overlay scrollbar: the pill rides the trailing edge
 * while the list moves and fades out when it settles. Under a mouse it is a
 * control — press it and drag, the list follows the thumb.
 */
@Composable
public fun ScrollBarScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    val spacing = RuiTheme.spacing
    val layoutDirection = LocalLayoutDirection.current
    val tracks = remember {
        val albums = listOf("Nightdrive", "Low Tide", "Paper Planes", "Half Light", "Driftwood")
        List(60) { i ->
            ScrollBarTrack(
                id = i,
                title = "Track ${i + 1}",
                album = albums[i % albums.size],
            )
        }
    }

    RuiLazyColumn(
        modifier = Modifier.fillMaxSize(),
        scrollBar = true,
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + spacing.lg,
            bottom = contentPadding.calculateBottomPadding() + spacing.lg,
            start = contentPadding.calculateStartPadding(layoutDirection) + spacing.xl,
            end = contentPadding.calculateEndPadding(layoutDirection) + spacing.xl,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        item { SectionLabel("Overlay scrollbar") }
        item {
            RuiText(
                text = "A pill rides the trailing edge while the list moves and " +
                    "fades once it settles. On touch it is an indicator only; " +
                    "under a mouse it is a control — press the pill and drag, " +
                    "the list follows the thumb.",
                style = RuiTheme.typography.bodySm,
                color = RuiTheme.colors.contentMuted,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        items(tracks, key = { it.id }) { track ->
            RuiListItem(
                title = track.title,
                subtitle = track.album,
            )
            RuiDivider()
        }    }
}
