package rui.components

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import rui.theme.RuiTheme
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Tells a [RuiScrollBar] how the scrollable it describes is positioned and how
 * to drive it, in pixels. Create one from a [ScrollState] or a [LazyListState]
 * with [rememberRuiScrollBarAdapter]; the adapter reads live state (including
 * lazy layout estimates) and scrolling through it keeps momentum — dragging
 * the thumb feels like scrolling the content itself.
 */
public interface RuiScrollBarAdapter {

    /** Current scroll position in pixels, `0.0` at the content's start. */
    public val scrollOffset: Double

    /** Total scrollable content size in pixels, padding included. */
    public val contentSize: Double

    /** The visible viewport's size along the scroll axis, in pixels. */
    public val viewportSize: Double

    /**
     * Scroll so that [scrollOffset] becomes the current position. Implementations
     * should preserve momentum: for lazy layouts prefer a relative [scrollBy]
     * over a jump when the distance is within a viewport.
     */
    public suspend fun scrollTo(scrollOffset: Double)
}

/**
 * Creates and [remember]s a [RuiScrollBarAdapter] for a [ScrollState] — the
 * scroll state of a plain `Column(Modifier.verticalScroll(...))` or
 * `Modifier.horizontalScroll(...)`.
 */
@Composable
public fun rememberRuiScrollBarAdapter(
    state: ScrollState,
): RuiScrollBarAdapter = remember(state) { ScrollStateScrollBarAdapter(state) }

/**
 * Creates and [remember]s a [RuiScrollBarAdapter] for a [LazyListState]. The
 * adapter estimates total content size from the visible rows' average size —
 * the thumb's length and position refine themselves as rows are measured.
 */
@Composable
public fun rememberRuiScrollBarAdapter(
    state: LazyListState,
): RuiScrollBarAdapter = remember(state) { LazyListScrollBarAdapter(state) }

/** Default values for [RuiScrollBar]. */
public object RuiScrollBarDefaults {

    /** Resting thumb thickness. */
    public val ThumbWidth: Dp = 3.5.dp

    /** Thumb thickness while dragging. */
    public val DragThumbWidth: Dp = 6.dp

    /** Smallest thumb length, so barely-scrollable content keeps a visible thumb. */
    public val ThumbMinLength: Dp = 36.dp

    /** Distance between the thumb and the viewport's trailing edge. */
    public val EdgePadding: Dp = 3.dp

    /** How wide a pointer press may land to start a thumb drag. */
    public val TouchStripWidth: Dp = 44.dp

    /** Resting thumb opacity over [RuiTheme.colors.content]. */
    public const val ThumbAlpha: Float = 0.35f

    /** Thumb opacity while dragging. */
    public const val DragThumbAlpha: Float = 0.6f

    /** Idle time after the last scroll event before the thumb starts fading. */
    public const val FadeDelayMillis: Int = 700

    /** How long the fade out takes. */
    public const val FadeDurationMillis: Int = 300

    /** How long the thumb's growth into its dragged thickness takes. */
    public const val GrowDurationMillis: Int = 150
}

/**
 * A scroll indicator in the iOS manner: a rounded content-colored pill riding
 * the viewport's trailing edge, shown while the content scrolls and fading out
 * once it settles. Place it as a sibling overlay on top of the scrollable —
 *
 * ```
 * Box {
 *     RuiLazyColumn(...) { ... }
 *     RuiScrollBar(adapter, Modifier.align(Alignment.CenterEnd))
 * }
 * ```
 *
 * With a mouse pointer (desktop, or a mouse attached to a tablet) the thumb is
 * a control: pressing it and dragging scrolls the content, the thumb grows and
 * darkens while held. Touch pointers leave the thumb alone — like UIKit's,
 * it is an indicator only — so resting a finger on the edge still scrolls.
 *
 * Content that fits its viewport draws nothing. The component measures only
 * as wide as its thumb needs, so it never blocks the content's own gestures.
 *
 * @param adapter The scrollable this bar describes; see [rememberRuiScrollBarAdapter].
 * @param orientation Which edge the pill rides and which axis it maps to.
 * @param reverseLayout Matches the scrollable's `reverseLayout` — flips which
 *   end of the track is the content's start.
 * @param trackPadding Insets the drawable track from the bar's own ends; pass
 *   the scrollable's content padding so the thumb does not ride under bars.
 * @param interactive Whether a mouse press can grab the thumb; false leaves a
 *   pure indicator even for mouse pointers.
 * @param thumbColor Overrides the content-colored thumb; use [Color.Unspecified]
 *   to keep the theme default. A specific color keeps its own alpha.
 * @param trackColor Draws a full-length track behind the thumb when specified;
 *   [Color.Unspecified] (default) draws no track.
 */
