package rui.sample.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.ui.unit.dp
import rui.foundation.RuiText
import rui.theme.RuiTheme

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

/**
 * Scrollable, inset content of a screen. [contentPadding] (from
 * [rui.components.RuiScaffold]) is applied inside the scroll, so the content
 * scrolls under the glass bars while its ends rest above them.
 */
@Composable
public fun ContentColumn(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = RuiTheme.spacing
    val layoutDirection = LocalLayoutDirection.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding(),
                start = contentPadding.calculateStartPadding(layoutDirection),
                end = contentPadding.calculateEndPadding(layoutDirection),
            )
            .padding(horizontal = spacing.xl, vertical = spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.xl),
    ) {
        content()
    }
}
