package rui.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The wheel's correctness lives in its decisions: what an external value
 * moves (and what is only the drum's own echo), what an interrupted move
 * leaves behind, what a settled drum commits, when a slow release needs a
 * snap, and when a passed row ticks. The snap geometry itself is LazyColumn
 * layout behavior and needs a real fling to exercise — these cover the logic
 * around it.
 */
class RuiWheelPickerTest {

    @Test
    fun rowCountRoundsEvenUpAndClampsToOne() {
        assertEquals(1, wheelRowCount(1))
        assertEquals(5, wheelRowCount(5))
        assertEquals(5, wheelRowCount(4))
        assertEquals(7, wheelRowCount(6))
        assertEquals(1, wheelRowCount(0))
        assertEquals(1, wheelRowCount(-3))
    }

    @Test
    fun correctionClampsIntoRange() {
        val state = WheelCorrectionState()
        assertEquals(0, state.pendingTarget(-5, 5))
        assertEquals(4, state.pendingTarget(99, 5))
        assertEquals(2, state.pendingTarget(2, 5))
    }

    @Test
    fun correctionIgnoresEmptyWheels() {
        val state = WheelCorrectionState()
        assertNull(state.pendingTarget(0, 0))
        state.completed(3)
        assertNull(state.pendingTarget(3, 0))
    }

    @Test
    fun completedTargetSuppressesTheDrumsOwnEcho() {
        // The caller echoing the reported index back must not re-trigger the
        // drum, or correction and reporting feed each other forever.
        val state = WheelCorrectionState()
        state.completed(3)
        assertNull(state.pendingTarget(3, 5))
        // A genuinely new value still moves the drum.
        assertEquals(4, state.pendingTarget(4, 5))
    }

    @Test
    fun settledReportSuppressesItsOwnEcho() {
        val state = WheelCorrectionState()
        state.reported(7)
        assertNull(state.pendingTarget(7, 10))
        assertEquals(3, state.pendingTarget(3, 10))
    }

    @Test
    fun cancelledMidMoveTargetsTheExternalValueAgain() {
        // 0 -> external 20 -> external 0 before the animation finishes: the
        // cancelled move leaves no confirmed echo, so 0 is a real target and
        // the drum cannot rest halfway and report the row it stopped on.
        val state = WheelCorrectionState()
        state.completed(0)
        assertEquals(20, state.pendingTarget(20, 21))
        state.cancelled()
        assertEquals(0, state.pendingTarget(0, 21))
        assertNull(state.lastReportedIndex)
    }

    @Test
    fun onlyTheNewestCorrectionOwnsTheCorrectingFlag() {
        val state = WheelCorrectionState()
        val first = state.beginCorrection()
        assertTrue(state.correcting)
        val second = state.beginCorrection()
        state.endCorrection(first)
        assertTrue(state.correcting)
        state.endCorrection(second)
        assertFalse(state.correcting)
    }

    @Test
    fun completionAfterCancellationRestoresEchoSuppression() {
        val state = WheelCorrectionState()
        state.completed(0)
        state.cancelled()
        state.completed(20)
        assertNull(state.pendingTarget(20, 21))
    }

    @Test
    fun cancellationStopsSuppressingIntermediateValues() {
        // An intermediate settle after an interrupted correction is a real
        // rest position, not an echo to swallow.
        val state = WheelCorrectionState()
        state.reported(20)
        state.cancelled()
        assertEquals(12, state.pendingTarget(12, 21))
        assertEquals(12, wheelSettledReport(12, state.lastReportedIndex))
    }

    @Test
    fun correctionMovesToFirstAndLastRows() {
        val state = WheelCorrectionState()
        state.completed(2)
        assertEquals(0, state.pendingTarget(0, 5))
        assertEquals(4, state.pendingTarget(4, 5))
    }

    @Test
    fun settledReportCommitsOnlyNewRestPositions() {
        assertNull(wheelSettledReport(-1, null))
        assertNull(wheelSettledReport(2, 2))
        assertEquals(3, wheelSettledReport(3, 2))
        assertEquals(3, wheelSettledReport(3, null))
    }

    @Test
    fun slowReleaseBetweenRowsNeedsASnapCorrection() {
        // A slow release never flings; anything short of one pixel off-center
        // is a fling settle already resting on the row.
        assertFalse(wheelNeedsSnapCorrection(0f))
        assertFalse(wheelNeedsSnapCorrection(0.99f))
        assertTrue(wheelNeedsSnapCorrection(1f))
        assertTrue(wheelNeedsSnapCorrection(17.5f))
    }

    @Test
    fun firstObservedRowIsAdoptedWithoutATick() {
        assertEquals(WheelTickAction.Adopt, wheelTickAction(-1, 2, false))
    }

    @Test
    fun rowsPassedWhileCorrectingDoNotTick() {
        assertEquals(WheelTickAction.None, wheelTickAction(-1, 2, true))
        assertEquals(WheelTickAction.None, wheelTickAction(2, 3, true))
    }

    @Test
    fun rowsPassedByTheDrumTick() {
        assertEquals(WheelTickAction.Tick, wheelTickAction(2, 3, false))
        assertEquals(WheelTickAction.Tick, wheelTickAction(3, 2, false))
        assertEquals(WheelTickAction.None, wheelTickAction(2, 2, false))
        assertEquals(WheelTickAction.None, wheelTickAction(2, -1, false))
    }
}
