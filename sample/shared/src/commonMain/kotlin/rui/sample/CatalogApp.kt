package rui.sample

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.composeunstyled.DialogHost
import rui.components.RuiNavigationBar
import rui.components.RuiNavigationBarItem
import rui.components.RuiScaffold
import rui.components.RuiTopAppBar
import rui.components.navigation.RuiNavDisplay
import rui.components.navigation.RuiNavKey
import rui.components.navigation.RuiNavController
import rui.components.navigation.rememberRuiNavBackStack
import rui.components.rememberRuiScrollBehavior
import rui.foundation.ExperimentalRuiApi
import rui.foundation.RuiIcons
import rui.foundation.rememberBlurBackdrop
import rui.foundation.utils.StatusBarAppearance
import rui.sample.screens.ButtonsScreen
import rui.sample.screens.DialogScreen
import rui.sample.screens.TabsScreen
import rui.sample.screens.TextFieldScreen
import rui.sample.screens.TokensScreen
import rui.sample.screens.TogglesScreen
import rui.theme.RuiTheme

/** The catalog's routes. */
public sealed interface CatalogPage : RuiNavKey {
    public data object Root : CatalogPage
    public data object Buttons : CatalogPage
    public data object TextField : CatalogPage
    public data object Toggles : CatalogPage
    public data object Tabs : CatalogPage
    public data object Dialog : CatalogPage
}

private fun CatalogPage.title(): String = when (this) {
    CatalogPage.Root -> "Rui"
    CatalogPage.Buttons -> "Buttons"
    CatalogPage.TextField -> "Text field"
    CatalogPage.Toggles -> "Selection controls"
    CatalogPage.Tabs -> "Segmented tabs"
    CatalogPage.Dialog -> "Dialog"
}

/**
 * Root of the catalog app: a large-title app bar, a frosted-glass bottom
 * navigation bar (Components / Tokens) and component pages pushed onto a
 * [RuiNavDisplay] back stack. [DialogHost] sits at the root so RuiDialog
 * panels portal above everything.
 */
@OptIn(ExperimentalRuiApi::class, ExperimentalComposeUiApi::class)
@Composable
public fun CatalogApp() {
    val systemDark = isSystemInDarkTheme()
    var darkTheme by remember { mutableStateOf(systemDark) }
    var frostedEnabled by remember { mutableStateOf(true) }
    var tab by remember { mutableStateOf(0) }
    val backStack = rememberRuiNavBackStack<CatalogPage>(CatalogPage.Root)
    val nav = remember { RuiNavController(backStack) }

    RuiTheme(darkTheme = darkTheme) {
        // In-app theme toggle: push the appearance to the system bars in the
        // same frame, so icons stay visible over the switched background.
        StatusBarAppearance(darkIcons = !darkTheme)
        DialogHost {
            RuiNavDisplay(backStack = backStack, onBack = { nav.pop() }) { page ->
                val atRoot = page == CatalogPage.Root
                // Top-level pages get the collapsing large title; pushed
                // pages keep miuix's SmallTopAppBar form: a pinned small
                // centered title (no scroll behavior).
                val scrollBehavior = if (atRoot) rememberRuiScrollBehavior() else null
                // One backdrop per stack entry — the capture and the bars
                // sampling it must share a coordinate space. A backdrop shared
                // across stacked pages samples covered pages' translated
                // content and the blur lands misaligned.
                val background = RuiTheme.colors.background
                val backdrop = rememberBlurBackdrop {
                    drawRect(background)
                    drawContent()
                }
                RuiScaffold(
                    backdrop = backdrop,
                    topBar = {
                        if (atRoot) {
                            RuiTopAppBar(
                                title = page.title(),
                                backdrop = backdrop,
                                scrollBehavior = scrollBehavior,
                            )
                        } else {
                            RuiTopAppBar(
                                title = page.title(),
                                showBack = true,
                                onBack = { nav.pop() },
                                backdrop = backdrop,
                            )
                        }
                    },
                    bottomBar = {
                        if (atRoot) {
                            RuiNavigationBar(
                                items = listOf(
                                    RuiNavigationBarItem("Components", RuiIcons.Grid),
                                    RuiNavigationBarItem("Tokens", RuiIcons.Swatch),
                                ),
                                selected = tab,
                                onSelect = { tab = it },
                                backdrop = backdrop,
                            )
                        }
                    },
                ) { padding ->
                    // Above the screens' overscroll modifiers, so the bar
                    // collapses/expands before the content bounces (miuix's
                    // example wiring). Only the large-title page drives a bar.
                    Box(
                        Modifier.then(
                            scrollBehavior?.let { Modifier.nestedScroll(it.nestedScrollConnection) }
                                ?: Modifier,
                        ),
                    ) {
                        when (page) {
                            CatalogPage.Root -> when (tab) {
                                0 -> ComponentsScreen(
                                    darkTheme = darkTheme,
                                    onDarkThemeChange = { darkTheme = it },
                                    frostedEnabled = frostedEnabled,
                                    onFrostedEnabledChange = { frostedEnabled = it },
                                    onOpen = { nav.push(it) },
                                    contentPadding = padding,
                                )
                                else -> TokensScreen(contentPadding = padding)
                            }
                            CatalogPage.Buttons -> ButtonsScreen(contentPadding = padding)
                            CatalogPage.TextField -> TextFieldScreen(contentPadding = padding)
                            CatalogPage.Toggles -> TogglesScreen(contentPadding = padding)
                            CatalogPage.Tabs -> TabsScreen(contentPadding = padding)
                            CatalogPage.Dialog -> DialogScreen(
                                frostedEnabled = frostedEnabled,
                                backdrop = backdrop,
                                contentPadding = padding,
                            )
                        }
                    }
                }
            }
        }
    }
}
