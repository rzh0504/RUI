package rui.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The calendar math is the picker's whole correctness — verified against
 * known weekdays (cross-checked with an independent calendar implementation)
 * including leap days, century rules and month boundaries.
 */
class RuiCalendarPickerTest {

    @Test
    fun weekdayMatchesKnownDates() {
        assertEquals(3, weekdayFromMonday(1970, 1, 1))  // Thursday
        assertEquals(5, weekdayFromMonday(2000, 1, 1))  // Saturday
        assertEquals(1, weekdayFromMonday(2026, 10, 6)) // Tuesday
        assertEquals(3, weekdayFromMonday(2024, 2, 29)) // Thursday, leap day
        assertEquals(4, weekdayFromMonday(2100, 12, 31)) // Friday, century non-leap
    }

    @Test
    fun monthCellsAlwaysRenderSixWeeks() {
        for (month in 1..12) {
            assertEquals(42, monthCells(2026, month).size, "month $month")
        }
    }

    @Test
    fun october2026GridEdges() {
        val cells = monthCells(2026, 10) // the 1st is a Thursday, 31 days
        assertEquals(RuiDate(2026, 9, 28), cells.first().date) // Monday backfill
        assertEquals(false, cells.first().inMonth)
        assertEquals(RuiDate(2026, 10, 1), cells[3].date)
        assertTrue(cells[3].inMonth)
        assertEquals(RuiDate(2026, 10, 31), cells[33].date)
        assertEquals(RuiDate(2026, 11, 1), cells[34].date) // first filler day
        assertEquals(RuiDate(2026, 11, 8), cells.last().date)
        assertEquals(false, cells.last().inMonth)
    }

    @Test
    fun february2021StartsOnMondayWithNoLeadingDays() {
        val cells = monthCells(2021, 2) // the 1st is a Monday, 28 days
        assertEquals(RuiDate(2021, 2, 1), cells.first().date)
        assertTrue(cells.first().inMonth)
        assertEquals(RuiDate(2021, 3, 14), cells.last().date)
    }

    @Test
    fun dayOfWeekIsContinuousAcrossMonthBoundaries() {
        // Consecutive civil days are consecutive weekdays, across every month
        // end of a leap and a common year.
        for (year in listOf(2024, 2025)) {
            for (month in 1..12) {
                val days = daysInMonth(year, month)
                for (day in 1 until days) {
                    assertTrue(
                        (weekdayFromMonday(year, month, day) + 1).mod(7) ==
                            weekdayFromMonday(year, month, day + 1),
                        "$year-$month-$day",
                    )
                }
                val next = when (month) {
                    12 -> 1 to (year + 1)
                    else -> month + 1 to year
                }
                assertTrue(
                    (weekdayFromMonday(year, month, days) + 1).mod(7) ==
                        weekdayFromMonday(next.second, next.first, 1),
                    "$year-$month-$days",
                )
            }
        }
    }

    @Test
    fun dateRejectsInvalidCivilValues() {
        assertFailsWith<IllegalArgumentException> { RuiDate(2024, 2, 30) }
        assertFailsWith<IllegalArgumentException> { RuiDate(2023, 2, 29) }
        assertFailsWith<IllegalArgumentException> { RuiDate(2024, 4, 31) }
        assertFailsWith<IllegalArgumentException> { RuiDate(2024, 1, 0) }
        assertFailsWith<IllegalArgumentException> { RuiDate(2024, 0, 1) }
        assertFailsWith<IllegalArgumentException> { RuiDate(2024, 13, 1) }
        assertFailsWith<IllegalArgumentException> { daysInMonth(2024, 0) }
    }

    @Test
    fun monthChangeClampsJanuary31ToFebruary() {
        assertEquals(RuiDate(2023, 2, 28), clampToMonth(RuiDate(2023, 1, 31), month = 2))
        assertEquals(RuiDate(2024, 2, 29), clampToMonth(RuiDate(2024, 1, 31), month = 2))
    }

    @Test
    fun monthChangeClamps31To30Days() {
        assertEquals(RuiDate(2024, 4, 30), clampToMonth(RuiDate(2024, 3, 31), month = 4))
    }

