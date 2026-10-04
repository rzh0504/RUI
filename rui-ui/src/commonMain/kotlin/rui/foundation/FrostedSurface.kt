package rui.foundation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.LocalContentColor
import rui.foundation.platform.isBlurSupported
import rui.theme.RuiTheme

/**
 * Marks APIs that are still settling — their shape may change across minor
 * releases. Components carrying this annotation are safe to try, less safe to
 * build a product on.
 */
@RequiresOptIn(message = "This Rui API is experimental and may change.")
public annotation class ExperimentalRuiApi

/**
 * A frosted surface: with a [backdrop] it renders the captured content
 * blurred behind a translucent tint (frosted glass); without one it is an
 * honest translucent panel — tint plus hairline border, no blur.
 *
 * Pass a [BlurBackdrop] from [rememberBlurBackdrop] + [blurBackdrop] to get
 * the frosted look; see the catalog app for the wiring. [progressive] ramps
 * the blur strength along a gradient instead of a uniform radius, and
 * [highlight] adds a subtle top-edge sheen — the glass look.
 */
@ExperimentalRuiApi
@Composable
public fun FrostedSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RuiTheme.shapes.md,
    tint: Color = RuiTheme.colors.surface,
    contentColor: Color = RuiTheme.colors.content,
    backdrop: BlurBackdrop? = null,
    blurRadius: Dp = 24.dp,
    progressive: ProgressiveBlur? = null,
    highlight: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = RuiTheme.colors
    val useBlur = backdrop != null && isBlurSupported
    val effectiveTint = if (useBlur) tint.copy(alpha = 0.72f) else tint.copy(alpha = 0.85f)

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Box(
            modifier = modifier
                .then(
                    if (useBlur) {
                        Modifier.blurSurface(
                            backdrop = backdrop!!,
                            radius = blurRadius,
                            tint = effectiveTint,
                            shape = shape,
                            progressive = progressive,
                            highlight = highlight,
                        )
                    } else {
                        Modifier.background(effectiveTint, shape)
                    },
                )
                .border(RuiTheme.dimens.borderWidth, colors.outlineSubtle, shape),
        ) {
            content()
        }
    }
}
