package rui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.composeunstyled.UnstyledProgress
import rui.theme.RuiTheme

/** One cycle of the indeterminate linear sweep. */
private const val LinearSweepDurationMillis = 1200

/** Width of the indeterminate linear segment, as a fraction of the track. */
private const val LinearSweepSegmentFraction = 0.4f

/**
 * Colors of [RuiLinearProgress] and [RuiCircularProgress]: a `primary` fill
 * over the faint track color the slider rests on.
 */
@Immutable
public data class RuiProgressColors(
    public val fill: Color,
    public val track: Color,
    public val disabledFill: Color,
    public val disabledTrack: Color,
)

public object RuiProgressDefaults {
    /** Thickness of the linear progress track. */
    public val LinearTrackHeight: Dp = 4.dp

    /** Diameter of the circular indicator. */
    public val CircularSize: Dp = 30.dp

    /** Stroke width of the circular ring. */
    public val CircularStrokeWidth: Dp = 4.dp

    @Composable
    public fun colors(
        fill: Color = RuiTheme.colors.primary,
        track: Color = RuiTheme.colors.outlineSubtle,
        disabledFill: Color = RuiTheme.colors.contentDisabled,
        disabledTrack: Color = RuiTheme.colors.outlineSubtle,
    ): RuiProgressColors = RuiProgressColors(
        fill = fill,
        track = track,
        disabledFill = disabledFill,
        disabledTrack = disabledTrack,
    )
}

/**
 * A linear progress bar. A [progress] in 0..1 renders a half-round-ended fill
 * anchored to the start edge (mirrored in RTL); `null` renders the
 * indeterminate state — a segment sweeping across the track. Semantics come
 * from Compose Unstyled's [UnstyledProgress] primitive.
 *
 * @param progress Current progress in 0..1, or `null` for indeterminate.
 * @param enabled When false, the disabled colors apply.
 * @param colors Visual colors, see [RuiProgressDefaults.colors].
 * @param height Track and fill thickness.
 */
@Composable
public fun RuiLinearProgress(
    progress: Float? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: RuiProgressColors = RuiProgressDefaults.colors(),
    height: Dp = RuiProgressDefaults.LinearTrackHeight,
) {
    val fill = if (enabled) colors.fill else colors.disabledFill
    val track = if (enabled) colors.track else colors.disabledTrack
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    if (progress == null) {
        val transition = rememberInfiniteTransition(label = "RuiLinearProgressIndeterminate")
        val position by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = LinearSweepDurationMillis, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "RuiLinearProgressSweep",
        )
        UnstyledProgress(modifier = modifier) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height),
            ) {
                val radius = CornerRadius(size.height / 2f)
                val trackClip = Path().apply {
                    addRoundRect(RoundRect(Rect(Offset.Zero, size), radius))
                }
                val segmentWidth = size.width * LinearSweepSegmentFraction
                val x = -segmentWidth + (size.width + segmentWidth) * position
                val left = if (isRtl) size.width - x - segmentWidth else x
                drawRoundRect(color = track, cornerRadius = radius)
                clipPath(trackClip) {
                    drawRoundRect(
                        color = fill,
                        topLeft = Offset(left, 0f),
                        size = Size(segmentWidth, size.height),
                        cornerRadius = radius,
                    )
                }
            }
        }
    } else {
        val target = progress.coerceIn(0f, 1f)
        val animated by animateFloatAsState(
            targetValue = target,
            animationSpec = RuiTheme.motion.normal,
            label = "RuiLinearProgressFill",
        )
        UnstyledProgress(progress = target, modifier = modifier) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height),
            ) {
                drawRoundRect(
                    color = track,
                    cornerRadius = CornerRadius(size.height / 2f),
                )
                val width = size.width * animated
                if (width > 0f) {
                    val left = if (isRtl) size.width - width else 0f
                    drawRoundRect(
                        color = fill,
                        topLeft = Offset(left, 0f),
                        size = Size(width, size.height),
                        cornerRadius = CornerRadius(size.height / 2f),
                    )
                }
            }
        }
    }
}

/**
 * A thin ring progress indicator. A [progress] in 0..1 renders an arc growing
 * clockwise from 12 o'clock; `null` renders the indeterminate spinner — an
 * arc rotating while its sweep breathes (mechanics from miuix's
 * ProgressIndicator). Semantics come from [UnstyledProgress].
 *
 * @param progress Current progress in 0..1, or `null` for indeterminate.
 * @param enabled When false, the disabled colors apply.
 * @param colors Visual colors, see [RuiProgressDefaults.colors].
 * @param strokeWidth Ring stroke width.
 */
@Composable
public fun RuiCircularProgress(
    progress: Float? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: RuiProgressColors = RuiProgressDefaults.colors(),
    strokeWidth: Dp = RuiProgressDefaults.CircularStrokeWidth,
) {
    val fill = if (enabled) colors.fill else colors.disabledFill
    val track = if (enabled) colors.track else colors.disabledTrack

    if (progress == null) {
        val transition = rememberInfiniteTransition(label = "RuiCircularProgressIndeterminate")
        val rotation by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "RuiCircularProgressRotation",
        )
        val sweep by transition.animateFloat(
            initialValue = 30f,
            targetValue = 120f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1600
                    120f at 800 using LinearEasing
                    30f at 1600 using LinearEasing
                },
                repeatMode = RepeatMode.Restart,
            ),
            label = "RuiCircularProgressSweep",
        )
        UnstyledProgress(modifier = modifier.size(RuiProgressDefaults.CircularSize)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRing(fill, track, startAngle = rotation, sweepAngle = sweep, strokeWidth = strokeWidth)
            }
        }
    } else {
        val target = progress.coerceIn(0f, 1f)
        val animated by animateFloatAsState(
            targetValue = target,
            animationSpec = RuiTheme.motion.normal,
            label = "RuiCircularProgressFill",
        )
        UnstyledProgress(progress = target, modifier = modifier.size(RuiProgressDefaults.CircularSize)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRing(fill, track, startAngle = -90f, sweepAngle = 360f * animated, strokeWidth = strokeWidth)
            }
        }
    }
}

/** Shared ring drawing: a faint full track and a round-capped fill arc. */
private fun DrawScope.drawRing(
    fillColor: Color,
    trackColor: Color,
    startAngle: Float,
    sweepAngle: Float,
    strokeWidth: Dp,
) {
    val stroke = strokeWidth.toPx()
    val radius = (size.minDimension - stroke) / 2f
    drawCircle(
        color = trackColor,
        radius = radius,
        style = Stroke(width = stroke),
    )
    if (sweepAngle > 0f) {
        drawArc(
            color = fillColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(stroke / 2f, stroke / 2f),
            size = Size(size.minDimension - stroke, size.minDimension - stroke),
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
    }
}
