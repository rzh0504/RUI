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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
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
    content: @Composable (PaddingValues) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().then(modifier).background(containerColor)) {
        SubcomposeLayout { constraints ->
            val layoutWidth = constraints.maxWidth
            val layoutHeight = constraints.maxHeight
            val loose = constraints.copy(minWidth = 0, minHeight = 0)

            val topBarPlaceables = subcompose(RuiScaffoldSections.TopBar, topBar)
                .map { it.measure(loose) }
            val bottomBarPlaceables = subcompose(RuiScaffoldSections.BottomBar, bottomBar)
                .map { it.measure(loose) }
            val topBarHeight = topBarPlaceables.maxOfOrNull { it.height } ?: 0
            val bottomBarHeight = bottomBarPlaceables.maxOfOrNull { it.height } ?: 0

            val contentPadding = PaddingValues(
                top = max(topBarHeight, contentWindowInsets.getTop(this)).toDp(),
                bottom = max(bottomBarHeight, contentWindowInsets.getBottom(this)).toDp(),
                start = contentWindowInsets.getLeft(this, layoutDirection).toDp(),
                end = contentWindowInsets.getRight(this, layoutDirection).toDp(),
            )

            val bodyPlaceables = subcompose(RuiScaffoldSections.Content) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (backdrop != null) Modifier.blurBackdrop(backdrop) else Modifier),
                ) {
                    content(contentPadding)
                }
            }.map { it.measure(loose) }

            layout(layoutWidth, layoutHeight) {
                bodyPlaceables.forEach { it.place(0, 0) }
                topBarPlaceables.forEach { it.place(0, 0) }
                bottomBarPlaceables.forEach { it.place(0, layoutHeight - it.height) }
            }
        }
    }
}

private enum class RuiScaffoldSections { TopBar, BottomBar, Content }
