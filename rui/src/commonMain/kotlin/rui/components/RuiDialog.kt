package rui.components

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.unit.dp
import com.composeunstyled.DialogPanel
import com.composeunstyled.Scrim
import com.composeunstyled.UnstyledDialog
import rui.foundation.BlurBackdrop
import rui.foundation.ExperimentalRuiApi
import rui.foundation.FrostedSurface
import rui.foundation.RuiGlassMaterial
import rui.foundation.RuiText
import rui.foundation.RuiSurface
import rui.theme.RuiTheme

/**
 * The Rui dialog: Unstyled's modal behavior (portal host, Esc/back dismiss,
 * pane title semantics, scrim) with a token-styled panel.
 *
 * The app root should compose [RuiOverlayHost] so dialog panels portal above
 * everything; without one, Unstyled falls back to a platform modal — insets,
 * backdrop and scrim behavior then differ, and a setup note is logged.
 */
@Composable
public fun RuiDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    supportingText: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {},
) {
    RuiDialogPanel(
        visible = visible,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        frosted = false,
        backdrop = null,
        material = RuiGlassMaterial.Frosted,
        title = title,
        supportingText = supportingText,
        actions = actions,
        content = content,
    )
}

/** Glass-skinned variant of [RuiDialog] — frosted by default, or the
 * liquid-glass material with [material]; experimental until backdrop blur
 * lands. */
@ExperimentalRuiApi
@Composable
public fun RuiDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    frosted: Boolean,
    modifier: Modifier = Modifier,
    backdrop: BlurBackdrop? = null,
    material: RuiGlassMaterial = RuiGlassMaterial.Frosted,
    title: String? = null,
    supportingText: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {},
) {
    RuiDialogPanel(
        visible = visible,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        frosted = frosted,
        backdrop = backdrop,
        material = material,
        title = title,
        supportingText = supportingText,
        actions = actions,
        content = content,
    )
}

@OptIn(ExperimentalRuiApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun RuiDialogPanel(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    frosted: Boolean,
    backdrop: BlurBackdrop?,
    material: RuiGlassMaterial,
    title: String?,
    supportingText: String?,
    actions: @Composable RowScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = RuiTheme.colors
    val typography = RuiTheme.typography
    val spacing = RuiTheme.spacing
    val motion = RuiTheme.motion
    val dimens = RuiTheme.dimens

    WarnWithoutRuiOverlayHost("RuiDialog")

    // Unstyled dismisses on Back as a *key event* (Esc/backspace); Android's
    // gesture back and the browser's back button go through the back
    // dispatcher instead, which would pop the page beneath and leave this
    // dialog animating out over the wrong screen. The nav display's back
    // handler registers before page content, so this one wins while the
    // dialog is up.
    if (visible) {
        BackHandler(onBack = onDismissRequest)
    }

    UnstyledDialog(
        visible = visible,
        onDismissRequest = onDismissRequest,
        overlay = {
            Scrim(
                scrimColor = colors.scrim,
                enter = fadeIn(motion.normal),
                exit = fadeOut(motion.fast),
            )
        },
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = spacing.xl),
            contentAlignment = Alignment.Center,
        ) {
            DialogPanel(
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .fillMaxWidth(),
                paneTitle = title,
                enter = fadeIn(motion.normal) + scaleIn(
                    initialScale = 0.94f,
                    animationSpec = motion.pop,
                ),
                exit = fadeOut(motion.fast) + scaleOut(targetScale = 0.96f),
            ) {
                if (frosted) {
                    FrostedSurface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RuiTheme.shapes.xl,
                        backdrop = backdrop,
                        material = material,
                    ) {
                        DialogBody(title, supportingText, actions, content)
                    }
                } else {
                    RuiSurface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RuiTheme.shapes.xl,
                        color = colors.surfaceElevated,
                    ) {
                        DialogBody(title, supportingText, actions, content)
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogBody(
    title: String?,
    supportingText: String?,
    actions: @Composable RowScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = RuiTheme.colors
    val typography = RuiTheme.typography
    val spacing = RuiTheme.spacing

    Column(modifier = Modifier.padding(spacing.xl)) {
        if (title != null) {
            RuiText(
                text = title,
                style = typography.titleMd,
                modifier = Modifier.padding(bottom = spacing.sm),
            )
        }
        if (supportingText != null) {
            RuiText(
                text = supportingText,
                style = typography.bodyMd,
                color = colors.contentMuted,
                modifier = Modifier.padding(bottom = spacing.sm),
            )
        }
        content()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = spacing.lg),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            actions()
        }
    }
}
