package rui.theme

import kotlin.test.Test
import kotlin.test.assertEquals

class RuiWindowWidthClassTest {

    @Test
    fun breakpointsMatchMaterialTiers() {
        assertEquals(RuiWindowWidthClass.Compact, windowWidthClassAt(0))
        assertEquals(RuiWindowWidthClass.Compact, windowWidthClassAt(599))
        assertEquals(RuiWindowWidthClass.Medium, windowWidthClassAt(600))
        assertEquals(RuiWindowWidthClass.Medium, windowWidthClassAt(839))
        assertEquals(RuiWindowWidthClass.Expanded, windowWidthClassAt(840))
        assertEquals(RuiWindowWidthClass.Expanded, windowWidthClassAt(2000))
    }
}
