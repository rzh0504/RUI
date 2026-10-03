package rui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
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
 * case); otherwise each hugs its label.
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
                                    maxLines = 1,
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
