package rui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import rui.foundation.RuiText
import rui.theme.RuiTheme

/**
 * Colors of [RuiBadge]. The default container is `danger` — notification
 * badges read as alerts — with white content in both appearances (like the
 * slider's thumb, the white is deliberate).
 */
@Immutable
public data class RuiBadgeColors(
    public val container: Color,
    public val content: Color,
)

public object RuiBadgeDefaults {

    /** Diameter of the dot badge (no text). */
    public val DotSize: Dp = 8.dp

    /** Minimum width of the text badge. */
    public val MinTextWidth: Dp = 20.dp

    @Composable
    public fun colors(
        container: Color = RuiTheme.colors.danger,
        content: Color = Color.White,
    ): RuiBadgeColors = RuiBadgeColors(
        container = container,
        content = content,
    )
}

/**
 * A small count or status marker: a dot when [text] is null, otherwise a
 * pill. Position it over the anchored element (icon button, avatar) with a
 * `Box` + `align` — see the buttons page of the catalog.
 *
 * @param text The count or short label; `null` renders the dot.
 */
@Composable
public fun RuiBadge(
    text: String? = null,
    modifier: Modifier = Modifier,
    colors: RuiBadgeColors = RuiBadgeDefaults.colors(),
) {
    val shape = if (text == null) CircleShape else RuiTheme.shapes.full

    Box(
        modifier = modifier
            .then(
                if (text == null) {
                    Modifier.size(RuiBadgeDefaults.DotSize)
                } else {
                    Modifier.defaultMinSize(minWidth = RuiBadgeDefaults.MinTextWidth)
                },
            )
            .background(colors.container, shape)
            .then(
                if (text == null) {
                    Modifier
                } else {
                    Modifier.padding(horizontal = RuiTheme.spacing.xs, vertical = RuiTheme.spacing.xs)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (text != null) {
            RuiText(
                text = text,
                style = RuiTheme.typography.labelSm,
                color = colors.content,
            )
        }
    }
}
