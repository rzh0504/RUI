package rui.sample

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.composeunstyled.TooltipHost
import rui.components.RuiOverlayHost
import rui.components.RuiFloatingNavigationBar
import rui.components.RuiNavigationBar
import rui.components.RuiNavigationBarItem
import rui.components.RuiNavigationRail
import rui.components.RuiSidebar
import rui.components.RuiScaffold
import rui.components.RuiTopAppBar
import rui.components.navigation.RuiNavDisplay
import rui.components.navigation.RuiNavKey
import rui.components.navigation.RuiNavController
import rui.components.navigation.rememberRuiNavBackStack
import rui.components.rememberRuiScrollBehavior
import rui.foundation.ExperimentalRuiApi
import rui.foundation.LocalRuiLiquidGlassTuning
import rui.foundation.RuiGlassMaterial
import rui.foundation.RuiLiquidGlassTuning
import rui.foundation.rememberBlurBackdrop
import rui.foundation.utils.StatusBarAppearance
import rui.icons.RuiCommonIcons
import rui.sample.screens.AccountScreen
import rui.sample.screens.BlocksScreen
import rui.sample.screens.ButtonsScreen
import rui.sample.screens.CartScreen
import rui.sample.screens.InboxScreen
import rui.sample.screens.SearchResultsScreen
import rui.sample.screens.SignInScreen

import rui.sample.screens.ChipsScreen
import rui.sample.screens.BottomSheetScreen
import rui.sample.screens.DialogScreen
import rui.sample.screens.DropdownMenuScreen
import rui.sample.screens.DrawerScreen
import rui.sample.screens.AvatarScreen
import rui.sample.screens.ImageScreen
import rui.sample.screens.IconsScreen
import rui.sample.screens.DisclosureScreen
import rui.sample.screens.ProgressScreen
import rui.sample.screens.PullToRefreshScreen
import rui.sample.screens.SnackbarScreen
import rui.sample.screens.SearchBarScreen
import rui.sample.screens.EmptyStateScreen
import rui.sample.screens.PageIndicatorScreen
import rui.sample.screens.PickersScreen
import rui.sample.screens.SkeletonScreen
import rui.sample.screens.SwipeActionsScreen
import rui.sample.screens.TooltipScreen
import rui.sample.screens.TabsScreen
import rui.sample.screens.TextFieldScreen
import rui.sample.screens.TokensScreen
import rui.sample.screens.TogglesScreen
import rui.sample.screens.LazyListScreen
import rui.sample.screens.ListsScreen
import rui.sample.screens.LiquidGlassScreen
import rui.sample.screens.BannerScreen
import rui.sample.screens.LocalizationScreen
import rui.sample.screens.ReorderScreen
import rui.sample.screens.ScrollBarScreen
import rui.sample.screens.NavigationRailScreen
import rui.sample.screens.SidebarScreen
import rui.sample.screens.LyricsScreen
import rui.sample.screens.NowPlayingScreen
import rui.theme.RuiTheme
import rui.theme.RuiWindowWidthClass

/** The catalog's routes. */
public sealed interface CatalogPage : RuiNavKey {
    public data object Root : CatalogPage
    public data object SignIn : CatalogPage
    public data object Account : CatalogPage
    public data object Cart : CatalogPage
    public data object Inbox : CatalogPage
    public data object SearchResults : CatalogPage
    public data object Buttons : CatalogPage
    public data object Chips : CatalogPage
    public data object TextField : CatalogPage
    public data object Toggles : CatalogPage
    public data object Tabs : CatalogPage
    public data object Dialog : CatalogPage
    public data object DropdownMenu : CatalogPage
    public data object BottomSheet : CatalogPage
    public data object Progress : CatalogPage
    public data object Avatar : CatalogPage
    public data object AsyncImage : CatalogPage
    public data object Tooltip : CatalogPage
    public data object Disclosure : CatalogPage
    public data object Snackbar : CatalogPage
    public data object PullToRefresh : CatalogPage
    public data object SwipeActions : CatalogPage
    public data object Search : CatalogPage
    public data object Pickers : CatalogPage
    public data object Skeleton : CatalogPage
    public data object EmptyState : CatalogPage
    public data object PageIndicator : CatalogPage
    public data object Drawer : CatalogPage
    public data object Lists : CatalogPage
    public data object LazyList : CatalogPage
    public data object Icons : CatalogPage
    public data object InlineBanner : CatalogPage
    public data object Localization : CatalogPage
    public data object Reorder : CatalogPage
    public data object ScrollBar : CatalogPage
    public data object NavigationRail : CatalogPage
    public data object Lyrics : CatalogPage
    public data object NowPlaying : CatalogPage
}

