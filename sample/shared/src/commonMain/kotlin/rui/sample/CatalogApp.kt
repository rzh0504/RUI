package rui.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import com.composeunstyled.DialogHost
import rui.foundation.ExperimentalRuiApi
import rui.sample.screens.ButtonsScreen
import rui.sample.screens.DialogScreen
import rui.sample.screens.FoundationsScreen
import rui.sample.screens.TabsScreen
import rui.sample.screens.TextFieldScreen
import rui.sample.screens.TogglesScreen
import rui.theme.RuiTheme

/** Pages reachable from the catalog index. */
public enum class Destination(public val label: String) {
    Foundations("Foundations"),
    Buttons("Buttons"),
    TextField("Text field"),
    Toggles("Switch & checkbox"),
    Tabs("Segmented tabs"),
    Dialog("Dialog"),
}

/**
 * Root of the catalog app: a home index with inset grouped rows, pushing
 * detail pages onto a back stack, plus theme and glass toggles. [DialogHost]
 * sits at the root so RuiDialog panels portal above everything.
 */
@OptIn(ExperimentalRuiApi::class, ExperimentalComposeUiApi::class)
@Composable
public fun CatalogApp() {
    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    var darkTheme by remember { mutableStateOf(systemDark) }
    var glassEnabled by remember { mutableStateOf(true) }
    val backStack = remember { mutableStateListOf<Destination>() }

    BackHandler(enabled = backStack.isNotEmpty()) {
        backStack.removeAt(backStack.lastIndex)
    }

    RuiTheme(darkTheme = darkTheme) {
        DialogHost {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(RuiTheme.colors.background),
            ) {
                val current = backStack.lastOrNull()
                if (current == null) {
                    HomeScreen(
                        darkTheme = darkTheme,
                        onDarkThemeChange = { darkTheme = it },
                        glassEnabled = glassEnabled,
                        onGlassEnabledChange = { glassEnabled = it },
                        onOpen = { backStack.add(it) },
                    )
                } else {
                    val onBack: () -> Unit = { backStack.removeAt(backStack.lastIndex) }
                    when (current) {
                        Destination.Foundations -> FoundationsScreen(onBack)
                        Destination.Buttons -> ButtonsScreen(onBack)
                        Destination.TextField -> TextFieldScreen(onBack)
                        Destination.Toggles -> TogglesScreen(onBack)
                        Destination.Tabs -> TabsScreen(onBack)
                        Destination.Dialog -> DialogScreen(onBack)
                    }
                }
            }
        }
    }
}
