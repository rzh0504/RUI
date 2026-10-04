package rui.components

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.AnchorAlignment
import com.composeunstyled.AnchorSide
import com.composeunstyled.TooltipPanel
import com.composeunstyled.UnstyledTooltip
import rui.foundation.RuiText
import rui.theme.RuiTheme

/**
 * Colors of [RuiTooltip]'s bubble. Defaults invert the scheme — `content` as
 * the container, `background` as the text color — the one sanctioned color
 * inversion in the system, so the bubble reads on any surface in either
 * appearance.
 */
@Immutable
public data class RuiTooltipColors(
    public val container: Color,
    public val content: Color,
)

public object RuiTooltipDefaults {

    /** Gap between the bubble and its anchor. */
    public val SideOffset: Dp = 8.dp

    /** Delay before hover shows the bubble (mouse/trackpad). */
    public const val HoverDelayMillis: Long = 300L

    @Composable
    public fun colors(
        container: Color = RuiTheme.colors.content,
        content: Color = RuiTheme.colors.background,
    ): RuiTooltipColors = RuiTooltipColors(
        container = container,
        content = content,
    )
}

/**
 * A floating bubble that explains its [anchor]: a short [text] placed next to
 * it on [side]. Behavior comes from Unstyled's [UnstyledTooltip] — it shows
 * on long-press (touch, ~1.5s, dismisses itself), on hover after a short
 * delay (mouse/trackpad) and instantly on keyboard focus; Escape dismisses.
 * Requires a `TooltipHost` ancestor (the app root usually provides one).
 *
 * The bubble is a small inverted panel: `content` container, `background`
 * text, the `sm` shape tier. Placement, screen-edge adjustment and the
 * live-region semantics come from the primitive.
 *
 * @param text The explanation shown in the bubble.
 * @param modifier Applied to the bubble panel.
 * @param side Which side of the anchor the bubble prefers.
 * @param alignment How the bubble aligns to the anchor on that side.
 * @param sideOffset Gap between the bubble and the anchor.
 * @param enabled When false, the tooltip never shows.
 * @param hoverDelayMillis Delay before hover shows the bubble.
 * @param anchor The element the tooltip explains and attaches to.
 */
@Composable
public fun RuiTooltip(
    text: String,
    modifier: Modifier = Modifier,
    side: AnchorSide = AnchorSide.Top,
    alignment: AnchorAlignment = AnchorAlignment.Center,
    sideOffset: Dp = RuiTooltipDefaults.SideOffset,
    enabled: Boolean = true,
    hoverDelayMillis: Long = RuiTooltipDefaults.HoverDelayMillis,
    anchor: @Composable () -> Unit,
) {
    val colors = RuiTooltipDefaults.colors()
    val motion = RuiTheme.motion

    UnstyledTooltip(
        enabled = enabled,
        side = side,
        alignment = alignment,
        sideOffset = sideOffset,
        hoverDelayMillis = hoverDelayMillis,
        panel = {
            TooltipPanel(
                modifier = modifier,
                enter = fadeIn(motion.fast),
                exit = fadeOut(motion.fast),
            ) { _ ->
                Box(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .background(colors.container, RuiTheme.shapes.sm)
                        .padding(horizontal = RuiTheme.spacing.sm, vertical = RuiTheme.spacing.xs),
                ) {
                    RuiText(
                        text = text,
                        style = RuiTheme.typography.bodySm,
                        color = colors.content,
                    )
                }
            }
        },
        anchor = anchor,
    )
}
