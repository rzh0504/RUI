package rui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import rui.foundation.BlurBackdrop
import rui.foundation.blurBackdrop
import rui.theme.RuiTheme

/**
 * The basic screen layout: content fills the whole screen and the `topBar` /
 * `bottomBar` slots overlay it, so translucent (glass) bars show scrolling
 * content through themselves. Pass the [backdrop] created with
 * [rui.foundation.rememberBlurBackdrop] and used by the glass bars — the
 * scaffold captures the content area into it.
 */
@Composable
public fun RuiScaffold(
    modifier: Modifier = Modifier,
    backdrop: BlurBackdrop? = null,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().then(modifier).background(RuiTheme.colors.background)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (backdrop != null) Modifier.blurBackdrop(backdrop) else Modifier),
        ) {
            content()
        }
        Box(modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth()) {
            topBar()
        }
        Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
            bottomBar()
        }
    }
}
