package rui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.composeunstyled.DropdownMenuPanel
import com.composeunstyled.UnstyledDropdownMenu
import com.composeunstyled.UnstyledDropdownMenuItem
import rui.foundation.BlurBackdrop
import rui.foundation.RuiGlassMaterial
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiText
import rui.foundation.blurSurface
import rui.foundation.liquidGlassSurface
import rui.foundation.liquidSource
import rui.foundation.platform.isBlurSupported
import rui.theme.RuiTheme
import rui.theme.toShadowShape

/** Air kept between a menu panel and the window edges. */
private val MenuPanelWindowMargin = 24.dp

// A menu is a small transient panel: its lift stays below the platform's
// default drop shadow so it reads as a soft edge, not a modal card.
private val MenuShadowElevation = 6.dp
private val MenuShadowAmbient = Color.Black.copy(alpha = 0.10f)
private val MenuShadowSpot = Color.Black.copy(alpha = 0.14f)

/**
 * The side a vertical menu actually opens on: it keeps [preferred] while its
 * anchor sits in the matching half of the window, and flips to the other
 * side when the anchor crosses the middle — a menu near the bottom edge
 * opens upward instead of being clamped over its trigger. Horizontal sides
 * are left alone. [anchorCenterY] and [windowHeightPx] are window pixels.
 */
internal fun autoFlippedMenuSide(
    preferred: AnchorSide,
    anchorCenterY: Float?,
    windowHeightPx: Int,
): AnchorSide {
    if (anchorCenterY == null || windowHeightPx <= 0) return preferred
    val middle = windowHeightPx / 2f
    return when {
        preferred == AnchorSide.Bottom && anchorCenterY > middle -> AnchorSide.Top
        preferred == AnchorSide.Top && anchorCenterY < middle -> AnchorSide.Bottom
        else -> preferred
    }
}

/** Panel width cap: the window minus the safe-drawing insets and margins. */
internal fun menuPanelMaxWidth(
    windowWidth: Dp,
    safeStart: Dp,
    safeEnd: Dp,
    margin: Dp,
): Dp = (windowWidth - safeStart - safeEnd - margin * 2).coerceAtLeast(0.dp)

/** Panel height cap: the window minus the safe-drawing insets and margins. */
internal fun menuPanelMaxHeight(
    windowHeight: Dp,
    safeTop: Dp,
    safeBottom: Dp,
    margin: Dp,
): Dp = (windowHeight - safeTop - safeBottom - margin * 2).coerceAtLeast(0.dp)

/**
 * An iOS-style dropdown menu anchored to [anchor]: a floating glass (or
 * elevated) panel with vertically arranged items — frosted by default, or
 * the liquid-glass material with `material = RuiGlassMaterial.Liquid`.
 * Dismisses on outside tap, back/Esc, or selecting an item. Compose
 * [RuiDropdownMenuItem] (and plain [RuiDivider] where wanted) inside
 * [content].
 *
 * The panel portals above everything; give it the screen's [backdrop] to
 * render the frosted material — without one it falls back to an opaque
 * elevated surface.
 *
 * @param expanded Whether the menu is showing; toggle from [anchor]'s onClick.
 * @param onDismissRequest Called when the menu should close.
 * @param side The preferred side of the anchor the panel appears on.
 * @param autoFlip When true, a [AnchorSide.Top]/[AnchorSide.Bottom] panel
 *   flips to the roomier half of the window when its anchor sits in the
 *   other half, so a menu near the edge opens inward instead of covering its
 *   trigger. Horizontal sides never flip.
 * @param alignment How the panel aligns with the anchor along that side.
 * @param sideOffset Air between the anchor and the panel's near edge.
 * @param alignmentOffset Shifts the panel along the alignment axis; pass a
 *   negative value with [AnchorAlignment.End] to keep the panel off the
 *   anchor's trailing edge (a settings card's edge, for instance).
 * @param reserveLeadingIcon When true, every row keeps a leading icon slot
 *   even if it has no `leading` content, so labels stay aligned in menus
 *   that mix icon and text-only rows. Cascading levels inherit the setting.
 */