    @Test
    fun yearChangeClampsLeapDayToCommonYear() {
        assertEquals(RuiDate(2025, 2, 28), clampToMonth(RuiDate(2024, 2, 29), year = 2025))
        assertEquals(RuiDate(2100, 2, 28), clampToMonth(RuiDate(2000, 2, 29), year = 2100))
    }

    @Test
    fun yearRangeLowerBoundClampsLeapDayWithoutChangingInput() {
        val value = RuiDate(2024, 2, 29)
        val yearRange = 2025..2027

        assertEquals(RuiDate(2025, 2, 28), clampToMonth(value, year = value.year.coerceIn(yearRange)))
        assertEquals(RuiDate(2024, 2, 29), value)
    }

    @Test
    fun yearRangeUpperBoundClampsLeapDayWithoutChangingInput() {
        val value = RuiDate(2028, 2, 29)
        val yearRange = 2025..2027

        assertEquals(RuiDate(2027, 2, 28), clampToMonth(value, year = value.year.coerceIn(yearRange)))
        assertEquals(RuiDate(2028, 2, 29), value)
    }

    @Test
    fun clampingPreservesValidDays() {
        val value = RuiDate(2024, 1, 15)
        val yearRange = 2023..2025

        assertEquals(value, clampToMonth(value))
        assertEquals(value, clampToMonth(value, year = value.year.coerceIn(yearRange)))
        assertEquals(RuiDate(2024, 2, 15), clampToMonth(value, month = 2))
        assertEquals(RuiDate(2025, 1, 15), clampToMonth(value, year = 2025))
        assertEquals(RuiDate(2028, 2, 29), clampToMonth(RuiDate(2024, 2, 29), year = 2028))
    }

    @Test
    fun clampingUsesBothTargetYearAndMonth() {
        assertEquals(RuiDate(2023, 2, 28), clampToMonth(RuiDate(2024, 1, 31), year = 2023, month = 2))
        assertEquals(RuiDate(2024, 2, 29), clampToMonth(RuiDate(2023, 1, 31), year = 2024, month = 2))
    }

    @Test
    fun clampingRejectsInvalidTargetMonths() {
        val value = RuiDate(2024, 1, 31)

        assertFailsWith<IllegalArgumentException> { clampToMonth(value, month = 0) }
        assertFailsWith<IllegalArgumentException> { clampToMonth(value, month = 13) }
    }

    @Test
    fun leapYearFebruaryBounds() {
        assertEquals(29, daysInMonth(2024, 2))
        assertEquals(29, daysInMonth(2000, 2))
        assertEquals(28, daysInMonth(2023, 2))
        assertEquals(28, daysInMonth(1900, 2))
        assertEquals(29, RuiDate(2024, 2, 29).day)
        assertFailsWith<IllegalArgumentException> { RuiDate(1900, 2, 29) }
    }

    @Test
    fun cursorClampsIntoYearRangePreservingTheMonth() {
        // An external value outside the range keeps the caller's day; the
        // grid opens on the nearest in-range year of the same month.
        assertEquals(RuiDate(2024, 5, 1), calendarCursor(RuiDate(2023, 5, 10), 2024..2026))
        assertEquals(RuiDate(2026, 7, 1), calendarCursor(RuiDate(2030, 7, 3), 2024..2026))
        assertEquals(RuiDate(2025, 3, 1), calendarCursor(RuiDate(2025, 3, 21), 2024..2026))
    }

    @Test
    fun cursorShiftIsRejectedAtTheRangeBounds() {
        assertNull(shiftedCursor(RuiDate(2024, 1, 1), -1, 2024..2025))
        assertNull(shiftedCursor(RuiDate(2025, 12, 1), 1, 2024..2025))
    }

    @Test
    fun cursorShiftCrossesYearBoundariesInsideTheRange() {
        assertEquals(RuiDate(2025, 1, 1), shiftedCursor(RuiDate(2024, 12, 1), 1, 2024..2025))
        assertEquals(RuiDate(2024, 12, 1), shiftedCursor(RuiDate(2025, 1, 1), -1, 2024..2025))
    }
}