@Composable
public fun RuiScrollBar(
    adapter: RuiScrollBarAdapter,
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.Vertical,
    reverseLayout: Boolean = false,
    trackPadding: PaddingValues = PaddingValues(0.dp),
    interactive: Boolean = true,
    thumbColor: Color = Color.Unspecified,
    trackColor: Color = Color.Unspecified,
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val scope = rememberCoroutineScope()
    val baseThumbColor = if (thumbColor.isSpecified) thumbColor else RuiTheme.colors.content
    val isVertical = orientation == Orientation.Vertical

    val minThumbPx = with(density) { RuiScrollBarDefaults.ThumbMinLength.toPx() }
    val beforeTrackPaddingPx = with(density) {
        if (isVertical) {
            trackPadding.calculateTopPadding().toPx()
        } else {
            trackPadding.calculateStartPadding(layoutDirection).toPx()
        }
    }
    val afterTrackPaddingPx = with(density) {
        if (isVertical) {
            trackPadding.calculateBottomPadding().toPx()
        } else {
            trackPadding.calculateEndPadding(layoutDirection).toPx()
        }
    }
    val totalTrackPaddingPx = beforeTrackPaddingPx + afterTrackPaddingPx

    var opacity by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var fadeJob by remember { mutableStateOf<Job?>(null) }
    var displayedThumbLength by remember { mutableFloatStateOf(0f) }
    var thumbLengthJob by remember { mutableStateOf<Job?>(null) }
    val dragMutex = remember { Mutex() }
    var unscrolledDragDistance by remember { mutableStateOf(0.0) }

    val growSpec = tween<Float>(RuiScrollBarDefaults.GrowDurationMillis)
    val animatedThumbWidthPx by animateFloatAsState(
        targetValue = with(density) {
            (if (isDragging) RuiScrollBarDefaults.DragThumbWidth else RuiScrollBarDefaults.ThumbWidth).toPx()
        },
        animationSpec = growSpec,
    )
    val animatedThumbAlpha by animateFloatAsState(
        targetValue = when {
            isDragging && thumbColor.isUnspecified -> RuiScrollBarDefaults.DragThumbAlpha
            thumbColor.isSpecified -> thumbColor.alpha
            else -> RuiScrollBarDefaults.ThumbAlpha
        },
        animationSpec = growSpec,
    )

    fun showAndScheduleFade() {
        opacity = 1f
        fadeJob?.cancel()
        if (!isDragging) {
            fadeJob = scope.launch {
                delay(RuiScrollBarDefaults.FadeDelayMillis.toLong())
                animate(
                    initialValue = 1f,
                    targetValue = 0f,
                    animationSpec = tween(RuiScrollBarDefaults.FadeDurationMillis),
                ) { value, _ -> opacity = value }
            }
        }
    }

    LaunchedEffect(adapter) {
        snapshotFlow { adapter.scrollOffset }
            .drop(1)
            .collect { if (!isDragging) showAndScheduleFade() }
    }
    LaunchedEffect(isDragging) {
        if (isDragging) {
            fadeJob?.cancel()
            opacity = 1f
        } else if (opacity > 0f) {
            showAndScheduleFade()
        }
    }

    val measurePolicy = if (isVertical) {
        verticalMeasurePolicy(density)
    } else {
        horizontalMeasurePolicy(density)
    }

    Layout(
        content = {},
        modifier = modifier
            .then(
                if (!interactive) {
                    Modifier
                } else {
                    Modifier.pointerInput(adapter, isVertical, beforeTrackPaddingPx, reverseLayout) {
                        val touchStripPx = RuiScrollBarDefaults.TouchStripWidth.toPx()

                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            // Touch pointers keep the thumb a pure indicator —
                            // dragging the edge must keep scrolling the content.
                            if (down.type != PointerType.Mouse) return@awaitEachGesture

                            val inStrip = if (isVertical) {
                                down.position.x >= size.width - touchStripPx
                            } else {
                                down.position.y >= size.height - touchStripPx
                            }
                            if (!inStrip) return@awaitEachGesture

                            val geometry = liveGeometry(
                                nodeExtent = (if (isVertical) size.height else size.width).toFloat(),
                                totalTrackPaddingPx = totalTrackPaddingPx,
                                adapter = adapter,
                                minThumbPx = minThumbPx,
                                reverseLayout = reverseLayout,
                            )
                            val thumbRange = geometry.thumbPixelRange(adapter.scrollOffset)
                            val alongTrack = if (isVertical) down.position.y else down.position.x
                            val adjusted = alongTrack - beforeTrackPaddingPx
                            if (adjusted < thumbRange.first || adjusted > thumbRange.last) {
                                return@awaitEachGesture
                            }

                            isDragging = true
                            down.consume()
                            unscrolledDragDistance = 0.0

                            drag(down.id) { change ->
                                val live = liveGeometry(
                                    nodeExtent = (if (isVertical) size.height else size.width).toFloat(),
                                    totalTrackPaddingPx = totalTrackPaddingPx,
                                    adapter = adapter,
                                    minThumbPx = minThumbPx,
                                    reverseLayout = reverseLayout,
                                )
                                val delta = (if (isVertical) change.positionChange().y else change.positionChange().x)
                                    .toDouble()
                                val step = dragThumbPosition(
                                    currentPosition = live.positionOf(adapter.scrollOffset),
                                    dragDelta = delta,
                                    unscrolled = unscrolledDragDistance,
                                    maxPosition = live.maxPosition,
                                )
                                unscrolledDragDistance = step.unscrolled
                                scope.launch(start = CoroutineStart.UNDISPATCHED) {
                                    dragMutex.withLock {
                                        adapter.scrollTo(live.scrollOffsetOf(step.position))
                                    }
                                }
                                change.consume()
                            }

                            isDragging = false
                        }
                    }
                },
            )
            .drawBehind {
                if (opacity <= 0f) return@drawBehind
                val geometry = liveGeometry(
                    nodeExtent = if (isVertical) size.height else size.width,
                    totalTrackPaddingPx = totalTrackPaddingPx,
                    adapter = adapter,
                    minThumbPx = minThumbPx,
                    reverseLayout = reverseLayout,
                )
                if (geometry.thumbSize >= geometry.trackSize) return@drawBehind

                val thumbWidthPx = animatedThumbWidthPx
                val edgePaddingPx = RuiScrollBarDefaults.EdgePadding.toPx()
                val cornerRadius = CornerRadius(thumbWidthPx / 2f)

                if (trackColor.isSpecified) {
                    val track = trackColor.copy(alpha = trackColor.alpha * opacity)
                    if (isVertical) {
                        drawRoundRect(
                            color = track,
                            topLeft = Offset(size.width - thumbWidthPx - edgePaddingPx, beforeTrackPaddingPx),
                            size = Size(thumbWidthPx, size.height - totalTrackPaddingPx.toFloat()),
                            cornerRadius = cornerRadius,
                        )
                    } else {
                        drawRoundRect(
                            color = track,
                            topLeft = Offset(beforeTrackPaddingPx, size.height - thumbWidthPx - edgePaddingPx),
                            size = Size(size.width - totalTrackPaddingPx.toFloat(), thumbWidthPx),
                            cornerRadius = cornerRadius,
                        )
                    }
                }

                // The lazy adapter's thumb length rides on a rolling average of
                // row sizes; easing the value hides the estimation's small jumps.
                val targetThumbLength = geometry.thumbSize.toFloat()
                if (abs(targetThumbLength - displayedThumbLength) >= 1f) {
                    if (thumbLengthJob?.isActive != true) {
                        val startValue = displayedThumbLength
                        thumbLengthJob = scope.launch {
                            animate(
                                initialValue = startValue,
                                targetValue = targetThumbLength,
                                animationSpec = tween(RuiScrollBarDefaults.GrowDurationMillis),
                            ) { value, _ -> displayedThumbLength = value }
                        }
                    }
                } else {
                    displayedThumbLength = targetThumbLength
                }

                val color = baseThumbColor.copy(alpha = animatedThumbAlpha * opacity)
                val thumbLength = displayedThumbLength
                val thumbOffset = beforeTrackPaddingPx + geometry.positionOf(adapter.scrollOffset).toFloat()

                if (isVertical) {
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(size.width - thumbWidthPx - edgePaddingPx, thumbOffset),
                        size = Size(thumbWidthPx, thumbLength),
                        cornerRadius = cornerRadius,
                    )
                } else {
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(thumbOffset, size.height - thumbWidthPx - edgePaddingPx),
                        size = Size(thumbLength, thumbWidthPx),
                        cornerRadius = cornerRadius,
                    )
                }
            },
        measurePolicy = measurePolicy,
    )
}