@Composable
public fun RuiDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    side: AnchorSide = AnchorSide.Bottom,
    autoFlip: Boolean = true,
    alignment: AnchorAlignment = AnchorAlignment.Start,
    sideOffset: Dp = 0.dp,
    alignmentOffset: Dp = 0.dp,
    reserveLeadingIcon: Boolean = false,
    backdrop: BlurBackdrop? = null,
    material: RuiGlassMaterial = RuiGlassMaterial.Frosted,
    anchor: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val colors = RuiTheme.colors
    val shapes = RuiTheme.shapes
    val motion = RuiTheme.motion
    val windowInfo = LocalWindowInfo.current
    val windowSize = windowInfo.containerDpSize
    val windowSizePx = windowInfo.containerSize
    val density = LocalDensity.current
    var anchorCenterY by remember { mutableStateOf<Float?>(null) }
    // A submenu level can be narrower than its parent. The panel width must
    // not snap down when the level transition drops the parent: remember the
    // widest width the panel has measured and keep it as the minimum.
    var widestPanelPx by remember { mutableIntStateOf(0) }
    val widestPanel = with(density) { widestPanelPx.toDp() }
    val resolvedSide = if (autoFlip) {
        autoFlippedMenuSide(side, anchorCenterY, windowSizePx.height)
    } else {
        side
    }

    // A menu is measured unconstrained by the anchoring machinery, so cap it
    // to the window's safe drawing area here and scroll the levels inside: a
    // long menu must stay reachable instead of running off the screen or
    // under the system bars/IME. The panel leaves a margin on every side,
    // like iOS's menus under the display edges.
    val layoutDirection = LocalLayoutDirection.current
    val safeDrawing = WindowInsets.safeDrawing.asPaddingValues()
    val panelMaxWidth = if (windowSize.width > 0.dp) {
        menuPanelMaxWidth(
            windowWidth = windowSize.width,
            safeStart = safeDrawing.calculateLeftPadding(layoutDirection),
            safeEnd = safeDrawing.calculateRightPadding(layoutDirection),
            margin = MenuPanelWindowMargin,
        )
    } else {
        null
    }
    val panelMaxHeight = if (windowSize.height > 0.dp) {
        menuPanelMaxHeight(
            windowHeight = windowSize.height,
            safeTop = safeDrawing.calculateTopPadding(),
            safeBottom = safeDrawing.calculateBottomPadding(),
            margin = MenuPanelWindowMargin,
        )
    } else {
        null
    }
    val panelSize = Modifier
        .onSizeChanged { widestPanelPx = maxOf(widestPanelPx, it.width) }
        .then(
            if (panelMaxWidth != null) {
                Modifier.widthIn(min = widestPanel.coerceAtMost(panelMaxWidth), max = panelMaxWidth)
            } else {
                Modifier.widthIn(min = widestPanel)
            },
        )
        .width(IntrinsicSize.Max)
        .then(if (panelMaxHeight != null) Modifier.heightIn(max = panelMaxHeight) else Modifier)

    // Menus float above the page: a soft shadow carries the depth, most
    // visibly in solid mode where the opaque panel otherwise melts into
    // the content behind it. The elevation and alphas stay below the
    // platform default so the panel does not read as a heavy drop shadow.
    // The shadow shape maps to a plain rounded rect (toShadowShape) —
    // HWUI renders path outlines' shadows on a per-frame software path,
    // which stutters the whole animation.
    val menuShadow = Modifier.shadow(
        elevation = MenuShadowElevation,
        shape = shapes.xl.toShadowShape(),
        clip = false,
        ambientColor = MenuShadowAmbient,
        spotColor = MenuShadowSpot,
    )

    val panelBackground = when {
        backdrop != null && material == RuiGlassMaterial.Liquid && backdrop.liquidSource != null && isBlurSupported ->
            Modifier.liquidGlassSurface(
                backdrop = backdrop,
                tint = colors.surfaceElevated,
                shape = shapes.xl,
            )
        backdrop != null && isBlurSupported ->
            Modifier.blurSurface(
                backdrop = backdrop,
                radius = 24.dp,
                tint = colors.surfaceElevated.copy(alpha = 0.72f),
                shape = shapes.xl,
            )
        else ->
            Modifier
                .clip(shapes.xl)
                .background(colors.surfaceElevated, shapes.xl)
    }

    UnstyledDropdownMenu(
        expanded = expanded,
        onExpandedChange = { if (!it) onDismissRequest() },
        modifier = modifier,
        side = resolvedSide,
        alignment = alignment,
        sideOffset = sideOffset,
        alignmentOffset = alignmentOffset,
        anchor = {
            Box(
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    if (coordinates.isAttached) {
                        anchorCenterY = coordinates.positionInWindow().y +
                            coordinates.size.height / 2f
                    }
                },
            ) {
                anchor()
            }
        },
        panel = {
            // Content-adaptive: the panel takes its content's MAX intrinsic
            // width — the widest option label on a single line — so long
            // options extend the menu instead of wrapping, up to the
            // window's width cap where they wrap. IntrinsicSize keeps
            // children measured with bounded width, so the weighted
            // cascading arrows keep working.
            DropdownMenuPanel(
                modifier = panelSize
                    .then(menuShadow)
                    .then(panelBackground),
                // Scale-in only; the collapse is a single-frame commit with
                // no exit animation at all. Both alpha fades (offscreen
                // compositing crops the shadow square — the snackbar's
                // lesson) and the scale-out (RenderNode re-projects the
                // elevation shadow every frame of the shrink, which
                // stuttered even with a convex outline) proved unusable
                // under this panel's shadow, so the close just commits.
                enter = scaleIn(
                    initialScale = 0.94f,
                    animationSpec = motion.pop,
                ),
                exit = ExitTransition.None,
            ) {
                val cascading = remember { RuiCascadingMenuState() }
                CompositionLocalProvider(
                    LocalRuiCascadingMenu provides cascading,
                    LocalRuiDropdownReserveLeadingIcon provides reserveLeadingIcon,
                ) {
                    MenuLevels(cascading = cascading, rootContent = content)
                }
            }
        },
    )
}

