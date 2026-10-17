package rui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import rui.foundation.ExperimentalRuiApi
import rui.theme.RuiTheme

/** Default values for [RuiSidebar]. */
public object RuiSidebarDefaults {

    /** The sidebar's column width, matching [RuiDrawer]'s panel cap. */
    public val Width: Dp = 280.dp
}

/**
 * The iPadOS-style navigation sidebar: a persistent leading column — full
 * height, 280dp — of destination rows sharing the bars'
 * [RuiNavigationBarItem] model, so a host swaps shell per
 * [RuiTheme.windowWidthClass] and keeps one item list. Rows read as list
 * rows (title level, the same as [RuiListItem]) on a stadium pill when
 * selected; the selected destination's content reads in `primary`.
 *
 * Like the rail, the sidebar is a plain page-background column with a
 * hairline at its end edge: it sits *beside* the content column, not over
 * it, so there is nothing to blur — glass stays a bar material. The [header]
 * sits pinned above the scrolling destinations (a screen title, a search
 * field), the [footer] pinned below them (a profile row, an action).
 *
 * Items are exposed as tabs and repeated taps on the selected item re-invoke
 * [onSelect] (scroll-to-top parity with the bars); [selected] outside
 * `items.indices` selects nothing; an empty [items] renders an empty column.
 */
@ExperimentalRuiApi
@Composable
public fun RuiSidebar(
    items: List<RuiNavigationBarItem>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable ColumnScope.() -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val colors = RuiTheme.colors
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .width(RuiSidebarDefaults.Width)
            .background(colors.background),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(vertical = RuiTheme.spacing.md),
        ) {
            header?.invoke(this)
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(scrollState)
                    .selectableGroup(),
            ) {
                items.forEachIndexed { index, item ->
                    NavigationRow(
                        item = item,
                        isSelected = index == selected,
                        onClick = { onSelect(index) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = RuiTheme.spacing.md, vertical = 2.dp),
                    )
                }
            }
            footer?.invoke(this)
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(RuiTheme.dimens.borderWidth)
                .background(colors.outlineSubtle),
        )
    }
}
