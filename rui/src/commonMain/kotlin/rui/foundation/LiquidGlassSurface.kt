package rui.foundation

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import rui.foundation.platform.isBlurSupported

/** Tint alpha of the liquid recipe — liquid glass stays clear where the
 *  frosted recipe washes 72% over the content. */
internal const val LiquidGlassTintAlpha = 0.30f

/**
 * Draws the liquid-glass material behind this composable's content: the
 * captured content saturated (vibrancy), lightly blurred and refracted at
 * the [shape]'s edges (lens), with a specular highlight along the edge and
 * [tint] washed on top. The liquid counterpart of [blurSurface] — chain it
 * before the content, it replaces the surface's own background.
 *
 * The lens/blur/vibrancy chain renders through the Backdrop library
 * (https://github.com/Kyant0/AndroidLiquidGlass), so the effect pipeline is
 * real on every Rui platform: Skia runtime shaders off Android, AGSL from
 * Android 13. Without a runtime shader (Android 12) refraction silently
 * drops and the chain degrades to vibrancy + blur; with no RenderEffect at
 * all (Android < 12) this modifier draws nothing extra — pass `enabled` from
 * [isBlurSupported] and fall back to a plain translucent background, exactly
 * like the frosted path does.
 *
 * @param backdrop The screen capture; it must have been created with
 *   `rememberBlurBackdrop(liquid = true)`, otherwise this modifier is a
 *   no-op (the component then falls back to the frosted path).
 * @param radius Blur radius of the liquid recipe — much lighter than the
 *   frosted one, the clarity is the look.
 * @param refraction Height of the lens refraction at the shape's edges;
 *   keep it within the shape's smallest corner radius (Rui's smallest glass
 *   corner is 14dp).
 * @param positionOverride The sampling position — this node's position in
 *   the capture's coordinate space — same contract as [blurSurface]'s: use
 *   it when the node is placed by a mechanism whose placement changes don't
 *   refresh captured coordinates (a lazy `Modifier.offset {}` inside another
 *   window, e.g. a bottom sheet). When null, the library tracks coordinates
 *   itself.
 */
public fun Modifier.liquidGlassSurface(
    backdrop: BlurBackdrop,
    radius: Dp = 16.dp,
    refraction: Dp = 8.dp,
    tint: Color = Color.Transparent,
    shape: Shape = RectangleShape,
    enabled: Boolean = isBlurSupported,
    positionOverride: (() -> Offset)? = null,
): Modifier {
    if (!enabled) return this
    val source = backdrop.liquidSource ?: return this
    val liquid: Backdrop = if (positionOverride != null) {
        LiquidOverrideBackdrop(source, positionOverride)
    } else {
        source
    }
    return drawBackdrop(
        backdrop = liquid,
        shape = { shape },
        effects = {
            vibrancy()
            blur(radius.toPx())
            val height = refraction.toPx()
            lens(refractionHeight = height, refractionAmount = height)
        },
        highlight = { Highlight.Default },
        shadow = null,
        onDrawSurface = { if (tint != Color.Transparent) drawRect(tint) },
    )
}

/**
 * A [Backdrop] view of the liquid capture with the consumer's position
 * supplied directly — the modal-window counterparts (bottom sheet, action
 * panel) slide with a lazy offset in their own window, where neither Rui's
 * nor the library's coordinates tracking can follow.
 */
private class LiquidOverrideBackdrop(
    private val source: LayerBackdrop,
    private val positionOverride: () -> Offset,
) : Backdrop {
    override val isCoordinatesDependent: Boolean = true

    override fun DrawScope.drawBackdrop(
        density: Density,
        coordinates: LayoutCoordinates?,
        layerBlock: (GraphicsLayerScope.() -> Unit)?,
    ) {
        val offset = positionOverride()
        if (!offset.x.isFinite() || !offset.y.isFinite()) return
        withTransform({ translate(-offset.x, -offset.y) }) {
            drawLayer(source.graphicsLayer)
        }
    }
}
