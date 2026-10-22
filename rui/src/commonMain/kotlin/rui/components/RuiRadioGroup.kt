package rui.components

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composeunstyled.RadioGroupScope
import com.composeunstyled.SelectedIndicator
import com.composeunstyled.UnstyledRadioButton
import com.composeunstyled.UnstyledRadioGroup
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiText
import rui.foundation.rememberRuiIndication
import rui.theme.RuiTheme

/**
 * An exclusive-selection group. Layout-agnostic — compose the rows inside any
 * container (the grouped-list card in the catalog): rows are typically
 * [RuiRadioRow]s separated by [RuiDivider]s. Group semantics (one selection,
 * arrow-key traversal) come from Unstyled's [UnstyledRadioGroup]; state is
 * hoisted.
 *
 * @param value The currently selected option.
 * @param onValueChange Called with the newly selected option.
 */
@Composable
public fun <T> RuiRadioGroup(
    value: T,
    onValueChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RadioGroupScope.() -> Unit,
) {
    UnstyledRadioGroup(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        content = content,
    )
}

/**
 * A selectable row of a [RuiRadioGroup], iOS settings style: title (and
 * optional subtitle) with a trailing check mark on the selected row. The
 * whole row is the hit target and carries the state layer.
 *
 * @param value The option this row represents.
 * @param title Row title, at the list title level.
 * @param subtitle Optional secondary line.
 * @param enabled When false, the row cannot be selected.
 */
@Composable
public fun <T> RadioGroupScope.RuiRadioRow(
    value: T,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
) {
    val colors = RuiTheme.colors
    val dimens = RuiTheme.dimens
    val spacing = RuiTheme.spacing
    val motion = RuiTheme.motion

    UnstyledRadioButton(
        value = value,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        indication = rememberRuiIndication(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = dimens.touchTarget)
                .padding(horizontal = spacing.lg, vertical = spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                RuiText(text = title, style = RuiTheme.typography.titleSm)
                if (subtitle != null) {
                    RuiText(
                        text = subtitle,
                        style = RuiTheme.typography.bodySm,
                        color = colors.contentMuted,
                    )
                }
            }
            // Reserved space keeps the row from shifting when the mark appears.
            SelectedIndicator(
                modifier = Modifier.size(dimens.iconSize),
                enter = fadeIn(motion.fast) + scaleIn(
                    initialScale = 0.6f,
                    animationSpec = motion.pop,
                ),
                exit = fadeOut(tween(durationMillis = 90)),
            ) {
                RuiIcon(
                    imageVector = RuiIcons.Check,
                    contentDescription = null,
                    tint = colors.primary,
                )
            }
        }
    }
}

/**
 * A standalone radio control: a circle that fills its ring with a dot when
 * selected — the circular sibling of the checkbox. Wraps the control in a
 * single-option group, so the radio role and selection semantics come from
 * Unstyled; the hit area covers the theme's touch target.
 *
 * @param selected Whether the radio is currently selected.
 * @param onSelected Called when the radio is selected (radios don't
 *   deselect on tap).
 * @param enabled When false, the radio cannot be interacted with.
 */
@Composable
public fun RuiRadioButton(
    selected: Boolean,
    onSelected: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = RuiTheme.colors
    val dimens = RuiTheme.dimens
    val motion = RuiTheme.motion

    UnstyledRadioGroup(
        value = selected,
        onValueChange = { if (it) onSelected?.invoke() },
        modifier = modifier,
    ) {
        UnstyledRadioButton(
            value = true,
            enabled = enabled && onSelected != null,
            modifier = Modifier.size(dimens.touchTarget),
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .border(
                            width = if (selected) 2.dp else dimens.borderWidth,
                            color = when {
                                selected -> colors.primary
                                enabled -> colors.outline
                                else -> colors.outlineSubtle
                            },
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    SelectedIndicator(
                        modifier = Modifier.size(10.dp),
                        enter = fadeIn(motion.fast) + scaleIn(
                            initialScale = 0.5f,
                            animationSpec = motion.pop,
                        ),
                        exit = fadeOut(tween(durationMillis = 90)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(colors.primary),
                        )
                    }
                }
            }
        }
    }
}
