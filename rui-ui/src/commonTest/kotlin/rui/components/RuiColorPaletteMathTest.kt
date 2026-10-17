package rui.components

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Grid splitting for [RuiColorPalette]: rows of [columns], padded tail. */
class RuiColorPaletteMathTest {

    private val twelve = List(12) { Color(it.toFloat(), 0.5f, 0.5f) }

    @Test
    fun rowsSplitEvenlyAndPadTheTail() {
        val rows = paletteRows(twelve, columns = 5)
        assertEquals(3, rows.size)
        assertEquals(5, rows[0].size)
        assertEquals(5, rows[1].size)
        assertEquals(5, rows[2].size)
        assertEquals(2, rows[2].count { it != null })
        assertTrue(rows[2].drop(2).all { it == null })
        assertEquals(twelve, rows.flatten().filterNotNull())
    }

    @Test
    fun exactDivisionLeavesNoPadding() {
        val rows = paletteRows(List(10) { Color.Red }, columns = 5)
        assertEquals(2, rows.size)
        assertTrue(rows.all { row -> row.all { it != null } })
    }

    @Test
    fun columnsClampToOne() {
        val rows = paletteRows(List(3) { Color.Red }, columns = 0)
        assertEquals(3, rows.size)
        assertTrue(rows.all { it.size == 1 })
    }

    @Test
    fun emptyPaletteRendersNothing() {
        assertTrue(paletteRows(emptyList(), columns = 5).isEmpty())
    }
}