private fun verticalMeasurePolicy(density: Density) = MeasurePolicy { _, constraints ->
    val width = with(density) {
        (RuiScrollBarDefaults.ThumbWidth + RuiScrollBarDefaults.EdgePadding * 2).roundToPx()
    }.coerceAtMost(constraints.maxWidth)
    layout(width, constraints.maxHeight) {}
}

private fun horizontalMeasurePolicy(density: Density) = MeasurePolicy { _, constraints ->
    val height = with(density) {
        (RuiScrollBarDefaults.ThumbWidth + RuiScrollBarDefaults.EdgePadding * 2).roundToPx()
    }.coerceAtMost(constraints.maxHeight)
    layout(constraints.maxWidth, height) {}
}

/**
 * The geometry of the thumb right now: the node's own extent carries the
 * track, so no state bridges measure, drag and draw — each reads what it needs.
 */
private fun liveGeometry(
    nodeExtent: Float,
    totalTrackPaddingPx: Float,
    adapter: RuiScrollBarAdapter,
    minThumbPx: Float,
    reverseLayout: Boolean,
): ScrollBarGeometry =
    ScrollBarGeometry(
        trackSize = (nodeExtent - totalTrackPaddingPx).toDouble().coerceAtLeast(0.0),
        contentSize = adapter.contentSize,
        viewportSize = adapter.viewportSize,
        minThumbSize = minThumbPx.toDouble(),
        reverseLayout = reverseLayout,
    )

