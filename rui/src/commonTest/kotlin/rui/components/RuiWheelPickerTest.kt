package rui.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The wheel's correctness lives in its decisions: what an external value
 * moves (and what is only the drum's own echo), what a settled drum commits,
 * when a slow release needs a snap, and when a passed row ticks. The snap
 * geometry itself is LazyColumn layout behavior and needs a real fling to
 * exercise — these cover the logic around it.
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
        assertEquals(0, wheelCorrectionTarget(-5, 5, null))
        assertEquals(4, wheelCorrectionTarget(99, 5, null))
        assertEquals(2, wheelCorrectionTarget(2, 5, null))
    }

    @Test
    fun correctionIgnoresEmptyWheels() {
        assertNull(wheelCorrectionTarget(0, 0, null))
        assertNull(wheelCorrectionTarget(3, 0, 3))
    }

    @Test
    fun correctionSuppressesTheDrumsOwnEcho() {
        // The caller echoing the reported index back must not re-trigger the
        // drum, or correction and reporting feed each other forever.
        assertNull(wheelCorrectionTarget(3, 5, 3))
        // A genuinely new value still moves the drum.
        assertEquals(4, wheelCorrectionTarget(4, 5, 3))
    }

    @Test
    fun correctionMovesToFirstAndLastRows() {
        assertEquals(0, wheelCorrectionTarget(0, 5, 2))
        assertEquals(4, wheelCorrectionTarget(4, 5, 2))
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
