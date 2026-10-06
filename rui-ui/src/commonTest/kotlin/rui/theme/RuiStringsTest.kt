package rui.theme

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RuiStringsTest {

    @Test
    fun builtInsCarryCompleteSets() {
        for (strings in listOf(RuiStringsDefaults.English, RuiStringsDefaults.SimplifiedChinese)) {
            assertEquals(12, strings.monthNames.size, strings::class.toString())
            assertEquals(7, strings.weekdayNames.size, strings::class.toString())
            assertEquals(2, strings.periodLabels.size, strings::class.toString())
            for (labels in listOf(strings.monthNames, strings.weekdayNames, strings.periodLabels)) {
                labels.forEach { assertTrue(it.isNotBlank(), "$labels: $it") }
            }
            for (label in listOf(
                strings.back, strings.clear, strings.dismiss,
                strings.decrease, strings.increase,
                strings.previousMonth, strings.nextMonth,
            )) {
                assertTrue(label.isNotBlank())
            }
        }
    }

    @Test
    fun weekdaysAreMondayFirst() {
        assertEquals("Mon", RuiStringsDefaults.English.weekdayNames.first())
        assertEquals("Sun", RuiStringsDefaults.English.weekdayNames.last())
        assertEquals("一", RuiStringsDefaults.SimplifiedChinese.weekdayNames.first())
        assertEquals("日", RuiStringsDefaults.SimplifiedChinese.weekdayNames.last())
    }

    @Test
    fun monthHeaderFollowsEachLanguageConvention() {
        assertEquals("January 2026", RuiStringsDefaults.English.monthHeader(2026, 1))
        assertEquals("2026年1月", RuiStringsDefaults.SimplifiedChinese.monthHeader(2026, 1))
    }

    @Test
    fun partialOverrideFillsFromDelegate() {
        val custom = object : RuiStrings by RuiStringsDefaults.English {
            override val monthNames = List(12) { "m${it + 1}" }
        }
        assertEquals("Back", custom.back)
        // Delegation caveat: monthHeader reads the delegate's names, so a
        // partial overrider of monthNames overrides monthHeader alongside it.
        assertEquals("March 2026", custom.monthHeader(2026, 3))
    }
}
