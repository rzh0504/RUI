package rui.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The range slider's correctness lives in its thumb decisions: which thumb a
 * press takes, how a move clamps so thumbs never cross, and when a drag
 * pinned on a collapsed pair changes hands. Drawing and gesture plumbing
 * need a real frame — these cover the logic around them.
 */
class RuiRangeSliderMathTest {

    @Test
    fun pressTakesTheNearestThumb() {
        assertEquals(RangeThumb.Start, selectRangeThumb(startPx = 100f, endPx = 300f, downPx = 90f))
        assertEquals(RangeThumb.End, selectRangeThumb(startPx = 100f, endPx = 300f, downPx = 310f))
        // A tie falls to the start thumb, matching miuix's <= comparison.
        assertEquals(RangeThumb.Start, selectRangeThumb(startPx = 100f, endPx = 300f, downPx = 200f))
    }

    @Test
    fun moveClampsAgainstThePartnerThumb() {
        // The start thumb can never pass the end thumb.
        val pinnedStart = rangeDragStep(RangeThumb.Start, moveValue = 0.8f, start = 0.2f, end = 0.5f)
        assertEquals(0.5f, pinnedStart.start)
        assertEquals(0.5f, pinnedStart.end)
        assertFalse(pinnedStart.handoff)

        val pinnedEnd = rangeDragStep(RangeThumb.End, moveValue = 0.1f, start = 0.4f, end = 0.6f)
        assertEquals(0.4f, pinnedEnd.start)
        assertEquals(0.4f, pinnedEnd.end)
        assertFalse(pinnedEnd.handoff)
    }

    @Test
    fun moveInsideTheRangeAppliesDirectly() {
        val step = rangeDragStep(RangeThumb.Start, moveValue = 0.3f, start = 0.2f, end = 0.7f)
        assertEquals(0.3f, step.start)
        assertEquals(0.7f, step.end)
        assertFalse(step.handoff)
    }

    @Test
    fun pushingPastACollapsedPairHandsTheDragOver() {
        // Both thumbs pinned at 0.5, finger keeps moving right: the end thumb
        // takes over and follows the finger.
        val spread = rangeDragStep(RangeThumb.Start, moveValue = 0.6f, start = 0.5f, end = 0.5f)
        assertEquals(0.5f, spread.start)
        assertEquals(0.6f, spread.end)
        assertTrue(spread.handoff)

        // And symmetrically leftward.
        val spreadLeft = rangeDragStep(RangeThumb.End, moveValue = 0.4f, start = 0.5f, end = 0.5f)
        assertEquals(0.4f, spreadLeft.start)
        assertEquals(0.5f, spreadLeft.end)
        assertTrue(spreadLeft.handoff)
    }

    @Test
    fun aSpreadPairPinsInsteadOfHandingOver() {
        // When the thumbs are apart, pushing the start thumb into the end one
        // just pins it — the drag must not change hands.
        val pinned = rangeDragStep(RangeThumb.Start, moveValue = 0.6f, start = 0.2f, end = 0.5f)
        assertEquals(0.5f, pinned.start)
        assertEquals(0.5f, pinned.end)
        assertFalse(pinned.handoff)
    }

    @Test
    fun afterAHandoffTheNewThumbStillPins() {
        // Following the handoff test: active is now End at 0.6; dragging back
        // below start must pin at start, not cross.
        val back = rangeDragStep(RangeThumb.End, moveValue = 0.3f, start = 0.5f, end = 0.6f)
        assertEquals(0.5f, back.start)
        assertEquals(0.5f, back.end)
        assertFalse(back.handoff)
    }
}
