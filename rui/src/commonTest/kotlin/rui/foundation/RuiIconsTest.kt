package rui.foundation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The core set's RTL contract: navigation glyphs and transport skips mirror,
 * everything symmetric does not. The extended set is covered in rui-icons.
 */
class RuiIconsTest {

    @Test
    fun directionalIconsMirrorInRtl() {
        assertTrue(RuiIcons.ChevronLeft.autoMirror)
        assertTrue(RuiIcons.ChevronRight.autoMirror)
        assertTrue(RuiIcons.SkipBack.autoMirror)
        assertTrue(RuiIcons.SkipForward.autoMirror)
    }

    @Test
    fun neutralIconsDoNotMirror() {
        assertFalse(RuiIcons.ChevronDown.autoMirror)
        assertFalse(RuiIcons.Check.autoMirror)
        assertFalse(RuiIcons.Close.autoMirror)
        assertFalse(RuiIcons.Play.autoMirror)
    }
}
