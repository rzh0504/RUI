package rui.sample

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.unit.dp
import com.composeunstyled.DialogHost
import rui.components.RuiNavigationBar
import rui.components.RuiNavigationBarItem
import rui.components.RuiScaffold
import rui.components.RuiTopAppBar
import rui.foundation.BlurBackdrop
import rui.foundation.ExperimentalRuiApi
import rui.foundation.RuiIcons
import rui.foundation.blurBackdrop
import rui.foundation.rememberBlurBackdrop
import rui.sample.screens.ButtonsScreen
import rui.sample.screens.DialogScreen
import rui.sample.screens.TabsScreen
import rui.sample.screens.TextFieldScreen
import rui.sample.screens.TokensScreen
import rui.sample.screens.TogglesScreen
import rui.theme.RuiTheme

/** Pages reachable from the components tab. */
public enum class Destination(public val label: String) {
    Buttons("Buttons"),
    TextField("Text field"),
    Toggles("Switch & checkbox"),
    Tabs("Segmented tabs"),
    Dialog("Dialog"),
}

/**
 * Root of the catalog app: a large-title app bar, a frosted-glass bottom
 * navigation bar (Components / Tokens) and a back stack of component pages.
 * [DialogHost] sits at the root so RuiDialog panels portal above everything.
 */
@OptIn(ExperimentalRuiApi::class, ExperimentalComposeUiApi::class)
@Composable
public fun CatalogApp() {
    val systemDark = isSystemInDarkTheme()
    var darkTheme by remember { mutableStateOf(systemDark) }
    var glassEnabled by remember { mutableStateOf(true) }
    var tab by remember { mutableStateOf(0) }
    val backStack = remember { mutableStateListOf<Destination>() }

    BackHandler(enabled = backStack.isNotEmpty()) {
        backStack.removeAt(backStack.lastIndex)
    }

    RuiTheme(darkTheme = darkTheme) {
        val background = RuiTheme.colors.background
        val backdrop = rememberBlurBackdrop {
            drawRect(background)
            drawContent()
        }

        DialogHost {
            CatalogBody(
                backdrop = backdrop,
                tab = tab,
                onTabChange = { tab = it },
                darkTheme = darkTheme,
                onDarkThemeChange = { darkTheme = it },
                glassEnabled = glassEnabled,
                onGlassEnabledChange = { glassEnabled = it },
                backStack = backStack,
            )
        }
    }
}

@OptIn(ExperimentalRuiApi::class)
@Composable
private fun CatalogBody(
    backdrop: BlurBackdrop,
    tab: Int,
    onTabChange: (Int) -> Unit,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    glassEnabled: Boolean,
    onGlassEnabledChange: (Boolean) -> Unit,
    backStack: MutableList<Destination>,
) {
    val current = backStack.lastOrNull()

    RuiScaffold(
        backdrop = backdrop,
        topBar = {
            if (current == null) {
                RuiTopAppBar(title = "Rui", largeTitle = true, backdrop = backdrop)
            } else {
                RuiTopAppBar(
                    title = current.label,
                    showBack = true,
                    onBack = { backStack.removeAt(backStack.lastIndex) },
                    backdrop = backdrop,
                )
            }
        },
        bottomBar = {
            if (current == null) {
                RuiNavigationBar(
                    items = listOf(
                        RuiNavigationBarItem("Components", RuiIcons.Grid),
                        RuiNavigationBarItem("Tokens", RuiIcons.Swatch),
                    ),
                    selected = tab,
                    onSelect = onTabChange,
                    backdrop = backdrop,
                )
            }
        },
    ) {
        val topPadding = if (current == null) 88.dp else 64.dp
        when (current) {
            null -> when (tab) {
                0 -> ComponentsScreen(
                    darkTheme = darkTheme,
                    onDarkThemeChange = onDarkThemeChange,
                    glassEnabled = glassEnabled,
                    onGlassEnabledChange = onGlassEnabledChange,
                    onOpen = { backStack.add(it) },
                    modifier = Modifier.fillMaxSize().padding(top = topPadding),
                )
                else -> TokensScreen(modifier = Modifier.fillMaxSize().padding(top = topPadding))
            }
            Destination.Buttons -> ButtonsScreen(Modifier.fillMaxSize().padding(top = topPadding))
            Destination.TextField -> TextFieldScreen(Modifier.fillMaxSize().padding(top = topPadding))
            Destination.Toggles -> TogglesScreen(Modifier.fillMaxSize().padding(top = topPadding))
            Destination.Tabs -> TabsScreen(Modifier.fillMaxSize().padding(top = topPadding))
            Destination.Dialog -> DialogScreen(
                glassEnabled = glassEnabled,
                backdrop = backdrop,
                modifier = Modifier.fillMaxSize().padding(top = topPadding),
            )
        }
    }
}
