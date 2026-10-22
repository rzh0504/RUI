package rui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

/**
 * Continuous-corner ("squircle") shape.
 *
 * A [androidx.compose.foundation.shape.RoundedCornerShape] corner is a pure
 * quarter circle, so curvature jumps discontinuously where the straight edge
 * meets the arc. This shape distributes the curvature across a wider corner
 * zone — the same idea as iOS continuous corners — so large surfaces read as
 * genuinely smooth instead of "circle glued to a rectangle".
 *
 * The corner zone is `radius * smoothing` wide; each corner is a single cubic
 * Bézier whose control points sit at `tile * (1 - 0.643)` along both edges.
 * At `smoothing = 1.0` this reduces exactly to a circular arc; the default
 * `1.1` is the standard continuous look. Radii larger than half the shorter
 * side clamp to it, producing smooth stadiums/pills.
 *
 * It IS a [CornerBasedShape] underneath — glass materials read corner
 * radii through that contract (backdrop's lens refraction, specular
 * highlights) — but the outline is drawn from the Bézier path, so the
 * smooth geometry is unchanged. Consequences of the inheritance: `copy`
 * with different corners returns a plain RoundedCornerShape (CornerSize
 * can't be reversed to a Dp, so the smoothing is lost there), and equality
 * stays strict to this class.
 *
 * The algorithm is adapted from the miuix project's squircle module
 * (https://github.com/composable-miuix/miuix, Apache License 2.0).
 */
public class SmoothCornerShape(
    public val radius: Dp,
    public val smoothing: Float = DefaultSmoothing,
) : CornerBasedShape(
    topStart = CornerSize(radius),
    topEnd = CornerSize(radius),
    bottomEnd = CornerSize(radius),
    bottomStart = CornerSize(radius),
) {

    override fun createOutline(
        size: Size,
        topStart: Float,
        topEnd: Float,
        bottomEnd: Float,
        bottomStart: Float,
        layoutDirection: LayoutDirection,
    ): Outline {
        val path = Path()
        // The base resolves and clamps the corners before handing them over;
        // the smooth rect is uniform-radius, so take the largest.
        path.addSmoothCornerRect(
            size.width,
            size.height,
            maxOf(topStart, topEnd, bottomEnd, bottomStart),
            smoothing,
        )
        return Outline.Generic(path)
    }

    override fun copy(
        topStart: CornerSize,
        topEnd: CornerSize,
        bottomEnd: CornerSize,
        bottomStart: CornerSize,
    ): CornerBasedShape =
        if (topStart == this.topStart && topEnd == this.topEnd &&
            bottomEnd == this.bottomEnd && bottomStart == this.bottomStart
        ) {
            this
        } else {
            RoundedCornerShape(topStart, topEnd, bottomEnd, bottomStart)
        }

    override fun equals(other: Any?): Boolean =
        other is SmoothCornerShape && other.radius == radius && other.smoothing == smoothing

    override fun hashCode(): Int = radius.hashCode() * 31 + smoothing.hashCode()

    override fun toString(): String = "SmoothCornerShape(radius=$radius, smoothing=$smoothing)"

    public companion object {
        public const val DefaultSmoothing: Float = 1.1f
    }
}

/**
 * Continuous corners on the top edge only, square at the bottom — for
 * surfaces docked to the screen's bottom edge (sheets), where square bottom
 * corners keep the shape flush with the screen. Radius and smoothing follow
 * the same model as [SmoothCornerShape]; the same CornerBasedShape contract
 * and `copy` caveat apply (top corners carry [radius], bottom corners are
 * zero).
 */
