package rui.components

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.runtime.BroadcastFrameClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import rui.foundation.RuiHaptics

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

    @Test
    fun columnExternalChangesCancelDragAndAllowRestart() = runTest {
        for (newKeys in externalOrders) {
            val fixture = ColumnFixture(this)
            val state = fixture.state
            state.startDrag("C", fromLongPress = false)
            state.drag(-10f)
            val generation = state.animationGeneration

            fixture.sync(newKeys)

            assertEquals(newKeys, state.keys)
            assertEquals(generation + 1, state.animationGeneration)
            assertCancelled(state)
            state.drag(-110f)
            state.onAutoScrolled(100f)
            state.endDrag()
            for (removedKey in originalKeys - newKeys.toSet()) {
                state.startDrag(removedKey, fromLongPress = false)
            }
            state.startDrag("missing", fromLongPress = true)
            assertCancelled(state)
            assertTrue(fixture.moves.isEmpty())
            assertEquals(1, fixture.haptics.impacts)

            fixture.sync(originalKeys)
            fixture.measure()
            state.startDrag("C", fromLongPress = true)
            state.drag(-110f)
            assertTrue(state.isDragging)
            assertEquals(listOf(2 to 1), fixture.moves)
            assertEquals(listOf("A", "C", "B"), fixture.items)
        }
    }

    @Test
    fun lazyExternalChangesCancelDragAndAllowRestart() = runTest {
        for (newKeys in externalOrders) {
            val fixture = LazyFixture(this)
            val state = fixture.state
            state.startDrag("C")
            state.drag(-10f)
            val generation = state.animationGeneration

            fixture.sync(newKeys)

            assertEquals(newKeys, state.keys)
            assertEquals(generation + 1, state.animationGeneration)
            assertCancelled(state)
            state.drag(-110f)
            state.onAutoScrolled(100f)
            state.endDrag()
            for (removedKey in originalKeys - newKeys.toSet()) {
                state.startDrag(removedKey)
            }
            state.startDrag("missing")
            assertCancelled(state)
            assertTrue(fixture.moves.isEmpty())
            assertEquals(1, fixture.haptics.impacts)

            fixture.sync(originalKeys)
            fixture.layout()
            state.startDrag("C")
            state.drag(-110f)
            assertTrue(state.isDragging)
            assertEquals(listOf(2 to 1), fixture.moves)
            assertEquals(listOf("A", "C", "B"), fixture.items)
        }
    }

    @Test
    fun columnMoveEchoPreservesDragAndAllowsConsecutiveMoves() = runTest {
        val fixture = ColumnFixture(this)
        val state = fixture.state
        state.startDrag("C", fromLongPress = false)
        val generation = state.animationGeneration

        state.drag(-110f)
        assertEquals(fixture.items, state.keys)
        fixture.sync(fixture.items)

        assertTrue(state.isDragging)
        assertEquals("C", state.draggedKey)
        assertEquals(1, state.draggedIndex)
        assertEquals(-10f, state.dragOffset)
        assertEquals(generation, state.animationGeneration)

        state.drag(-100f)
        fixture.sync(fixture.items)

        assertTrue(state.isDragging)
        assertEquals("C", state.draggedKey)
        assertEquals(0, state.draggedIndex)
        assertEquals(-10f, state.dragOffset)
        assertEquals(generation, state.animationGeneration)
        assertEquals(listOf(2 to 1, 1 to 0), fixture.moves)
        assertEquals(listOf("C", "A", "B"), state.keys)
        assertEquals(fixture.items, state.keys)
        assertEquals(100f, state.takeHomeShift("A"))
        assertEquals(100f, state.takeHomeShift("B"))
    }

    @Test
    fun lazyMoveEchoPreservesDragAndWaitsForLayoutBeforeNextMove() = runTest {
        val fixture = LazyFixture(this)
        val state = fixture.state
        state.startDrag("C")
        val generation = state.animationGeneration

        state.drag(-110f)
        assertEquals(fixture.items, state.keys)
        fixture.sync(fixture.items)

        assertTrue(state.isDragging)
        assertEquals("C", state.draggedKey)
        assertEquals(1, state.draggedIndex)
        assertEquals(-10f, state.dragOffset)
        assertEquals(generation, state.animationGeneration)
        assertNull(state.draggedInfo())

        state.drag(-100f)
        assertEquals(listOf(2 to 1), fixture.moves)
        fixture.layout()
        state.drag(0f)
        fixture.sync(fixture.items)

        assertTrue(state.isDragging)
        assertEquals("C", state.draggedKey)
        assertEquals(0, state.draggedIndex)
        assertEquals(-10f, state.dragOffset)
        assertEquals(generation, state.animationGeneration)
        assertEquals(listOf(2 to 1, 1 to 0), fixture.moves)
        assertEquals(listOf("C", "A", "B"), state.keys)
        assertEquals(fixture.items, state.keys)
        assertEquals(100f, state.takeHomeShift("A"))
        assertEquals(100f, state.takeHomeShift("B"))
    }

    @Test
    fun columnRemovalClearsMeasurementsAndPendingShifts() = runTest {
        val fixture = ColumnFixture(this)
        val state = fixture.state
        state.startDrag("C", fromLongPress = false)
        state.drag(-110f)

        fixture.sync(listOf("A", "C"))

        assertCancelled(state)
        assertEquals(0f, state.takeHomeShift("B"))
        fixture.sync(originalKeys)
        state.startDrag("B", fromLongPress = false)
        assertNull(state.draggedCenter())
        state.startDrag("C", fromLongPress = false)
        state.drag(-110f)
        assertEquals(listOf(2 to 1), fixture.moves)

        state.reportHeight("B", 100)
        state.drag(0f)
        assertEquals(listOf(2 to 1, 2 to 1), fixture.moves)
        assertEquals(listOf("A", "C", "B"), state.keys)
    }

    @Test
    fun lazyExternalChangesClearRemovedAndRetainedPendingShifts() = runTest {
        for (newKeys in listOf(listOf("A", "C"), listOf("B", "C"))) {
            val fixture = LazyFixture(this)
            val state = fixture.state
            state.startDrag("C")
            state.drag(-110f)
            val generation = state.animationGeneration

            fixture.sync(newKeys)

            assertCancelled(state)
            assertEquals(generation + 1, state.animationGeneration)
            assertEquals(0f, state.takeHomeShift("B"))
            state.drag(-110f)
            assertEquals(listOf(2 to 1), fixture.moves)
        }
    }

    @Test
    fun lazyStaleLayoutWithOutOfBoundsTargetDoesNotMove() = runTest {
        val fixture = LazyFixture(this)
        val state = fixture.state
        fixture.sync(listOf("A", "B"))
        state.startDrag("A")

        state.drag(250f)

        assertTrue(state.isDragging)
        assertNull(state.draggedInfo())
        assertTrue(fixture.moves.isEmpty())
        assertEquals(listOf("A", "B"), state.keys)

        fixture.layout()
        state.drag(0f)
        assertEquals(listOf(0 to 1), fixture.moves)
        assertEquals(listOf("B", "A"), state.keys)
    }

    @Test
    fun lazyStaleLayoutWithMismatchedNeighborDoesNotMoveWrongKey() = runTest {
        val fixture = LazyFixture(this)
        val state = fixture.state
        fixture.sync(listOf("A", "C", "B"))
        state.startDrag("A")

        state.drag(50f)

        assertTrue(state.isDragging)
        assertNull(state.draggedInfo())
        assertTrue(fixture.moves.isEmpty())
        assertEquals(listOf("A", "C", "B"), state.keys)

        fixture.layout()
        state.drag(0f)
        assertEquals(listOf(0 to 1), fixture.moves)
        assertEquals(listOf("C", "A", "B"), state.keys)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun columnExternalChangeStopsSettlingAndClearsRetainedShifts() = runTest {
        val clock = BroadcastFrameClock()
        val fixture = ColumnFixture(CoroutineScope(coroutineContext + clock))
        val state = fixture.state
        state.startDrag("C", fromLongPress = false)
        state.drag(-110f)
        state.endDrag()
        runCurrent()
        assertEquals("C", state.settlingKey)
        assertTrue(clock.hasAwaiters)

        fixture.sync(listOf("B", "C"))
        runCurrent()

        assertCancelled(state)
        assertFalse(clock.hasAwaiters)
        assertEquals(0f, state.takeHomeShift("B"))
        state.startDrag("C", fromLongPress = false)
        state.drag(-110f)
        clock.sendFrame(1_000_000_000L)
        runCurrent()
        assertEquals(-10f, state.dragOffset)
        assertEquals(listOf(2 to 1, 1 to 0), fixture.moves)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun lazyExternalChangeStopsSettlingAndClearsRetainedShifts() = runTest {
        val clock = BroadcastFrameClock()
        val fixture = LazyFixture(CoroutineScope(coroutineContext + clock))
        val state = fixture.state
        state.startDrag("C")
        state.drag(-110f)
        state.endDrag()
        runCurrent()
        assertEquals("C", state.settlingKey)
        assertTrue(clock.hasAwaiters)

        fixture.sync(listOf("B", "C"))
        runCurrent()

        assertCancelled(state)
        assertFalse(clock.hasAwaiters)
        assertEquals(0f, state.takeHomeShift("B"))
        fixture.layout()
        state.startDrag("C")
        state.drag(-110f)
        clock.sendFrame(1_000_000_000L)
        runCurrent()
        assertEquals(-10f, state.dragOffset)
        assertEquals(listOf(2 to 1, 1 to 0), fixture.moves)
    }

    private fun assertCancelled(state: ReorderState) {
        assertFalse(state.isDragging)
        assertNull(state.draggedKey)
        assertEquals(-1, state.draggedIndex)
        assertEquals(0f, state.dragOffset)
        assertNull(state.settlingKey)
        assertNull(state.draggedCenter())
    }

    private fun assertCancelled(state: LazyReorderState) {
        assertFalse(state.isDragging)
        assertNull(state.draggedKey)
        assertEquals(-1, state.draggedIndex)
        assertEquals(0f, state.dragOffset)
        assertNull(state.settlingKey)
        assertNull(state.draggedInfo())
    }

    private class ColumnFixture(scope: CoroutineScope) {
        var items: List<Any> = originalKeys
        val moves = mutableListOf<Pair<Int, Int>>()
        val haptics = TestHaptics()
        val state = ReorderState(
            onMove = { from, to ->
                moves += from to to
                items = items.toMutableList().apply { add(to, removeAt(from)) }
            },
            haptics = haptics,
            scope = scope,
        )

        init {
            state.syncOrder(items)
            measure()
        }

        fun sync(newKeys: List<Any>) {
            items = newKeys
            state.syncOrder(items)
        }

        fun measure() {
            items.forEachIndexed { index, key ->
                state.reportHeight(key, 100)
                state.reportCenter(key, index * 100f + 50f)
            }
        }
    }

    private class LazyFixture(scope: CoroutineScope) {
        var items: List<Any> = originalKeys
        var visible: List<LazyListItemInfo> = emptyList()
        val moves = mutableListOf<Pair<Int, Int>>()
        val haptics = TestHaptics()
        val state = LazyReorderState(
            visibleItemsInfo = { visible },
            onMove = { from, to ->
                moves += from to to
                items = items.toMutableList().apply { add(to, removeAt(from)) }
            },
            haptics = haptics,
            scope = scope,
        )

        init {
            state.syncOrder(items)
            layout()
        }

        fun sync(newKeys: List<Any>) {
            items = newKeys
            state.syncOrder(items)
        }

        fun layout() {
            visible = items.mapIndexed { index, key -> TestItemInfo(index, key) }
        }
    }

    private class TestItemInfo(
        override val index: Int,
        override val key: Any,
    ) : LazyListItemInfo {
        override val offset: Int = index * 100
        override val size: Int = 100
        override val contentType: Any? = null
    }

    private class TestHaptics : RuiHaptics {
        var impacts = 0

        override fun selection() = Unit

        override fun impactLight() {
            impacts++
        }

        override fun impactMedium() {
            impacts++
        }

        override fun success() = Unit
        override fun warning() = Unit
        override fun error() = Unit
    }

    private companion object {
        val originalKeys = listOf("A", "B", "C")
        val externalOrders = listOf(
            listOf("B", "C"),
            listOf("A", "B"),
            listOf("A", "X", "B", "C"),
            listOf("C", "B", "A"),
            emptyList(),
        )
    }
}
