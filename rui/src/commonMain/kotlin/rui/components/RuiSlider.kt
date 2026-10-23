package rui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import rui.foundation.RuiHaptics
import rui.foundation.rememberRuiHaptics
import rui.theme.RuiTheme
import kotlin.math.roundToInt

/**
 * Colors of [RuiSlider]. The thumb stays white in both light and dark
 * appearance, as on iOS — pass [thumb] to override.
 */
@Immutable
public data class RuiSliderColors(
    public val trackFill: Color,
    public val trackRest: Color,
    public val thumb: Color,
    public val disabledTrackFill: Color,
    public val disabledTrackRest: Color,
    public val disabledThumb: Color,
)

public object RuiSliderDefaults {
    /** Thickness of the track capsule. */
    public val TrackHeight: Dp = 4.dp

    /** Diameter of the thumb knob. */
    public val ThumbSize: Dp = 20.dp

    /** Elevation of the thumb's soft shadow. */
    public val ThumbElevation: Dp = 2.dp

    @Composable
    public fun colors(
        trackFill: Color = RuiTheme.colors.primary,
        trackRest: Color = RuiTheme.colors.outlineSubtle,
        thumb: Color = Color.White,
        disabledTrackFill: Color = RuiTheme.colors.contentDisabled,
        disabledTrackRest: Color = RuiTheme.colors.outlineSubtle,
        disabledThumb: Color = Color.White,
    ): RuiSliderColors = RuiSliderColors(
        trackFill = trackFill,
        trackRest = trackRest,
        thumb = thumb,
        disabledTrackFill = disabledTrackFill,
        disabledTrackRest = disabledTrackRest,
        disabledThumb = disabledThumb,
    )
}

/**
 * An iOS-style slider: a thin capsule track with a filled leading portion in
 * the accent color and a white circular thumb. A tap on the track jumps the
 * thumb to the touch point; a horizontal drag follows the finger, as on
 * UISlider. The drag claims the gesture only after horizontal touch slop, so
 * a vertical swipe still scrolls the enclosing page instead of nudging the
 * value. With [steps] > 0 the thumb snaps to evenly spaced discrete values
 * and a haptic tick fires whenever the selection crosses into the next step.
 *
 * @param value The current value, coerced into [valueRange].
 * @param onValueChange Called with the new value while dragging.
 * @param enabled Whether the slider responds to touch.
 * @param valueRange Inclusive range the slider can take.
 * @param steps Number of discrete values *between* the endpoints (a range of
 *   0..10 with 4 steps allows 2, 4, 6, 8 in between). 0 = continuous.
 * @param onValueChangeFinished Called when a drag or tap ends; not for updating value.
 * @param colors Visual colors, see [RuiSliderDefaults.colors].
 */
