package rui.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import rui.foundation.platform.isBlurSupported
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Which edge of a progressive blur sits at full strength, the ramp running
 * toward the opposite edge.
 */
public enum class ProgressiveEdge {

    /** Blur strongest at the top edge, fading downward. */
    Top,

    /** Blur strongest at the bottom edge, fading upward — the signature iOS
     *  look under content that scrolls beneath a glass bar. */
    Bottom,

    /** Blur strongest at the leading edge, fading across. */
    Left,

    /** Blur strongest at the trailing edge, fading across. */
    Right,
}

/**
 * A blur whose strength ramps from full at one edge to zero along the surface,
 * instead of the uniform radius a plain [Modifier.blurSurface] draws.
 *
 * @param edge The edge at full strength.
 * @param startFraction Fraction in `[0, 1]` of the surface's extent along the
 *   ramp where full strength ends. `0f` (default) pins full strength to the
 *   edge itself.
 * @param endFraction Fraction where the blur reaches zero; `1f` (default)
 *   spends the whole extent on the ramp. Must be greater than
 *   [startFraction].
 * @param curve Power-curve exponent reshaping the falloff: `< 1` concentrates
 *   the radius change toward the strong end (fast early change, long soft
 *   tail), `> 1` toward the clear end. Must be positive.
 */
public data class ProgressiveBlur(
    public val edge: ProgressiveEdge = ProgressiveEdge.Bottom,
    public val startFraction: Float = 0f,
    public val endFraction: Float = 1f,
    public val curve: Float = 1f,
) {
    init {
        require(startFraction in 0f..1f && endFraction in 0f..1f) {
            "startFraction/endFraction must lie in [0, 1], got $startFraction..$endFraction"
        }
        require(startFraction < endFraction) {
            "startFraction must be smaller than endFraction, got $startFraction..$endFraction"
        }
        require(curve > 0f) { "curve must be positive, got $curve" }
    }
}

/** One band of the segmented ramp: an extent range and the blur radius it draws with. */
internal class ProgressiveBlurBand(val start: Float, val end: Float, val radius: Float)

/**
 * Splits the ramp along the extent into contiguous bands. Every band draws
 * with the radius at its clear-side boundary, so neighboring bands meet at a
 * shared radius — no seam. The blur itself samples the whole backdrop layer
 * (RenderEffect runs before the band's clip masks it), so a band's pixels
 * still carry blur contributions from beyond its own range; what remains of
 * the segmentation is only the small radius step per band.
 */
internal fun progressiveBlurBands(
    extent: Float,
    maxRadius: Float,
    progression: ProgressiveBlur,
    segments: Int,
): List<ProgressiveBlurBand> {
    check(segments >= 1) { "segments must be at least 1, got $segments" }
    val startPx = progression.startFraction * extent
    val endPx = progression.endFraction * extent
    val bands = ArrayList<ProgressiveBlurBand>(segments + 2)
    if (startPx > 0f) {
        bands += ProgressiveBlurBand(0f, startPx, maxRadius)
    }
    val span = endPx - startPx
    for (i in 1..segments) {
        val radius = maxRadius * (1.0 - (i.toDouble() / segments).pow(progression.curve.toDouble()))
        bands += ProgressiveBlurBand(
            start = startPx + span * (i - 1) / segments,
            end = startPx + span * i / segments,
            radius = radius.toFloat(),
        )
    }
    if (endPx < extent) {
        bands += ProgressiveBlurBand(endPx, extent, 0f)
    }
    return bands
}

