package rui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import rui.foundation.rememberRuiHaptics
import rui.foundation.utils.SpringEngine
import rui.foundation.utils.SpringMath
import rui.theme.RuiTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

/**
 * A container that turns overscroll at its content's top edge into a
 * pull-to-refresh gesture. The state machine, the nested-scroll coordination,
 * the damped drag physics and the pointer-session tracking are adapted from
 * miuix's PullToRefresh (Apache License 2.0, see NOTICE); the indicator is
 * [RuiCircularProgress] — pulling grows a determinate arc, refreshing spins
 * indeterminate — instead of a custom canvas.
 *
 * The hoisted [isRefreshing] is the source of truth in both directions:
 * raising it while idle shows the indicator programmatically; lowering it
 * runs the completion collapse. [onRefresh] must raise it and lower it when
 * the work is done. The content should be a scrollable that reaches the top
 * edge without its own bounce overscroll.
 */
@Composable
public fun RuiPullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    state: RuiPullToRefreshState = rememberRuiPullToRefreshState(),
    content: @Composable () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val currentOnRefresh by rememberUpdatedState(onRefresh)
    // Reified callback reference that stays current across recompositions —
    // capturing the raw Boolean would freeze it.
    val currentIsRefreshing by rememberUpdatedState(isRefreshing)
    val isRefreshingNow: () -> Boolean = remember { { currentIsRefreshing } }

    // Sync only the hoisted level. Internal state changes are deliberately not
    // effect keys: finishRefreshing() enters RefreshComplete before its
    // collapse animation, and restarting this effect there would cancel that
    // animation and leave the header at a non-zero height.
    LaunchedEffect(isRefreshing, state) {
        if (!isRefreshing && state.refreshState == RuiRefreshState.Refreshing) {
            state.finishRefreshing(isRefreshingNow)
        } else if (isRefreshing && state.refreshState == RuiRefreshState.Idle) {
            state.showRefreshing(isRefreshingNow)
        }
    }

    val nestedScrollConnection = remember(state) {
        state.cachedNestedScrollConnection = null
        state.createNestedScrollConnection()
    }

    // Press/pan gesture session: the pull engages and resolves only inside
    // one. Scroll events never alter it; mouse presses never open it.
    val pointerModifier = remember(state) {
        state.isGestureActive = false
        Modifier.pointerInput(state) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    val active = when (event.type) {
                        PointerEventType.PanStart, PointerEventType.PanMove -> true
                        PointerEventType.PanEnd -> false
                        PointerEventType.Scroll -> state.isGestureActive
                        else -> event.changes.any { it.pressed && it.type != PointerType.Mouse }
                    }
                    if (state.isGestureActive != active) {
                        state.isGestureActive = active
                        if (
                            !active &&
                            (state.refreshState == RuiRefreshState.Pulling ||
                                state.refreshState == RuiRefreshState.ThresholdReached)
                        ) {
                            coroutineScope.launch {
                                state.handlePointerRelease(currentOnRefresh, isRefreshingNow)
                            }
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .nestedScroll(nestedScrollConnection)
            .then(pointerModifier),
    ) {
        Column {
            RefreshHeader(state = state, modifier = Modifier)
            content()
        }
    }
}

/** Represents the visual states of the pull-to-refresh indicator. */
public enum class RuiRefreshState { Idle, Pulling, ThresholdReached, Refreshing, RefreshComplete }

/** Default values for the pull-to-refresh container. */
public object RuiPullToRefreshDefaults {

    /** Distance the indicator rests at while refreshing. */
    public val RefreshThresholdOffset: Dp = 80.dp

    /** Fraction of the full damped drag range that triggers a refresh. */
    public const val RefreshThreshold: Float = 0.25f
}

/**
 * Creates and remembers a [RuiPullToRefreshState].
 *
 * @param refreshThreshold Pull progress (0..1) across the full damped drag
 *   range required to trigger a refresh.
 */
@Composable
public fun rememberRuiPullToRefreshState(
    refreshThreshold: Float = RuiPullToRefreshDefaults.RefreshThreshold,
): RuiPullToRefreshState {
    val coroutineScope = rememberCoroutineScope()
    val state = remember { RuiPullToRefreshState(coroutineScope) }

    val windowInfo = LocalWindowInfo.current
    state.maxDragDistancePx = windowInfo.containerSize.height.toFloat()
    state.fullDragRangePx = SpringMath.obtainDampingDistance(1f, state.maxDragDistancePx)
    state.refreshThreshold = refreshThreshold.coerceIn(0f, 1f)
    state.triggerProgressOffset = state.refreshThreshold * state.fullDragRangePx
    state.refreshThresholdOffset = with(LocalDensity.current) {
        RuiPullToRefreshDefaults.RefreshThresholdOffset.toPx()
    }

    SideEffect {
        // Re-glue a settled indicator when the threshold moves mid-refresh
        // (window resize, or a show that ran before first measure).
        val target = state.refreshThresholdOffset
        if (state.refreshState == RuiRefreshState.Refreshing &&
            state.animationJob == null &&
            state.dragOffset != target
        ) {
            state.dragOffset = target
            state.currentTouch = SpringMath.obtainTouchDistance(target, state.maxDragDistancePx)
        }
    }

    return state
}

/**
 * UI state of the pull-to-refresh container: manages the indicator's offset
 * and state machine. The logical `isRefreshing` is hoisted by the caller.
 */
@Stable
public class RuiPullToRefreshState internal constructor(
    internal var coroutineScope: CoroutineScope,
) {
    internal var maxDragDistancePx: Float = 0f
    internal var fullDragRangePx: Float = 0f
    internal var refreshThresholdOffset: Float = 0f
    internal var triggerProgressOffset: Float = 0f

    internal var refreshThreshold: Float = 0f

    /** The drag offset in pixels — also the header height driving the layout. */
    var dragOffset by mutableFloatStateOf(0f)
        internal set

    internal var cachedNestedScrollConnection: NestedScrollConnection? = null

    private var internalRefreshState by mutableStateOf(RuiRefreshState.Idle)

    /** The current visual state of the indicator. */
    val refreshState: RuiRefreshState get() = internalRefreshState

    /** Pull progress 0..1 relative to the effective threshold. */
    val pullProgress: Float by derivedStateOf {
        if (effectiveThresholdOffset > 0f) {
            (dragOffset / effectiveThresholdOffset).coerceIn(0f, 1f)
        } else {
            0f
        }
    }

    /** Visual scaling progress of the indicator, 0..1. */
    val visualProgress: Float by derivedStateOf {
        val scaleEndOffset = min(refreshThresholdOffset, triggerProgressOffset)
        when {
            scaleEndOffset > 0f -> (dragOffset / scaleEndOffset).coerceIn(0f, 1f)
            dragOffset > 0f -> 1f
            else -> 0f
        }
    }

    internal var currentTouch by mutableFloatStateOf(0f)

    internal var isRefreshing by mutableStateOf(false)
    internal var isTouching by mutableStateOf(false)
    internal var isGestureActive = false
    internal var isRebounding by mutableStateOf(false)
    private var isProcessingRelease = false
    private val refreshCompleteAnimProgressState = mutableFloatStateOf(1f)
    internal val refreshCompleteAnimProgress: Float get() = refreshCompleteAnimProgressState.floatValue

    private val springEngine = SpringEngine()
    internal var animationJob: Job? = null

    private val effectiveThresholdOffset: Float
        get() = max(refreshThresholdOffset, triggerProgressOffset)

    /**
     * Drives [dragOffset] with the damped spring physics. Runs in the caller's
     * coroutine context; cancelling the caller stops the animation. Single
     * writer: every offset change outside the drag gesture goes through here.
     */
    internal suspend fun animateToSpring(targetValue: Float) {
        animationJob?.cancel()

        springEngine.start(
            startValue = dragOffset,
            targetValue = targetValue,
            initialVel = 0f,
        )

        val currentJob = currentCoroutineContext()[Job]
        animationJob = currentJob

        var lastFrameTimeNanos = -1L
        var isFinished = false

        try {
            while (currentCoroutineContext().isActive) {
                isFinished = withFrameNanos { frameTimeNanos ->
                    if (lastFrameTimeNanos == -1L) {
                        lastFrameTimeNanos = frameTimeNanos
                        return@withFrameNanos false
                    }
                    val dt = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f
                    lastFrameTimeNanos = frameTimeNanos

                    val finished = springEngine.step(dt)
                    val newOffset = springEngine.currentPos.toFloat()

                    dragOffset = newOffset
                    currentTouch = SpringMath.obtainTouchDistance(newOffset, maxDragDistancePx)

                    finished
                }
                if (isFinished) break
            }
        } finally {
            if (animationJob == currentJob) animationJob = null
            if (isFinished) {
                dragOffset = targetValue
                currentTouch = SpringMath.obtainTouchDistance(targetValue, maxDragDistancePx)
            }
        }
    }

    internal suspend fun startRefreshing(onRefresh: () -> Unit, isRefreshingNow: () -> Boolean) {
        if (!isRefreshing) {
            isRefreshing = true
            try {
                animateToSpring(refreshThresholdOffset)
            } finally {
                if (!isTouching) {
                    // Refreshing must be written before onRefresh(): the sync
                    // effect keys on this change to re-sample the level.
                    internalRefreshState = RuiRefreshState.Refreshing
                    if (!isRefreshingNow()) onRefresh()
                } else {
                    isRefreshing = false
                }
            }
        }
    }

    /** Programmatically expands the indicator when the level turns true while idle. */
    internal suspend fun showRefreshing(isRefreshingNow: () -> Boolean) {
        if (isRefreshing || internalRefreshState != RuiRefreshState.Idle || !isRefreshingNow()) return
        isRefreshing = true
        internalRefreshState = RuiRefreshState.Refreshing
        animateToSpring(refreshThresholdOffset)
        while (internalRefreshState == RuiRefreshState.Refreshing && dragOffset != refreshThresholdOffset) {
            animateToSpring(refreshThresholdOffset)
        }
    }

    /** Completion collapse when the hoisted level turns false. */
    internal suspend fun finishRefreshing(isRefreshingNow: () -> Boolean) {
        if (isRefreshing && !isRefreshingNow()) {
            animationJob?.cancel()
            isRefreshing = false
            internalRefreshState = RuiRefreshState.RefreshComplete
            startManualRefreshCompleteAnimation()
        }
    }

    /** Handles pointer release: trigger a refresh or rebound to rest. */
    internal suspend fun handlePointerRelease(onRefresh: () -> Unit, isRefreshingNow: () -> Boolean) {
        if (isProcessingRelease || isRefreshing || isRebounding) return
        isTouching = false
        isProcessingRelease = true
        try {
            if (dragOffset > 0f && dragOffset >= triggerProgressOffset) {
                startRefreshing(onRefresh, isRefreshingNow)
            } else {
                try {
                    isRebounding = true
                    animateToSpring(0f)
                } finally {
                    isRebounding = false
                    if (!isTouching) resetToIdle()
                }
            }
        } finally {
            isProcessingRelease = false
        }
    }

    private suspend fun startManualRefreshCompleteAnimation() {
        // Height-continuous handoff when completing mid-expansion.
        val initialProgress = if (refreshThresholdOffset > 0f) {
            1f - (dragOffset / refreshThresholdOffset).coerceIn(0f, 1f)
        } else {
            1f
        }
        refreshCompleteAnimProgressState.floatValue = initialProgress
        val animatedValue = androidx.compose.animation.core.Animatable(initialProgress)
        animatedValue.animateTo(
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 200),
        ) {
            refreshCompleteAnimProgressState.floatValue = this.value
            dragOffset = refreshThresholdOffset * (1f - this.value)
            currentTouch = SpringMath.obtainTouchDistance(dragOffset, maxDragDistancePx)
        }
        internalRefreshState = RuiRefreshState.Idle
    }

    private fun resetToIdle() {
        if (internalRefreshState != RuiRefreshState.Refreshing &&
            internalRefreshState != RuiRefreshState.RefreshComplete
        ) {
            internalRefreshState = RuiRefreshState.Idle
        }
    }

    /** The nested-scroll coordination for the pull itself. */
    internal fun createNestedScrollConnection(): NestedScrollConnection = object : NestedScrollConnection {
        private fun applyDrag(delta: Float) {
            if (delta == 0f) return
            currentTouch += delta
            currentTouch = currentTouch.coerceIn(-maxDragDistancePx, maxDragDistancePx)

            val normalized = min(abs(currentTouch) / maxDragDistancePx, 1.0f)
            val dampedDist = SpringMath.obtainDampingDistance(normalized, maxDragDistancePx)
            dragOffset = sign(currentTouch) * dampedDist

            val nextState = when {
                dragOffset > 0f && dragOffset >= triggerProgressOffset -> RuiRefreshState.ThresholdReached
                dragOffset > 0f -> RuiRefreshState.Pulling
                else -> RuiRefreshState.Idle
            }
            if (refreshState != nextState) {
                internalRefreshState = nextState
            }
        }

        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            // While the refresh is in progress, consume all scroll events.
            if (refreshState == RuiRefreshState.Refreshing || refreshState == RuiRefreshState.RefreshComplete) {
                return available
            }

            // Pulling up while the indicator is visible retracts it first.
            if (source == NestedScrollSource.UserInput && isGestureActive &&
                available.y < 0f && (dragOffset > 0f || currentTouch > 0f)
            ) {
                isTouching = true
                animationJob?.cancel()
                applyDrag(available.y)
                return Offset(0f, available.y)
            }
            return Offset.Zero
        }

        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            // While the refresh is in progress, consume all scroll events.
            if (refreshState == RuiRefreshState.Refreshing || refreshState == RuiRefreshState.RefreshComplete) {
                return available
            }

            // Pulling down after the content reached its top shows the indicator.
            if (source == NestedScrollSource.UserInput && isGestureActive && available.y > 0f) {
                isTouching = true
                animationJob?.cancel()
                applyDrag(available.y)
                return Offset(0f, available.y)
            }
            return Offset.Zero
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            // While the indicator is active the content does not fling.
            return if (refreshState != RuiRefreshState.Idle) available else Velocity.Zero
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            if (refreshState != RuiRefreshState.Idle) {
                // Rebound when the pull released below the threshold.
                if (
                    refreshState != RuiRefreshState.Refreshing &&
                    refreshState != RuiRefreshState.RefreshComplete &&
                    !isRefreshing &&
                    !isRebounding &&
                    dragOffset > 0f &&
                    dragOffset < triggerProgressOffset
                ) {
                    try {
                        isRebounding = true
                        animateToSpring(0f)
                    } finally {
                        isRebounding = false
                        if (!isTouching) resetToIdle()
                    }
                }
                return available
            }
            return Velocity.Zero
        }
    }
}

