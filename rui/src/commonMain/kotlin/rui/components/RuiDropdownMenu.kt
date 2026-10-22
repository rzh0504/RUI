package rui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalLayoutDirection
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
 * @param side Which side of the anchor the panel appears on.
 * @param alignment How the panel aligns with the anchor along that side.
 * @param sideOffset Air between the anchor and the panel's near edge.
 * @param alignmentOffset Shifts the panel along the alignment axis; pass a
 *   negative value with [AnchorAlignment.End] to keep the panel off the
 *   anchor's trailing edge (a settings card's edge, for instance).
 */
@Composable
public fun RuiDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    side: AnchorSide = AnchorSide.Bottom,
    alignment: AnchorAlignment = AnchorAlignment.Start,
    sideOffset: Dp = 0.dp,
    alignmentOffset: Dp = 0.dp,
    backdrop: BlurBackdrop? = null,
    material: RuiGlassMaterial = RuiGlassMaterial.Frosted,
    anchor: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val colors = RuiTheme.colors
    val shapes = RuiTheme.shapes
    val motion = RuiTheme.motion

    // Menus float above the page: the shadow carries the depth, most
    // visibly in solid mode where the opaque panel otherwise melts into
    // the content behind it. The shadow shape maps to a plain rounded
    // rect (toShadowShape) — HWUI renders path outlines' shadows on a
    // per-frame software path, which stutters the whole animation.
    val menuShadow = Modifier.shadow(
        elevation = 8.dp,
        shape = shapes.xl.toShadowShape(),
        clip = false,
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
        side = side,
        alignment = alignment,
        sideOffset = sideOffset,
        alignmentOffset = alignmentOffset,
        anchor = anchor,
        panel = {
            // Content-adaptive: the panel takes its content's MAX intrinsic
            // width — the widest option label on a single line — so long
            // options extend the menu instead of wrapping. IntrinsicSize
            // keeps children measured with bounded width, so the weighted
            // cascading arrows keep working.
            DropdownMenuPanel(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
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
                CompositionLocalProvider(LocalRuiCascadingMenu provides cascading) {
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
 * [RuiDropdownSubmenu] pushes its level).
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
            if (leading != null) leading()
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
        Column {
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
