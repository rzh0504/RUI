// Overscroll gesture/settle behavior adapted from the miuix project's
// Overscroll.kt (https://github.com/composable-miuix-ui/miuix,
// Apache License 2.0) — reduced by removing pull-to-refresh coordination.

package rui.foundation.utils

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScrollModifierNode
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidatePlacement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.round
import kotlin.math.sign

/**
 * iOS-style bounce overscroll for a vertical scrollable: dragging past the
 * boundaries pulls the content with a damping curve and settles back on a
 * critical-damped spring. The effect engages only during touch/trackpad pan
 * gestures — mouse wheel and keyboard scrolling pass through untouched.
 *
 * Adapted from the miuix project (Apache License 2.0, see NOTICE).
 */
@Stable
public fun Modifier.overScrollVertical(
    nestedScrollToParent: Boolean = true,
): Modifier = overScrollOutOfBound(isVertical = true, nestedScrollToParent = nestedScrollToParent)

/** Horizontal counterpart of [overScrollVertical]. */
@Stable
public fun Modifier.overScrollHorizontal(
    nestedScrollToParent: Boolean = true,
): Modifier = overScrollOutOfBound(isVertical = false, nestedScrollToParent = nestedScrollToParent)

@Stable
private fun Modifier.overScrollOutOfBound(
    isVertical: Boolean,
    nestedScrollToParent: Boolean,
): Modifier = this
    .clipToBounds()
    .then(OverscrollElement(isVertical, nestedScrollToParent))

private data class OverscrollElement(
    val isVertical: Boolean,
    val nestedScrollToParent: Boolean,
) : ModifierNodeElement<OverscrollNode>() {
    override fun create(): OverscrollNode = OverscrollNode(isVertical, nestedScrollToParent)

    override fun update(node: OverscrollNode) {
        node.update(isVertical, nestedScrollToParent)
        node.invalidatePlacement()
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "overScrollOutOfBound"
        properties["isVertical"] = isVertical
        properties["nestedScrollToParent"] = nestedScrollToParent
    }
}