/**
 * Thumb placement math for one track: a proportional thumb with a minimum
 * length, and the scroll-to-position mapping in both directions.
 */
internal class ScrollBarGeometry(
    val trackSize: Double,
    contentSize: Double,
    viewportSize: Double,
    minThumbSize: Double,
    private val reverseLayout: Boolean = false,
) {
    val maxScrollOffset: Double = (contentSize - viewportSize).coerceAtLeast(0.0)

    private val visiblePart: Double =
        if (contentSize <= 0.0) 1.0 else (viewportSize / contentSize).coerceIn(0.0, 1.0)

    val thumbSize: Double = (trackSize * visiblePart).coerceAtLeast(minThumbSize)

    private val scrollScale: Double =
        if (maxScrollOffset <= 0.0) 1.0 else (trackSize - thumbSize) / maxScrollOffset

    /** The furthest position the thumb's start can take along the track. */
    val maxPosition: Double = (trackSize - thumbSize).coerceAtLeast(0.0)

    /** The thumb's start position along the track for a scroll offset. */
    fun positionOf(scrollOffset: Double): Double {
        val raw = scrollScale * scrollOffset.coerceIn(0.0, maxScrollOffset)
        return if (reverseLayout) trackSize - thumbSize - raw else raw
    }

    /** The scroll offset that puts the thumb's start at [position]; inverse of [positionOf]. */
    fun scrollOffsetOf(position: Double): Double {
        val raw = if (reverseLayout) trackSize - thumbSize - position else position
        return (raw / scrollScale).coerceIn(0.0, maxScrollOffset)
    }

    /** The thumb's occupied pixel range along the track, for hit testing. */
    fun thumbPixelRange(scrollOffset: Double): IntRange {
        val start = positionOf(scrollOffset).roundToInt()
        return start until start + thumbSize.roundToInt()
    }
}

/**
 * The thumb position a drag step lands on, plus the carried-over distance the
 * track could not absorb at its ends — so dragging past an edge and back
 * resumes from where the finger actually is, as a scrollbar should.
 */
internal data class DragThumbStep(val position: Double, val unscrolled: Double)

internal fun dragThumbPosition(
    currentPosition: Double,
    dragDelta: Double,
    unscrolled: Double,
    maxPosition: Double,
): DragThumbStep {
    val target = (currentPosition + dragDelta + unscrolled).coerceIn(0.0, maxPosition)
    return DragThumbStep(
        position = target,
        unscrolled = unscrolled + dragDelta - (target - currentPosition),
    )
}

/** Adapter over a [ScrollState]: exact sizes, no estimation. */
private class ScrollStateScrollBarAdapter(
    private val state: ScrollState,
) : RuiScrollBarAdapter {

    override val scrollOffset: Double get() = state.value.toDouble()

    override val contentSize: Double
        get() = (state.maxValue + state.viewportSize).toDouble()

    override val viewportSize: Double get() = state.viewportSize.toDouble()

    override suspend fun scrollTo(scrollOffset: Double) {
        state.scrollTo(scrollOffset.roundToInt())
    }
}

/**
 * Adapter over a [LazyListState]: scroll offset and content size are estimated
 * from the visible rows — a stretched or gapped first row (a header riding the
 * edge) is skipped when averaging so it cannot skew the estimate.
 */