private fun CatalogPage.title(): String = when (this) {
    CatalogPage.Root -> "Rui"
    CatalogPage.SignIn -> "Sign in"
    CatalogPage.Account -> "Account"
    CatalogPage.Cart -> "Cart"
    CatalogPage.Inbox -> "Inbox"
    CatalogPage.SearchResults -> "Search results"
    CatalogPage.Buttons -> "Buttons"
    CatalogPage.Chips -> "Chips"
    CatalogPage.TextField -> "Text field"
    CatalogPage.Toggles -> "Selection controls"
    CatalogPage.Tabs -> "Segmented tabs"
    CatalogPage.Dialog -> "Dialog"
    CatalogPage.DropdownMenu -> "Dropdown menu"
    CatalogPage.BottomSheet -> "Bottom sheet"
    CatalogPage.Progress -> "Progress"
    CatalogPage.Avatar -> "Avatar"
    CatalogPage.AsyncImage -> "Async image"
    CatalogPage.Tooltip -> "Tooltip"
    CatalogPage.Disclosure -> "Disclosure"
    CatalogPage.Snackbar -> "Snackbar"
    CatalogPage.PullToRefresh -> "Pull to refresh"
    CatalogPage.SwipeActions -> "Swipe actions"
    CatalogPage.Search -> "Search bar"
    CatalogPage.Pickers -> "Pickers"
    CatalogPage.Skeleton -> "Skeleton"
    CatalogPage.EmptyState -> "Empty state"
    CatalogPage.PageIndicator -> "Page indicator"
    CatalogPage.Drawer -> "Drawer"
    CatalogPage.Lists -> "Lists"
    CatalogPage.LazyList -> "Lazy list"
    CatalogPage.Icons -> "Icons"
    CatalogPage.InlineBanner -> "Inline banner"
    CatalogPage.Localization -> "Localization"
    CatalogPage.Reorder -> "Reorder"
    CatalogPage.ScrollBar -> "Scroll bar"
    CatalogPage.NavigationRail -> "Navigation rail"
    CatalogPage.Lyrics -> "Lyrics"
    CatalogPage.NowPlaying -> "Now playing"
}

/**
 * Root of the catalog app: a large-title app bar, a bottom navigation bar
 * switchable between the docked bar and the floating capsule, and component
 * pages pushed onto a [RuiNavDisplay] back stack.
 * [DialogHost] sits at the root so RuiDialog panels portal above everything.
 */
/** The catalog-wide glass material. Solid shows what every surface does
 *  with no capture at all; Frosted and Liquid thread the mode through the
 *  bars and the overlay demo pages. The Liquid tab is mode-independent. */
public enum class GlassMode { Solid, Frosted, Liquid }

private const val KeyDarkTheme = "appearance.darkTheme"
private const val KeyGlassMode = "appearance.glassMode"
private const val KeyFloatingNavBar = "appearance.floatingNavBar"
private const val KeyLanguage = "appearance.language"

/** Index of the liquid-glass tab among the root nav items. */
private const val LiquidTab = 3

