package rui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.composeunstyled.theme.ColoredIndication
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiSurface
import rui.foundation.RuiText
import rui.foundation.rememberRuiIndication
import rui.theme.RuiTheme
import rui.theme.StateLayerAlphas

/** How a [RuiInlineBanner] reads. */
public enum class RuiInlineBannerVariant {

    /** Quiet notice: muted strip, standard content color. */
    Neutral,

    /** Error or blocking problem: `danger` fill with white content. */
    Danger,
}

/**
 * Colors of [RuiInlineBanner]. The danger variant uses the `danger` fill with
 * white content in both appearances — the badge's fixed white, deliberate.
 */
@Immutable
public data class RuiInlineBannerColors(
    public val container: Color,
    public val content: Color,
)

public object RuiInlineBannerDefaults {

    @Composable
    public fun colors(
        variant: RuiInlineBannerVariant = RuiInlineBannerVariant.Neutral,
    ): RuiInlineBannerColors = when (variant) {
        RuiInlineBannerVariant.Neutral ->
            RuiInlineBannerColors(RuiTheme.colors.surfaceMuted, RuiTheme.colors.content)
        RuiInlineBannerVariant.Danger ->
            RuiInlineBannerColors(RuiTheme.colors.danger, Color.White)
    }
}

/**
 * An in-flow notice strip — the snackbar's complement. The snackbar floats
 * above everything and times out; the banner sits inside the content flow and
 * stays until the caller stops composing it or the user dismisses it, for
 * conditions that outlive a moment (offline, sync failure, a blocked state).
 *
 * Neutral banners are a muted strip; [RuiInlineBannerVariant.Danger] uses the
 * danger fill with white content. The optional [icon], [actionLabel] action
 * and dismiss affordance all render in the banner's content color. Announced
 * politely to screen readers on appearance.
 *
 * @param message The notice text.
 * @param icon Optional leading icon; decorative, pass `null` to omit.
 * @param actionLabel Optional action ("Retry"); rendered only with [onAction].
 * @param onDismiss When set, a small close affordance is shown at the end.
 */
@Composable
public fun RuiInlineBanner(
    message: String,
    modifier: Modifier = Modifier,
    variant: RuiInlineBannerVariant = RuiInlineBannerVariant.Neutral,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    colors: RuiInlineBannerColors = RuiInlineBannerDefaults.colors(variant),
) {
    val spacing = RuiTheme.spacing

    RuiSurface(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        shape = RuiTheme.shapes.md,
        color = colors.container,
        contentColor = colors.content,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            if (icon != null) {
                RuiIcon(imageVector = icon, contentDescription = null)
            }
            RuiText(
                text = message,
                style = RuiTheme.typography.bodySm,
                modifier = Modifier.weight(1f),
            )
            if (actionLabel != null && onAction != null) {
                BannerAction(
                    label = actionLabel,
                    color = colors.content,
                    onClick = onAction,
                )
            }
            if (onDismiss != null) {
                DismissButton(onClick = onDismiss)
            }
        }
    }
}

/**
 * The banner's action: a plain content-colored text. It measures like the
 * message line — a full [RuiButton] reserves the touch-target token in its
 * layout slot and balloons a one-line banner — while the state layer still
 * reads over the strip.
 */
@Composable
private fun BannerAction(
    label: String,
    color: Color,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val spacing = RuiTheme.spacing
    val motion = RuiTheme.motion
    val indication = remember(color, motion) {
        ColoredIndication(
            hoveredColor = color.copy(alpha = StateLayerAlphas.hovered),
            pressedColor = color.copy(alpha = StateLayerAlphas.pressed),
            focusedColor = color.copy(alpha = StateLayerAlphas.focused),
            draggedColor = color.copy(alpha = StateLayerAlphas.dragged),
            animationSpecEnter = motion.stateLayerIn,
            animationSpecExit = motion.stateLayerOut,
        )
    }
    Box(
        modifier = Modifier
            .clip(RuiTheme.shapes.full)
            .clickable(
                interactionSource = interactionSource,
                indication = indication,
                onClickLabel = label,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = spacing.sm, vertical = spacing.xs),
        contentAlignment = Alignment.Center,
    ) {
        RuiText(text = label, style = RuiTheme.typography.labelMd, color = color)
    }
}

/**
 * Icon-sized dismiss affordance — the same 20dp footprint as the search
 * bar's clear button, so it never drives the banner's height.
 */
@Composable
private fun DismissButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val dismissLabel = RuiTheme.strings.dismiss
    Box(
        modifier = Modifier
            .size(RuiTheme.dimens.iconSize)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRuiIndication(),
                onClickLabel = dismissLabel,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        RuiIcon(
            imageVector = RuiIcons.Close,
            contentDescription = dismissLabel,
        )
    }
}
