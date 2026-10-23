package rui.components

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The single slider's geometry is pure: the thumb travels between the track
 * insets, a touch maps proportionally (mirrored in RTL), and steps snap to
 * the discrete slots. These replace the removed range-form tests.
 */
class RuiSliderMathTest {

    @Test
    fun fractionClampsToTheRange() {
        assertEquals(0f, sliderFractionOf(-1f, 0f..10f))
        assertEquals(0.5f, sliderFractionOf(5f, 0f..10f))
        assertEquals(1f, sliderFractionOf(11f, 0f..10f))
    }

    @Test
    fun thumbTravelsBetweenTheTrackInsets() {
        assertEquals(10f, sliderThumbX(0f, trackWidthPx = 110f, thumbRadiusPx = 10f, isRtl = false))
        assertEquals(55f, sliderThumbX(0.5f, trackWidthPx = 110f, thumbRadiusPx = 10f, isRtl = false))
        assertEquals(100f, sliderThumbX(1f, trackWidthPx = 110f, thumbRadiusPx = 10f, isRtl = false))
    }

    @Test
    fun thumbMirrorsInRtl() {
        assertEquals(100f, sliderThumbX(0f, trackWidthPx = 110f, thumbRadiusPx = 10f, isRtl = true))
        assertEquals(10f, sliderThumbX(1f, trackWidthPx = 110f, thumbRadiusPx = 10f, isRtl = true))
    }

    @Test
    fun touchMapsProportionallyAndClamps() {
        assertEquals(0f, sliderValueAt(0f, 110f, 10f, 0, isRtl = false, 0f..1f))
        assertEquals(0.5f, sliderValueAt(55f, 110f, 10f, 0, isRtl = false, 0f..1f))
        assertEquals(1f, sliderValueAt(110f, 110f, 10f, 0, isRtl = false, 0f..1f))
        assertEquals(150f, sliderValueAt(55f, 110f, 10f, 0, isRtl = false, 100f..200f))
    }

    @Test
    fun touchMirrorsInRtl() {
        assertEquals(1f, sliderValueAt(10f, 110f, 10f, 0, isRtl = true, 0f..1f))
        assertEquals(0f, sliderValueAt(100f, 110f, 10f, 0, isRtl = true, 0f..1f))
    }

    @Test
    fun touchSnapsToSteps() {
        assertEquals(6f, sliderValueAt(70f, 110f, 10f, 4, isRtl = false, 0f..10f))
        assertEquals(2f, sliderValueAt(28f, 110f, 10f, 4, isRtl = false, 0f..10f))
        assertEquals(10f, sliderValueAt(110f, 110f, 10f, 4, isRtl = false, 0f..10f))
    }
}
