package rui.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import rui.foundation.platform.isBlurSupported

/**
 * True while a navigation transition is running. Backdrop captures pause
 * re-recording during transitions — moving bars sample the last frame, which
 * is invisible in motion, and the animation keeps its full frame budget.
 *
 * Read-tracked ([compositionLocalOf], not a static local): the flag flips at
 * the start and the end of every transition, and a static local would
 * invalidate the whole provided subtree — the full rendered stack — at each
 * flip, spending the transition's first and settle frames on recomposition
 * instead of motion.
 */
internal val LocalRuiTransitionActive = compositionLocalOf { false }

/**
 * Captures the drawing of one composable so other surfaces can render it
 * blurred behind themselves — the backdrop of a frosted-glass material.
 *
 * The capture mechanism follows the approach of the miuix project's blur
 * module (https://github.com/composable-miuix-ui/miuix, Apache License 2.0):
 * content is recorded into a [GraphicsLayer] every frame and blurred surfaces
 * draw that layer with a [RenderEffect], translated into their own local
 * space. No pixels are read back.
 */
public interface BlurBackdrop {
    public val graphicsLayer: GraphicsLayer
    public val layerCoordinates: LayoutCoordinates?
}

private class BlurBackdropImpl(
    override val graphicsLayer: GraphicsLayer,
    val onDraw: ContentDrawScope.() -> Unit,
    val liquid: LayerBackdrop? = null,
) : BlurBackdrop {
    override var layerCoordinates: LayoutCoordinates? by mutableStateOf(null)
}

/** The liquid capture recorded by `rememberBlurBackdrop(liquid = true)` —
 *  the source [Modifier.liquidGlassSurface] consumes. */
internal val BlurBackdrop.liquidSource: LayerBackdrop?
    get() = (this as? BlurBackdropImpl)?.liquid

/**
 * Creates a backdrop whose capturing node replays [onDraw] into its layer
 * (defaults to drawing the captured content as-is). Draw an opaque background
 * color first when the captured tree has transparent areas — blurring
 * transparent pixels fades toward nothing.
 *
 * Scope one backdrop to one screen (one navigation entry): every
 * [Modifier.blurBackdrop] recorder overwrites the layer and its coordinates,
 * so pages stacked on top of each other sharing a backdrop would sample the
 * wrong content at the wrong offset.
 *
 * Keep glass consumers OUT of the recorded subtree. The recorder re-records
 * its layer after drawing the content; a glass element inside that content
 * re-executes inside the record and leaves the layer graph cyclical (glass
 * layer references the capture, the capture references the glass layer),
 * which the render thread walks until it dies. Bars and popup panels are
 * safe — [RuiScaffold] records only its content Box, and popups live in
 * their own windows. For glass inside a page, record a pure-background
 * layer beneath the glass instead (see the catalog's liquid-glass tab).
 *
 * Set [liquid] to also record a second capture into the Backdrop library's
 * [com.kyant.backdrop.backdrops.LayerBackdrop] — the source the
 * [RuiGlassMaterial.Liquid] surfaces consume (see
 * [Modifier.liquidGlassSurface]). Both captures replay the same [onDraw];
 * the extra one costs one GraphicsLayer record per frame, so leave it off
 * for screens that stay frosted. Unlike the frosted capture, the liquid one
 * keeps recording through navigation transitions (its consumer has no
 * recordEnabled escape hatch) — a page that transitions heavily is better
 * off frosted-only.
 */
@Composable
public fun rememberBlurBackdrop(
    liquid: Boolean = false,
    onDraw: ContentDrawScope.() -> Unit = { drawContent() },
): BlurBackdrop {
    val graphicsLayer = rememberGraphicsLayer()
    // Callers commonly pass an inline capture body; keying the recorder on
    // the lambda's identity would rebuild the backdrop — and reset the
    // library's layer coordinates — on every recomposition. Keep one stable
    // recorder and read the latest body at draw time.
    val latestOnDraw = rememberUpdatedState(onDraw)
    val recorder: ContentDrawScope.() -> Unit = remember { { latestOnDraw.value(this) } }
    val liquidSource = if (liquid) {
        rememberLayerBackdrop(onDraw = recorder)
    } else {
        null
    }
    return remember(graphicsLayer, recorder, liquidSource) {
        BlurBackdropImpl(graphicsLayer, recorder, liquidSource)
    }
}

