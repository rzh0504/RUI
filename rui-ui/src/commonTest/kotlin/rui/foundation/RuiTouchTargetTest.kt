package rui.foundation

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.LayoutDirection
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The reserve-and-anchor geometry of the touch-target modifier: a visual
 * smaller than the token grows the layout slot to it, a tighter parent wins
 * over the token, and the anchor decides where the visual lands — including
 * the mirrored edge anchors in RTL.
 */
class RuiTouchTargetTest {

    private val token = 44

    @Test
    fun smallerContentExpandsToTheTokenAndCenters() {
        val placement = touchTargetPlacement(32, 32, token, Alignment.Center, LayoutDirection.Ltr)
        assertEquals(44, placement.width)
        assertEquals(44, placement.height)
        assertEquals(6, placement.contentOffsetX)
        assertEquals(6, placement.contentOffsetY)
    }

    @Test
    fun contentAtOrAboveTheTokenIsUntouched() {
        val at = touchTargetPlacement(44, 44, token, Alignment.Center, LayoutDirection.Ltr)
        assertEquals(44, at.width)
        assertEquals(0, at.contentOffsetX)
        val above = touchTargetPlacement(48, 48, token, Alignment.Center, LayoutDirection.Ltr)
        assertEquals(48, above.width)
        assertEquals(0, above.contentOffsetX)
    }

    @Test
    fun oddCenteringRoundsHalfUp() {
        // Compose's Center rounding: (44 - 41) / 2 = 1.5 lands on 2.
        val placement = touchTargetPlacement(41, 41, token, Alignment.Center, LayoutDirection.Ltr)
        assertEquals(44, placement.width)
        assertEquals(2, placement.contentOffsetX)
    }

    @Test
    fun onlyTheSmallDimensionExpands() {
        val placement = touchTargetPlacement(200, 20, token, Alignment.Center, LayoutDirection.Ltr)
        assertEquals(200, placement.width)
        assertEquals(44, placement.height)
        assertEquals(0, placement.contentOffsetX)
        assertEquals(12, placement.contentOffsetY)
    }

    @Test
    fun aTighterParentWinsOverTheToken() {
        // Exact width from weight: the slot keeps 40 instead of overflowing
        // to 44 — the touch layer still expands past the visual on its own.
        val exact = touchTargetPlacement(
            32, 32, token, Alignment.Center, LayoutDirection.Ltr,
            minWidth = 40, maxWidth = 40,
        )
        assertEquals(40, exact.width)
        assertEquals(4, exact.contentOffsetX)
        // A bounded parent clamps the reservation.
        val bounded = touchTargetPlacement(
            32, 32, token, Alignment.Center, LayoutDirection.Ltr,
            maxWidth = 36,
        )
        assertEquals(36, bounded.width)
        // The untouched dimension still reserves.
        assertEquals(44, bounded.height)
    }

    @Test
    fun edgeAnchorsPinTheContentAndMirrorInRtl() {
        val endLtr = touchTargetPlacement(20, 20, token, Alignment.CenterEnd, LayoutDirection.Ltr)
        assertEquals(44, endLtr.width)
        assertEquals(24, endLtr.contentOffsetX)
        val endRtl = touchTargetPlacement(20, 20, token, Alignment.CenterEnd, LayoutDirection.Rtl)
        assertEquals(0, endRtl.contentOffsetX)
        val startLtr = touchTargetPlacement(20, 20, token, Alignment.CenterStart, LayoutDirection.Ltr)
        assertEquals(0, startLtr.contentOffsetX)
        val startRtl = touchTargetPlacement(20, 20, token, Alignment.CenterStart, LayoutDirection.Rtl)
        assertEquals(24, startRtl.contentOffsetX)
    }
}
