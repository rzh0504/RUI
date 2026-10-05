package rui.foundation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.LocalContentColor
import rui.theme.RuiTheme

/**
 * The base container of the design system: a token-colored, shaped box that
 * scopes [LocalContentColor] so descendant [rui.foundation.RuiText] picks up
 * the right color automatically. Content is clipped to the [shape] — children
 * (rows and their state layers) never draw past a card's rounded corners.
 *
 * Surfaces separate from their surroundings through color contrast, not
 * borders — borders are opt-in for the rare case a hairline is needed.
 */
@Composable
public fun RuiSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RuiTheme.shapes.md,
    color: Color = RuiTheme.colors.surface,
    contentColor: Color = RuiTheme.colors.content,
    border: BorderStroke? = null,
    elevation: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Box(
            modifier = modifier
                .then(if (elevation > 0.dp) Modifier.shadow(elevation, shape) else Modifier)
                .clip(shape)
                .background(color, shape)
                .then(if (border != null) Modifier.border(border, shape) else Modifier),
        ) {
            content()
        }
    }
}