/**
 * The indicator header above the content: its height tracks [RuiPullToRefreshState.dragOffset]
 * so the content is pushed down with the pull, and the spinner rides the
 * bottom edge of the header.
 */
@Composable
private fun RefreshHeader(
    state: RuiPullToRefreshState,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberRuiHaptics()

    LaunchedEffect(state.refreshState) {
        if (state.refreshState == RuiRefreshState.ThresholdReached) {
            haptics.impactMedium()
        }
    }

    val density = LocalDensity.current
    val headerHeight by remember(state) {
        derivedStateOf {
            with(density) {
                val rest = RuiPullToRefreshDefaults.RefreshThresholdOffset.toPx()
                val stretched = state.dragOffset - rest
                if (stretched <= 0f) {
                    // Ramp in with the pull.
                    (rest * state.visualProgress).toDp()
                } else {
                    (rest + stretched).toDp()
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(headerHeight),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // Below the threshold the arc grows with the pull; at the threshold
        // (and while refreshing) the ring is full, so the switch to the
        // indeterminate spinner is invisible — loading reads as starting
        // immediately without touching the state machine's timing.
        val progress: Float? = when (state.refreshState) {
            RuiRefreshState.Idle -> null
            RuiRefreshState.Pulling -> state.visualProgress
            RuiRefreshState.ThresholdReached, RuiRefreshState.Refreshing -> null
            RuiRefreshState.RefreshComplete -> 1f
        }
        val alpha: Float = when (state.refreshState) {
            RuiRefreshState.Idle -> 0f
            RuiRefreshState.RefreshComplete -> (1f - state.refreshCompleteAnimProgress).coerceAtLeast(0f)
            else -> (state.visualProgress - 0.2f).coerceIn(0f, 1f)
        }
        if (alpha > 0f) {
            RuiCircularProgress(
                progress = progress,
                modifier = Modifier
                    .padding(vertical = RuiTheme.spacing.sm)
                    .graphicsLayer { this.alpha = alpha },
            )
        }
    }
}