/**
 * One selectable row of a [RuiDropdownMenu]. The row spans the menu's width;
 * pass [leading] for an icon and [trailing] for a checkmark or shortcut.
 * Danger rows render in the destructive color, as on iOS. Selecting a row
 * closes the menu — [closeOnClick] = false keeps it open (how
 * [RuiDropdownSubmenu] pushes its level). When the menu sets
 * `reserveLeadingIcon`, rows without a leading view keep an empty icon slot
 * so labels stay aligned.
 */
@Composable
public fun RuiDropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    danger: Boolean = false,
    closeOnClick: Boolean = true,
) {
    val colors = RuiTheme.colors
    val typography = RuiTheme.typography
    val spacing = RuiTheme.spacing
    val reserveLeading = LocalRuiDropdownReserveLeadingIcon.current

    // No press indication: iOS menu rows don't paint a state layer — the
    // tap just commits.
    UnstyledDropdownMenuItem(
        onClick = onClick,
        enabled = enabled,
        closeOnClick = closeOnClick,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = RuiTheme.dimens.touchTarget)
                .padding(horizontal = RuiTheme.spacing.lg, vertical = RuiTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            if (reserveLeading) {
                Box(
                    modifier = Modifier.size(RuiTheme.dimens.iconSize),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    leading?.invoke()
                }
            } else if (leading != null) {
                leading()
            }
            RuiText(
                text = text,
                style = typography.bodyLg,
                color = when {
                    !enabled -> colors.contentDisabled
                    danger -> colors.danger
                    else -> colors.content
                },
                modifier = Modifier.weight(1f),
            )
            if (trailing != null) trailing()
        }
    }
}

/** Which side of the anchor the menu panel appears on (Unstyled's vocabulary). */
public typealias AnchorSide = com.composeunstyled.AnchorSide
/** How the menu panel aligns with the anchor along [AnchorSide] (Unstyled's vocabulary). */
public typealias AnchorAlignment = com.composeunstyled.AnchorAlignment

/**
 * The submenu path of one open [RuiDropdownMenu]. [RuiDropdownSubmenu] rows
 * push a level carrying their own children — the level owns its content, so
 * the row that pushed it leaving composition (as the panel's level
 * transition animates it out) cannot retract the push.
 *
 * Pure state, no composition: the path decisions are unit tested.
 */
