package rui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import rui.foundation.RuiText
import rui.theme.RuiTheme

/**
 * A titled section of an inset grouped list — the shape of an iOS settings
 * page: an optional muted header above a [RuiListGroup] card and an optional
 * muted footer below it, both hanging on the card's start edge. Rows go in
 * [content] (a [ColumnScope]); separate them with [RuiDivider] inset to the
 * row's leading edge. A section without header or footer is just a
 * [RuiListGroup] — compose that directly.
 *
 * @param header The section's title; `null` omits it.
 * @param footer A quiet footnote under the card (legal note, hint);
 *   `null` omits it.
 */
@Composable
public fun RuiListSection(
    header: String?,
    modifier: Modifier = Modifier,
    footer: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = RuiTheme.spacing

    Column(modifier = modifier.fillMaxWidth()) {
        if (header != null) {
            RuiText(
                text = header,
                style = RuiTheme.typography.labelMd,
                color = RuiTheme.colors.contentMuted,
                modifier = Modifier.padding(start = spacing.lg, bottom = spacing.sm),
            )
        }
        RuiListGroup(content = content)
        if (footer != null) {
            RuiText(
                text = footer,
                style = RuiTheme.typography.bodySm,
                color = RuiTheme.colors.contentMuted,
                modifier = Modifier.padding(start = spacing.lg, top = spacing.sm),
            )
        }
    }
}
