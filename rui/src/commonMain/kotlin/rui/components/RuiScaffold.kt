package rui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import rui.foundation.BlurBackdrop
import rui.foundation.blurBackdrop
import rui.theme.RuiTheme
import kotlin.math.max

/**
 * The basic screen layout, modeled after miuix's Scaffold.
 *
 * Content fills the whole screen so translucent (glass) bars can show it
 * through themselves; the measured [topBar]/[bottomBar] heights plus
 * [contentWindowInsets] are reported to the content as [PaddingValues], so
 * lists can pad their scrollable ends and never end up permanently hidden
 * behind a bar while still scrolling under it.
 *
 * The padding is a single mutable [PaddingValues] updated during measurement,
 * so a [topBar] that changes height while scrolling (e.g. a collapsing large
 * title) only re-measures the content — it never recomposes it.
 *
 * Pass the [backdrop] created with [rui.foundation.rememberBlurBackdrop] and
 * used by the glass bars — the scaffold captures the content area into it.
 */
@Composable
public fun RuiScaffold(
    modifier: Modifier = Modifier,
    backdrop: BlurBackdrop? = null,
    containerColor: Color = RuiTheme.colors.background,
    contentWindowInsets: WindowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout),
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    // PaddingValues whose values change without changing the instance — reads
    // stay in the layout phase of the consuming padding modifiers. Pattern
    // from miuix's Scaffold.
    val contentPadding = remember {
        object : PaddingValues {
            var paddingHolder by mutableStateOf(PaddingValues(0.dp))

            override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp = paddingHolder.calculateLeftPadding(layoutDirection)

            override fun calculateTopPadding(): Dp = paddingHolder.calculateTopPadding()

            override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp = paddingHolder.calculateRightPadding(layoutDirection)

            override fun calculateBottomPadding(): Dp = paddingHolder.calculateBottomPadding()
        }
    }
    // Remembered so re-measures during scrolling don't hand subcompose a fresh
    // lambda (which would recompose the whole content). The explicit
    // @Composable type is what makes the inner lambda composable. The
    // transition flag is read inside the lambda rather than keyed here — a key
    // would rebuild and re-measure this whole subcomposition at both flips of
    // a nav transition, when the frame budget belongs to the motion; read at
    // the recorder's scope the flip only swaps the recording modifier.
    val bodyContent: @Composable () -> Unit = remember(content, contentPadding, backdrop) {
        {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (backdrop != null) {
                            Modifier.blurBackdrop(
                                backdrop,
                                recordEnabled = !rui.foundation.LocalRuiTransitionActive.current,
                            )
                        } else {
                            Modifier
                        },
                    ),
            ) {
                content(contentPadding)
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize().then(modifier).background(containerColor)) {
        SubcomposeLayout { constraints ->
            val layoutWidth = constraints.maxWidth
            val layoutHeight = constraints.maxHeight
            val loose = constraints.copy(minWidth = 0, minHeight = 0)

            val topBarPlaceables = subcompose(RuiScaffoldSections.TopBar, topBar)
                .map { it.measure(loose) }
            val bottomBarPlaceables = subcompose(RuiScaffoldSections.BottomBar, bottomBar)
                .map { it.measure(loose) }
            val snackbarPlaceables = subcompose(RuiScaffoldSections.SnackbarHost, snackbarHost)
                .map { it.measure(loose) }
            val topBarHeight = topBarPlaceables.maxOfOrNull { it.height } ?: 0
            val bottomBarHeight = bottomBarPlaceables.maxOfOrNull { it.height } ?: 0
            val bottomInsetPx = contentWindowInsets.getBottom(this)

            contentPadding.paddingHolder = PaddingValues(
                top = max(topBarHeight, contentWindowInsets.getTop(this)).toDp(),
                bottom = max(bottomBarHeight, bottomInsetPx).toDp(),
                start = contentWindowInsets.getLeft(this, layoutDirection).toDp(),
                end = contentWindowInsets.getRight(this, layoutDirection).toDp(),
            )

            val bodyPlaceables = subcompose(RuiScaffoldSections.Content, bodyContent)
                .map { it.measure(loose) }

            layout(layoutWidth, layoutHeight) {
                bodyPlaceables.forEach { it.place(0, 0) }
                topBarPlaceables.forEach { it.place(0, 0) }
                bottomBarPlaceables.forEach { it.place(0, layoutHeight - it.height) }
                // The snackbar floats above the bottom bar (or the bottom
                // inset when there is no bar).
                val snackbarBottom = max(bottomBarHeight, bottomInsetPx)
                snackbarPlaceables.forEach { it.place(0, layoutHeight - snackbarBottom - it.height) }
            }
        }
    }
}

private enum class RuiScaffoldSections { TopBar, BottomBar, SnackbarHost, Content }
