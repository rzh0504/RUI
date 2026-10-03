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
 * A translucent material inspired by Liquid Glass: content shows through with a
 * tint, a hairline border and the ambient content color scoped to descendants.
 *
 * On platforms without backdrop-blur support ([isBlurSupported]) the tint
 * becomes near-opaque so overlays never compromise legibility. When backdrop
 * blur lands in the design system, the same flag will gate it — the degradation
 * path is already wired.
 */
@ExperimentalRuiApi
@Composable
public fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RuiTheme.shapes.lg,
    tint: Color = RuiTheme.colors.surfaceElevated,
    contentColor: Color = RuiTheme.colors.content,
    content: @Composable () -> Unit,
) {
    val effectiveTint = if (isBlurSupported) {
        tint.copy(alpha = 0.78f)
    } else {
        tint.copy(alpha = 0.97f)
    }

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Box(
            modifier = modifier
                .clip(shape)
                .background(effectiveTint, shape)
                .border(RuiTheme.dimens.borderWidth, RuiTheme.colors.outlineSubtle, shape),
        ) {
            content()
        }
    }
}
