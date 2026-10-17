package rui.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
) : BlurBackdrop {
    override var layerCoordinates: LayoutCoordinates? by mutableStateOf(null)

    /**
     * Bumped every time the capture re-records. Consumers read it inside their
     * own draw so a changing capture invalidates them: layers that draw the
     * capture through an *intermediate* layer (the progressive blur's bands)
     * must re-record whenever the captured content changes, or they keep
     * showing the frame the intermediate layer was last recorded at — a ghost
     * that freezes under every scroll.
     */
    var revision: Int = 0
}

/** The capture's [BlurBackdropImpl.revision], for Rui's own blur consumers. */
internal val BlurBackdrop.captureRevision: Int
    get() = (this as BlurBackdropImpl).revision

/**
 * Creates a backdrop whose capturing node replays [onDraw] into its layer
 * (defaults to drawing the captured content as-is). Draw an opaque background
 * color first when the captured tree has transparent areas — blurring
 * transparent pixels fades toward nothing.
 *
 * The capture re-records whenever the capturing node's own draw runs. Content
 * that moves *without* that draw — a scrollable translating its children by
 * placement, for one — keeps sampling the frame it was recorded at; pass the
 * driving state through [Modifier.blurBackdrop]'s `redrawKey` (e.g.
 * `{ scrollState.value }`) so the capture re-records every frame the content
 * moves.
 *
 * Scope one backdrop to one screen (one navigation entry): every
 * [Modifier.blurBackdrop] recorder overwrites the layer and its coordinates,
 * so pages stacked on top of each other sharing a backdrop would sample the
 * wrong content at the wrong offset.
 */
@Composable
public fun rememberBlurBackdrop(
    onDraw: ContentDrawScope.() -> Unit = { drawContent() },
): BlurBackdrop {
    val graphicsLayer = rememberGraphicsLayer()
    return remember(onDraw) { BlurBackdropImpl(graphicsLayer, onDraw) }
}

/**
 * Marks this composable as the content [backdrop] captures. Every redraw
 * re-records the layer; recording is cheap and doesn't invalidate consumers
 * on its own. Set [recordEnabled] to false while a transition is running to
 * keep the animation's frame budget (surfaces sample the last frame).
 *
 * @param redrawKey Read on every draw of this node *before* recording: pass
 *   the state that drives content which moves without redrawing it — a scroll
 *   position translating its children by placement (`{ scrollState.value }`),
 *   for one — so the capture re-records every frame that state moves. Reads
 *   inside `onDraw` do not register here; the key must ride the recorder's
 *   own draw.
 */
public fun Modifier.blurBackdrop(
    backdrop: BlurBackdrop,
    recordEnabled: Boolean = true,
    redrawKey: (() -> Any?)? = null,
): Modifier =
    drawWithContent {
        // Read before drawing: a key that moved (a scroll position driving
        // placement-only content movement) invalidates this draw, so the
        // capture re-records even when no child redraws on its own.
        redrawKey?.invoke()
        drawContent()
        if (!recordEnabled) return@drawWithContent
        val impl = backdrop as BlurBackdropImpl
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
        impl.revision++
    }.onGloballyPositioned { coordinates ->
        if (coordinates.isAttached) {
            (backdrop as BlurBackdropImpl).layerCoordinates = coordinates
        }
    }

/**
 * Resolves where the consuming node sits in the backdrop's coordinate space.
 * Null means the blur cannot be drawn this frame (no captured coordinates yet,
 * detached nodes, an exception from translation). A [positionOverride] value
 * passes through as-is — a NaN component is the caller's "skip the layer this
 * frame" signal and is checked by the draw site.
 */
internal fun resolveBackdropOffset(
    backdrop: BlurBackdrop,
    consumerCoordinates: LayoutCoordinates?,
    positionOverride: (() -> Offset)?,
): Offset? =
    if (positionOverride != null) {
        positionOverride()
    } else {
        val source = backdrop.layerCoordinates
        val consumer = consumerCoordinates
        if (source == null || consumer == null) {
            null
        } else {
            try {
                source.localPositionOf(consumer, Offset.Zero)
            } catch (_: Exception) {
                null
            }
        }
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
            // Reading the capture's revision keeps this draw live: a layer
            // drawn through RenderEffect must re-run whenever the captured
            // content re-records, or skiko's recorded reference shows the
            // frame this draw last ran at.
            backdrop.captureRevision

            val offset = resolveBackdropOffset(backdrop, consumerCoordinates.value, positionOverride)
            if (offset == null) {
                drawContent()
                return@drawWithContent
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
