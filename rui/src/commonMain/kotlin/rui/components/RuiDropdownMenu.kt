package rui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.composeunstyled.DropdownMenuPanel
import com.composeunstyled.UnstyledDropdownMenu
import com.composeunstyled.UnstyledDropdownMenuItem
import com.composeunstyled.theme.ColoredIndication
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
import rui.theme.StateLayerAlphas

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
 */
@Composable
public fun RuiDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    side: AnchorSide = AnchorSide.Bottom,
    alignment: AnchorAlignment = AnchorAlignment.Start,
    backdrop: BlurBackdrop? = null,
    material: RuiGlassMaterial = RuiGlassMaterial.Frosted,
    anchor: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val colors = RuiTheme.colors
    val shapes = RuiTheme.shapes
    val motion = RuiTheme.motion

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
        anchor = anchor,
        panel = {
            // Fixed width: the panel layer measures content with unbounded
            // constraints, so min-width would leave item rows unbounded too
            // (weighted text collapses). Same approach as Unstyled's demo.
            DropdownMenuPanel(
                modifier = Modifier.width(MenuMinWidth).then(panelBackground),
                enter = fadeIn(motion.normal) + scaleIn(
                    initialScale = 0.94f,
                    animationSpec = motion.pop,
                ),
                exit = fadeOut(motion.fast) + scaleOut(targetScale = 0.96f),
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
    val motion = RuiTheme.motion
    val indication = remember(colors.content, motion) {
        ColoredIndication(
            hoveredColor = colors.content.copy(alpha = StateLayerAlphas.hovered),
            pressedColor = colors.content.copy(alpha = StateLayerAlphas.pressed),
            focusedColor = colors.content.copy(alpha = StateLayerAlphas.focused),
            draggedColor = colors.content.copy(alpha = StateLayerAlphas.dragged),
            animationSpecEnter = motion.stateLayerIn,
            animationSpecExit = motion.stateLayerOut,
        )
    }

    UnstyledDropdownMenuItem(
        onClick = onClick,
        enabled = enabled,
        closeOnClick = closeOnClick,
        indication = indication,
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

/** iOS pull-down menus are a fixed 250pt wide; honor that as the panel minimum. */
private val MenuMinWidth = 250.dp

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
    val motion = RuiTheme.motion
    val indication = remember(colors.content, motion) {
        ColoredIndication(
            hoveredColor = colors.content.copy(alpha = StateLayerAlphas.hovered),
            pressedColor = colors.content.copy(alpha = StateLayerAlphas.pressed),
            focusedColor = colors.content.copy(alpha = StateLayerAlphas.focused),
            draggedColor = colors.content.copy(alpha = StateLayerAlphas.dragged),
            animationSpecEnter = motion.stateLayerIn,
            animationSpecExit = motion.stateLayerOut,
        )
    }

    UnstyledDropdownMenuItem(
        onClick = onBack,
        closeOnClick = false,
        indication = indication,
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
