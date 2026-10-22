package rui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import rui.theme.RuiTheme

/** Diameter of one page dot; the active page stretches to [ActiveWidth]. */
private val DotSize = 6.dp

/** Width of the active page's pill. */
private val ActiveWidth = 18.dp

/**
 * The active width morphs with a Dp spring mirroring the [rui.theme.RuiMotion.pop]
 * values — the same local mirror the switch thumb uses for its Dp animation.
 */
private val PillSpring = spring<Dp>(
    dampingRatio = 0.85f,
    stiffness = 450f,
)

/**
 * The page position dots for paged content, iOS style: quiet dots in
 * `outline`, the current page stretched into a primary pill. Pass
 * [onPageSelect] to make the dots tappable (each carries selected semantics).
 * The current page is hoisted — wire it to a pager's state.
 */
@Composable
public fun RuiPageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    onPageSelect: ((Int) -> Unit)? = null,
) {
    require(pageCount > 0) { "pageCount must be positive, was $pageCount" }
    val colors = RuiTheme.colors
    val spacing = RuiTheme.spacing
    val current = currentPage.coerceIn(0, pageCount - 1)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        repeat(pageCount) { index ->
            val active = index == current
            Box(
                modifier = Modifier
                    .then(
                        if (onPageSelect != null) {
                            Modifier.selectable(
                                selected = active,
                                interactionSource = null,
                                indication = null,
                                role = Role.Tab,
                                onClick = { onPageSelect(index) },
                            )
                        } else {
                            Modifier
                        },
                    )
                    // Widen the touch area around the small visual.
                    .padding(horizontal = spacing.xs, vertical = spacing.sm),
                contentAlignment = Alignment.Center,
            ) {
                val width by animateDpAsState(
                    targetValue = if (active) ActiveWidth else DotSize,
                    animationSpec = PillSpring,
                    label = "RuiPageIndicatorWidth",
                )
                Box(
                    modifier = Modifier
                        .width(width)
                        .height(DotSize)
                        .clip(CircleShape)
                        .background(if (active) colors.primary else colors.outline),
                )
            }
        }
    }
}
