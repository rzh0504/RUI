package rui.components

import androidx.compose.runtime.Composable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The cascading menu's correctness lives in its path decisions: registration
 * feeds pushes, pops unwind, and a registration leaving composition pops its
 * own level if it was active. The panel's animation and focus handling need a
 * real frame — these cover the logic around them.
 */
class RuiCascadingMenuStateTest {

    @Test
    fun pushBuildsThePathFromRegistrations() {
        val state = RuiCascadingMenuState()
        val child: @Composable () -> Unit = {}
        val grandchild: @Composable () -> Unit = {}
        state.register(childId, "Share to…", child)
        state.register(grandchildId, "More…", grandchild)

        state.push(childId)
        state.push(grandchildId)

        assertEquals(
            listOf(
                RuiCascadingMenuState.Level(childId, "Share to…"),
                RuiCascadingMenuState.Level(grandchildId, "More…"),
            ),
            state.path,
        )
    }

    @Test
    fun pushWithoutRegistrationIsIgnored() {
        val state = RuiCascadingMenuState()
        state.push(neverRegistered)
        assertTrue(state.path.isEmpty())
    }

    @Test
    fun popUnwindsOneLevelAtATime() {
        val state = singleLevelState()
        state.push(childId)

        state.pop()
        assertTrue(state.path.isEmpty())
        state.pop() // popping the root is a no-op
        assertTrue(state.path.isEmpty())
    }

    @Test
    fun unregisterPopsItsOwnLevelAndDropsDescendants() {
        val state = singleLevelState()
        val deeperId = Any()
        state.register(deeperId, "Deeper…", {})
        state.push(childId)
        state.push(deeperId)
        assertEquals(2, state.path.size)

        // The child row leaves composition while two levels reference it:
        // both the level and everything pushed past it go with it.
        state.unregister(childId)
        assertTrue(state.path.isEmpty())
    }

    @Test
    fun unregisterKeepsUnrelatedLevels() {
        val state = RuiCascadingMenuState()
        val otherId = Any()
        state.register(childId, "Share to…", {})
        state.register(otherId, "Other…", {})
        state.push(otherId)

        state.unregister(childId)

        assertEquals(1, state.path.size)
        assertEquals(otherId, state.path.single().id)
    }

    @Test
    fun reregistrationReplacesTheContent() {
        val state = RuiCascadingMenuState()
        val first: @Composable () -> Unit = {}
        val second: @Composable () -> Unit = {}
        state.register(childId, "Share to…", first)
        state.register(childId, "Share to…", second)

        assertEquals(second, state.contents[childId])
    }

    private val childId = Any()
    private val grandchildId = Any()
    private val neverRegistered = Any()

    private fun singleLevelState(): RuiCascadingMenuState {
        val state = RuiCascadingMenuState()
        state.register(childId, "Share to…", {})
        return state
    }
}
