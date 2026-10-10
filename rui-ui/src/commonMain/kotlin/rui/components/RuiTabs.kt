package rui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composeunstyled.Tab
import com.composeunstyled.TabList
import com.composeunstyled.TabPanel
import com.composeunstyled.UnstyledTabGroup
import rui.foundation.RuiText
import rui.theme.RuiTheme

/**
 * The Rui tab group: Unstyled's roving-focus, keyboard-navigable tab behavior
 * rendered as a segmented control — a muted track with a raised pill marking
 * the selection.
 *
 * With [fillWidth] the segments share the available width evenly (the common
 * case); otherwise each hugs its label. Labels wrap to up to [maxLines] lines
 * and ellipsize beyond that — the visible truncation never hides the text from
 * assistive technology, which reads the full label. All pills share the track
 * height (the tallest label's), so a wrapping tab never leaves its neighbors
 * shorter. Segments never scroll — iOS segmented controls truncate, they don't
 * pan; with [fillWidth] = false an oversized label group clips at the track.
 *
 * Tab panels are composed through [content] for the activated tab key.
 */
@Composable
public fun <T> RuiTabGroup(
    selectedTab: T,
    onSelectedTabChange: (T) -> Unit,
    tabs: List<T>,
    modifier: Modifier = Modifier,
    fillWidth: Boolean = true,
    labelStyle: TextStyle = RuiTheme.typography.labelLg,
    maxLines: Int = 2,
    label: (T) -> String,
    content: @Composable (T) -> Unit,
) {
    val colors = RuiTheme.colors
    val spacing = RuiTheme.spacing

    UnstyledTabGroup(
        selectedTab = selectedTab,
        onSelectedTabChange = onSelectedTabChange,
        tabs = tabs,
        modifier = modifier,
    ) {
        Column {
            TabList(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        // Equal-height pills: the track resolves the tallest
                        // label's height and every tab fills it.
                        .height(IntrinsicSize.Min)
                        .clip(RuiTheme.shapes.full)
                        .background(colors.surfaceMuted)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    tabs.forEach { key ->
                        Tab(
                            key = key,
                            modifier = if (fillWidth) Modifier.weight(1f) else Modifier,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .clip(RuiTheme.shapes.full)
                                    .background(if (selected) colors.surface else androidx.compose.ui.graphics.Color.Transparent)
                                    .padding(vertical = spacing.sm, horizontal = spacing.md),
                                contentAlignment = Alignment.Center,
                            ) {
                                RuiText(
                                    text = label(key),
                                    style = labelStyle,
                                    color = if (selected) colors.content else colors.contentMuted,
                                    textAlign = TextAlign.Center,
                                    maxLines = maxLines,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }

            tabs.forEach { key ->
                TabPanel(key = key) {
                    content(key)
                }
            }
        }
    }
}
