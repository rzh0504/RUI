package rui.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
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
 */
internal val LocalRuiTransitionActive = staticCompositionLocalOf { false }

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
}

/**
 * Creates a backdrop whose capturing node replays [onDraw] into its layer
 * (defaults to drawing the captured content as-is). Draw an opaque background
 * color first when the captured tree has transparent areas — blurring
 * transparent pixels fades toward nothing.
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
 */
public fun Modifier.blurBackdrop(
    backdrop: BlurBackdrop,
    recordEnabled: Boolean = true,
): Modifier =
    drawWithContent {
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
    }.onGloballyPositioned { coordinates ->
        if (coordinates.isAttached) {
            (backdrop as BlurBackdropImpl).layerCoordinates = coordinates
        }
    }

/**
 * Draws the blurred [backdrop] behind this composable's content, clipped to
 * [shape] and washed with [tint]. Falls back to drawing content directly when
 * [enabled] is false (platforms without RenderEffect support).
 *
 * The modifier paints backdrop, tint and then the content — it replaces the
 * surface's own background in the blur path. Chain it before the content.
 */
public fun Modifier.blurSurface(
    backdrop: BlurBackdrop,
    radius: Dp = 24.dp,
    tint: Color = Color.Transparent,
    shape: Shape = RectangleShape,
    enabled: Boolean = isBlurSupported,
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
            val source = backdrop.layerCoordinates
            val consumer = consumerCoordinates.value
            if (source == null || consumer == null) {
                drawContent()
                return@drawWithContent
            }
            val offset = try {
                source.localPositionOf(consumer, Offset.Zero)
            } catch (_: Exception) {
                drawContent()
                return@drawWithContent
            }

            val radiusPx = radius.toPx()
            backdrop.graphicsLayer.renderEffect =
                BlurEffect(radiusPx, radiusPx, TileMode.Clamp)
            translate(-offset.x, -offset.y) {
                drawLayer(backdrop.graphicsLayer)
            }
            if (tint != Color.Transparent) {
                drawRect(tint)
            }
            drawContent()
        }
}
