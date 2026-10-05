package rui.sample.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import rui.components.RuiDivider
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.components.RuiSnackbarDuration
import rui.components.RuiSnackbarHostState
import rui.components.RuiSnackbarResult
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

@Composable
public fun SnackbarScreen(
    hostState: RuiSnackbarHostState,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val scope = rememberCoroutineScope()
    var lastResult by rememberSaveable { mutableStateOf("Nothing shown yet") }

    ContentColumn(contentPadding = contentPadding) {
        SectionLabel("Snackbar")
        RuiListGroup {
            RuiListItem(
                title = "Show message",
                subtitle = "Acknowledgment, auto-dismisses",
                showChevron = true,
                onClick = {
                    scope.launch {
                        lastResult = "Result: ${hostState.showSnackbar("Saved to drafts")}"
                    }
                },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "With action",
                subtitle = "Swipe the card away, or tap Undo",
                showChevron = true,
                onClick = {
                    scope.launch {
                        val result = hostState.showSnackbar(
                            message = "Draft deleted",
                            actionLabel = "Undo",
                        )
                        lastResult = when (result) {
                            RuiSnackbarResult.ActionPerformed -> "Result: undo tapped"
                            RuiSnackbarResult.Dismissed -> "Result: swiped away"
                            RuiSnackbarResult.Timeout -> "Result: timed out"
                        }
                    }
                },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Long duration",
                subtitle = "About ten seconds on screen",
                showChevron = true,
                onClick = {
                    scope.launch {
                        lastResult = "Result: ${hostState.showSnackbar(
                            "Long-lived message worth reading",
                            duration = RuiSnackbarDuration.Long,
                        )}"
                    }
                },
            )
        }

        RuiText(
            text = lastResult,
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
        )
    }
}