private class LazyListScrollBarAdapter(
    private val state: LazyListState,
) : RuiScrollBarAdapter {

    override val scrollOffset: Double get() = lazyGeometry().scrollOffset

    override val contentSize: Double get() = lazyGeometry().contentSize

    override val viewportSize: Double
        get() = with(state.layoutInfo) {
            (if (orientation == Orientation.Vertical) viewportSize.height else viewportSize.width).toDouble()
        }

    override suspend fun scrollTo(target: Double) {
        when (val plan = lazyGeometry().planTo(target)) {
            is LazyScrollBarPlan.ByDelta -> if (plan.delta != 0f) state.scrollBy(plan.delta)
            is LazyScrollBarPlan.ToRow -> state.scrollToItem(plan.index, plan.offset)
        }
    }

    private fun lazyGeometry(): LazyScrollBarGeometry = with(state.layoutInfo) {
        val isVertical = orientation == Orientation.Vertical
        LazyScrollBarGeometry(
            // LazyListItemInfo's offset and size are already main-axis values.
            rows = visibleItemsInfo.map { LazyScrollBarRow(it.index, it.offset, it.size) },
            spacing = mainAxisItemSpacing,
            totalRows = totalItemsCount,
            contentPadding = beforeContentPadding + afterContentPadding,
            viewportSize = if (isVertical) viewportSize.height else viewportSize.width,
        )
    }
}

/** One visible row's main-axis geometry, carried as plain data for testing. */
internal data class LazyScrollBarRow(val index: Int, val offset: Int, val size: Int)

internal sealed interface LazyScrollBarPlan {
    /** A small move: hand the delta to the scrollable so momentum survives. */
    data class ByDelta(val delta: Float) : LazyScrollBarPlan

    /** A long jump: snap to the estimated row, then offset within it. */
    data class ToRow(val index: Int, val offset: Int) : LazyScrollBarPlan
}

/**
 * The estimation math behind [LazyListScrollBarAdapter]: with only the visible
 * rows measured, total content size and the current offset ride on the rows'
 * average size, refined as more rows come on screen.
 */
internal class LazyScrollBarGeometry(
    rows: List<LazyScrollBarRow>,
    private val spacing: Int,
    private val totalRows: Int,
    contentPadding: Int,
    private val viewportSize: Int,
) {
    private val firstRowIndex: Int = firstStableRowIndex(rows, spacing) ?: -1
    private val firstRow: LazyScrollBarRow? = if (firstRowIndex >= 0) rows[firstRowIndex] else null

    private val averageRowSize: Double = if (firstRowIndex < 0 || totalRows == 0) {
        0.0
    } else {
        val first = rows[firstRowIndex]
        val last = rows.last()
        val count = rows.size - firstRowIndex
        (last.offset + last.size - first.offset - (count - 1) * spacing).toDouble() / count
    }

    private val averageWithSpacing: Double =
        if (totalRows <= 1) averageRowSize else averageRowSize + spacing

    /** The estimated scroll position in pixels. */
    val scrollOffset: Double = firstRow?.let { it.index * averageWithSpacing - it.offset } ?: 0.0

    /** The estimated total content size in pixels, padding included. */
    val contentSize: Double =
        averageRowSize * totalRows + spacing * (totalRows - 1).coerceAtLeast(0) + contentPadding

    /**
     * How to reach [targetOffset]: a relative delta when the move is within a
     * viewport (smooth, momentum-preserving), otherwise a row snap.
     */
    fun planTo(targetOffset: Double): LazyScrollBarPlan {
        val distance = targetOffset - scrollOffset
        if (abs(distance) <= viewportSize) return LazyScrollBarPlan.ByDelta(distance.toFloat())

        val maxScroll = (contentSize - viewportSize).coerceAtLeast(0.0)
        if (averageWithSpacing <= 0.0) return LazyScrollBarPlan.ByDelta(0f)
        val coerced = targetOffset.coerceIn(0.0, maxScroll)
        val index = (coerced / averageWithSpacing).toInt().coerceIn(0, (totalRows - 1).coerceAtLeast(0))
        return LazyScrollBarPlan.ToRow(
            index = index,
            offset = (coerced - index * averageWithSpacing).toInt().coerceAtLeast(0),
        )
    }

    companion object {

        /**
         * The first visible row safe to average over: a row that overlaps its
         * successor or leaves a gap to it (a stretched header riding the edge)
         * would inflate the average, so averaging starts past it.
         */
        internal fun firstStableRowIndex(rows: List<LazyScrollBarRow>, spacing: Int): Int? {
            if (rows.isEmpty()) return null
            if (rows.size == 1) return 0
            val first = rows[0]
            val second = rows[1]
            return if (first.index < second.index - 1 || first.offset + first.size + spacing > second.offset) {
                1
            } else {
                0
            }
        }
    }
}
