package rui.sample.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import rui.components.RuiListGroup
import rui.components.RuiTabGroup
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

private enum class MediaTab { Listen, Watch, Read }
private enum class PairTab { Day, Night }

@Composable
public fun TabsScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    val typography = RuiTheme.typography
    val colors = RuiTheme.colors

    var media by remember { mutableStateOf(MediaTab.Listen) }
    var pair by remember { mutableStateOf(PairTab.Day) }

    ContentColumn(contentPadding = contentPadding) {
        RuiText(
            text = "Unstyled's roving-focus tab behavior in a segmented skin: " +
                "arrow keys move the selection, Tab joins and leaves the group.",
            style = typography.bodySm,
            color = colors.contentMuted,
        )

        SectionLabel("Three segments")
        RuiListGroup {
            RuiTabGroup(
                selectedTab = media,
                onSelectedTabChange = { media = it },
                tabs = MediaTab.entries.toList(),
                label = { it.name },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(RuiTheme.spacing.md),
            ) { active ->
                RuiText(
                    text = when (active) {
                        MediaTab.Listen -> "Audio goes here."
                        MediaTab.Watch -> "Video goes here."
                        MediaTab.Read -> "Articles go here."
                    },
                    style = typography.bodyMd,
                    color = colors.contentMuted,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        SectionLabel("Two segments")
        RuiListGroup {
            RuiTabGroup(
                selectedTab = pair,
                onSelectedTabChange = { pair = it },
                tabs = PairTab.entries.toList(),
                labelStyle = typography.labelMd,
                label = { it.name },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(RuiTheme.spacing.md),
            ) { active ->
                RuiText(
                    text = when (active) {
                        PairTab.Day -> "Light content."
                        PairTab.Night -> "Dark content."
                    },
                    style = typography.bodyMd,
                    color = colors.contentMuted,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
