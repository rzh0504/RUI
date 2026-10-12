package rui.components

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The floating bar's indicator is arithmetic: equal slots carved out of the
 * measured capsule, walked by index along the reading direction. Everything
 * else (animation, semantics) is Compose behavior — these pin the math,
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
    fun indicatorWalksForwardInLtr() {
        assertEquals(0f, floatingNavBarIndicatorX(fraction = 0f, slotWidthPx = 88f, itemCount = 4, isLtr = true))
        assertEquals(88f, floatingNavBarIndicatorX(fraction = 1f, slotWidthPx = 88f, itemCount = 4, isLtr = true))
        assertEquals(264f, floatingNavBarIndicatorX(fraction = 3f, slotWidthPx = 88f, itemCount = 4, isLtr = true))
    }

    @Test
    fun indicatorMirrorsInRtl() {
        // RTL starts at the right edge but remains inside the capsule.
        assertEquals(264f, floatingNavBarIndicatorX(fraction = 0f, slotWidthPx = 88f, itemCount = 4, isLtr = false))
        assertEquals(176f, floatingNavBarIndicatorX(fraction = 1f, slotWidthPx = 88f, itemCount = 4, isLtr = false))
        assertEquals(0f, floatingNavBarIndicatorX(fraction = 3f, slotWidthPx = 88f, itemCount = 4, isLtr = false))
    }

    @Test
    fun indicatorIsContinuousBetweenSlots() {
        // Mid-flight between slot 1 and slot 2, not parked on either.
        val x = floatingNavBarIndicatorX(fraction = 1.5f, slotWidthPx = 88f, itemCount = 4, isLtr = true)
        assertTrue(abs(x - 132f) < 0.01f)
    }
}
