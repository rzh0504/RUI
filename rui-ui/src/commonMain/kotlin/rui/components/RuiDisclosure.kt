package rui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import com.composeunstyled.DisclosureButton
import com.composeunstyled.DisclosedContent
import com.composeunstyled.UnstyledDisclosure
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiSurface
import rui.foundation.RuiText
import rui.foundation.rememberRuiIndication
import rui.theme.RuiTheme

/**
 * An expandable section: a grouped card whose header row toggles a content
 * area revealed in place. Behavior and the expand/collapse semantics come
 * from Unstyled's [UnstyledDisclosure]; the card, the rotating chevron and
 * the motion come from Rui's tokens.
 *
 * State is hoisted — pass [expanded] and [onExpandedChange].
 *
 * @param title Header row label, at the list title level.
 * @param expanded Whether the content area is currently revealed.
 * @param onExpandedChange Called with the new expanded state when the header
 *   is activated.
 * @param content The revealed area, laid out in a [ColumnScope].
 */
@Composable
public fun RuiDisclosure(
    title: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = RuiTheme.colors
    val spacing = RuiTheme.spacing
    val motion = RuiTheme.motion

    UnstyledDisclosure(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier,
    ) {
        RuiSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RuiTheme.shapes.md,
        ) {
            Column {
                DisclosureButton(
                    modifier = Modifier.fillMaxWidth(),
                    indication = rememberRuiIndication(),
                    interactionSource = null,
                    contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.md),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.md),
                    ) {
                        RuiText(
                            text = title,
                            style = RuiTheme.typography.titleSm,
                            color = colors.content,
                            modifier = Modifier.weight(1f),
                        )
                        val chevronRotation by animateFloatAsState(
                            targetValue = if (expanded) 90f else 0f,
                            animationSpec = motion.normal,
                            label = "RuiDisclosureChevron",
                        )
                        RuiIcon(
                            imageVector = RuiIcons.ChevronRight,
                            contentDescription = null,
                            tint = colors.contentDisabled,
                            modifier = Modifier.rotate(chevronRotation),
                        )
                    }
                }
                DisclosedContent(
                    // Size animations take IntSize specs; the duration matches
                    // RuiTheme.motion.fast.
                    enter = expandVertically(tween(durationMillis = 120)) + fadeIn(motion.fast),
                    exit = shrinkVertically(tween(durationMillis = 120)) + fadeOut(motion.fast),
                ) {
                    Column(
                        modifier = Modifier.padding(start = spacing.lg, end = spacing.lg, bottom = spacing.md),
                        content = content,
                    )
                }
            }
        }
    }
}
