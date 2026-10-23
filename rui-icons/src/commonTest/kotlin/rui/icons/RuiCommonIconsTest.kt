package rui.icons

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The generated set's contract: every entry is a 24dp/24-viewport vector with
 * a unique semantic name, and only the direction-dependent glyphs mirror.
 */
class RuiCommonIconsTest {

    @Test
    fun everyIconIsA24dpVectorWithAUniqueName() {
        assertTrue(RuiCommonIcons.All.isNotEmpty())
        assertEquals(
            RuiCommonIcons.All.size,
            RuiCommonIcons.All.map { it.name }.distinct().size,
        )
        RuiCommonIcons.All.forEach { entry ->
            assertEquals(24.dp, entry.image.defaultWidth, entry.name)
            assertEquals(24.dp, entry.image.defaultHeight, entry.name)
            assertEquals(24f, entry.image.viewportWidth, entry.name)
            assertEquals(24f, entry.image.viewportHeight, entry.name)
        }
    }

    @Test
    fun directionalIconsMirrorInRtl() {
        assertTrue(RuiCommonIcons.ChevronLeft.autoMirror)
        assertTrue(RuiCommonIcons.ChevronRight.autoMirror)
        assertTrue(RuiCommonIcons.ArrowLeft.autoMirror)
        assertTrue(RuiCommonIcons.ArrowRight.autoMirror)
        assertTrue(RuiCommonIcons.SkipBack.autoMirror)
        assertTrue(RuiCommonIcons.SkipForward.autoMirror)
        assertTrue(RuiCommonIcons.Undo.autoMirror)
        assertTrue(RuiCommonIcons.Redo.autoMirror)
        assertTrue(RuiCommonIcons.Send.autoMirror)
    }

    @Test
    fun neutralIconsDoNotMirror() {
        assertFalse(RuiCommonIcons.Search.autoMirror)
        assertFalse(RuiCommonIcons.ArrowUp.autoMirror)
        assertFalse(RuiCommonIcons.Close.autoMirror)
        assertFalse(RuiCommonIcons.ArrowUpRight.autoMirror)
    }

    @Test
    fun semanticNamesKeepTheirLucideSource() {
        assertEquals("check", RuiCommonIcons.Check.name)
        assertEquals("ellipsis", RuiCommonIcons.MoreHorizontal.name)
        assertEquals("house", RuiCommonIcons.Home.name)
        assertEquals("lock-open", RuiCommonIcons.Unlock.name)
        assertEquals("sliders-horizontal", RuiCommonIcons.Sliders.name)
    }
}