/**
 * Draws the [backdrop] behind this composable's content with a
 * [ProgressiveBlur] ramp — full blur radius at the strong edge, easing to
 * clear along the surface. The iOS-style companion to [Modifier.blurSurface],
 * built for content that dies into a glass edge: under a bottom bar, behind a
 * status bar, along the leading edge of a media block.
 *
 * Each band records its slice of the backdrop into its *own* private
 * [androidx.compose.ui.graphics.layer.GraphicsLayer] (inflated by twice its
 * radius so the blur still samples content past the band's edges) whose
 * `renderEffect` is that band's radius.
 * The radii are never written onto the shared captured layer: a layer's
 * renderEffect is global to every draw of it in a frame and resolves at
 * composite time (HWUI defers it), so per-band radii on the shared layer
 * would all collapse to the last value written.
 *
 * Without RenderEffect support, or before the backdrop's coordinates are
 * known, it draws the tint and content only, like [Modifier.blurSurface].
 * The consumer must live outside the subtree the [backdrop] captures — a
 * surface sampling a backdrop whose capture node contains it would make the
 * layer self-drawing and recurse.
 *
 * The modifier paints backdrop, tint and then the content — it replaces the
 * surface's own background. Chain it before the content.
 *
 * @param progression The ramp's edge, span and curve.
 * @param radius The blur radius at full strength (the strong edge).
 * @param tint Washed over the blurred backdrop before the content.
 * @param positionOverride Same contract as [Modifier.blurSurface] — an offset
 *   with a NaN component skips the backdrop for a frame.
 * @param segments How many bands the ramp splits into. More bands smooth the
 *   radius steps at the cost of more band layers and blur passes.
 */
@Composable
public fun Modifier.progressiveBlurSurface(
    backdrop: BlurBackdrop,
    progression: ProgressiveBlur = ProgressiveBlur(),
    radius: Dp = 24.dp,
    tint: Color = Color.Transparent,
    shape: Shape = RectangleShape,
    enabled: Boolean = isBlurSupported,
    positionOverride: (() -> Offset)? = null,
    segments: Int = 10,
): Modifier {
    val consumerCoordinates = mutableStateOf<LayoutCoordinates?>(null)
    // A ramp holds at most a full-strength plateau + one band per segment + a
    // clear zone; every band gets its own layer so its radius is that layer's
    // stable property rather than a mid-frame mutation of the shared capture.
    val bandLayers = List(segments + 2) { rememberGraphicsLayer() }

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
            // The band layers reference the capture through their own recorded
            // slices; reading the capture's revision re-runs this draw — and
            // re-records the slices — whenever the captured content changes,
            // so scrolling never freezes a stale frame into the ramp.
            backdrop.captureRevision

            val offset = resolveBackdropOffset(backdrop, consumerCoordinates.value, positionOverride)
            if (offset == null) {
                drawContent()
                return@drawWithContent
            }

            val maxRadiusPx = radius.toPx()
            val vertical = progression.edge == ProgressiveEdge.Top || progression.edge == ProgressiveEdge.Bottom
            val extent = if (vertical) size.height else size.width

            if (offset.x.isFinite() && offset.y.isFinite() && maxRadiusPx >= 0.5f && extent > 0f) {
                val bands = progressiveBlurBands(extent, maxRadiusPx, progression, segments)
                for ((index, band) in bands.withIndex()) {
                    val rect = when (progression.edge) {
                        ProgressiveEdge.Top -> Rect(0f, band.start, size.width, band.end)
                        ProgressiveEdge.Bottom ->
                            Rect(0f, size.height - band.end, size.width, size.height - band.start)
                        ProgressiveEdge.Left -> Rect(band.start, 0f, band.end, size.height)
                        ProgressiveEdge.Right ->
                            Rect(size.width - band.end, 0f, size.width - band.start, size.height)
                    }
                    val layer = bandLayers[index]
                    // Gaussian influence dies well inside two radii, so a
                    // margin of twice the radius keeps the band's blur fed by
                    // content beyond its own edges — without it, the recorded
                    // slice's clamped edges would show as seams.
                    val margin = if (band.radius >= 0.5f) band.radius * 2f else 0f
                    val regionLeft = rect.left - margin
                    val regionTop = rect.top - margin
                    layer.record(
                        IntSize(
                            (rect.width + margin * 2).roundToInt().coerceAtLeast(1),
                            (rect.height + margin * 2).roundToInt().coerceAtLeast(1),
                        ),
                    ) {
                        translate(-offset.x - regionLeft, -offset.y - regionTop) {
                            drawLayer(backdrop.graphicsLayer)
                        }
                    }
                    layer.renderEffect =
                        if (band.radius >= 0.5f) BlurEffect(band.radius, band.radius, TileMode.Clamp) else null
                    clipRect(rect.left, rect.top, rect.right, rect.bottom) {
                        translate(regionLeft, regionTop) {
                            drawLayer(layer)
                        }
                    }
                }
            }
            if (tint != Color.Transparent) {
                drawRect(tint)
            }
            drawContent()
        }
}
