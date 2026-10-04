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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

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
 * With [progressive] set, the blur strength ramps along the descriptor's
 * gradient instead of staying uniform: the backdrop is composited at three
 * blur levels (full radius, half, clear) whose alpha weights cross-fade along
 * the gradient — an approximation of per-pixel progressive blur built on the
 * same layer capture, with no runtime shaders.
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
@Composable
public fun Modifier.blurSurface(
    backdrop: BlurBackdrop,
    radius: Dp = 24.dp,
    tint: Color = Color.Transparent,
    shape: Shape = RectangleShape,
    enabled: Boolean = isBlurSupported,
    progressive: ProgressiveBlur? = null,
    positionOverride: (() -> Offset)? = null,
): Modifier {
    val consumerCoordinates = mutableStateOf<LayoutCoordinates?>(null)
    val levelLayer = rememberGraphicsLayer()

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

            if (offset.x.isFinite() && offset.y.isFinite()) {
                if (progressive == null) {
                    val radiusPx = radius.toPx()
                    backdrop.graphicsLayer.renderEffect =
                        BlurEffect(radiusPx, radiusPx, TileMode.Clamp)
                    translate(-offset.x, -offset.y) {
                        drawLayer(backdrop.graphicsLayer)
                    }
                    backdrop.graphicsLayer.renderEffect = null
                } else {
                    drawProgressive(
                        backdrop = backdrop,
                        levelLayer = levelLayer,
                        offset = offset,
                        radiusPx = radius.toPx(),
                        progressive = progressive,
                    )
                }
            }
            if (tint != Color.Transparent) {
                drawRect(tint)
            }
            drawContent()
        }
}

/**
 * Composites the backdrop at three blur levels — full radius, half, clear —
 * masked by gradient weights that cross-fade along [progressive]'s gradient,
 * so the effective blur strength ramps from full at [ProgressiveBlur.startFraction]
 * to zero at [ProgressiveBlur.endFraction]. Each level is recorded into
 * [levelLayer] with a `DstIn` gradient mask, then drawn immediately — the
 * effect is set before the record and changed only after the draw, so the
 * shared backdrop layer is sampled with the intended radius.
 */
private fun ContentDrawScope.drawProgressive(
    backdrop: BlurBackdrop,
    levelLayer: GraphicsLayer,
    offset: Offset,
    radiusPx: Float,
    progressive: ProgressiveBlur,
) {
    val direction = Offset(cos(progressive.angle * (PI / 180.0).toFloat()), sin(progressive.angle * (PI / 180.0).toFloat()))
    val extent = size.width * abs(direction.x) + size.height * abs(direction.y)
    if (extent <= 0f) {
        return
    }
    // The corner where the projection starts (blur-full end of the gradient).
    val gradientStart = Offset(
        if (direction.x < 0f) size.width else 0f,
        if (direction.y < 0f) size.height else 0f,
    )
    val gradientEnd = gradientStart + direction * extent

    fun blurFactor(t: Float): Float {
        val raw = ((t - progressive.startFraction) /
            (progressive.endFraction - progressive.startFraction)).coerceIn(0f, 1f)
        val shaped = raw.pow(progressive.curve)
        return 1f - shaped
    }

    fun weightBrush(weight: (Float) -> Float): Brush = Brush.linearGradient(
        colorStops = arrayOf(
            0f to Color.White.copy(alpha = weight(0f)),
            0.25f to Color.White.copy(alpha = weight(0.25f)),
            0.5f to Color.White.copy(alpha = weight(0.5f)),
            0.75f to Color.White.copy(alpha = weight(0.75f)),
            1f to Color.White.copy(alpha = weight(1f)),
        ),
        start = gradientStart,
        end = gradientEnd,
    )

    // Weights sum to 1: effective radius = wFull·r + wMid·r/2.
    val full = weightBrush { t -> (2f * blurFactor(t) - 1f).coerceIn(0f, 1f) }
    val mid = weightBrush { t -> 1f - abs(2f * blurFactor(t) - 1f) }
    val clear = weightBrush { t -> (1f - 2f * blurFactor(t)).coerceIn(0f, 1f) }

    fun drawLevel(mask: Brush, radiusFactor: Float) {
        if (radiusFactor > 0f) {
            val r = radiusPx * radiusFactor
            backdrop.graphicsLayer.renderEffect = BlurEffect(r, r, TileMode.Clamp)
        } else {
            backdrop.graphicsLayer.renderEffect = null
        }
        levelLayer.record(IntSize(size.width.toInt(), size.height.toInt())) {
            translate(-offset.x, -offset.y) {
                drawLayer(backdrop.graphicsLayer)
            }
            drawRect(mask, blendMode = BlendMode.DstIn)
        }
        drawLayer(levelLayer)
    }

    drawLevel(full, 1f)
    drawLevel(mid, 0.5f)
    drawLevel(clear, 0f)
    backdrop.graphicsLayer.renderEffect = null
}