public class TopSmoothCornerShape(
    public val radius: Dp,
    public val smoothing: Float = SmoothCornerShape.DefaultSmoothing,
) : CornerBasedShape(
    topStart = CornerSize(radius),
    topEnd = CornerSize(radius),
    bottomEnd = CornerSize(0.dp),
    bottomStart = CornerSize(0.dp),
) {

    override fun createOutline(
        size: Size,
        topStart: Float,
        topEnd: Float,
        bottomEnd: Float,
        bottomStart: Float,
        layoutDirection: LayoutDirection,
    ): Outline {
        val path = Path()
        path.addSmoothTopCornerRect(
            size.width,
            size.height,
            maxOf(topStart, topEnd),
            smoothing,
        )
        return Outline.Generic(path)
    }

    override fun copy(
        topStart: CornerSize,
        topEnd: CornerSize,
        bottomEnd: CornerSize,
        bottomStart: CornerSize,
    ): CornerBasedShape =
        if (topStart == this.topStart && topEnd == this.topEnd &&
            bottomEnd == this.bottomEnd && bottomStart == this.bottomStart
        ) {
            this
        } else {
            RoundedCornerShape(topStart, topEnd, bottomEnd, bottomStart)
        }

    override fun equals(other: Any?): Boolean =
        other is TopSmoothCornerShape && other.radius == radius && other.smoothing == smoothing

    override fun hashCode(): Int = radius.hashCode() * 31 + smoothing.hashCode()

    override fun toString(): String = "TopSmoothCornerShape(radius=$radius, smoothing=$smoothing)"
}

/** Cubic Bézier handle ratio matching Apple-style continuous corners. */
private const val CornerControl = 0.643f

/**
 * Appends a closed smooth-corner rectangle spanning [width] × [height] with
 * the given pixel [cornerRadius] and [smoothing] factor to this path.
 */
public fun Path.addSmoothCornerRect(
    width: Float,
    height: Float,
    cornerRadius: Float,
    smoothing: Float = SmoothCornerShape.DefaultSmoothing,
) {
    if (width <= 0f || height <= 0f) {
        return
    }

    val ext = smoothing.coerceIn(1f, 2f)
    val halfMin = min(width, height) * 0.5f
    val tile = max(0f, cornerRadius * ext).coerceAtMost(halfMin)

    if (tile <= 0f) {
        addRect(Rect(0f, 0f, width, height))
        return
    }

    val handle = tile * (1f - CornerControl)

    moveTo(tile, 0f)
    lineTo(width - tile, 0f)
    cubicTo(width - handle, 0f, width, handle, width, tile)
    lineTo(width, height - tile)
    cubicTo(width, height - handle, width - handle, height, width - tile, height)
    lineTo(tile, height)
    cubicTo(handle, height, 0f, height - handle, 0f, height - tile)
    lineTo(0f, tile)
    cubicTo(0f, handle, handle, 0f, tile, 0f)
    close()
}

/**
 * Appends a closed smooth-corner rectangle with rounded top corners and
 * square bottom corners, for surfaces docked to the screen's bottom edge.
 */
public fun Path.addSmoothTopCornerRect(
    width: Float,
    height: Float,
    cornerRadius: Float,
    smoothing: Float = SmoothCornerShape.DefaultSmoothing,
) {
    if (width <= 0f || height <= 0f) {
        return
    }

    val ext = smoothing.coerceIn(1f, 2f)
    val halfMin = min(width, height) * 0.5f
    val tile = max(0f, cornerRadius * ext).coerceAtMost(halfMin)

    if (tile <= 0f) {
        addRect(Rect(0f, 0f, width, height))
        return
    }

    val handle = tile * (1f - CornerControl)

    moveTo(tile, 0f)
    lineTo(width - tile, 0f)
    cubicTo(width - handle, 0f, width, handle, width, tile)
    lineTo(width, height)
    lineTo(0f, height)
    lineTo(0f, tile)
    cubicTo(0f, handle, handle, 0f, tile, 0f)
    close()
}

/**
 * The elevation-shadow twin of a Rui shape. HWUI renders shadow outlines only
 * from rounded rects — a squircle's Outline.Generic degrades to a rectangular
 * shadow — so every Rui shape maps to a plain rounded shadow of the same
 * radius: a shadow is a blur where squircle smoothing is imperceptible. The
 * top-only variant keeps its square bottom edge flush with the screen edge.
 */
internal fun Shape.toShadowShape(): Shape = when (this) {
    is SmoothCornerShape -> RoundedCornerShape(radius)
    is TopSmoothCornerShape -> RoundedCornerShape(
        topStart = radius,
        topEnd = radius,
        bottomStart = 0.dp,
        bottomEnd = 0.dp,
    )
    else -> this
}
