package rui.foundation

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.Immutable
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
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import rui.foundation.platform.isBlurSupported

/**
 * The liquid recipe's dials. Every liquid-glass surface reads them, so a
 * host can restyle the whole material from one place — provide
 * [LocalRuiLiquidGlassTuning] above the tree, or hand a [RuiLiquidGlassTuning]
 * to the surfaces that should deviate.
 *
 * The defaults are the shipped look: a clear 30% tint, a light 16dp blur,
 * 8dp of edge refraction, iOS-style saturation lift and a half-alpha
 * specular highlight.
 */
@Immutable
public data class RuiLiquidGlassTuning(
    /** Alpha of the [tint] wash — liquid glass stays clear where the frosted
     *  recipe sits at 0.72. */
    public val tintAlpha: Float = 0.30f,
    /** Blur radius, much lighter than the frosted one; the clarity is the look. */
    public val blurRadius: Dp = 16.dp,
    /** Height of the lens refraction at the shape's edges; keep it within
     *  the shape's smallest corner radius (Rui's smallest glass corner is
     *  14dp). */
    public val refractionHeight: Dp = 8.dp,
    /** Saturation multiplier of the sampled content (iOS vibrancy). */
    public val saturation: Float = 1.5f,
    /** Alpha of the specular edge highlight; 0 removes it. */
    public val highlightAlpha: Float = 0.5f,
    /** Split the lens into per-channel refractions (Android 13+ / Skia). */
    public val chromaticAberration: Boolean = false,
)

/**
 * The [RuiLiquidGlassTuning] the liquid surfaces use. Defaults to the shipped
 * look; provide a value to retune every liquid surface below — the capture,
 * the material and the components all pick it up, bars included.
 */
public val LocalRuiLiquidGlassTuning = compositionLocalOf { RuiLiquidGlassTuning() }

/**
 * Draws the liquid-glass material behind this composable's content: the
 * captured content saturated, lightly blurred and refracted at the [shape]'s
 * edges (lens), with a specular highlight along the edge and [tint] washed
 * on top at the tuning's alpha. The liquid counterpart of [blurSurface] —
 * chain it before the content, it replaces the surface's own background.
 *
 * The lens/blur/color chain renders through the Backdrop library
 * (https://github.com/Kyant0/AndroidLiquidGlass), so the effect pipeline is
 * real on every Rui platform: Skia runtime shaders off Android, AGSL from
 * Android 13. Without a runtime shader (Android 12) refraction silently
 * drops and the chain degrades to saturation + blur; with no RenderEffect at
 * all (Android < 12) this modifier draws nothing extra — pass `enabled` from
 * [isBlurSupported] and fall back to a plain translucent background, exactly
 * like the frosted path does.
 *
 * @param backdrop The screen capture; it must have been created with
 *   `rememberBlurBackdrop(liquid = true)`, otherwise this modifier is a
 *   no-op (the component then falls back to the frosted path).
 * @param tint The wash color; its alpha is replaced by the tuning's
 *   [RuiLiquidGlassTuning.tintAlpha], so hosts retune the translucency
 *   globally.
 * @param edgeEffects Whether the edge treatments draw — the specular
 *   highlight and the lens refraction at the shape's boundary. Strips
 *   attached to the screen's edges ([rui.components.RuiTopAppBar], the
 *   docked [rui.components.RuiNavigationBar]) pass false, following iOS's
 *   bar treatment: a bar is pure material, edge glass belongs to floating
 *   elements — a highlight along a screen-wide rectangle reads as a glowing
 *   frame, especially in dark themes.
 * @param positionOverride The sampling position — this node's position in
 *   the capture's coordinate space — same contract as [blurSurface]'s: use
 *   it when the node is placed by a mechanism whose placement changes don't
 *   refresh captured coordinates (a lazy `Modifier.offset {}` inside another
 *   window, e.g. a bottom sheet). When null, the library tracks coordinates
 *   itself.
 */
@Composable
public fun Modifier.liquidGlassSurface(
    backdrop: BlurBackdrop,
    tint: Color = Color.Transparent,
    shape: Shape = RectangleShape,
    enabled: Boolean = isBlurSupported,
    edgeEffects: Boolean = true,
    tuning: RuiLiquidGlassTuning = LocalRuiLiquidGlassTuning.current,
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
            colorControls(saturation = tuning.saturation)
            blur(tuning.blurRadius.toPx())
            // The lens shader needs corner radii; an arbitrary Shape would
            // throw inside the library, so it degrades to saturation + blur.
            if (edgeEffects && shape is CornerBasedShape) {
                val height = tuning.refractionHeight.toPx()
                lens(
                    refractionHeight = height,
                    refractionAmount = height,
                    chromaticAberration = tuning.chromaticAberration,
                )
            }
        },
        highlight = {
            if (edgeEffects && tuning.highlightAlpha > 0f) {
                Highlight.Default.copy(alpha = tuning.highlightAlpha)
            } else {
                null
            }
        },
        shadow = null,
        onDrawSurface = {
            if (tint != Color.Transparent) {
                drawRect(tint.copy(alpha = tuning.tintAlpha))
            }
        },
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
