package rui.components

import androidx.compose.runtime.Composable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The cascading menu's correctness lives in its path decisions: a push
 * appends a level carrying its own children (so the pushing row leaving
 * composition during the level transition cannot retract the push), and a
 * pop unwinds one level at a time. The panel's animation and focus handling
 * need a real frame — these cover the logic around them.
 */
class RuiCascadingMenuStateTest {

    private val childContent: @Composable () -> Unit = {}

    @Test
    fun pushAppendsLevelsCarryingTheirContent() {
        val state = RuiCascadingMenuState()
        val deeper: @Composable () -> Unit = {}

        state.push(childId, "Share to…", childContent)
        state.push(deeperId, "More…", deeper)

        assertEquals(2, state.path.size)
        assertEquals("Share to…", state.path[0].title)
        assertEquals(childContent, state.path[0].content)
        assertEquals("More…", state.path[1].title)
        assertEquals(deeperId, state.path[1].id)
    }

    @Test
    fun popUnwindsOneLevelAtATime() {
        val state = RuiCascadingMenuState()
        state.push(childId, "Share to…", childContent)

        state.pop()
        assertTrue(state.path.isEmpty())
        state.pop() // popping the root is a no-op
        assertTrue(state.path.isEmpty())
    }

    @Test
    fun levelsWithDistinctIdsCoexist() {
        val state = RuiCascadingMenuState()
        val other: @Composable () -> Unit = {}

        state.push(childId, "Share to…", childContent)
        state.push(otherId, "Other…", other)

        assertEquals(childId, state.path[0].id)
        assertEquals(otherId, state.path[1].id)
    }

    private val childId = Any()
    private val otherId = Any()
    private val deeperId = Any()
}
