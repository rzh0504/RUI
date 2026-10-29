package rui.components

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composeunstyled.Scrim
import com.composeunstyled.Sheet
import com.composeunstyled.SheetDetent
import com.composeunstyled.UnstyledModalBottomSheet
import com.composeunstyled.rememberModalBottomSheetState
import rui.foundation.BlurBackdrop
import rui.foundation.RuiIcon
import rui.foundation.RuiText
import rui.foundation.blurSurface
import rui.foundation.platform.isBlurSupported
import rui.foundation.rememberRuiIndication
import rui.theme.RuiTheme

/**
 * One option of a [RuiActionPanel].
 *
 * @param label The action text.
 * @param icon Optional leading icon.
 * @param danger Renders the option in the danger color — destructive actions.
 * @param enabled When false, the option cannot be selected.
 * @param onSelect Called after the panel starts closing.
 */
public data class RuiActionOption(
    public val label: String,
    public val icon: ImageVector? = null,
    public val danger: Boolean = false,
    public val enabled: Boolean = true,
    public val onSelect: () -> Unit,
)

/**
 * A modal bottom panel for choosing among [actions] — Android's bottom-sheet
 * dialog interaction model (scrim/back dismiss, bottom presentation) rendered
 * with the sheet machinery and visual of this system: grabber, centered
 * title, hairline-separated option rows, and an optional explicit cancel row.
 * Selecting an option closes the panel and reports through [RuiActionOption.onSelect].
 *
 * Give it the screen's [backdrop] to render the frosted glass material. The
 * app root should compose [RuiOverlayHost] (the catalog does).
 *
 * @param visible Whether the panel is showing; the caller owns this state.
 * @param onDismiss Called for every dismiss path (scrim, back, drag, selection).
 * @param title Centered title above the options.
 * @param message Optional centered secondary line.
 * @param actions The options, top to bottom.
 * @param cancelLabel Optional explicit cancel row below the options; the
 *   scrim, back and grabber drag always dismiss regardless.
 */
@Composable
public fun RuiActionPanel(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    actions: List<RuiActionOption>,
    modifier: Modifier = Modifier,
    message: String? = null,
    cancelLabel: String? = null,
    backdrop: BlurBackdrop? = null,
) {
    val colors = RuiTheme.colors
    val typography = RuiTheme.typography
    val spacing = RuiTheme.spacing
    val motion = RuiTheme.motion
    val isVisible by rememberUpdatedState(visible)

    WarnWithoutRuiOverlayHost("RuiActionPanel")

    val state = rememberModalBottomSheetState(
        initialDetent = SheetDetent.Hidden,
        animationSpec = motion.navigation,
    )

    LaunchedEffect(visible) {
        state.targetDetent = if (visible) SheetDetent.FullyExpanded else SheetDetent.Hidden
    }
    // Only a return to Hidden after having left it counts as a drag dismissal —
    // the sheet starts AT Hidden (same contract as RuiBottomSheet).
    LaunchedEffect(state) {
        var entered = false
        snapshotFlow { state.currentDetent }.collect { detent ->
            if (detent != SheetDetent.Hidden) {
                entered = true
            } else if (entered) {
                entered = false
                if (isVisible) onDismiss()
            }
        }
    }

    val shapes = RuiTheme.shapes
    val sheetBackground = if (backdrop != null && isBlurSupported) {
        Modifier.blurSurface(
            backdrop = backdrop,
            radius = 24.dp,
            tint = colors.surfaceElevated.copy(alpha = 0.72f),
            shape = shapes.sheet,
            positionOverride = {
                Offset(0f, backdrop.graphicsLayer.size.height.toFloat() - state.bottomSheetState.offset)
            },
        )
    } else {
        Modifier.background(colors.surfaceElevated, shapes.sheet)
    }

    UnstyledModalBottomSheet(
        state = state,
        onDismiss = onDismiss,
        overlay = {
            Scrim(
                scrimColor = colors.scrim,
                enter = fadeIn(motion.normal),
                exit = fadeOut(motion.fast),
            )
        },
    ) {
        Sheet(modifier = modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().then(sheetBackground)) {
                Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = spacing.sm, bottom = spacing.xs),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 36.dp, height = 5.dp)
                                .background(colors.outline, CircleShape),
                        )
                    }
                    RuiText(
                        text = title,
                        style = typography.titleMd,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.xl),
                    )
                    if (message != null) {
                        RuiText(
                            text = message,
                            style = typography.bodySm,
                            color = colors.contentMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.xl)
                                .padding(top = spacing.xs),
                        )
                    }
                    Column(modifier = Modifier.padding(top = spacing.md)) {
                        actions.forEachIndexed { index, action ->
                            if (index > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 1.dp)
                                        .background(colors.outlineSubtle),
                                )
                            }
                            ActionRow(
                                action = action,
                                onDismiss = onDismiss,
                            )
                        }
                    }
                    if (cancelLabel != null) {
                        ActionRow(
                            action = RuiActionOption(
                                label = cancelLabel,
                                // The row dismisses before reporting; the
                                // cancel option is the dismiss label, not a
                                // second dismiss call.
                                onSelect = {},
                            ),
                            onDismiss = onDismiss,
                            modifier = Modifier.padding(top = spacing.sm),
                            emphasized = true,
                        )
                    }
                }
            }
        }
    }
}

/**
 * One full-bleed option row: optional leading icon, label, state layer.
 * Selecting closes the panel first, then reports the action.
 */
@Composable
private fun ActionRow(
    action: RuiActionOption,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
) {
    val colors = RuiTheme.colors
    val spacing = RuiTheme.spacing
    val interactionSource = remember { MutableInteractionSource() }
    val indication = rememberRuiIndication()
    val contentColor = when {
        !action.enabled -> colors.contentMuted
        action.danger -> colors.danger
        else -> colors.content
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = RuiTheme.dimens.touchTarget)
            .then(
                if (action.enabled) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = indication,
                        onClick = { onDismiss(); action.onSelect() },
                    )
                } else {
                    Modifier
                },
            )
            .padding(horizontal = spacing.lg, vertical = spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        if (action.icon != null) {
            RuiIcon(
                imageVector = action.icon,
                contentDescription = null,
                tint = contentColor,
            )
        }
        RuiText(
            text = action.label,
            style = if (emphasized) RuiTheme.typography.titleSm else RuiTheme.typography.bodyMd,
            color = contentColor,
        )
    }
}
