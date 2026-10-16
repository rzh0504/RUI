package rui.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rui.components.RuiButton
import rui.components.RuiInlineBanner
import rui.components.RuiInlineBannerVariant
import rui.components.RuiTextField
import rui.foundation.RuiSurface
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.theme.RuiTheme

/** Sign-in page: form, inline validation banner, one primary action. */
@Composable
public fun SignInScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    val spacing = RuiTheme.spacing
    val email = rememberTextFieldState()
    val password = rememberTextFieldState()
    var attempted by remember { mutableStateOf(false) }
    var signedInAs by remember { mutableStateOf<String?>(null) }

    val fieldsFilled = email.text.isNotBlank() && password.text.isNotBlank()

    ContentColumn(contentPadding = contentPadding) {
        RuiSurface(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                RuiText(text = "Welcome back", style = RuiTheme.typography.titleMd)
                RuiTextField(
                    state = email,
                    label = "Email",
                    placeholder = "you@example.com",
                    isError = attempted && !fieldsFilled,
                    supportingText = if (attempted && !fieldsFilled) "Enter your email and password." else null,
                )
                RuiTextField(
                    state = password,
                    label = "Password",
                    isError = attempted && !fieldsFilled,
                )
                when {
                    signedInAs != null -> RuiInlineBanner(
                        message = "Signed in as $signedInAs",
                        variant = RuiInlineBannerVariant.Neutral,
                        onDismiss = { signedInAs = null },
                    )
                    attempted && !fieldsFilled -> RuiInlineBanner(
                        message = "Couldn't sign in — check your details.",
                        variant = RuiInlineBannerVariant.Danger,
                    )
                }
                RuiButton(
                    onClick = {
                        attempted = true
                        if (fieldsFilled) {
                            signedInAs = email.text.toString()
                            attempted = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    RuiText(text = "Sign in")
                }
            }
        }
    }
}
