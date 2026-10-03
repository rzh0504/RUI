package rui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.composeunstyled.Tab
import com.composeunstyled.TabList
import com.composeunstyled.TabPanel
import com.composeunstyled.UnstyledTabGroup
import rui.foundation.RuiText
import rui.theme.RuiTheme

/**
 * The Rui tab group: Unstyled's roving-focus, keyboard-navigable tab behavior
 * with an underline indicator. `activateOnFocus` follows Unstyled's default, so
 * arrow keys move selection directly.
 *
 * With [fillWidth] the tabs share the available width evenly; otherwise each
 * tab hugs its label.
 *
 * Tab panels are composed through [content] for the activated tab key.
 */
@Composable
public fun <T> RuiTabGroup(
    selectedTab: T,
    onSelectedTabChange: (T) -> Unit,
    tabs: List<T>,
    modifier: Modifier = Modifier,
    fillWidth: Boolean = false,
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                ) {
                    tabs.forEach { key ->
                        Tab(
                            key = key,
                            modifier = if (fillWidth) Modifier.weight(1f) else Modifier,
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier.padding(
                                        horizontal = spacing.sm,
                                        vertical = spacing.sm,
                                    ),
                                ) {
                                    RuiText(
                                        text = label(key),
                                        style = labelStyle,
                                        color = if (selected) colors.content else colors.contentMuted,
                                        maxLines = 1,
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .background(
                                            color = if (selected) colors.primary else Color.Transparent,
                                            shape = RoundedCornerShape(1.dp),
                                        ),
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
