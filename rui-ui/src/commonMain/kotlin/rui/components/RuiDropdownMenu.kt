package rui.components

import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.composeunstyled.DropdownMenuPanel
import com.composeunstyled.UnstyledDropdownMenu
import com.composeunstyled.UnstyledDropdownMenuItem
import com.composeunstyled.theme.ColoredIndication
import rui.foundation.BlurBackdrop
import rui.foundation.RuiText
import rui.foundation.blurSurface
import rui.foundation.platform.isBlurSupported
import rui.theme.RuiTheme
import rui.theme.StateLayerAlphas

/**
 * An iOS-style dropdown menu anchored to [anchor]: a floating frosted (or
 * elevated) panel with vertically arranged items. Dismisses on outside tap,
 * back/Esc, or selecting an item. Compose [RuiDropdownMenuItem] (and plain
 * [RuiDivider] where wanted) inside [content].
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
    anchor: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val colors = RuiTheme.colors
    val shapes = RuiTheme.shapes
    val motion = RuiTheme.motion

    val panelBackground = if (backdrop != null && isBlurSupported) {
        Modifier.blurSurface(
            backdrop = backdrop,
            radius = 24.dp,
            tint = colors.surfaceElevated.copy(alpha = 0.72f),
            shape = shapes.xl,
        )
    } else {
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
            DropdownMenuPanel(
                modifier = Modifier.widthIn(min = MenuMinWidth).then(panelBackground),
                enter = fadeIn(motion.normal) + scaleIn(
                    initialScale = 0.94f,
                    animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f),
                ),
                exit = fadeOut(motion.fast) + scaleOut(targetScale = 0.96f),
            ) {
                content()
            }
        },
    )
}

/**
 * One selectable row of a [RuiDropdownMenu]. The row spans the menu's width;
 * pass [leading] for an icon and [trailing] for a checkmark or shortcut.
 * Danger rows render in the destructive color, as on iOS.
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
