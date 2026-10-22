package rui.sample.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import rui.components.RuiButton
import rui.components.RuiButtonVariant
import rui.components.RuiDialog
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.foundation.BlurBackdrop
import rui.foundation.ExperimentalRuiApi
import rui.foundation.RuiGlassMaterial
import rui.foundation.RuiText
import rui.sample.GlassMode
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

@OptIn(ExperimentalRuiApi::class)
@Composable
public fun DialogScreen(
    glassMode: GlassMode,
    backdrop: BlurBackdrop,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var standardDialog by remember { mutableStateOf(false) }
    var glassDialog by remember { mutableStateOf(false) }

    ContentColumn(contentPadding = contentPadding) {
        RuiText(
            text = "Portaled through DialogHost at the app root. Esc and back " +
                "dismiss, the scrim fades, the panel scales in on a spring. " +
                "The glass panel follows the Appearance glass mode.",
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
                subtitle = when (glassMode) {
                    GlassMode.Solid -> "Solid mode — opaque elevated surface"
                    GlassMode.Frosted -> "Backdrop blur behind a translucent panel"
                    GlassMode.Liquid -> "Edge refraction and a specular highlight"
                },
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
        frosted = glassMode != GlassMode.Solid,
        backdrop = if (glassMode == GlassMode.Solid) null else backdrop,
        material = if (glassMode == GlassMode.Liquid) RuiGlassMaterial.Liquid else RuiGlassMaterial.Frosted,
        title = "Glass panel",
        supportingText = "Unsupported platforms get a near-opaque fallback automatically.",
        actions = {
            RuiButton(onClick = { glassDialog = false }, variant = RuiButtonVariant.Tonal) {
                RuiText("Got it")
            }
        },
    )
}
