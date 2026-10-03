package rui.sample.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import rui.components.RuiListGroup
import rui.components.RuiTextField
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

@Composable
public fun TextFieldScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    ContentColumn(contentPadding = contentPadding) {
        RuiText(
            text = "Filled, borderless input built on Unstyled's TextFieldState. " +
                "A hairline border appears only to mark errors.",
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
        )

        SectionLabel("Default")
        RuiListGroup {
            RuiTextField(
                state = rememberTextFieldState(),
                label = "Email",
                placeholder = "you@example.com",
                supportingText = "We never share your address.",
                leading = { RuiIcon(imageVector = RuiIcons.Check, contentDescription = null) },
                modifier = Modifier.padding(RuiTheme.spacing.md),
            )
        }

        SectionLabel("Error")
        RuiListGroup {
            RuiTextField(
                state = rememberTextFieldState("Invalid input"),
                label = "Email",
                isError = true,
                supportingText = "That doesn't look like an email address.",
                modifier = Modifier.padding(RuiTheme.spacing.md),
            )
        }

        SectionLabel("Disabled")
        RuiListGroup {
            RuiTextField(
                state = rememberTextFieldState("Read-only value"),
                label = "Account",
                enabled = false,
                modifier = Modifier.padding(RuiTheme.spacing.md),
            )
        }
    }
}
