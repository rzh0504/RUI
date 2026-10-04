package rui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiSurface
import rui.foundation.RuiText
import rui.foundation.rememberRuiIndication
import rui.theme.RuiTheme

/**
 * A row of an inset grouped list: optional leading view (icon, avatar), title
 * (and optional subtitle), trailing control, optional chevron. Rows with
 * [onClick] get the state layer and are pressed as one.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun RuiListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable RowScope.() -> Unit)? = null,
    showChevron: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val colors = RuiTheme.colors
    val spacing = RuiTheme.spacing
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = RuiTheme.dimens.touchTarget)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = rememberRuiIndication(),
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            )
            .padding(horizontal = spacing.lg, vertical = spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        leading?.invoke(this)
        Column(modifier = Modifier.weight(1f)) {
            RuiText(text = title, style = RuiTheme.typography.titleSm)
            if (subtitle != null) {
                RuiText(
                    text = subtitle,
                    style = RuiTheme.typography.bodySm,
                    color = colors.contentMuted,
                )
            }
        }
        trailing?.invoke(this)
        if (showChevron) {
            RuiIcon(
                imageVector = RuiIcons.ChevronRight,
                contentDescription = null,
                tint = colors.contentDisabled,
            )
        }
    }
}

/**
 * The grouped container of [RuiListItem]s: a token-shaped surface card.
 * Separate rows with [RuiDivider] (inset to the row's leading edge).
 */
@Composable
public fun RuiListGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    RuiSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RuiTheme.shapes.md,
    ) {
        Column(content = content)
    }
}
