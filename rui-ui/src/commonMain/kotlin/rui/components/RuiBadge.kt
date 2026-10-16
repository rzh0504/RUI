package rui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import rui.foundation.RuiText
import rui.theme.RuiTheme

/**
 * Colors of [RuiBadge]. The default container is `danger` — notification
 * badges read as alerts — with white content in both appearances (like the
 * slider's thumb, the white is deliberate).
 */
@Immutable
public data class RuiBadgeColors(
    public val container: Color,
    public val content: Color,
)

public object RuiBadgeDefaults {

    /** Diameter of the dot badge (no text). */
    public val DotSize: Dp = 6.dp

    /** Minimum width and height of the text badge. */
    public val MinSize: Dp = 16.dp

    /** Horizontal air between the text and the pill's ends. */
    public val HorizontalPadding: Dp = 4.dp

    @Composable
    public fun colors(
        container: Color = RuiTheme.colors.danger,
        content: Color = Color.White,
    ): RuiBadgeColors = RuiBadgeColors(
        container = container,
        content = content,
    )
}

// Offsets of [RuiBadgedBox], following the Material 3 placement miuix
// implements: the dot tucks into the anchor's top-end corner, the text pill
// overlaps that corner and protrudes beyond it.
private val DotOffset = 6.dp
private val WithContentHorizontalOffset = 12.dp
private val WithContentVerticalOffset = 14.dp

private const val AnchorId = "rui_badge_anchor"
private const val BadgeId = "rui_badge_badge"

/**
 * A small count or status marker: a dot when [text] is null, otherwise a
 * pill sized to its label. Position it over the anchored element with
 * [RuiBadgedBox]; a bare [RuiBadge] is for deliberate placements the box
 * can't express — a dot centered on an avatar's bottom-end corner. The pill
 * uses a plain (unsmoothed) stadium: at this size corner smoothing is
 * illegible.
 *
 * @param text The count or short label; `null` renders the dot.
 */
@Composable
public fun RuiBadge(
    text: String? = null,
    modifier: Modifier = Modifier,
    colors: RuiBadgeColors = RuiBadgeDefaults.colors(),
) {
    val shape = if (text == null) CircleShape else RoundedCornerShape(percent = 50)

    Box(
        modifier = modifier
            .then(
                if (text == null) {
                    Modifier.size(RuiBadgeDefaults.DotSize)
                } else {
                    Modifier.defaultMinSize(
                        minWidth = RuiBadgeDefaults.MinSize,
                        minHeight = RuiBadgeDefaults.MinSize,
                    )
                },
            )
            .background(colors.container, shape)
            .then(
                if (text == null) {
                    Modifier
                } else {
                    Modifier.padding(horizontal = RuiBadgeDefaults.HorizontalPadding)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (text != null) {
            RuiText(
                text = text,
                style = RuiTheme.typography.labelSm,
                color = colors.content,
            )
        }
    }
}

/**
 * Lays out [badge] against [content] the way iOS positions notification
 * markers: the anchor keeps its own size and baselines while the badge hangs
 * off its top-end corner — a dot tucks into the corner, a text pill overlaps
 * the corner and protrudes beyond it. (Placement math from miuix's
 * BadgedBox, following Material 3 — see NOTICE.)
 */
@Composable
public fun RuiBadgedBox(
    badge: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Layout(
        modifier = modifier,
        content = {
            Box(
                modifier = Modifier.layoutId(AnchorId),
                contentAlignment = Alignment.Center,
                content = content,
            )
            Box(modifier = Modifier.layoutId(BadgeId), content = badge)
        },
    ) { measurables, constraints ->
        val badgePlaceable = measurables.first { it.layoutId == BadgeId }.measure(
            // Loose height: the badge never stretches to the anchor's.
            constraints.copy(minHeight = 0),
        )
        val anchorPlaceable = measurables.first { it.layoutId == AnchorId }.measure(constraints)

        val hasContent = badgePlaceable.width > RuiBadgeDefaults.DotSize.roundToPx()
        val horizontalOffset = if (hasContent) WithContentHorizontalOffset else DotOffset
        val verticalOffset = if (hasContent) WithContentVerticalOffset else DotOffset
        val badgeX = anchorPlaceable.width - horizontalOffset.roundToPx()
        val badgeY = -badgePlaceable.height + verticalOffset.roundToPx()

        layout(
            width = anchorPlaceable.width,
            height = anchorPlaceable.height,
            // Baselines come from the anchor alone so badge text never
            // shifts a surrounding row's alignment.
            alignmentLines = mapOf(
                FirstBaseline to anchorPlaceable[FirstBaseline],
                LastBaseline to anchorPlaceable[LastBaseline],
            ),
        ) {
            anchorPlaceable.placeRelative(0, 0)
            badgePlaceable.placeRelative(badgeX, badgeY)
        }
    }
}
