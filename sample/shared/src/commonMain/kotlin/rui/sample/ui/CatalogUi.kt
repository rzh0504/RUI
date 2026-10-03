package rui.sample.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiSurface
import rui.foundation.RuiText
import rui.foundation.rememberRuiIndication
import rui.theme.RuiTheme

/** Top bar of a detail screen: optional back button plus the page title. */
@Composable
public fun CatalogNavBar(
    title: String,
    showBack: Boolean = false,
    onBack: () -> Unit = {},
) {
    val spacing = RuiTheme.spacing

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .padding(horizontal = spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showBack) {
            rui.components.RuiIconButton(
                onClick = onBack,
                variant = rui.components.RuiButtonVariant.Plain,
                modifier = Modifier.padding(end = spacing.xs),
            ) {
                RuiIcon(imageVector = RuiIcons.ChevronLeft, contentDescription = "Back")
            }
        }
        RuiText(text = title, style = RuiTheme.typography.titleMd)
    }
}

/** Uppercase-feeling section label above a grouped card. */
@Composable
public fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    RuiText(
        text = text,
        style = RuiTheme.typography.labelMd,
        color = RuiTheme.colors.contentMuted,
        modifier = modifier.padding(start = RuiTheme.spacing.lg, bottom = RuiTheme.spacing.xs),
    )
}

/** Inset grouped container: the quiet workhorse of list-based screens. */
@Composable
public fun GroupedCard(
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

/** Hairline separator between rows, inset past the row's leading edge. */
@Composable
public fun RowSeparator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = RuiTheme.spacing.lg)
            .height(0.8.dp)
            .background(RuiTheme.colors.outlineSubtle),
    )
}

/**
 * A list row: leading content, title (and optional subtitle), trailing
 * control and an optional chevron. Rows with [onClick] get the state layer.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun ListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
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
        Column(modifier = Modifier.weight(1f)) {
            RuiText(text = title, style = RuiTheme.typography.bodyMd)
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

/** Scrollable, inset content of a screen. */
@Composable
public fun ContentColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = RuiTheme.spacing

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.xl, vertical = spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.xl),
    ) {
        content()
    }
}

/** Standard detail page: nav bar with back button, scrollable inset content. */
@Composable
public fun DetailScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = RuiTheme.colors

    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.background)
            .safeDrawingPadding(),
    ) {
        CatalogNavBar(title = title, showBack = true, onBack = onBack)
        ContentColumn {
            content()
        }
    }
}
