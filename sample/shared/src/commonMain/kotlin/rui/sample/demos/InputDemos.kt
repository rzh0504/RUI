package rui.sample.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import rui.theme.RuiTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import rui.components.RuiCheckbox
import rui.components.RuiSwitch
import rui.components.RuiTextField
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiText

@Composable
internal fun InputDemos() {
    DemoCard(title = "Text fields") {
        RuiTextField(
            state = rememberTextFieldState(),
            label = "Email",
            placeholder = "you@example.com",
            supportingText = "We never share your address.",
            leading = { RuiIcon(imageVector = RuiIcons.Check, contentDescription = null) },
        )
        RuiTextField(
            state = rememberTextFieldState("Invalid input"),
            label = "Error state",
            isError = true,
            supportingText = "Something looks wrong.",
        )
        RuiTextField(
            state = rememberTextFieldState("Read only value"),
            label = "Disabled",
            enabled = false,
        )
    }

    DemoCard(title = "Switches") {
        var wifi by remember { mutableStateOf(true) }
        var bluetooth by remember { mutableStateOf(false) }

        SwitchRow("Wi-Fi", wifi, { wifi = it })
        SwitchRow("Bluetooth", bluetooth, { bluetooth = it })
        SwitchRow("Unavailable", false, null)
    }

    DemoCard(title = "Checkboxes") {
        var first by remember { mutableStateOf(true) }
        var second by remember { mutableStateOf(false) }

        CheckboxRow("Sync on Wi-Fi only", first, { first = it })
        CheckboxRow("Usage statistics", second, { second = it })
        CheckboxRow("Unavailable", false, null)
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        RuiText(text = label, style = RuiTheme.typography.bodyMd)
        RuiSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun CheckboxRow(label: String, checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RuiTheme.spacing.sm),
    ) {
        RuiCheckbox(
            checked = checked,
            onCheckedChange = { onCheckedChange?.invoke(it) },
            enabled = onCheckedChange != null,
        )
        RuiText(
            text = label,
            style = RuiTheme.typography.bodyMd,
            color = if (onCheckedChange == null) RuiTheme.colors.contentDisabled else RuiTheme.colors.content,
        )
    }
}
