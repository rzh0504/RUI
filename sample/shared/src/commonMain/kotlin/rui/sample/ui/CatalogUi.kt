package rui.sample.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
            .padding(horizontal = spacing.xl, vertical = spacing.lg)
            .safeDrawingPadding(),
        verticalArrangement = Arrangement.spacedBy(spacing.xl),
    ) {
        content()
    }
}
