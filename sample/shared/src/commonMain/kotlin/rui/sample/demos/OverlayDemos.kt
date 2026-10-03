package rui.sample.demos

import androidx.compose.runtime.Composable
import rui.theme.RuiTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import rui.components.RuiButton
import rui.components.RuiButtonVariant
import rui.components.RuiDialog
import rui.foundation.ExperimentalRuiApi
import rui.foundation.RuiText

@OptIn(ExperimentalRuiApi::class)
@Composable
internal fun OverlayDemos(glassEnabled: Boolean) {
    var standardDialog by remember { mutableStateOf(false) }
    var glassDialog by remember { mutableStateOf(false) }

    DemoCard(title = "Dialog") {
        RuiText(
            text = "Portaled through DialogHost; Esc and back dismiss; scrim and panel animate.",
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
        )
        RuiButton(onClick = { standardDialog = true }) {
            RuiText("Show dialog")
        }
    }

    DemoCard(title = "Glass dialog") {
        RuiText(
            text = if (glassEnabled) {
                "The panel renders with the glass material."
            } else {
                "Glass is off — the panel falls back to a solid surface."
            },
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
        )
        RuiButton(onClick = { glassDialog = true }, variant = RuiButtonVariant.Secondary) {
            RuiText("Show glass dialog")
        }
    }

    RuiDialog(
        visible = standardDialog,
        onDismissRequest = { standardDialog = false },
        title = "Delete item?",
        supportingText = "This action cannot be undone. The item will be removed from all your devices.",
        actions = {
            RuiButton(onClick = { standardDialog = false }, variant = RuiButtonVariant.Ghost) {
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
        title = "Glass panel",
        supportingText = "A translucent material for floating chrome. Unsupported platforms get a near-opaque fallback.",
        actions = {
            RuiButton(onClick = { glassDialog = false }, variant = RuiButtonVariant.Secondary) {
                RuiText("Got it")
            }
        },
    )
}
