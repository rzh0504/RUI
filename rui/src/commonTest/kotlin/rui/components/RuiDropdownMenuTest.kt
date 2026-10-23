package rui.components

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The auto-flip decision is pure geometry: a vertical menu keeps its
 * preferred side until the anchor crosses the window's middle, then opens
 * toward the larger half. These pin the boundary and the untouched
 * horizontal sides.
 */
class RuiDropdownMenuTest {

    @Test
    fun bottomMenusFlipUpWhenTheAnchorCrossesTheMiddle() {
        assertEquals(AnchorSide.Top, autoFlippedMenuSide(AnchorSide.Bottom, 60f, 100))
        assertEquals(AnchorSide.Bottom, autoFlippedMenuSide(AnchorSide.Bottom, 40f, 100))
        // Exactly on the middle keeps the preferred side.
        assertEquals(AnchorSide.Bottom, autoFlippedMenuSide(AnchorSide.Bottom, 50f, 100))
    }

    @Test
    fun topMenusFlipDownWhenTheAnchorCrossesTheMiddle() {
        assertEquals(AnchorSide.Bottom, autoFlippedMenuSide(AnchorSide.Top, 40f, 100))
        assertEquals(AnchorSide.Top, autoFlippedMenuSide(AnchorSide.Top, 60f, 100))
        assertEquals(AnchorSide.Top, autoFlippedMenuSide(AnchorSide.Top, 50f, 100))
    }

    @Test
    fun horizontalSidesNeverFlip() {
        assertEquals(AnchorSide.Start, autoFlippedMenuSide(AnchorSide.Start, 90f, 100))
        assertEquals(AnchorSide.End, autoFlippedMenuSide(AnchorSide.End, 10f, 100))
    }

    @Test
    fun unknownGeometryKeepsThePreferredSide() {
        assertEquals(AnchorSide.Bottom, autoFlippedMenuSide(AnchorSide.Bottom, null, 100))
        assertEquals(AnchorSide.Top, autoFlippedMenuSide(AnchorSide.Top, 90f, 0))
    }

    @Test
    fun panelWidthSubtractsSafeInsetsAndMargins() {
        assertEquals(312.dp, menuPanelMaxWidth(360.dp, 0.dp, 0.dp, 24.dp))
        assertEquals(240.dp, menuPanelMaxWidth(360.dp, 24.dp, 48.dp, 24.dp))
        assertEquals(0.dp, menuPanelMaxWidth(40.dp, 20.dp, 20.dp, 24.dp))
    }

    @Test
    fun panelHeightSubtractsSafeInsetsAndMargins() {
        assertEquals(680.dp, menuPanelMaxHeight(800.dp, 24.dp, 48.dp, 24.dp))
        assertEquals(0.dp, menuPanelMaxHeight(80.dp, 24.dp, 48.dp, 24.dp))
    }
}
