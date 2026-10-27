package rui.components

import androidx.compose.animation.core.tween
import androidx.compose.runtime.MonotonicFrameClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import rui.components.navigation.RuiNavKey
import rui.components.navigation.RuiNavTransitionState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RuiNavDisplayTest {
    private enum class Route : RuiNavKey { A, B, C, D }

    private class SteppingFrameClock : MonotonicFrameClock {
        private var now = 0L

        override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
            delay(16)
            now += 16_000_000L
            return onFrame(now)
        }
    }

    private val motion = tween<Float>(durationMillis = 160)

    private fun withTransition(block: suspend TestScope.(RuiNavTransitionState<Route>) -> Unit) =
        runTest(context = SteppingFrameClock()) {
            block(RuiNavTransitionState(Route.A))
        }

    @Test
    fun popRetainsExitingEntriesUntilAnimationSettles() = withTransition { state ->
        val original = listOf(Route.A, Route.B, Route.C)
        state.update(original, motion)

        val pop = launch { state.update(listOf(Route.A), motion) }
        advanceTimeBy(64)
        runCurrent()
        assertEquals(original, state.renderStack.toList())
        assertTrue(state.transitionRunning)
        assertTrue(state.animatedTop.value > 0f && state.animatedTop.value < 2f)

        pop.join()
        assertEquals(listOf(Route.A), state.renderStack.toList())
        assertEquals(0f, state.animatedTop.value)
        assertFalse(state.transitionRunning)
    }

    @Test
    fun pushDuringMultiLevelPopReplacesStaleRenderedDestination() = withTransition { state ->
        state.update(listOf(Route.A, Route.B, Route.C), motion)
        val pop = launch { state.update(listOf(Route.A), motion) }
        advanceTimeBy(64)
        runCurrent()
        pop.cancelAndJoin()
        val interruptedTop = state.animatedTop.value

        val push = launch { state.update(listOf(Route.A, Route.D), motion) }
        runCurrent()
        assertEquals(listOf(Route.A, Route.D, Route.C), state.renderStack.toList())
        assertEquals(interruptedTop, state.animatedTop.value)

        push.join()
        assertEquals(listOf(Route.A, Route.D), state.renderStack.toList())
        assertEquals(1f, state.animatedTop.value)
        assertFalse(state.transitionRunning)
    }

    @Test
    fun replaceDuringPopUpdatesDestinationWithoutKeepingOldRoute() = withTransition { state ->
        state.update(listOf(Route.A, Route.B, Route.C), motion)
        val pop = launch { state.update(listOf(Route.A, Route.B), motion) }
        advanceTimeBy(64)
        runCurrent()
        pop.cancelAndJoin()

        state.update(listOf(Route.A, Route.D), motion)
        assertEquals(listOf(Route.A, Route.D), state.renderStack.toList())
        assertEquals(1f, state.animatedTop.value)
        assertFalse(state.transitionRunning)
    }

    @Test
    fun unchangedDepthAndTopStillSynchronizeCoveredEntries() = withTransition { state ->
        state.update(listOf(Route.A, Route.B, Route.C), motion)
        val target = listOf(Route.A, Route.D, Route.C)

        state.update(target, motion)
        assertEquals(target, state.renderStack.toList())
        assertEquals(2f, state.animatedTop.value)
        assertFalse(state.transitionRunning)
    }

    @Test
    fun canceledPushAtZeroDistanceStillRemovesItsEntry() = withTransition { state ->
        val push = launch { state.update(listOf(Route.A, Route.B), motion) }
        runCurrent()
        assertEquals(listOf(Route.A, Route.B), state.renderStack.toList())
        assertEquals(0f, state.animatedTop.value)
        assertTrue(state.transitionRunning)
        push.cancelAndJoin()
        assertFalse(state.transitionRunning)

        state.update(listOf(Route.A), motion)
        assertEquals(listOf(Route.A), state.renderStack.toList())
        assertEquals(0f, state.animatedTop.value)
        assertFalse(state.transitionRunning)
    }

    @Test
    fun canceledSettledFrameStillCleansUpAtSameTarget() = withTransition { state ->
        state.update(listOf(Route.A, Route.B, Route.C), motion)
        val pop = launch { state.update(listOf(Route.A), motion) }
        advanceTimeBy(176)
        runCurrent()
        assertEquals(0f, state.animatedTop.value)
        assertTrue(state.transitionRunning)
        assertEquals(3, state.renderStack.size)
        pop.cancelAndJoin()

        state.update(listOf(Route.D), motion)
        assertEquals(listOf(Route.D), state.renderStack.toList())
        assertFalse(state.transitionRunning)
    }

    @Test
    fun repeatedReversalsSettleToTheLatestStack() = withTransition { state ->
        state.update(listOf(Route.A, Route.B, Route.C), motion)
        val targets = listOf(
            listOf(Route.A),
            listOf(Route.A, Route.D),
            listOf(Route.A, Route.D, Route.B),
            listOf(Route.A),
        )
        for (target in targets) {
            val transition = launch { state.update(target, motion) }
            advanceTimeBy(48)
            runCurrent()
            assertEquals(target, state.renderStack.take(target.size))
            transition.cancelAndJoin()
            assertFalse(state.transitionRunning)
        }

        val target = listOf(Route.A, Route.C)
        state.update(target, motion)
        assertEquals(target, state.renderStack.toList())
        assertEquals(1f, state.animatedTop.value)
        assertFalse(state.transitionRunning)
    }
}
