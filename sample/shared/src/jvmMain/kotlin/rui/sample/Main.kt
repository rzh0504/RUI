package rui.sample

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        title = "Rui",
        // Phone proportions so the catalog keeps its mobile layout; the
        // window stays resizable for breakpoint experiments.
        state = rememberWindowState(width = 420.dp, height = 900.dp),
        onCloseRequest = ::exitApplication,
    ) {
        CatalogApp()
    }
}
