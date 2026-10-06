package rui.components

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The slot math is the reorder's correctness: which slot the dragged center
 * falls into, where homes sit, and which neighbors a move displaces.
 */
class RuiReorderableColumnTest {

    @Test
    fun slotIndexFlipsAtMidpoints() {
        val heights = floatArrayOf(100f, 100f, 100f)
        assertEquals(0, slotIndexOf(49.9f, heights))
        assertEquals(1, slotIndexOf(50f, heights))
        assertEquals(1, slotIndexOf(149.9f, heights))
        assertEquals(2, slotIndexOf(150f, heights))
        assertEquals(2, slotIndexOf(400f, heights))
        assertEquals(0, slotIndexOf(-20f, heights))
    }

    @Test
    fun slotIndexHonorsVaryingHeights() {
        val heights = floatArrayOf(200f, 100f)
        assertEquals(0, slotIndexOf(99f, heights))
        assertEquals(1, slotIndexOf(101f, heights))
    }

    @Test
    fun homeTopSumsPrecedingHeights() {
        val heights = floatArrayOf(100f, 200f, 50f)
        assertEquals(0f, homeTopOf(0, heights))
        assertEquals(100f, homeTopOf(1, heights))
        assertEquals(300f, homeTopOf(2, heights))
        assertEquals(350f, homeTopOf(3, heights))
    }

    @Test
    fun downwardMovePushesCrossedItemsUp() {
        val shifts = moveShifts(from = 0, to = 2, draggedHeight = 100f)
        assertEquals(-100f, shifts[1])
        assertEquals(-100f, shifts[2])
        assertEquals(null, shifts[3])
    }

    @Test
    fun upwardMovePushesCrossedItemsDown() {
        val shifts = moveShifts(from = 3, to = 0, draggedHeight = 40f)
        assertEquals(40f, shifts[0])
        assertEquals(40f, shifts[1])
        assertEquals(40f, shifts[2])
        assertEquals(null, shifts[3])
    }

    @Test
    fun adjacentMoveShiftsOnlyTheCrossedNeighbor() {
        assertEquals(mapOf(2 to -60f), moveShifts(1, 2, 60f))
        assertEquals(mapOf(0 to 60f), moveShifts(1, 0, 60f))
    }
}
