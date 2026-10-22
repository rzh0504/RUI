package rui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import rui.foundation.RuiIcon
import rui.foundation.RuiText
import rui.theme.RuiTheme

/** Diameter of the muted circle behind the icon. */
private val EmptyIconCircle = 56.dp

/**
 * The quiet placeholder for a screen or region with nothing to show: an
 * optional muted glyph in a circle, a headline, an optional supporting
 * message and one suggested action. The stack centers itself in whatever
 * canvas the caller gives it — `Modifier.fillMaxSize()` for a whole screen,
 * a fixed height for a region inside a card.
 *
 * @param title The headline — what is empty.
 * @param message Optional supporting line — why it is empty, what to do next.
 * @param icon Optional glyph, rendered muted inside a `surfaceMuted` circle
 *   (the avatar fallback's visual sibling).
 * @param actionLabel Optional label of the single suggested action; rendered
 *   as a tonal button.
 * @param onAction Called when the action is tapped.
 */
@Composable
public fun RuiEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = RuiTheme.colors
    val spacing = RuiTheme.spacing
    val typography = RuiTheme.typography

    Column(
        modifier = modifier.padding(spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(EmptyIconCircle)
                    .background(colors.surfaceMuted, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                RuiIcon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.contentMuted,
                )
            }
        }
        RuiText(
            text = title,
            style = typography.titleMd,
            textAlign = TextAlign.Center,
            modifier = if (icon != null) Modifier.padding(top = spacing.lg) else Modifier,
        )
        if (message != null) {
            RuiText(
                text = message,
                style = typography.bodySm,
                color = colors.contentMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = spacing.sm),
            )
        }
        if (actionLabel != null && onAction != null) {
            RuiButton(
                onClick = onAction,
                variant = RuiButtonVariant.Tonal,
                modifier = Modifier.padding(top = spacing.lg),
            ) {
                RuiText(actionLabel)
            }
        }
    }
}
