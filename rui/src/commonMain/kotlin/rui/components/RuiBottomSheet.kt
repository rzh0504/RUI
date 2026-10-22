package rui.components

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composeunstyled.Scrim
import com.composeunstyled.Sheet
import com.composeunstyled.SheetDetent
import com.composeunstyled.UnstyledModalBottomSheet
import com.composeunstyled.rememberModalBottomSheetState
import rui.foundation.BlurBackdrop
import rui.foundation.LiquidGlassTintAlpha
import rui.foundation.RuiGlassMaterial
import rui.foundation.RuiText
import rui.foundation.blurSurface
import rui.foundation.liquidGlassSurface
import rui.foundation.liquidSource
import rui.foundation.platform.isBlurSupported
import rui.theme.RuiTheme

/**
 * An iOS-style modal bottom sheet: a grabber handle, an optional centered
 * title, and [content], docked to the screen's bottom edge on the `sheet`
 * corner token (rounded top, square edge). Enter/dismiss are driven by the
 * detent system — drag the sheet down, tap the scrim or press back to
 * dismiss; drags report through [onDismiss] like the other dismiss paths.
 *
 * Give it the screen's [backdrop] to render glass — frosted by default, or
 * the liquid-glass material with `material = RuiGlassMaterial.Liquid` (the
 * capture must have been created with `rememberBlurBackdrop(liquid = true)`);
 * without one the sheet falls back to an opaque elevated surface.
 *
 * The app root should compose [RuiOverlayHost] so sheets portal above
 * everything — the same host dialogs use; without one the platform modal
 * fallback logs a setup note pointing there.
 *
 * @param visible Whether the sheet is showing; the caller owns this state.
 * @param onDismiss Called for every dismiss path (scrim, back, drag-to-hide).
 * @param title Optional centered title below the grabber.
 */
@Composable
public fun RuiBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String? = null,
    modifier: Modifier = Modifier,
    backdrop: BlurBackdrop? = null,
    material: RuiGlassMaterial = RuiGlassMaterial.Frosted,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = RuiTheme.colors
    val typography = RuiTheme.typography
    val spacing = RuiTheme.spacing
    val motion = RuiTheme.motion
    val isVisible by rememberUpdatedState(visible)

    WarnWithoutRuiOverlayHost("RuiBottomSheet")

    val state = rememberModalBottomSheetState(
        initialDetent = SheetDetent.Hidden,
        animationSpec = motion.navigation,
    )

    // Drive the detents from the caller's visibility flag…
    LaunchedEffect(visible) {
        state.targetDetent = if (visible) SheetDetent.FullyExpanded else SheetDetent.Hidden
    }
    // …and report drag-to-hidden back to the caller, like scrim/back do. The
    // sheet starts AT Hidden, so only a return to Hidden after having left it
    // counts — otherwise the entrance itself would read as a dismissal.
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
    val sheetBackground = when {
        backdrop != null && material == RuiGlassMaterial.Liquid && backdrop.liquidSource != null && isBlurSupported ->
            Modifier.liquidGlassSurface(
                backdrop = backdrop,
                tint = colors.surfaceElevated.copy(alpha = LiquidGlassTintAlpha),
                shape = shapes.sheet,
                // The sheet lives in the modal's own window and slides with a
                // lazy Modifier.offset, whose placement changes never refresh
                // captured coordinates — the liquid consumer can't track them
                // either, so provide the sheet's live top edge
                // (layerHeight - state.offset) directly, same as below.
                positionOverride = {
                    Offset(0f, backdrop.graphicsLayer.size.height.toFloat() - state.bottomSheetState.offset)
                },
            )
        backdrop != null && isBlurSupported ->
            Modifier.blurSurface(
                backdrop = backdrop,
                radius = 24.dp,
                tint = colors.surfaceElevated.copy(alpha = 0.72f),
                shape = shapes.sheet,
                // The sheet lives in the modal's own window and slides with a
                // lazy Modifier.offset, whose placement changes never refresh
                // captured coordinates — sampling from onGloballyPositioned
                // stuck at the pre-entrance position. The backdrop layer is the
                // full-screen page, so provide the sheet's live top edge
                // (layerHeight - state.offset) directly instead.
                positionOverride = {
                    Offset(0f, backdrop.graphicsLayer.size.height.toFloat() - state.bottomSheetState.offset)
                },
            )
        else -> Modifier.background(colors.surfaceElevated, shapes.sheet)
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
                    if (title != null) {
                        RuiText(
                            text = title,
                            style = typography.titleMd,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.xl)
                                .padding(bottom = spacing.md),
                        )
                    }
                    content()
                }
            }
        }
    }
}
