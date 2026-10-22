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
 * behind a translucent tint — frosted glass by default, or the liquid-glass
 * material with `material = RuiGlassMaterial.Liquid` (lighter tint, edge
 * refraction, specular highlight; needs the capture created with
 * `rememberBlurBackdrop(liquid = true)`); without a backdrop it is an
 * honest opaque panel — flat tint plus hairline border, no blur, no
 * translucency.
 *
 * Pass a [BlurBackdrop] from [rememberBlurBackdrop] + [blurBackdrop] to get
 * the glass look; see the catalog app for the wiring.
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
    material: RuiGlassMaterial = RuiGlassMaterial.Frosted,
    content: @Composable () -> Unit,
) {
    val colors = RuiTheme.colors
    val useLiquid = material == RuiGlassMaterial.Liquid &&
        backdrop != null && backdrop.liquidSource != null && isBlurSupported
    val useBlur = backdrop != null && isBlurSupported && !useLiquid
    val effectiveTint = when {
        // Liquid: alpha comes from the tuning, surfaces pass the raw color.
        useLiquid -> tint
        useBlur -> tint.copy(alpha = 0.72f)
        else -> tint
    }

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Box(
            modifier = modifier
                .then(
                    when {
                        useLiquid -> Modifier.liquidGlassSurface(
                            backdrop = requireNotNull(backdrop),
                            tint = effectiveTint,
                            shape = shape,
                        )
                        useBlur -> Modifier.blurSurface(
                            backdrop = requireNotNull(backdrop),
                            radius = blurRadius,
                            tint = effectiveTint,
                            shape = shape,
                        )
                        else -> Modifier
                            .background(effectiveTint, shape)
                            .clip(shape)
                    },
                )
                .then(
                    // Liquid glass reads its edge from the specular highlight;
                    // a hairline on top of it doubles the border.
                    if (useLiquid) {
                        Modifier
                    } else {
                        Modifier.border(RuiTheme.dimens.borderWidth, colors.outlineSubtle, shape)
                    },
                ),
        ) {
            content()
        }
    }
}
