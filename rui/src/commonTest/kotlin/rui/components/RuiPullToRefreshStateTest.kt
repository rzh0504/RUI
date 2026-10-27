package rui.components

import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import rui.foundation.utils.SpringMath
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RuiPullToRefreshStateTest {

    private class SteppingFrameClock : MonotonicFrameClock {
        private var now = 0L
        private val frames = Channel<Long>(Channel.RENDEZVOUS)

        override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R =
            onFrame(frames.receive())

        fun advanceFrame() {
            now += 16_666_667L
            check(frames.trySend(now).isSuccess) { "No animation is awaiting a frame" }
        }
    }

    private val maxDragDistancePx = 800f
    private val thresholdOffsetPx = 100f

    private fun withState(block: suspend TestScope.(RuiPullToRefreshState, SteppingFrameClock) -> Unit) = runTest {
        val clock = SteppingFrameClock()
        val state = RuiPullToRefreshState(CoroutineScope(coroutineContext + clock))
        state.maxDragDistancePx = maxDragDistancePx
        state.fullDragRangePx = SpringMath.obtainDampingDistance(1f, maxDragDistancePx)
        state.refreshThreshold = 0.25f
        state.refreshThresholdOffset = thresholdOffsetPx
        state.triggerProgressOffset = state.refreshThreshold * state.fullDragRangePx
        block(state, clock)
    }

    private fun TestScope.advanceFrames(clock: SteppingFrameClock, count: Int = 1) {
        runCurrent()
        repeat(count) {
            clock.advanceFrame()
            runCurrent()
        }
    }

    private suspend fun TestScope.finishAnimation(clock: SteppingFrameClock, job: Job) {
        runCurrent()
        repeat(600) {
            if (job.isCompleted) {
                job.join()
                assertFalse(job.isCancelled)
                return
            }
            advanceFrames(clock)
        }
        error("Animation did not finish within 600 frames")
    }

    private suspend fun TestScope.runAnimation(clock: SteppingFrameClock, block: suspend () -> Unit) {
        finishAnimation(clock, launch(clock) { block() })
    }

    private fun assertIdle(state: RuiPullToRefreshState) {
        assertEquals(RuiRefreshState.Idle, state.refreshState)
        assertEquals(0f, state.dragOffset)
        assertEquals(0f, state.currentTouch)
        assertFalse(state.isRefreshing)
        assertNull(state.animationJob)
    }

    @Test
    fun showExpandsToTheThresholdRest() = withState { state, clock ->
        runAnimation(clock) { state.showRefreshing({ true }) }
        assertEquals(RuiRefreshState.Refreshing, state.refreshState)
        assertEquals(thresholdOffsetPx, state.dragOffset)
        assertNull(state.animationJob)
    }

    @Test
    fun finishCollapsesBackToIdleWithoutAStuckAnimation() = withState { state, clock ->
        runAnimation(clock) { state.showRefreshing({ true }) }
        runAnimation(clock) { state.finishRefreshing({ false }) }
        assertIdle(state)
    }

    @Test
    fun finishIsIgnoredWhileTheLevelIsStillTrue() = withState { state, clock ->
        runAnimation(clock) { state.showRefreshing({ true }) }
        runAnimation(clock) { state.finishRefreshing({ true }) }
        assertEquals(RuiRefreshState.Refreshing, state.refreshState)
        assertEquals(thresholdOffsetPx, state.dragOffset)
    }

    @Test
    fun levelDropWhileExpandingStillCollapses() = withState { state, clock ->
        var level = true
        val show = launch(clock) { state.syncRefreshing(level) { level } }
        advanceFrames(clock, 4)
        assertEquals(RuiRefreshState.Refreshing, state.refreshState)
        assertTrue(state.dragOffset > 0f && state.dragOffset < thresholdOffsetPx)
        level = false
        runAnimation(clock) { state.syncRefreshing(level) { level } }
        show.join()
        assertTrue(show.isCancelled)
        assertIdle(state)
    }

    @Test
    fun showWithoutALevelRaiseIsIgnored() = withState { state, clock ->
        // A sub-frame true->false pulse: the state machine sees a show request
        // with the level already false again and must not latch refreshing.
        runAnimation(clock) { state.showRefreshing({ false }) }
        assertIdle(state)
        runAnimation(clock) { state.syncRefreshing(true) { false } }
        assertIdle(state)
    }

    @Test
    fun finishOnIdleIsNoOp() = withState { state, clock ->
        runAnimation(clock) { state.finishRefreshing({ false }) }
        assertIdle(state)
    }

    @Test
    fun levelRaiseAfterCancelledCompletionRefreshesAgainAndReleasesScroll() = withState { state, clock ->
        var level = true
        runAnimation(clock) { state.syncRefreshing(level) { level } }
        level = false
        var completionReturned = false
        val completion = launch(clock) {
            state.syncRefreshing(level) { level }
            completionReturned = true
        }
        advanceFrames(clock, 4)
        assertEquals(RuiRefreshState.RefreshComplete, state.refreshState)
        assertTrue(state.dragOffset > 0f && state.dragOffset < thresholdOffsetPx)
        val completionAnimation = assertNotNull(state.animationJob)
        assertTrue(completionAnimation.isActive)
        val connection = state.createNestedScrollConnection()
        val scroll = Offset(4f, -20f)
        assertEquals(scroll, connection.onPreScroll(scroll, NestedScrollSource.UserInput))
        assertEquals(scroll, connection.onPostScroll(Offset.Zero, scroll, NestedScrollSource.UserInput))

        completion.cancelAndJoin()
        assertTrue(completion.isCancelled)
        assertTrue(completionAnimation.isCancelled)
        assertFalse(completionReturned)
        assertEquals(RuiRefreshState.RefreshComplete, state.refreshState)
        assertNull(state.animationJob)
        level = true
        runAnimation(clock) { state.syncRefreshing(level) { level } }
        assertEquals(RuiRefreshState.Refreshing, state.refreshState)
        assertTrue(state.isRefreshing)
        assertEquals(thresholdOffsetPx, state.dragOffset)
        assertNull(state.animationJob)

        level = false
        runAnimation(clock) { state.syncRefreshing(level) { level } }
        assertIdle(state)
        assertEquals(Offset.Zero, connection.onPreScroll(scroll, NestedScrollSource.UserInput))
        assertEquals(Offset.Zero, connection.onPostScroll(Offset.Zero, scroll, NestedScrollSource.UserInput))
        val wheelScroll = Offset(0f, 20f)
        assertEquals(Offset.Zero, connection.onPostScroll(Offset.Zero, wheelScroll, NestedScrollSource.UserInput))
        assertEquals(Offset.Zero, connection.onPreScroll(scroll, NestedScrollSource.SideEffect))
        assertEquals(Velocity.Zero, connection.onPreFling(Velocity(0f, -100f)))
        assertEquals(Velocity.Zero, connection.onPostFling(Velocity.Zero, Velocity(0f, -100f)))
    }

    @Test
    fun repeatedLevelChangesDuringAnimationsStillSettle() = withState { state, clock ->
        var level = true
        runAnimation(clock) { state.syncRefreshing(level) { level } }
        repeat(5) {
            level = false
            val completion = launch(clock) { state.syncRefreshing(level) { level } }
            advanceFrames(clock, 4)
            assertEquals(RuiRefreshState.RefreshComplete, state.refreshState)
            completion.cancelAndJoin()
            assertNull(state.animationJob)

            level = true
            val show = launch(clock) { state.syncRefreshing(level) { level } }
            advanceFrames(clock, 4)
            assertEquals(RuiRefreshState.Refreshing, state.refreshState)
            assertTrue(state.isRefreshing)
            show.cancelAndJoin()
            assertNull(state.animationJob)
        }
        level = false
        runAnimation(clock) { state.syncRefreshing(level) { level } }
        assertIdle(state)
    }

    @Test
    fun falseLevelResumesAnInterruptedCompletion() = withState { state, clock ->
        runAnimation(clock) { state.syncRefreshing(true) { true } }
        val completion = launch(clock) { state.syncRefreshing(false) { false } }
        advanceFrames(clock, 4)
        completion.cancelAndJoin()
        val interruptedOffset = state.dragOffset
        assertTrue(interruptedOffset > 0f && interruptedOffset < thresholdOffsetPx)
        assertEquals(RuiRefreshState.RefreshComplete, state.refreshState)
        assertNull(state.animationJob)

        val resumed = launch(clock) { state.syncRefreshing(false) { false } }
        advanceFrames(clock)
        assertEquals(RuiRefreshState.RefreshComplete, state.refreshState)
        assertTrue(abs(state.dragOffset - interruptedOffset) < 0.01f)
        assertNotNull(state.animationJob)
        finishAnimation(clock, resumed)
        assertIdle(state)
    }

    @Test
    fun showTakesAnimationOwnershipFromCompletion() = withState { state, clock ->
        var level = true
        runAnimation(clock) { state.syncRefreshing(level) { level } }
        level = false
        val completion = launch(clock) { state.syncRefreshing(level) { level } }
        advanceFrames(clock, 4)
        val oldAnimation = assertNotNull(state.animationJob)
        level = true
        val show = launch(clock) { state.syncRefreshing(level) { level } }
        runCurrent()
        assertTrue(completion.isCancelled)
        assertTrue(oldAnimation.isCancelled)
        assertEquals(RuiRefreshState.Refreshing, state.refreshState)
        val newAnimation = assertNotNull(state.animationJob)
        assertTrue(newAnimation !== oldAnimation)
        assertTrue(newAnimation.isActive)
        finishAnimation(clock, show)
        assertEquals(RuiRefreshState.Refreshing, state.refreshState)
        assertEquals(thresholdOffsetPx, state.dragOffset)
        assertNull(state.animationJob)
        level = false
        runAnimation(clock) { state.syncRefreshing(level) { level } }
        assertIdle(state)
    }

    @Test
    fun releaseAboveTheThresholdTriggersRefresh() = withState { state, clock ->
        var refreshCount = 0
        state.dragOffset = state.triggerProgressOffset + 100f
        runAnimation(clock) { state.handlePointerRelease({ refreshCount++ }, { false }) }
        assertEquals(1, refreshCount)
        assertEquals(RuiRefreshState.Refreshing, state.refreshState)
        assertEquals(thresholdOffsetPx, state.dragOffset)
    }

    @Test
    fun releaseBelowTheThresholdReboundsToIdleWithoutRefresh() = withState { state, clock ->
        var refreshed = false
        state.dragOffset = state.triggerProgressOffset / 2f
        runAnimation(clock) { state.handlePointerRelease({ refreshed = true }, { true }) }
        assertFalse(refreshed)
        assertIdle(state)
    }

    @Test
    fun releaseDoesNotNotifyAgainWhenTheLevelIsAlreadyTrue() = withState { state, clock ->
        var refreshCount = 0
        state.dragOffset = state.triggerProgressOffset + 100f
        runAnimation(clock) { state.handlePointerRelease({ refreshCount++ }, { true }) }
        assertEquals(0, refreshCount)
        assertEquals(RuiRefreshState.Refreshing, state.refreshState)
        assertEquals(thresholdOffsetPx, state.dragOffset)
    }

    @Test
    fun cancelledReleaseWithoutANewTouchStillNotifiesOnce() = withState { state, clock ->
        var refreshCount = 0
        state.dragOffset = state.triggerProgressOffset + 100f
        val release = launch(clock) { state.handlePointerRelease({ refreshCount++ }, { false }) }
        advanceFrames(clock, 4)
        release.cancelAndJoin()
        assertTrue(release.isCancelled)
        assertEquals(1, refreshCount)
        assertEquals(RuiRefreshState.Refreshing, state.refreshState)
        assertNull(state.animationJob)
        runAnimation(clock) { state.syncRefreshing(false) { false } }
        assertIdle(state)
    }

    @Test
    fun newDragCancelsReleaseWithoutOverwritingTheDragOrNotifying() = withState { state, clock ->
        var refreshCount = 0
        state.dragOffset = state.triggerProgressOffset + 100f
        val release = launch(clock) { state.handlePointerRelease({ refreshCount++ }, { false }) }
        advanceFrames(clock, 4)
        state.isGestureActive = true
        state.createNestedScrollConnection().onPostScroll(
            Offset.Zero,
            Offset(0f, 20f),
            NestedScrollSource.UserInput,
        )
        val draggedOffset = state.dragOffset
        runCurrent()
        release.join()
        assertTrue(release.isCancelled)
        assertEquals(0, refreshCount)
        assertFalse(state.isRefreshing)
        assertEquals(draggedOffset, state.dragOffset)
        assertNull(state.animationJob)
    }
}
