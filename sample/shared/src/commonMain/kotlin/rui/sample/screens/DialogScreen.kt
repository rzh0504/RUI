package rui.sample.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import rui.components.RuiButton
import rui.components.RuiButtonVariant
import rui.components.RuiDialog
import rui.components.RuiDivider
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.foundation.BlurBackdrop
import rui.foundation.ExperimentalRuiApi
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

@OptIn(ExperimentalRuiApi::class)
@Composable
public fun DialogScreen(
    glassEnabled: Boolean,
    backdrop: BlurBackdrop,
    modifier: Modifier = Modifier,
) {
    var standardDialog by remember { mutableStateOf(false) }
    var glassDialog by remember { mutableStateOf(false) }

    ContentColumn(modifier = modifier) {
        RuiText(
            text = "Portaled through DialogHost at the app root. Esc and back " +
                "dismiss, the scrim fades, the panel scales in on a spring.",
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
        )

        SectionLabel("Alert")
        RuiListGroup {
            RuiListItem(
                title = "Destructive confirmation",
                subtitle = "Title, supporting text, actions",
                showChevron = true,
                onClick = { standardDialog = true },
            )
        }

        SectionLabel("Glass")
        RuiListGroup {
            RuiListItem(
                title = "Glass panel",
                subtitle = if (glassEnabled) "Backdrop blur behind a translucent panel" else "Glass is off — solid surface",
                showChevron = true,
                onClick = { glassDialog = true },
            )
        }
    }

    RuiDialog(
        visible = standardDialog,
        onDismissRequest = { standardDialog = false },
        title = "Delete item?",
        supportingText = "This action cannot be undone. The item will be removed from all your devices.",
        actions = {
            RuiButton(onClick = { standardDialog = false }, variant = RuiButtonVariant.Plain) {
                RuiText("Cancel")
            }
            RuiButton(onClick = { standardDialog = false }, variant = RuiButtonVariant.Danger) {
                RuiText("Delete")
            }
        },
    )

    RuiDialog(
        visible = glassDialog,
        onDismissRequest = { glassDialog = false },
        glass = glassEnabled,
        backdrop = backdrop,
        title = "Glass panel",
        supportingText = "Unsupported platforms get a near-opaque fallback automatically.",
        actions = {
            RuiButton(onClick = { glassDialog = false }, variant = RuiButtonVariant.Tonal) {
                RuiText("Got it")
            }
        },
    )
}