@Composable
public fun RuiSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: RuiSliderColors = RuiSliderDefaults.colors(),
) {
    require(steps >= 0) { "steps should be >= 0" }
    require(valueRange.start < valueRange.endInclusive) { "valueRange start should be less than end" }

    val haptics = rememberRuiHaptics()
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl
    val onValueChangeState by rememberUpdatedState(onValueChange)
    val onValueChangeFinishedState by rememberUpdatedState(onValueChangeFinished)
    val hapticState = remember { SliderHapticState() }
    var isDragging by remember { mutableStateOf(false) }
    var trackWidthPx by remember { mutableIntStateOf(0) }
    val motion = RuiTheme.motion

    val coercedValue = value.coerceIn(valueRange.start, valueRange.endInclusive)

    // Tight spring while dragging so the thumb tracks the finger; soft spring
    // for tap jumps and programmatic changes.
    val progressSpec = if (isDragging) motion.tracking else motion.settle
    val animatedValue by animateFloatAsState(coercedValue, progressSpec)
    val thumbScale by animateFloatAsState(
        targetValue = if (isDragging) 1.127f else 1f,
        animationSpec = motion.press,
    )

    val fill = if (enabled) colors.trackFill else colors.disabledTrackFill
    val rest = if (enabled) colors.trackRest else colors.disabledTrackRest
    val thumb = if (enabled) colors.thumb else colors.disabledThumb

    Box(
        modifier = modifier
            .heightIn(min = RuiTheme.dimens.touchTarget)
            .fillMaxWidth()
            .onSizeChanged { trackWidthPx = it.width }
            .then(
                if (enabled) {
                    Modifier
                        // Tap-to-jump and drag-to-adjust are separate
                        // detectors: the tap does not claim any movement, and
                        // the drag only claims the gesture after horizontal
                        // touch slop. A vertical swipe is left to the
                        // enclosing scroll instead of nudging the value.
                        .pointerInput(valueRange, steps, isRtl) {
                            detectTapGestures { offset ->
                                val newValue = sliderValueAt(
                                    x = offset.x,
                                    trackWidthPx = size.width.toFloat(),
                                    thumbRadiusPx = RuiSliderDefaults.ThumbSize.toPx() / 2f,
                                    steps = steps,
                                    isRtl = isRtl,
                                    valueRange = valueRange,
                                )
                                hapticState.onDragStart(
                                    fraction = sliderFractionOf(newValue, valueRange),
                                    steps = steps,
                                    haptics = haptics,
                                )
                                onValueChangeState(newValue)
                                onValueChangeFinishedState?.invoke()
                            }
                        }
                        .pointerInput(valueRange, steps, isRtl) {
                            val thumbRadiusPx = RuiSliderDefaults.ThumbSize.toPx() / 2f
                            fun valueAtX(x: Float): Float =
                                sliderValueAt(x, size.width.toFloat(), thumbRadiusPx, steps, isRtl, valueRange)

                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    isDragging = true
                                    val newValue = valueAtX(offset.x)
                                    hapticState.onDragStart(
                                        fraction = sliderFractionOf(newValue, valueRange),
                                        steps = steps,
                                        haptics = haptics,
                                    )
                                    onValueChangeState(newValue)
                                },
                                onHorizontalDrag = { change, _ ->
                                    val newValue = valueAtX(change.position.x)
                                    hapticState.onValueChange(
                                        fraction = sliderFractionOf(newValue, valueRange),
                                        steps = steps,
                                        haptics = haptics,
                                    )
                                    onValueChangeState(newValue)
                                },
                                onDragEnd = {
                                    isDragging = false
                                    onValueChangeFinishedState?.invoke()
                                },
                                onDragCancel = {
                                    isDragging = false
                                    onValueChangeFinishedState?.invoke()
                                },
                            )
                        }
                } else {
                    Modifier
                },
            )
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(
                    coercedValue,
                    valueRange.start..valueRange.endInclusive,
                    if (steps > 0) steps else 0,
                )
                if (enabled) {
                    setProgress { target ->
                        onValueChangeState(target.coerceIn(valueRange.start, valueRange.endInclusive))
                        true
                    }
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(RuiSliderDefaults.TrackHeight),
        ) {
            val stroke = size.height
            val thumbRadiusPx = RuiSliderDefaults.ThumbSize.toPx() / 2f
            val visual = sliderFractionOf(animatedValue, valueRange)
            val thumbCx = sliderThumbX(visual, size.width, thumbRadiusPx, isRtl)
            val cy = size.height / 2f
            // Round caps extend by stroke/2; inset the ends so the capsule
            // spans exactly 0..width.
            drawLine(
                color = rest,
                start = Offset(stroke / 2f, cy),
                end = Offset(size.width - stroke / 2f, cy),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
            if (thumbCx > stroke / 2f + 0.5f) {
                drawLine(
                    color = fill,
                    start = Offset(stroke / 2f, cy),
                    end = Offset(thumbCx, cy),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .graphicsLayer {
                    // The box is start-anchored: translationX must place its
                    // LEFT edge at the intended center minus one radius, or
                    // every thumb renders one radius right of its value and
                    // overflows the track at the maximum.
                    val thumbRadiusPx = RuiSliderDefaults.ThumbSize.toPx() / 2f
                    translationX = sliderThumbX(
                        sliderFractionOf(animatedValue, valueRange),
                        trackWidthPx.toFloat(),
                        thumbRadiusPx,
                        isRtl,
                    ) - thumbRadiusPx
                    scaleX = thumbScale
                    scaleY = thumbScale
                }
                .size(RuiSliderDefaults.ThumbSize)
                .shadow(RuiSliderDefaults.ThumbElevation, CircleShape)
                .background(thumb, CircleShape),
        )
    }
}

/**
 * Haptic ticks while dragging: a selection tick per step crossing when [steps]
 * is set, and a light impact on reaching either end. Mechanics from miuix's
 * SliderHapticState.
 */
internal class SliderHapticState {
    private var lastStepIndex: Int? = null
    private var lastEdge: Int = -1

    fun onDragStart(fraction: Float, steps: Int, haptics: RuiHaptics) {
        lastStepIndex = if (steps > 0) (fraction * (steps + 1)).roundToInt() else null
        lastEdge = when {
            fraction <= 0f -> 0
            fraction >= 1f -> 1
            else -> -1
        }
    }

    fun onValueChange(fraction: Float, steps: Int, haptics: RuiHaptics) {
        if (steps > 0) {
            val index = (fraction * (steps + 1)).roundToInt()
            if (index != lastStepIndex) {
                lastStepIndex = index
                haptics.selection()
            }
        }
        val edge = when {
            fraction <= 0f -> 0
            fraction >= 1f -> 1
            else -> -1
        }
        if (edge != -1 && edge != lastEdge) {
            lastEdge = edge
            haptics.impactLight()
        }
    }
}

/** The fraction of [value] between [valueRange]'s endpoints, clamped to 0..1. */
internal fun sliderFractionOf(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
): Float = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)

/** The x of a thumb at [fraction] along a track [trackWidthPx] wide. */
internal fun sliderThumbX(
    fraction: Float,
    trackWidthPx: Float,
    thumbRadiusPx: Float,
    isRtl: Boolean,
): Float {
    val available = (trackWidthPx - 2f * thumbRadiusPx).coerceAtLeast(0f)
    val visual = if (isRtl) 1f - fraction else fraction
    return thumbRadiusPx + visual * available
}

/** The value a touch at [x] selects: track-proportional, snapped to [steps]. */
internal fun sliderValueAt(
    x: Float,
    trackWidthPx: Float,
    thumbRadiusPx: Float,
    steps: Int,
    isRtl: Boolean,
    valueRange: ClosedFloatingPointRange<Float>,
): Float {
    val available = (trackWidthPx - 2f * thumbRadiusPx).coerceAtLeast(0f)
    val fraction = ((x - thumbRadiusPx) / available).coerceIn(0f, 1f)
    val visual = if (isRtl) 1f - fraction else fraction
    val intervals = steps + 1
    val snapped = if (steps > 0) {
        (visual * intervals).roundToInt().toFloat() / intervals
    } else {
        visual
    }
    return valueRange.start + (valueRange.endInclusive - valueRange.start) * snapped
}
