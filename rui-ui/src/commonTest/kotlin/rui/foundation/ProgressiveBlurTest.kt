package rui.foundation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * The progressive blur's correctness lives in the band ramp: bands must
 * partition the extent without gaps, radii must ease monotonically from full
 * strength to zero, and the curve exponent must reshape the falloff as
 * documented. The drawing itself is RenderEffect work needing a real frame —
 * these cover the geometry.
 */
class ProgressiveBlurTest {

    private val extent = 300f
    private val maxRadius = 24f

    @Test
    fun bandsPartitionTheExtentContiguously() {
        val bands = progressiveBlurBands(extent, maxRadius, ProgressiveBlur(), 10)
        assertEquals(0f, bands.first().start)
        assertEquals(extent, bands.last().end)
        for (i in 0 until bands.size - 1) {
            assertEquals(bands[i].end, bands[i + 1].start, 1e-4f)
        }
    }

    @Test
    fun defaultRampHasOneBandPerSegment() {
        val bands = progressiveBlurBands(extent, maxRadius, ProgressiveBlur(), 10)
        assertEquals(10, bands.size)
    }

    @Test
    fun radiiEaseFromNearFullStrengthToZero() {
        val bands = progressiveBlurBands(extent, maxRadius, ProgressiveBlur(), 10)
        // Each band draws at its clear-side boundary radius, so the strongest
        // band sits one segment below full strength; the last reaches zero and
        // blends seamlessly into clear content.
        assertEquals(maxRadius * (1f - 1f / 10), bands.first().radius, 1e-4f)
        assertEquals(0f, bands.last().radius)
        for (i in 0 until bands.size - 1) {
            assertTrue(bands[i].radius >= bands[i + 1].radius)
        }
    }

    @Test
    fun plateauHoldsFullStrengthBeforeTheRamp() {
        val bands = progressiveBlurBands(
            extent, maxRadius,
            ProgressiveBlur(startFraction = 0.2f, endFraction = 0.8f),
            8,
        )
        // Plateau + 8 ramp bands + clear zone.
        assertEquals(10, bands.size)
        assertEquals(maxRadius, bands.first().radius)
        assertEquals(extent * 0.2f, bands.first().end, 1e-4f)
        assertEquals(0f, bands.last().radius)
        assertEquals(extent, bands.last().end)
    }

    @Test
    fun curveReshapesTheFalloff() {
        fun midRadius(curve: Float): Float {
            val bands = progressiveBlurBands(extent, maxRadius, ProgressiveBlur(curve = curve), 4)
            return bands[1].radius // the band whose clear edge is the ramp's midpoint
        }
        val early = midRadius(0.5f)
        val linear = midRadius(1f)
        val late = midRadius(2f)
        // curve < 1 spends the radius early (long soft tail); curve > 1 holds
        // strength and drops late.
        assertTrue(early < linear)
        assertTrue(linear < late)
    }

    @Test
    fun clearZoneBeyondEndFractionDrawsUnblurred() {
        val bands = progressiveBlurBands(
            extent, maxRadius,
            ProgressiveBlur(endFraction = 0.7f),
            5,
        )
        assertEquals(0f, bands.last().radius)
        assertEquals(extent * 0.7f, bands.last().start, 1e-4f)
        assertEquals(extent, bands.last().end)
    }

    @Test
    fun rejectsBrokenProgressions() {
        fun broken(vararg build: () -> ProgressiveBlur) {
            for (make in build) {
                try {
                    make()
                    fail("expected IllegalArgumentException")
                } catch (_: IllegalArgumentException) {
                }
            }
        }
        broken(
            { ProgressiveBlur(startFraction = 0.5f, endFraction = 0.5f) },
            { ProgressiveBlur(startFraction = 0.8f, endFraction = 0.2f) },
            { ProgressiveBlur(startFraction = -0.1f) },
            { ProgressiveBlur(endFraction = 1.1f) },
            { ProgressiveBlur(curve = 0f) },
        )
    }
}
