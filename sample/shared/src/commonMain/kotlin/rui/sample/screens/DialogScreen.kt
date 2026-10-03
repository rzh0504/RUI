package rui.sample.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import rui.components.RuiButton
import rui.components.RuiButtonVariant
import rui.components.RuiDialog
import rui.foundation.ExperimentalRuiApi
import rui.foundation.RuiText
import rui.sample.ui.DetailScaffold
import rui.sample.ui.GroupedCard
import rui.sample.ui.ListRow
import rui.sample.ui.RowSeparator
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

@OptIn(ExperimentalRuiApi::class)
@Composable
public fun DialogScreen(onBack: () -> Unit) {
    var standardDialog by remember { mutableStateOf(false) }
    var glassDialog by remember { mutableStateOf(false) }

    DetailScaffold(title = "Dialog", onBack = onBack) {
        RuiText(
            text = "Portaled through DialogHost at the app root. Esc and back " +
                "dismiss, the scrim fades, the panel scales in on a spring.",
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
        )

        SectionLabel("Alert")
        GroupedCard {
            ListRow(
                title = "Destructive confirmation",
                subtitle = "Title, supporting text, actions",
                showChevron = true,
                onClick = { standardDialog = true },
            )
        }

        SectionLabel("Glass")
        GroupedCard {
            ListRow(
                title = "Glass panel",
                subtitle = "Same behavior, translucent material",
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
        glass = true,
        title = "Glass panel",
        supportingText = "Unsupported platforms get a near-opaque fallback automatically.",
        actions = {
            RuiButton(onClick = { glassDialog = false }, variant = RuiButtonVariant.Tonal) {
                RuiText("Got it")
            }
        },
    )
}
