package rui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.horizontalDrag
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
import kotlin.math.abs
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
    public val ThumbSize: Dp = 26.dp

    /** Elevation of the thumb's soft shadow. */
    public val ThumbElevation: Dp = 3.dp

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
 * the accent color and a white circular thumb. Touching the track moves the
 * thumb to the touch point immediately and the drag follows the finger, as on
 * UISlider. With [steps] > 0 the thumb snaps to evenly spaced discrete values
 * and a haptic tick fires whenever the selection crosses into the next step.
 *
 * @param value The current value, coerced into [valueRange].
 * @param onValueChange Called with the new value while dragging.
 * @param enabled Whether the slider responds to touch.
 * @param valueRange Inclusive range the slider can take.
 * @param steps Number of discrete values *between* the endpoints (a range of
 *   0..10 with 4 steps allows 2, 4, 6, 8 in between). 0 = continuous.
 * @param onValueChangeFinished Called when a drag ends; not for updating value.
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
                    Modifier.pointerInput(valueRange, steps, isRtl) {
                        val thumbRadiusPx = RuiSliderDefaults.ThumbSize.toPx() / 2f
                        fun valueAtX(x: Float): Float =
                            sliderValueAt(x, size.width.toFloat(), thumbRadiusPx, steps, isRtl, valueRange)

                        awaitEachGesture {
                            val down = awaitFirstDown()
                            isDragging = true
                            val startValue = valueAtX(down.position.x)
                            hapticState.onDragStart(
                                fraction = (startValue - valueRange.start) /
                                    (valueRange.endInclusive - valueRange.start),
                                steps = steps,
                                haptics = haptics,
                            )
                            onValueChangeState(startValue)
                            horizontalDrag(down.id) { change ->
                                val newValue = valueAtX(change.position.x)
                                hapticState.onValueChange(
                                    fraction = (newValue - valueRange.start) /
                                        (valueRange.endInclusive - valueRange.start),
                                    steps = steps,
                                    haptics = haptics,
                                )
                                onValueChangeState(newValue)
                                change.consume()
                            }
                            isDragging = false
                            onValueChangeFinishedState?.invoke()
                        }
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

/** Which of the two range thumbs a press at [downPx] takes — always the nearest. */
internal fun selectRangeThumb(startPx: Float, endPx: Float, downPx: Float): RangeThumb =
    if (abs(downPx - startPx) <= abs(downPx - endPx)) RangeThumb.Start else RangeThumb.End

internal enum class RangeThumb { Start, End }

/** One drag move's outcome: the clamped pair, and whether the drag changed hands. */
internal data class RangeDragResult(val start: Float, val end: Float, val handoff: Boolean)

/**
 * Applies one drag move to the pair, clamping the moving thumb against the
 * other so thumbs never cross. When the thumbs are pinned together and the
 * finger keeps pushing *past* them, the drag changes hands — the far thumb
 * takes over and follows the finger, the way a range slider must feel.
 */
internal fun rangeDragStep(
    active: RangeThumb,
    moveValue: Float,
    start: Float,
    end: Float,
): RangeDragResult = when (active) {
    RangeThumb.Start -> when {
        moveValue > end && start == end -> RangeDragResult(start = start, end = moveValue, handoff = true)
        else -> RangeDragResult(start = moveValue.coerceAtMost(end), end = end, handoff = false)
    }
    RangeThumb.End -> when {
        moveValue < start && start == end -> RangeDragResult(start = moveValue, end = end, handoff = true)
        else -> RangeDragResult(start = start, end = moveValue.coerceAtLeast(start), handoff = false)
    }
}

/**
 * The two-thumb range form of [RuiSlider]: the same thin capsule with the
 * segment *between* the thumbs filled in the accent color, both thumbs white
 * with the slider's soft shadow. Touching a thumb drags it; touching the
 * track jumps the nearest thumb to the touch point and drags it from there,
 * as on [RuiSlider]. Thumbs never cross — a thumb pins against its partner —
 * and when the two are pinned together and the finger keeps pushing past,
 * the drag changes hands to the far thumb so a collapsed range can be spread
 * again without lifting the finger (miuix's hand-off, see NOTICE). With
 * [steps] > 0 both thumbs snap to discrete values and a haptic tick fires
 * per step crossing.
 *
 * @param value The current range, both endpoints coerced into [valueRange].
 * @param onValueChange Called with the new range while dragging.
 * @param valueRange Inclusive range the slider can take.
 * @param steps Number of discrete values *between* the endpoints. 0 = continuous.
 * @param onValueChangeFinished Called when a drag ends; not for updating value.
 * @param colors Visual colors, see [RuiSliderDefaults.colors].
 */
@Composable
public fun RuiRangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
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
    val startHaptics = remember { SliderHapticState() }
    val endHaptics = remember { SliderHapticState() }
    var activeThumb by remember { mutableStateOf<RangeThumb?>(null) }
    var trackWidthPx by remember { mutableIntStateOf(0) }
    val motion = RuiTheme.motion

    val coercedStart = value.start.coerceIn(valueRange.start, valueRange.endInclusive)
    val coercedEnd = value.endInclusive.coerceIn(valueRange.start, valueRange.endInclusive)

    // Tight spring while dragging so the active thumb tracks the finger; soft
    // spring for jump-to-touch and programmatic changes.
    val progressSpec = if (activeThumb != null) motion.tracking else motion.settle
    val animatedStart by animateFloatAsState(coercedStart, progressSpec)
    val animatedEnd by animateFloatAsState(coercedEnd, progressSpec)
    val startThumbScale by animateFloatAsState(
        targetValue = if (activeThumb == RangeThumb.Start) 1.127f else 1f,
        animationSpec = motion.press,
    )
    val endThumbScale by animateFloatAsState(
        targetValue = if (activeThumb == RangeThumb.End) 1.127f else 1f,
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
                    Modifier.pointerInput(valueRange, steps, isRtl) {
                        val trackWidth = size.width.toFloat()
                        val thumbRadiusPx = RuiSliderDefaults.ThumbSize.toPx() / 2f
                        // miuix's generous hit zone: the knob plus half a thumb.
                        val hitRadiusPx = thumbRadiusPx * 0.72f + thumbRadiusPx * 0.5f

                        awaitEachGesture {
                            val down = awaitFirstDown()
                            var start = coercedStart
                            var end = coercedEnd
                            val startPx = sliderThumbX(
                                sliderFractionOf(start, valueRange), trackWidth, thumbRadiusPx, isRtl,
                            )
                            val endPx = sliderThumbX(
                                sliderFractionOf(end, valueRange), trackWidth, thumbRadiusPx, isRtl,
                            )
                            var active = selectRangeThumb(startPx, endPx, down.position.x)
                            val downValue = sliderValueAt(
                                down.position.x, trackWidth, thumbRadiusPx, steps, isRtl, valueRange,
                            )
                            if (abs(down.position.x - (if (active == RangeThumb.Start) startPx else endPx)) > hitRadiusPx) {
                                // Track press: jump the nearest thumb to the
                                // touch point, pinned against its partner.
                                when (active) {
                                    RangeThumb.Start -> start = downValue.coerceAtMost(end)
                                    RangeThumb.End -> end = downValue.coerceAtLeast(start)
                                }
                                onValueChangeState(start..end)
                            }
                            activeThumb = active
                            (if (active == RangeThumb.Start) startHaptics else endHaptics).onDragStart(
                                fraction = sliderFractionOf(
                                    if (active == RangeThumb.Start) start else end,
                                    valueRange,
                                ),
                                steps = steps,
                                haptics = haptics,
                            )

                            horizontalDrag(down.id) { change ->
                                val step = rangeDragStep(
                                    active = active,
                                    moveValue = sliderValueAt(
                                        change.position.x, trackWidth, thumbRadiusPx, steps, isRtl, valueRange,
                                    ),
                                    start = start,
                                    end = end,
                                )
                                start = step.start
                                end = step.end
                                if (step.handoff) {
                                    active = if (active == RangeThumb.Start) RangeThumb.End else RangeThumb.Start
                                    activeThumb = active
                                }
                                (if (active == RangeThumb.Start) startHaptics else endHaptics).onValueChange(
                                    fraction = sliderFractionOf(
                                        if (active == RangeThumb.Start) start else end,
                                        valueRange,
                                    ),
                                    steps = steps,
                                    haptics = haptics,
                                )
                                onValueChangeState(start..end)
                                change.consume()
                            }

                            activeThumb = null
                            onValueChangeFinishedState?.invoke()
                        }
                    }
                } else {
                    Modifier
                },
            )
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(
                    coercedStart,
                    valueRange.start..valueRange.endInclusive,
                    if (steps > 0) steps else 0,
                )
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
            val startCx = sliderThumbX(
                sliderFractionOf(animatedStart, valueRange), size.width, thumbRadiusPx, isRtl,
            )
            val endCx = sliderThumbX(
                sliderFractionOf(animatedEnd, valueRange), size.width, thumbRadiusPx, isRtl,
            )
            val cy = size.height / 2f
            drawLine(
                color = rest,
                start = Offset(stroke / 2f, cy),
                end = Offset(size.width - stroke / 2f, cy),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
            if (abs(endCx - startCx) > 0.5f) {
                drawLine(
                    color = fill,
                    start = Offset(minOf(startCx, endCx), cy),
                    end = Offset(maxOf(startCx, endCx), cy),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
            }
        }
        RangeThumb.entries.forEach { thumbEdge ->
            val animated = if (thumbEdge == RangeThumb.Start) animatedStart else animatedEnd
            val scale = if (thumbEdge == RangeThumb.Start) startThumbScale else endThumbScale
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .graphicsLayer {
                        // Same left-edge correction as the single slider.
                        val thumbRadiusPx = RuiSliderDefaults.ThumbSize.toPx() / 2f
                        translationX = sliderThumbX(
                            sliderFractionOf(animated, valueRange),
                            trackWidthPx.toFloat(),
                            thumbRadiusPx,
                            isRtl,
                        ) - thumbRadiusPx
                        scaleX = scale
                        scaleY = scale
                    }
                    .size(RuiSliderDefaults.ThumbSize)
                    .shadow(RuiSliderDefaults.ThumbElevation, CircleShape)
                    .background(thumb, CircleShape),
            )
        }
    }
}
