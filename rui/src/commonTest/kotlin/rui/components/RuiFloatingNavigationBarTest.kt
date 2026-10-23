package rui.components

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The floating bar's indicator is arithmetic: equal slots carved out of the
 * measured capsule, walked from the capsule's start edge. The pill is
 * start-aligned in layout, so the offset is physical — positive in LTR,
 * negative in RTL — and already includes the inner ring. These pin the math,
 * including the RTL mirror and the degenerate sizes.
 */
class RuiFloatingNavigationBarTest {

    @Test
    fun slotWidthSplitsTheInnerWidthEvenly() {
        // A 360dp capsule at 1x density minus the 4dp ring, four slots.
        assertEquals(88f, floatingNavBarSlotWidthPx(360f, innerPaddingPx = 4f, itemCount = 4))
    }

    @Test
    fun slotWidthNeverGoesNegative() {
        assertEquals(0f, floatingNavBarSlotWidthPx(8f, innerPaddingPx = 4f, itemCount = 4))
        assertEquals(0f, floatingNavBarSlotWidthPx(0f, innerPaddingPx = 4f, itemCount = 4))
    }

    @Test
    fun slotWidthIsEmptyWithoutItems() {
        assertEquals(0f, floatingNavBarSlotWidthPx(360f, innerPaddingPx = 4f, itemCount = 0))
    }

    @Test
    fun indicatorWalksRightFromTheStartEdgeInLtr() {
        assertEquals(4f, floatingNavBarIndicatorOffsetPx(0f, 88f, 4f, isLtr = true))
        assertEquals(92f, floatingNavBarIndicatorOffsetPx(1f, 88f, 4f, isLtr = true))
        assertEquals(268f, floatingNavBarIndicatorOffsetPx(3f, 88f, 4f, isLtr = true))
    }

    @Test
    fun indicatorWalksLeftFromTheStartEdgeInRtl() {
        // The start edge is the right side; negative physical offsets keep the
        // pill inside the capsule instead of pushing it past the screen edge.
        assertEquals(-4f, floatingNavBarIndicatorOffsetPx(0f, 88f, 4f, isLtr = false))
        assertEquals(-92f, floatingNavBarIndicatorOffsetPx(1f, 88f, 4f, isLtr = false))
        assertEquals(-268f, floatingNavBarIndicatorOffsetPx(3f, 88f, 4f, isLtr = false))
    }

    @Test
    fun indicatorIsContinuousBetweenSlots() {
        // Mid-flight between slot 1 and slot 2, not parked on either.
        val ltr = floatingNavBarIndicatorOffsetPx(1.5f, 88f, 4f, isLtr = true)
        assertTrue(abs(ltr - 136f) < 0.01f)
        val rtl = floatingNavBarIndicatorOffsetPx(1.5f, 88f, 4f, isLtr = false)
        assertTrue(abs(rtl + 136f) < 0.01f)
    }
}