@OptIn(ExperimentalRuiApi::class, ExperimentalComposeUiApi::class)
@Composable
public fun CatalogApp() {
    val systemDark = isSystemInDarkTheme()
    var darkTheme by remember { mutableStateOf(SampleSettings.getBoolean(KeyDarkTheme, systemDark)) }
    var glassMode by remember {
        mutableStateOf(
            GlassMode.entries.firstOrNull {
                it.name == SampleSettings.getString(KeyGlassMode, GlassMode.Frosted.name)
            } ?: GlassMode.Frosted,
        )
    }
    var floatingNavBarEnabled by remember { mutableStateOf(SampleSettings.getBoolean(KeyFloatingNavBar, false)) }
    // Liquid-material test bench: the Liquid tab's sliders write it, the
    // provider makes every liquid surface below — bars included — follow.
    // Session-scoped on purpose; the shipped look is one app restart away.
    var liquidTuning by remember { mutableStateOf(RuiLiquidGlassTuning()) }
    var languageChoice by remember {
        mutableStateOf(
            LanguageChoice.entries.firstOrNull { it.name == SampleSettings.getString(KeyLanguage, LanguageChoice.System.name) }
                ?: LanguageChoice.System,
        )
    }
    var tab by remember { mutableStateOf(0) }
    val backStack = rememberRuiNavBackStack<CatalogPage>(CatalogPage.Root)
    val nav = remember { RuiNavController(backStack) }
    // One app-level host and producer scope keep transient feedback alive
    // while navigation entries are pushed and popped.
    val snackbarHostState = rui.components.rememberRuiSnackbarHostState()
    val appScope = rememberCoroutineScope()

    RuiTheme(darkTheme = darkTheme, strings = languageChoice.resolveStrings()) {
        // In-app theme toggle: push the appearance to the system bars in the
        // same frame, so icons stay visible over the switched background.
        // Pages that drive the bars themselves (the immersive player) hand
        // control back to this call when they leave composition — the util's
        // caller-stack restore, no re-key needed.
        StatusBarAppearance(darkIcons = !darkTheme)
        val widthClass = RuiTheme.windowWidthClass
        val motion = RuiTheme.motion
        val layoutDirection = LocalLayoutDirection.current
        // Positive means the next tab lives to the end of the reading
        // direction; the root tab slide follows the bar's item order.
        val tabForward = if (layoutDirection == LayoutDirection.Ltr) 1 else -1
        val rootNavItems = listOf(
            RuiNavigationBarItem("UI", RuiCommonIcons.Component),
            RuiNavigationBarItem("Blocks", RuiCommonIcons.Blocks),
            RuiNavigationBarItem("Tokens", RuiCommonIcons.Palette),
            RuiNavigationBarItem("Liquid", RuiCommonIcons.Droplet),
        )
        // Dialogs and bottom sheets portal above the navigation stack —
        // both through the one overlay host.
        CompositionLocalProvider(LocalRuiLiquidGlassTuning provides liquidTuning) {
        RuiOverlayHost {
            // Tooltip panels portal above the navigation stack too.
            TooltipHost {
            val density = LocalDensity.current
            val snackbarBottomDp = when {
                backStack.size > 1 -> 0.dp
                widthClass != RuiWindowWidthClass.Compact -> 48.dp
                floatingNavBarEnabled -> 88.dp
                else -> 48.dp
            }
            val snackbarBottomPx = with(density) { snackbarBottomDp.toPx() }
            val snackbarOffset = remember(density) { Animatable(snackbarBottomPx) }
            val snackbarMotion = RuiTheme.motion.navigation
            LaunchedEffect(snackbarBottomPx) {
                snackbarOffset.animateTo(snackbarBottomPx, snackbarMotion)
            }
            Box(Modifier.fillMaxSize()) {
                // Adaptive root navigation: the bars own Compact widths; from
                // Medium up a rail or sidebar leads the root page instead.
                // Pushed pages take the full width again.
                Row(Modifier.fillMaxSize()) {
                    if (backStack.size == 1 && widthClass != RuiWindowWidthClass.Compact) {
                        if (widthClass == RuiWindowWidthClass.Expanded) {
                            RuiSidebar(
                                items = rootNavItems,
                                selected = tab,
                                onSelect = { tab = it },
                                modifier = Modifier.fillMaxHeight(),
                            )
                        } else {
                            RuiNavigationRail(
                                items = rootNavItems,
                                selected = tab,
                                onSelect = { tab = it },
                                modifier = Modifier.fillMaxHeight(),
                            )
                        }
                    }
                    Box(Modifier.weight(1f)) {
                RuiNavDisplay(backStack = backStack, onBack = { nav.pop() }) { page ->
                val atRoot = page == CatalogPage.Root
                // Top-level pages get the collapsing large title; pushed
                // pages keep miuix's SmallTopAppBar form: a pinned small
                // centered title (no scroll behavior).
                val scrollBehavior = if (atRoot) rememberRuiScrollBehavior() else null
                // One backdrop per stack entry — the capture and the bars
                // sampling it must share a coordinate space. A backdrop shared
                // across stacked pages samples covered pages' translated
                // content and the blur lands misaligned. The liquid capture
                // costs one extra record per frame, so it exists only where
                // something consumes it: everything in Liquid mode, and the
                // Liquid tab (mode-independent) while it shows.
                val background = RuiTheme.colors.background
                val useGlass = glassMode != GlassMode.Solid
                val barMaterial = if (glassMode == GlassMode.Liquid) {
                    RuiGlassMaterial.Liquid
                } else {
                    RuiGlassMaterial.Frosted
                }
                val backdrop = rememberBlurBackdrop(
                    liquid = glassMode == GlassMode.Liquid || (atRoot && tab == LiquidTab),
                ) {
                    drawRect(background)
                    drawContent()
                }
                val glassBackdrop = if (useGlass) backdrop else null
                // The Liquid tab showcases the material in every mode, so its
                // liquid surfaces need the page capture even when the app's
                // bars are solid; only the bars follow the mode.
                val scaffoldBackdrop = if (atRoot && tab == LiquidTab) backdrop else glassBackdrop
                RuiScaffold(
                    backdrop = scaffoldBackdrop,
                    // The immersive player owns the status-bar area itself.
                    topScrim = page != CatalogPage.NowPlaying,
                    topBar = {
                        // The title area stays solid: no backdrop and no
                        // material, whatever the app-wide glass mode is.
                        if (page == CatalogPage.NowPlaying) {
                            // The music block is fully immersive: it owns
                            // its chrome and draws behind the status bar.
                        } else if (atRoot) {
                            RuiTopAppBar(
                                title = page.title(),
                                scrollBehavior = scrollBehavior,
                            )
                        } else {
                            RuiTopAppBar(
                                title = page.title(),
                                showBack = true,
                                onBack = { nav.pop() },
                            )
                        }
                    },
                    bottomBar = {
                        // The bars own Compact widths; from Medium up the rail
                        // or sidebar leads the root page instead.
                        if (atRoot && widthClass == RuiWindowWidthClass.Compact) {
                            if (floatingNavBarEnabled) {
                                // Four tabs fill the capsule — take back most
                                // of the standing margins so the slots stay
                                // wide (20dp breathing per side).
                                BoxWithConstraints {
                                    RuiFloatingNavigationBar(
                                        items = rootNavItems,
                                        selected = tab,
                                        onSelect = { tab = it },
                                        backdrop = glassBackdrop,
                                        material = barMaterial,
                                        barWidth = maxWidth - 40.dp,
                                    )
                                }
                            } else {
                                RuiNavigationBar(
                                    items = rootNavItems,
                                    selected = tab,
                                    onSelect = { tab = it },
                                    backdrop = glassBackdrop,
                                    material = barMaterial,
                                )
                            }
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
                            CatalogPage.Root -> AnimatedContent(
                                targetState = tab,
                                modifier = Modifier.fillMaxSize(),
                                transitionSpec = {
                                    val direction = if (targetState > initialState) {
                                        tabForward
                                    } else {
                                        -tabForward
                                    }
                                    (
                                        slideInHorizontally { width ->
                                            direction * width / 10
                                        } + fadeIn(animationSpec = motion.normal)
                                        ) togetherWith
                                        (
                                            slideOutHorizontally { width ->
                                                -direction * width / 10
                                            } + fadeOut(animationSpec = motion.normal)
                                            )
                                },
                                label = "catalog_root_tab",
                            ) { currentTab ->
                                when (currentTab) {
                                    0 -> ComponentsScreen(
                                        darkTheme = darkTheme,
                                        onDarkThemeChange = { darkTheme = it; SampleSettings.putBoolean(KeyDarkTheme, it) },
                                        glassMode = glassMode,
                                        onGlassModeChange = { glassMode = it; SampleSettings.putString(KeyGlassMode, it.name) },
                                        backdrop = glassBackdrop,
                                        material = barMaterial,
                                        floatingNavBarEnabled = floatingNavBarEnabled,
                                        onFloatingNavBarEnabledChange = { floatingNavBarEnabled = it; SampleSettings.putBoolean(KeyFloatingNavBar, it) },
                                        onOpen = { nav.push(it) },
                                        contentPadding = padding,
                                    )
                                    1 -> BlocksScreen(
                                        onOpen = { nav.push(it) },
                                        contentPadding = padding,
                                    )
                                    2 -> TokensScreen(contentPadding = padding)
                                    LiquidTab -> LiquidGlassScreen(
                                        backdrop = backdrop,
                                        tuning = liquidTuning,
                                        onTuningChange = { liquidTuning = it },
                                        contentPadding = padding,
                                    )
                                }
                            }
                            CatalogPage.SignIn -> SignInScreen(contentPadding = padding)
                            CatalogPage.Account -> AccountScreen(contentPadding = padding)
                            CatalogPage.Cart -> CartScreen(contentPadding = padding)
                            CatalogPage.Inbox -> InboxScreen(contentPadding = padding)
                            CatalogPage.SearchResults -> SearchResultsScreen(contentPadding = padding)
                            CatalogPage.Buttons -> ButtonsScreen(contentPadding = padding)
                            CatalogPage.Chips -> ChipsScreen(contentPadding = padding)
                            CatalogPage.TextField -> TextFieldScreen(contentPadding = padding)
                            CatalogPage.Toggles -> TogglesScreen(contentPadding = padding)
                            CatalogPage.Tabs -> TabsScreen(contentPadding = padding)
                            CatalogPage.Dialog -> DialogScreen(
                                glassMode = glassMode,
                                backdrop = backdrop,
                                contentPadding = padding,
                            )
                            CatalogPage.DropdownMenu -> DropdownMenuScreen(
                                glassMode = glassMode,
                                backdrop = backdrop,
                                contentPadding = padding,
                            )
                            CatalogPage.BottomSheet -> BottomSheetScreen(
                                glassMode = glassMode,
                                backdrop = backdrop,
                                contentPadding = padding,
                            )
                            CatalogPage.Progress -> ProgressScreen(contentPadding = padding)
                            CatalogPage.Avatar -> AvatarScreen(contentPadding = padding)
                            CatalogPage.AsyncImage -> ImageScreen(contentPadding = padding)
                            CatalogPage.Tooltip -> TooltipScreen(contentPadding = padding)
                            CatalogPage.Disclosure -> DisclosureScreen(contentPadding = padding)
                            CatalogPage.Snackbar -> SnackbarScreen(
                                hostState = snackbarHostState,
                                requestScope = appScope,
                                contentPadding = padding,
                            )
                            CatalogPage.PullToRefresh -> PullToRefreshScreen(contentPadding = padding)
                            CatalogPage.SwipeActions -> SwipeActionsScreen(contentPadding = padding)
                            CatalogPage.Search -> SearchBarScreen(contentPadding = padding)
                            CatalogPage.Pickers -> PickersScreen(contentPadding = padding)
                            CatalogPage.Skeleton -> SkeletonScreen(contentPadding = padding)
                            CatalogPage.EmptyState -> EmptyStateScreen(contentPadding = padding)
                            CatalogPage.PageIndicator -> PageIndicatorScreen(contentPadding = padding)
                            CatalogPage.Drawer -> DrawerScreen(contentPadding = padding)
                            CatalogPage.Lists -> ListsScreen(
                                backdrop = glassBackdrop,
                                material = barMaterial,
                                contentPadding = padding,
                            )
                            CatalogPage.LazyList -> LazyListScreen(contentPadding = padding)
                            CatalogPage.Icons -> IconsScreen(contentPadding = padding)
                            CatalogPage.InlineBanner -> BannerScreen(contentPadding = padding)
                            CatalogPage.Localization -> LocalizationScreen(
                                languageChoice = languageChoice,
                                onLanguageChoiceChange = { languageChoice = it; SampleSettings.putString(KeyLanguage, it.name) },
                                contentPadding = padding,
                            )
                            CatalogPage.Reorder -> ReorderScreen(contentPadding = padding)
                            CatalogPage.ScrollBar -> ScrollBarScreen(contentPadding = padding)
                            CatalogPage.NavigationRail -> NavigationRailScreen(contentPadding = padding)
                            CatalogPage.Lyrics -> LyricsScreen(contentPadding = padding)
                            CatalogPage.NowPlaying -> NowPlayingScreen(onBack = { nav.pop() })
                        }
                    }
                }
                }
                }
                }
                Box(
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 8.dp)
                        .offset(y = with(density) { -snackbarOffset.value.toDp() }),
                ) {
                    rui.components.RuiSnackbarHost(snackbarHostState)
                }
            }
        }
        }
    }
}
}
