package rui.components

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composeunstyled.DrawerHost
import com.composeunstyled.DrawerPlacement
import com.composeunstyled.DrawerPresentation
import com.composeunstyled.DrawerSnapPoint
import com.composeunstyled.DrawerSnapPoints
import com.composeunstyled.Overlay
import com.composeunstyled.Panel
import com.composeunstyled.SwipeArea
import com.composeunstyled.UnstyledDrawer
import com.composeunstyled.UnstyledDrawerState
import com.composeunstyled.Viewport
import rui.theme.RuiTheme

/** Where a drawer currently rests. */
public enum class RuiDrawerValue {
    /** The drawer is closed; a start-edge swipe strip can still open it. */
    Closed,

    /** The drawer panel is presented. */
    Open,
}

/**
 * Drag and settle state of a [RuiDrawer]: a start panel with two snap
 * points — closed at zero, open at the panel's content size.
 */
@Stable
public class RuiDrawerState internal constructor(initialValue: RuiDrawerValue) {
    internal val unstyledState: UnstyledDrawerState<RuiDrawerValue> = UnstyledDrawerState(
        initialValue = initialValue,
        snapPoints = DrawerSnapPoints {
            RuiDrawerValue.Closed at DrawerSnapPoint.Zero
            RuiDrawerValue.Open at DrawerSnapPoint.ContentSize
        },
    )

    /** The value the drawer is resting on. */
    public val currentValue: RuiDrawerValue
        get() = unstyledState.currentValue

    /** Open the drawer. */
    public fun open() {
        unstyledState.targetValue = RuiDrawerValue.Open
    }

    /** Close the drawer. */
    public fun close() {
        unstyledState.targetValue = RuiDrawerValue.Closed
    }
}

/** Remember the state of a [RuiDrawer]. */
@Composable
public fun rememberRuiDrawerState(
    initialValue: RuiDrawerValue = RuiDrawerValue.Closed,
): RuiDrawerState = remember { RuiDrawerState(initialValue) }

/**
 * A navigation drawer, iOS-adjacent and restrained: a start-edge panel of
 * `surface` sliding over the screen with a scrim behind it. It opens by
 * [RuiDrawerState.open] or a swipe from the screen's start edge, closes by
 * panel drag, scrim tap, Back or Escape, and panels are drag-draggable.
 * The panel content sits inside a safe-drawing-padded column — rows typically
 * close the drawer through [RuiDrawerState.close] after acting.
 *
 * The drawer renders above the [content] through a portal host of its own;
 * place this where the drawer should be able to reach.
 *
 * @param state The hoisted drawer state.
 * @param drawerContent The panel's content, arranged in a column.
 * @param content The screen the drawer slides over.
 */
@Composable
public fun RuiDrawer(
    state: RuiDrawerState,
    modifier: Modifier = Modifier,
    gesturesEnabled: Boolean = true,
    drawerContent: @Composable ColumnScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    val colors = RuiTheme.colors
    val motion = RuiTheme.motion

    DrawerHost(modifier = modifier) {
        Box(Modifier.fillMaxSize()) {
            content()
            UnstyledDrawer(
                state = state.unstyledState,
                modifier = Modifier.fillMaxSize(),
                placement = DrawerPlacement.Start,
                presentation = DrawerPresentation.Overlay,
                gesturesEnabled = gesturesEnabled,
                overlay = {
                    Overlay(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.scrim),
                        enter = fadeIn(motion.normal),
                        exit = fadeOut(motion.fast),
                    )
                },
            ) {
                Viewport(Modifier.fillMaxSize()) {
                    Panel(
                        modifier = Modifier
                            .widthIn(max = 320.dp)
                            .fillMaxWidth()
                            .fillMaxHeight(),
                    ) {
                        Box(Modifier.fillMaxSize().background(colors.surface)) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .windowInsetsPadding(
                                        WindowInsets.systemBars.union(WindowInsets.displayCutout),
                                    ),
                                content = drawerContent,
                            )
                        }
                    }
                    SwipeArea(Modifier.width(24.dp).fillMaxHeight())
                }
            }
        }
    }
}
