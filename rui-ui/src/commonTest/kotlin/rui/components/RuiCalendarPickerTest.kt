package rui.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
        assertFailsWith<IllegalArgumentException> { RuiDate(2024, 13, 1) }
        assertFailsWith<IllegalArgumentException> { daysInMonth(2024, 0) }
    }
}