/**
 * Marks this composable as the content [backdrop] captures. Every redraw
 * re-records the layer; recording is cheap and doesn't invalidate consumers
 * on its own. Set [recordEnabled] to false while a transition is running to
 * keep the animation's frame budget (surfaces sample the last frame). The
 * liquid capture, when present, records through unconditionally — the
 * library's recorder has no pause.
 */
public fun Modifier.blurBackdrop(
    backdrop: BlurBackdrop,
    recordEnabled: Boolean = true,
): Modifier {
    val impl = backdrop as BlurBackdropImpl
    return this
        .drawWithContent {
            drawContent()
            if (!recordEnabled) return@drawWithContent
            val sourceDensity = drawContext.density
            val scope: ContentDrawScope = this
            impl.graphicsLayer.record(IntSize(size.width.toInt(), size.height.toInt())) {
                val previous = drawContext.density
                drawContext.density = sourceDensity
                try {
                    impl.onDraw(scope)
                } finally {
                    drawContext.density = previous
                }
            }
        }
        .onGloballyPositioned { coordinates ->
            if (coordinates.isAttached) {
                impl.layerCoordinates = coordinates
            }
        }
        .then(
            if (impl.liquid != null) {
                Modifier.layerBackdrop(impl.liquid)
            } else {
                Modifier
            },
        )
}

/**
 * Draws the blurred [backdrop] behind this composable's content, clipped to
 * [shape] and washed with [tint]. Falls back to drawing content directly when
 * [enabled] is false (platforms without RenderEffect support).
 *
 * The modifier paints backdrop, tint and then the content — it replaces the
 * surface's own background in the blur path. Chain it before the content.
 *
 * @param positionOverride The sampling position — this node's position in the
 *   backdrop's coordinate space — provided directly instead of derived from
 *   `onGloballyPositioned`. Use it when the node is placed by a mechanism
 *   whose placement changes don't refresh captured coordinates (a lazy
 *   `Modifier.offset {}` inside another window, e.g. a bottom sheet): return
 *   an offset with a NaN component to skip the blur for a frame. When null,
 *   coordinates are tracked automatically.
 */
public fun Modifier.blurSurface(
    backdrop: BlurBackdrop,
    radius: Dp = 24.dp,
    tint: Color = Color.Transparent,
    shape: Shape = RectangleShape,
    enabled: Boolean = isBlurSupported,
    positionOverride: (() -> Offset)? = null,
): Modifier {
    val consumerCoordinates = mutableStateOf<LayoutCoordinates?>(null)

    return this
        .clip(shape)
        .onGloballyPositioned { coordinates ->
            if (coordinates.isAttached) {
                consumerCoordinates.value = coordinates
            }
        }
        .drawWithContent {
            if (!enabled) {
                drawContent()
                return@drawWithContent
            }

            val offset = if (positionOverride != null) {
                positionOverride()
            } else {
                val source = backdrop.layerCoordinates
                val consumer = consumerCoordinates.value
                if (source == null || consumer == null) {
                    drawContent()
                    return@drawWithContent
                }
                try {
                    source.localPositionOf(consumer, Offset.Zero)
                } catch (_: Exception) {
                    drawContent()
                    return@drawWithContent
                }
            }

            val radiusPx = radius.toPx()
            if (offset.x.isFinite() && offset.y.isFinite()) {
                backdrop.graphicsLayer.renderEffect =
                    BlurEffect(radiusPx, radiusPx, TileMode.Clamp)
                translate(-offset.x, -offset.y) {
                    drawLayer(backdrop.graphicsLayer)
                }
            }
            if (tint != Color.Transparent) {
                drawRect(tint)
            }
            drawContent()
        }
}