internal class RuiCascadingMenuState {

    data class Level internal constructor(
        val id: Any,
        val title: String,
        val content: @Composable () -> Unit,
    )

    var path by mutableStateOf(emptyList<Level>())
        private set

    fun push(id: Any, title: String, content: @Composable () -> Unit) {
        path = path + Level(id, title, content)
    }

    fun pop() {
        path = path.dropLast(1)
    }
}

/** The open menu's cascade, readable by [RuiDropdownSubmenu] rows. */
internal val LocalRuiCascadingMenu = staticCompositionLocalOf<RuiCascadingMenuState?> { null }

/** Whether menu rows reserve an empty leading icon slot when they have none. */
internal val LocalRuiDropdownReserveLeadingIcon = staticCompositionLocalOf { false }

/**
 * The levels of one open menu: the caller's [rootContent] at the root, a
 * pushed submenu's children (under its back header) otherwise, sliding
 * forward on push and back on pop like an iOS context menu.
 */
@Composable
internal fun MenuLevels(
    cascading: RuiCascadingMenuState,
    rootContent: @Composable () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val forward = if (layoutDirection == LayoutDirection.Ltr) 1 else -1

    AnimatedContent(
        targetState = cascading.path.lastOrNull(),
        transitionSpec = {
            if (targetState != initialState) {
                // Push: the child slides in from the end while the parent
                // retreats toward the start, parallax like an iOS push.
                (slideInHorizontally { full -> forward * full } + fadeIn()) togetherWith
                    (slideOutHorizontally { full -> -forward * full / 3 } + fadeOut())
            } else {
                // Pop: the parent returns from the start.
                (slideInHorizontally { full -> -forward * full / 3 } + fadeIn()) togetherWith
                    (slideOutHorizontally { full -> forward * full } + fadeOut())
            }
        },
        label = "RuiDropdownMenuLevels",
        contentKey = { it?.id },
    ) { level ->
        // One scroll position per level: a long submenu scrolls on its own,
        // and returning to a level starts it at the top again.
        val scrollState = remember(level) { ScrollState(0) }
        Column(modifier = Modifier.verticalScroll(scrollState)) {
            if (level == null) {
                rootContent()
            } else {
                CascadingBackRow(title = level.title, onBack = { cascading.pop() })
                level.content()
            }
        }
    }
}

/**
 * The back header at the top of a pushed submenu level: the submenu's own
 * title with a leading back chevron; tapping it pops one level.
 */
@Composable
private fun CascadingBackRow(
    title: String,
    onBack: () -> Unit,
) {
    val colors = RuiTheme.colors
    val typography = RuiTheme.typography
    val spacing = RuiTheme.spacing

    UnstyledDropdownMenuItem(
        onClick = onBack,
        closeOnClick = false,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = RuiTheme.dimens.touchTarget)
                .padding(horizontal = RuiTheme.spacing.md, vertical = RuiTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            RuiIcon(imageVector = RuiIcons.ChevronLeft, contentDescription = null, tint = colors.contentMuted)
            RuiText(text = title, style = typography.bodyLg, color = colors.content)
        }
    }
    RuiDivider()
}

/**
 * A row that opens a submenu inside its [RuiDropdownMenu]: tapping it pushes
 * the panel to the next level, where [content]'s items (more submenus
 * included, to any depth) render under a back header. Rows must sit inside a
 * [RuiDropdownMenu]'s content — elsewhere the row renders but does nothing.
 *
 * Selecting an item at any level closes the whole menu; outside tap, back
 * and Esc close it from any depth too.
 *
 * @param text The row's label and the back header's title at the child level.
 * @param content The child level's items.
 */
@Composable
public fun RuiDropdownSubmenu(
    text: String,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val cascading = LocalRuiCascadingMenu.current
    val id = remember { Any() }

    RuiDropdownMenuItem(
        text = text,
        onClick = { cascading?.push(id, text, content) },
        modifier = modifier,
        leading = leading,
        enabled = enabled,
        closeOnClick = false,
        trailing = {
            RuiIcon(
                imageVector = RuiIcons.ChevronRight,
                contentDescription = null,
                tint = RuiTheme.colors.contentMuted,
            )
        },
    )
}
