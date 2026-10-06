package rui.sample.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import rui.components.RuiButton
import rui.components.RuiButtonVariant
import rui.components.RuiInlineBanner
import rui.components.RuiInlineBannerVariant
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

@Composable
public fun BannerScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    var offline by rememberSaveable { mutableStateOf(true) }
    var syncing by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(syncing) {
        if (syncing) {
            delay(1_500)
            syncing = false
        }
    }

    ContentColumn(contentPadding = contentPadding) {
        RuiText(
            text = "The snackbar's complement: an in-flow strip for conditions " +
                "that outlive a moment. It stays until the caller stops " +
                "composing it or the user dismisses it.",
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
            modifier = Modifier.fillMaxWidth(),
        )

        SectionLabel("Neutral, dismissible")
        if (offline) {
            RuiInlineBanner(
                message = "You're offline. Messages will send when you reconnect.",
                onDismiss = { offline = false },
            )
        } else {
            RuiButton(
                onClick = { offline = true },
                variant = RuiButtonVariant.Tonal,
            ) {
                RuiText("Show the banner again")
            }
        }

        SectionLabel("Danger, with action")
        RuiInlineBanner(
            message = if (syncing) "Syncing your notes…" else "Couldn't sync your notes.",
            variant = RuiInlineBannerVariant.Danger,
            actionLabel = if (syncing) null else "Retry",
            onAction = { syncing = true },
        )
    }
}
