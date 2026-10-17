package rui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import rui.foundation.ExperimentalRuiApi
import rui.foundation.RuiIcon
import rui.foundation.RuiText
import rui.foundation.ruiMinimumTouchTarget
import rui.theme.RuiTheme

/** Default values for [RuiNavigationRail]. */
public object RuiNavigationRailDefaults {

    /** Width of the icon-only collapsed form. */
    public val CollapsedWidth: Dp = 80.dp

    /** Width of the expanded form, where labels sit beside their icons. */
    public val ExpandedWidth: Dp = 250.dp

    /** Destination icon size, matching the bars. */
    public val IconSize: Dp = 22.dp
}

/**
 * The adaptive navigation rail: the bars' vertical sibling for widths where a
 * bottom bar wastes the window — a leading column of destinations sharing the
 * bars' [RuiNavigationBarItem] model, so a host swaps shell per
 * [RuiTheme.windowWidthClass] and keeps one item list. The rail is a plain
 * page-background column with a hairline at its end edge — it sits *beside*
 * the content column, not over it, so unlike the bars there is nothing to
 * blur; glass stays a bar material.
 *
 * [expanded] picks between the two static forms — an 80dp icon strip with the
 * label under its icon, and a 250dp row form with labels beside icons (one
 * step short of [RuiSidebar]). The host swaps it at breakpoints; there is no
 * in-place collapse/expand morph.
 *
 * Items are exposed as tabs and repeated taps on the selected item re-invoke
 * [onSelect] (scroll-to-top parity with the bars); [selected] outside
 * `items.indices` selects nothing; an empty [items] renders an empty rail.
 * The [header] sits pinned above the scrolling destinations, the [footer]
 * pinned below them.
 */
@ExperimentalRuiApi
@Composable
public fun RuiNavigationRail(
    items: List<RuiNavigationBarItem>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    header: (@Composable ColumnScope.() -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val colors = RuiTheme.colors
    val typography = RuiTheme.typography
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .width(if (expanded) RuiNavigationRailDefaults.ExpandedWidth else RuiNavigationRailDefaults.CollapsedWidth)
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
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .selectableGroup(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items.forEachIndexed { index, item ->
                    if (expanded) {
                        NavigationRow(
                            item = item,
                            isSelected = index == selected,
                            onClick = { onSelect(index) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 2.dp),
                        )
                    } else {
                        CollapsedRailItem(
                            item = item,
                            isSelected = index == selected,
                            onClick = { onSelect(index) },
                        )
                    }
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

/**
 * The collapsed form's destination: a full-width slot carrying a stadium pill
 * behind the stacked icon and label. The pill is a plain stadium — corner
 * smoothing is illegible at this curvature, the floating bar's exemption.
 */
@Composable
private fun CollapsedRailItem(
    item: RuiNavigationBarItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = RuiTheme.colors
    val typography = RuiTheme.typography
    val contentColor = if (isSelected) colors.primary else colors.contentMuted

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .heightIn(min = RuiTheme.dimens.touchTarget)
            .ruiMinimumTouchTarget()
            .clip(RoundedCornerShape(percent = 50))
            .then(
                if (isSelected) {
                    Modifier.background(
                        colors.primary.copy(alpha = RuiFloatingNavigationBarDefaults.IndicatorAlpha),
                        RoundedCornerShape(percent = 50),
                    )
                } else {
                    Modifier
                },
            )
            // No press indication: iOS tab bars don't ripple.
            .selectable(
                selected = isSelected,
                interactionSource = null,
                indication = null,
                role = Role.Tab,
            ) { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (item.icon != null) {
                RuiIcon(imageVector = item.icon, contentDescription = null, size = 22.dp, tint = contentColor)
            }
            RuiText(text = item.label, style = typography.labelSm, color = contentColor, maxLines = 1)
        }
    }
}

/**
 * The row destination shared by the rail's expanded form and [RuiSidebar]:
 * an icon and its label on a stadium row pill — the collapsed rail keeps its
 * own stacked form.
 */
@Composable
internal fun NavigationRow(
    item: RuiNavigationBarItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = RuiTheme.colors
    val contentColor = if (isSelected) colors.primary else colors.contentMuted

    Box(
        modifier = modifier
            .heightIn(min = RuiTheme.dimens.touchTarget)
            .ruiMinimumTouchTarget()
            .clip(RoundedCornerShape(percent = 50))
            .then(
                if (isSelected) {
                    Modifier.background(
                        colors.primary.copy(alpha = RuiFloatingNavigationBarDefaults.IndicatorAlpha),
                        RoundedCornerShape(percent = 50),
                    )
                } else {
                    Modifier
                },
            )
            // No press indication: iOS sidebars don't ripple.
            .selectable(
                selected = isSelected,
                interactionSource = null,
                indication = null,
                role = Role.Tab,
            ) { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (item.icon != null) {
                RuiIcon(imageVector = item.icon, contentDescription = null, size = 22.dp, tint = contentColor)
            }
            RuiText(
                text = item.label,
                style = RuiTheme.typography.titleSm,
                color = contentColor,
                maxLines = 1,
            )
        }
    }
}
