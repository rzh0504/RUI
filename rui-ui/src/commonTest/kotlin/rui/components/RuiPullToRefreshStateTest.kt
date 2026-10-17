package rui.components

import androidx.compose.runtime.MonotonicFrameClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import kotlinx.coroutines.test.runTest
import rui.foundation.utils.SpringMath
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The pull-to-refresh state machine driven directly, on a frame clock whose
 * every awaited frame advances virtual time a fixed step — the springs get a
 * deterministic dt while the test runs as fast as the frames are consumed.
 * What is covered is the sync behavior the review flagged: fast true/false
 * pulses and the level dropping while the show animation is still expanding
 * must always end idle with no animation left running.
 */
class RuiPullToRefreshStateTest {

    private class SteppingFrameClock : MonotonicFrameClock {
        var now = 0L
            private set

        override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
            now += 16_666_667L
            return onFrame(now)
        }
    }

    private val maxDragDistancePx = 800f
    private val thresholdOffsetPx = 100f

    private fun newTestState(): Triple<RuiPullToRefreshState, CoroutineScope, SteppingFrameClock> {
        val clock = SteppingFrameClock()
        val scope = CoroutineScope(Job() + Dispatchers.Default + clock)
        val state = RuiPullToRefreshState(scope)
        state.maxDragDistancePx = maxDragDistancePx
        state.fullDragRangePx = SpringMath.obtainDampingDistance(1f, maxDragDistancePx)
        state.refreshThreshold = 0.25f
        state.refreshThresholdOffset = thresholdOffsetPx
        state.triggerProgressOffset = state.refreshThreshold * state.fullDragRangePx
        return Triple(state, scope, clock)
    }

    private fun withState(block: suspend (RuiPullToRefreshState) -> Unit) {
        val (state, scope, clock) = newTestState()
        try {
            runTest(context = clock) {
                block(state)
            }
        } finally {
            scope.coroutineContext.cancelChildren()
        }
    }

    @Test
    fun showExpandsToTheThresholdRest() = withState { state ->
        state.showRefreshing({ true })
        assertEquals(RuiRefreshState.Refreshing, state.refreshState)
        assertEquals(thresholdOffsetPx, state.dragOffset)
    }

    @Test
    fun finishCollapsesBackToIdleWithoutAStuckAnimation() = withState { state ->
        state.showRefreshing({ true })
        state.finishRefreshing({ false })
        assertEquals(RuiRefreshState.Idle, state.refreshState)
        assertTrue(abs(state.dragOffset) < 0.01f)
        assertNull(state.animationJob)
    }

    @Test
    fun finishIsIgnoredWhileTheLevelIsStillTrue() = withState { state ->
        state.showRefreshing({ true })
        state.finishRefreshing({ true })
        assertEquals(RuiRefreshState.Refreshing, state.refreshState)
        assertEquals(thresholdOffsetPx, state.dragOffset)
    }

    @Test
    fun levelDropWhileExpandingStillCollapses() {
        val (state, scope, clock) = newTestState()
        try {
            runTest(context = clock) {
                // The show animation is mid-expansion when the hoisted level
                // drops — the completion must take over from wherever the
                // spring is and end idle with nothing left running.
                val show = scope.launch { state.showRefreshing({ true }) }
                withTimeout(5_000) {
                    while (state.refreshState != RuiRefreshState.Refreshing) yield()
                }
                state.finishRefreshing({ false })
                show.join()
                assertEquals(RuiRefreshState.Idle, state.refreshState)
                assertTrue(abs(state.dragOffset) < 0.01f)
                assertNull(state.animationJob)
            }
        } finally {
            scope.coroutineContext.cancelChildren()
        }
    }

    @Test
    fun showWithoutALevelRaiseIsIgnored() = withState { state ->
        // A sub-frame true->false pulse: the state machine sees a show request
        // with the level already false again and must not latch refreshing.
        state.showRefreshing({ false })
        assertEquals(RuiRefreshState.Idle, state.refreshState)
        assertEquals(0f, state.dragOffset)
    }

    @Test
    fun finishOnIdleIsNoOp() = withState { state ->
        state.finishRefreshing({ false })
        assertEquals(RuiRefreshState.Idle, state.refreshState)
        assertEquals(0f, state.dragOffset)
    }

    @Test
    fun releaseAboveTheThresholdTriggersRefresh() = withState { state ->
        var refreshed = false
        state.dragOffset = state.triggerProgressOffset + 100f
        state.handlePointerRelease({ refreshed = true }, { false })
        assertTrue(refreshed)
        assertEquals(RuiRefreshState.Refreshing, state.refreshState)
        assertEquals(thresholdOffsetPx, state.dragOffset)
    }

    @Test
    fun releaseBelowTheThresholdReboundsToIdleWithoutRefresh() = withState { state ->
        var refreshed = false
        state.dragOffset = state.triggerProgressOffset / 2f
        state.handlePointerRelease({ refreshed = true }, { true })
        assertFalse(refreshed)
        assertEquals(RuiRefreshState.Idle, state.refreshState)
        assertEquals(0f, state.dragOffset)
    }
}
