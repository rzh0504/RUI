package rui.foundation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
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
 * the right color automatically.
 *
 * Rui surfaces are flat by default — separation comes from hairline borders and
 * (optionally) a soft shadow, not from tonal color shifts.
 */
@Composable
public fun RuiSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RuiTheme.shapes.md,
    color: Color = RuiTheme.colors.surface,
    contentColor: Color = RuiTheme.colors.content,
    border: BorderStroke? = BorderStroke(RuiTheme.dimens.borderWidth, RuiTheme.colors.outlineSubtle),
    elevation: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Box(
            modifier = modifier
                .then(if (elevation > 0.dp) Modifier.shadow(elevation, shape) else Modifier)
                .background(color, shape)
                .then(if (border != null) Modifier.border(border, shape) else Modifier),
        ) {
            content()
        }
    }
}
