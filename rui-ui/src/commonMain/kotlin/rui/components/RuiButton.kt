package rui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.LocalContentColor
import com.composeunstyled.UnstyledButton
import com.composeunstyled.theme.ColoredIndication
import rui.foundation.ruiFocusRing
import rui.theme.RuiTheme
import rui.theme.StateLayerAlphas

/** Visual weight of a [RuiButton]. */
public enum class RuiButtonVariant {
    /** Filled with the accent color — the one main action on a screen. */
    Primary,

    /** Filled with a neutral muted color — secondary actions. */
    Tonal,

    /** No fill — inline and low-emphasis actions. */
    Plain,

    /** Filled with the danger color — destructive actions. */
    Danger,
}

/** Height and padding tier of a [RuiButton]. */
public enum class RuiButtonSize { Sm, Md, Lg }

@Immutable
public data class RuiButtonColors(
    public val container: Color,
    public val content: Color,
)

public object RuiButtonDefaults {
    @Composable
    public fun colors(
        variant: RuiButtonVariant,
        enabled: Boolean = true,
    ): RuiButtonColors {
        val colors = RuiTheme.colors
        val disabledContainer = colors.surfaceMuted
        val disabledContent = colors.contentDisabled

        return when (variant) {
            RuiButtonVariant.Primary ->
                if (enabled) RuiButtonColors(colors.primary, colors.onPrimary)
                else RuiButtonColors(disabledContainer, disabledContent)

            RuiButtonVariant.Tonal ->
                if (enabled) RuiButtonColors(colors.surfaceMuted, colors.content)
                else RuiButtonColors(disabledContainer, disabledContent)

            RuiButtonVariant.Plain ->
                RuiButtonColors(Color.Transparent, if (enabled) colors.content else disabledContent)

            RuiButtonVariant.Danger ->
                if (enabled) RuiButtonColors(colors.danger, Color.White)
                else RuiButtonColors(disabledContainer, disabledContent)
        }
    }

    public fun minHeight(size: RuiButtonSize): Dp = when (size) {
        RuiButtonSize.Sm -> 32.dp
        RuiButtonSize.Md -> 40.dp
        RuiButtonSize.Lg -> 48.dp
    }

    public fun contentPadding(size: RuiButtonSize): PaddingValues = when (size) {
        RuiButtonSize.Sm -> PaddingValues(horizontal = 12.dp)
        RuiButtonSize.Md -> PaddingValues(horizontal = 16.dp)
        RuiButtonSize.Lg -> PaddingValues(horizontal = 20.dp)
    }

    @Composable
    public fun textStyle(size: RuiButtonSize): TextStyle = when (size) {
        RuiButtonSize.Sm -> RuiTheme.typography.labelMd
        RuiButtonSize.Md -> RuiTheme.typography.labelLg
        RuiButtonSize.Lg -> RuiTheme.typography.labelLg
    }
}

/**
 * The Rui push button: a token-driven skin over Unstyled's behavior (role,
 * keyboard activation, focus). Filled variants carry the hierarchy; no
 * borders — emphasis comes from fill and content weight.
 */
@Composable
public fun RuiButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: RuiButtonVariant = RuiButtonVariant.Primary,
    size: RuiButtonSize = RuiButtonSize.Md,
    enabled: Boolean = true,
    shape: Shape = RuiTheme.shapes.md,
    colors: RuiButtonColors = RuiButtonDefaults.colors(variant, enabled),
    interactionSource: MutableInteractionSource? = null,
    contentPadding: PaddingValues = RuiButtonDefaults.contentPadding(size),
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val motion = RuiTheme.motion

    val stateLayer = remember(colors.content, motion) {
        ColoredIndication(
            hoveredColor = colors.content.copy(alpha = StateLayerAlphas.hovered),
            pressedColor = colors.content.copy(alpha = StateLayerAlphas.pressed),
            focusedColor = colors.content.copy(alpha = StateLayerAlphas.focused),
            draggedColor = colors.content.copy(alpha = StateLayerAlphas.dragged),
            animationSpecEnter = motion.stateLayerIn,
            animationSpecExit = motion.stateLayerOut,
        )
    }

    UnstyledButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .heightIn(min = RuiButtonDefaults.minHeight(size))
            .clip(shape)
            .background(colors.container, shape)
            .ruiFocusRing(resolvedInteractionSource, shape),
        role = Role.Button,
        indication = stateLayer,
        interactionSource = resolvedInteractionSource,
    ) {
        CompositionLocalProvider(LocalContentColor provides colors.content) {
            Row(
                modifier = Modifier.padding(contentPadding),
                horizontalArrangement = Arrangement.spacedBy(RuiTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leading?.invoke()
                content()
                trailing?.invoke()
            }
        }
    }
}
