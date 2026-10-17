package rui.components

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The scrollbar's correctness lives in its math: the thumb's size and position
 * on the track, the drag's position-to-scroll mapping with its carried-over
 * overshoot, and the lazy adapter's estimates. Drawing and gesture plumbing
 * need a real frame loop — these cover the logic around them.
 */
class RuiScrollBarMathTest {

    private val geometry = ScrollBarGeometry(
        trackSize = 1000.0,
        contentSize = 5000.0,
        viewportSize = 1000.0,
        minThumbSize = 36.0,
    )

    @Test
    fun thumbLengthIsProportionalAndViewportShare() {
        assertEquals(200.0, geometry.thumbSize)
    }

    @Test
    fun thumbLengthClampsToMinimum() {
        val shallow = ScrollBarGeometry(
            trackSize = 1000.0,
            contentSize = 100_000.0,
            viewportSize = 1000.0,
            minThumbSize = 36.0,
        )
        assertEquals(36.0, shallow.thumbSize)
    }

    @Test
    fun positionRunsTrackEndToEnd() {
        assertEquals(0.0, geometry.positionOf(0.0))
        assertEquals(800.0, geometry.positionOf(geometry.maxScrollOffset))
        assertEquals(400.0, geometry.positionOf(geometry.maxScrollOffset / 2))
    }

    @Test
    fun positionMapsBackToTheSameScrollOffset() {
        val offset = geometry.maxScrollOffset * 0.37
        assertEquals(offset, geometry.scrollOffsetOf(geometry.positionOf(offset)), 1e-9)
    }

    @Test
    fun reverseLayoutFlipsTheTrack() {
        val reversed = ScrollBarGeometry(
            trackSize = 1000.0,
            contentSize = 5000.0,
            viewportSize = 1000.0,
            minThumbSize = 36.0,
            reverseLayout = true,
        )
        assertEquals(800.0, reversed.positionOf(0.0))
        assertEquals(0.0, reversed.positionOf(reversed.maxScrollOffset))
    }

    @Test
    fun contentFittingTheViewportHasNoRange() {
        val fits = ScrollBarGeometry(
            trackSize = 1000.0,
            contentSize = 1000.0,
            viewportSize = 1000.0,
            minThumbSize = 36.0,
        )
        assertEquals(1000.0, fits.thumbSize)
        assertEquals(0.0, fits.maxScrollOffset)
        assertEquals(0.0, fits.positionOf(123.0))
    }

    @Test
    fun thumbPixelRangeCoversTheThumb() {
        val range = geometry.thumbPixelRange(geometry.maxScrollOffset)
        assertEquals(800, range.first)
        assertEquals(999, range.last)
    }

    @Test
    fun dragMovesTheThumbOneToOneInsideTheTrack() {
        val step = dragThumbPosition(
            currentPosition = 300.0,
            dragDelta = 40.0,
            unscrolled = 0.0,
            maxPosition = 800.0,
        )
        assertEquals(340.0, step.position)
        assertEquals(0.0, step.unscrolled)
    }

    @Test
    fun dragOvershootCarriesUnabsorbedDistance() {
        val clamped = dragThumbPosition(
            currentPosition = 790.0,
            dragDelta = 50.0,
            unscrolled = 0.0,
            maxPosition = 800.0,
        )
        assertEquals(800.0, clamped.position)
        assertEquals(40.0, clamped.unscrolled)

        val resumed = dragThumbPosition(
            currentPosition = clamped.position,
            dragDelta = -25.0,
            unscrolled = clamped.unscrolled,
            maxPosition = 800.0,
        )
        // Dragging back 25px first pays down the carried overshoot (40 → 15);
        // the thumb stays pinned until the debt is spent, so it never jumps.
        assertEquals(800.0, resumed.position)
        assertEquals(15.0, resumed.unscrolled)
    }

    @Test
    fun dragClampsAtTheStartToo() {
        val step = dragThumbPosition(
            currentPosition = 10.0,
            dragDelta = -40.0,
            unscrolled = 0.0,
            maxPosition = 800.0,
        )
        assertEquals(0.0, step.position)
        assertEquals(-30.0, step.unscrolled)
    }

    @Test
    fun lazyEstimatesRideOnTheAverageRowPitch() {
        // 5 uniform rows of size 96 with spacing 4 (pitch 100), first fully at
        // the top of the viewport, 100 rows total.
        val rows = (5..9).map { LazyScrollBarRow(index = it, offset = (it - 5) * 100, size = 96) }
        val lazy = LazyScrollBarGeometry(
            rows = rows,
            spacing = 4,
            totalRows = 100,
            contentPadding = 0,
            viewportSize = 500,
        )
        assertEquals(500.0, lazy.scrollOffset)
        assertEquals(96.0 * 100 + 4 * 99, lazy.contentSize)
    }

    @Test
    fun lazyEstimatesSkipAStretchedFirstRow() {
        // A header riding the top edge (offset -50, size 300) overlaps the
        // next row's span — averaging must start at the second row.
        val rows = listOf(
            LazyScrollBarRow(index = 0, offset = -50, size = 300),
            LazyScrollBarRow(index = 1, offset = 250, size = 96),
            LazyScrollBarRow(index = 2, offset = 350, size = 96),
        )
        val lazy = LazyScrollBarGeometry(
            rows = rows,
            spacing = 4,
            totalRows = 30,
            contentPadding = 0,
            viewportSize = 500,
        )
        // Second row's pitch: offset 250 at index 1 → scrollOffset = 1 * (avg + spacing) - 250.
        val expectedAvg = (350.0 + 96 - 250.0 - 4) / 2
        assertTrue(abs(expectedAvg - 96.0) < 1e-9)
        assertEquals(expectedAvg + 4 - 250.0, lazy.scrollOffset, 1e-9)
    }

    @Test
    fun lazyShortMovesStayRelative() {
        val lazy = uniformLazy()
        val plan = lazy.planTo(lazy.scrollOffset + 120.0)
        assertEquals(LazyScrollBarPlan.ByDelta(120f), plan)
    }

    @Test
    fun lazyLongMovesSnapToARow() {
        val lazy = uniformLazy()
        val plan = lazy.planTo(5000.0)
        assertEquals(LazyScrollBarPlan.ToRow(index = 50, offset = 0), plan)

        val insideRow = lazy.planTo(5050.0)
        assertEquals(LazyScrollBarPlan.ToRow(index = 50, offset = 50), insideRow)
    }

    @Test
    fun lazySnapTargetClampsIntoContent() {
        val lazy = uniformLazy()
        val plan = lazy.planTo(-3000.0)
        assertEquals(LazyScrollBarPlan.ToRow(index = 0, offset = 0), plan)
    }

    private fun uniformLazy(): LazyScrollBarGeometry {
        val rows = (5..9).map { LazyScrollBarRow(index = it, offset = (it - 5) * 100, size = 96) }
        return LazyScrollBarGeometry(
            rows = rows,
            spacing = 4,
            totalRows = 100,
            contentPadding = 0,
            viewportSize = 500,
        )
    }
}