private class OverscrollNode(
    var isVertical: Boolean,
    var nestedScrollToParent: Boolean,
) : DelegatingNode(),
    CompositionLocalConsumerModifierNode,
    LayoutModifierNode,
    NestedScrollConnection {
    private val density: androidx.compose.ui.unit.Density
        get() = currentValueOf(LocalDensity)

    private val windowInfo: androidx.compose.ui.platform.WindowInfo
        get() = currentValueOf(LocalWindowInfo)

    private val dispatcher = NestedScrollDispatcher()
    private val springEngine = SpringEngine()
    private var animationJob: Job? = null
    private val offsetThreshold = 1f

    // Track press/pan sessions, including custom mouse drags. Wheel-only input
    // must not accumulate overscroll because it may never dispatch a fling to
    // settle the offset.
    private var gestureActive = false

    init {
        delegate(
            SuspendingPointerInputModifierNode {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val active = when (event.type) {
                            PointerEventType.PanStart, PointerEventType.PanMove -> true
                            PointerEventType.PanEnd -> false
                            PointerEventType.Scroll -> gestureActive
                            else -> event.changes.any { it.pressed }
                        }
                        if (gestureActive && !active && animationJob?.isActive != true && abs(offset) > offsetThreshold) {
                            // Settle gestures without a fling; onPreFling replaces
                            // this fallback when needed.
                            startSpringAnimation()
                        }
                        gestureActive = active
                    }
                }
            },
        )
    }

    private var lastPlacedOffset = 0f
    var offset = 0f
        private set(value) {
            if (field != value) {
                field = value
                // Placement rounds to pixels; invalidate only on pixel changes.
                val rounded = round(value)
                if (rounded != lastPlacedOffset) {
                    lastPlacedOffset = rounded
                    if (isAttached) invalidatePlacement()
                }
            }
        }

    private var rawTouchAccumulation = 0f
    private var scrollRange: Float = 0f
    private var cachedScrollRangeDensity: androidx.compose.ui.unit.Density? = null
    private var cachedScrollRangeWindowInfo: androidx.compose.ui.platform.WindowInfo? = null

    override fun onAttach() {
        super.onAttach()
        updateScrollRange()
        delegate(nestedScrollModifierNode(this, dispatcher))
    }

    override fun onDetach() {
        super.onDetach()
        gestureActive = false
        resetState()
    }

    fun update(isVertical: Boolean, nestedScrollToParent: Boolean) {
        var rangeChanged = false
        if (this.isVertical != isVertical) {
            rangeChanged = true
        }

        this.isVertical = isVertical
        this.nestedScrollToParent = nestedScrollToParent

        if (rangeChanged && isAttached) {
            updateScrollRange()
        }
    }

    private fun updateScrollRange() {
        val currentDensity = density
        val currentWindowInfo = windowInfo
        if (currentDensity == cachedScrollRangeDensity && currentWindowInfo == cachedScrollRangeWindowInfo) return
        cachedScrollRangeDensity = currentDensity
        cachedScrollRangeWindowInfo = currentWindowInfo
        scrollRange = with(currentDensity) {
            if (isVertical) {
                currentWindowInfo.containerDpSize.height.toPx()
            } else {
                currentWindowInfo.containerDpSize.width.toPx()
            }
        }
    }

    private fun resetState() {
        offset = 0f
        rawTouchAccumulation = 0f
    }

    private fun startSpringAnimation(initialVelocity: Float = 0f) {
        if (abs(offset) <= offsetThreshold && initialVelocity == 0f) {
            resetState()
            return
        }

        animationJob?.cancel()
        animationJob = coroutineScope.launch {
            springEngine.runSettleAnimation(
                startValue = offset,
                initialVelocity = initialVelocity,
                onFrame = { currentPos ->
                    offset = currentPos
                },
                onSettle = {
                    if (abs(offset) <= offsetThreshold) resetState()
                },
            )
        }
    }

    private fun applyDrag(delta: Float) {
        if (delta == 0f) return
        rawTouchAccumulation += delta
        rawTouchAccumulation = rawTouchAccumulation.coerceIn(-scrollRange, scrollRange)
        val normalized = min(abs(rawTouchAccumulation) / scrollRange, 1.0f)
        val dampedDist = SpringMath.obtainDampingDistance(normalized, scrollRange)
        offset = sign(rawTouchAccumulation) * dampedDist
    }

    /** Invert the damping curve when a drag takes over a running spring. */
    private fun syncRawAccumulationFromOffset() {
        rawTouchAccumulation = sign(offset) * SpringMath.obtainTouchDistance(offset, scrollRange)
    }

    /** Release stale overscroll once the child can scroll again. */
    private fun unwindStaleOffset(consumedDelta: Float) {
        if (abs(offset) <= offsetThreshold || consumedDelta == 0f) return
        if (rawTouchAccumulation == 0f) syncRawAccumulationFromOffset()
        if (sign(consumedDelta) != sign(rawTouchAccumulation)) return
        if (abs(rawTouchAccumulation) <= abs(consumedDelta)) {
            resetState()
        } else {
            applyDrag(-consumedDelta)
        }
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        updateScrollRange()
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            placeable.placeWithLayer(0, 0) {
                if (isVertical) {
                    translationY = round(offset)
                } else {
                    translationX = round(offset)
                }
                clip = true
            }
        }
    }

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (!isAttached) return Offset.Zero

        if (source != NestedScrollSource.UserInput || !gestureActive) return Offset.Zero

        val delta = if (isVertical) available.y else available.x
        if (delta == 0f) return Offset.Zero

        // Resync raw accumulation when a drag takes over a running spring.
        if (animationJob?.isActive == true) syncRawAccumulationFromOffset()
        animationJob?.cancel()

        if (abs(offset) <= offsetThreshold || sign(delta) == sign(rawTouchAccumulation)) {
            return Offset.Zero
        }

        if (sign(delta) != sign(rawTouchAccumulation)) {
            val actualConsumed = if (abs(rawTouchAccumulation) <= abs(delta)) {
                -rawTouchAccumulation
            } else {
                delta
            }

            if (abs(rawTouchAccumulation) <= abs(delta)) {
                resetState()
            } else {
                applyDrag(actualConsumed)
            }

            return if (isVertical) Offset(0f, actualConsumed) else Offset(actualConsumed, 0f)
        }

        applyDrag(delta)
        return available
    }

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        if (!isAttached) return Offset.Zero

        if (source != NestedScrollSource.UserInput || !gestureActive) {
            // A running spring settles to zero on its own; only reclaim when
            // it was interrupted.
            if (animationJob?.isActive != true) {
                unwindStaleOffset(if (isVertical) consumed.y else consumed.x)
            }
            return Offset.Zero
        }

        val delta = if (isVertical) available.y else available.x
        val consumedDelta = if (isVertical) consumed.y else consumed.x
        if (delta == 0f && consumedDelta == 0f) return Offset.Zero
        if (animationJob?.isActive == true) syncRawAccumulationFromOffset()
        animationJob?.cancel()
        unwindStaleOffset(consumedDelta)

        applyDrag(delta)
        return available
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        if (!isAttached) return Velocity.Zero

        animationJob?.cancel()

        val velocity = if (isVertical) available.y else available.x

        if (abs(offset) > offsetThreshold) {
            startSpringAnimation(velocity)
            // Attenuate the velocity when flinging back toward the content.
            return if (isVertical) {
                Velocity(0f, velocity / 2.13333f)
            } else {
                Velocity(velocity / 2.13333f, 0f)
            }
        }

        return Velocity.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        if (!isAttached) return Velocity.Zero

        animationJob?.cancel()

        // Dampen the velocity transferred to the overscroll spring.
        val velocity = (if (isVertical) available.y else available.x) / 1.53333f
        startSpringAnimation(velocity)

        return if (isVertical) Velocity(0f, velocity) else Velocity(velocity, 0f)
    }
}
