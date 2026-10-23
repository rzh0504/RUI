package rui.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import rui.foundation.RuiIcons

/**
 * The selected-glyph rule lives on the item model, so every shell sharing it
 * ([RuiNavigationBar], [RuiFloatingNavigationBar], [RuiNavigationRail],
 * [RuiSidebar]) swaps the same way: [RuiNavigationBarItem.selectedIcon] while
 * selected, [RuiNavigationBarItem.icon] elsewhere.
 */
class RuiNavigationBarItemTest {

    private val rest = RuiIcons.Grid
    private val selected = RuiIcons.Layers

    @Test
    fun selectedIconIsUsedWhileSelected() {
        assertEquals(selected, RuiNavigationBarItem("Library", rest, selected).iconFor(selected = true))
    }

    @Test
    fun restIconIsUsedWhileUnselected() {
        assertEquals(rest, RuiNavigationBarItem("Library", rest, selected).iconFor(selected = false))
    }

    @Test
    fun withoutASelectedIconTheRestIconIsKept() {
        val item = RuiNavigationBarItem("Library", rest)
        assertEquals(rest, item.iconFor(selected = true))
        assertEquals(rest, item.iconFor(selected = false))
    }

    @Test
    fun withOnlyASelectedIconTheUnselectedSlotHasNoGlyph() {
        val item = RuiNavigationBarItem("Library", selectedIcon = selected)
        assertEquals(selected, item.iconFor(selected = true))
        assertNull(item.iconFor(selected = false))
    }
}
