package rui.sample.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rui.components.RuiButton
import rui.components.RuiButtonVariant
import rui.components.RuiDivider
import rui.components.RuiEmptyState
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.foundation.RuiIcons
import rui.foundation.RuiSurface
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

@Composable
public fun EmptyStateScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    var drafts by rememberSaveable { mutableIntStateOf(0) }

    ContentColumn(contentPadding = contentPadding) {
        RuiText(
            text = "A quiet placeholder for nothing to show — it centers itself " +
                "in whatever canvas you give it, from a whole screen to a " +
                "region inside a card.",
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
            modifier = Modifier.fillMaxWidth(),
        )

        SectionLabel("Quiet")
        RuiSurface {
            Box(Modifier.fillMaxWidth().height(200.dp)) {
                RuiEmptyState(
                    title = "All caught up",
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        SectionLabel("With message")
        RuiSurface {
            Box(Modifier.fillMaxWidth().height(220.dp)) {
                RuiEmptyState(
                    title = "No connection",
                    message = "Check your network and try again.",
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        SectionLabel("With icon and action")
        if (drafts == 0) {
            RuiSurface {
                Box(Modifier.fillMaxWidth().height(260.dp)) {
                    RuiEmptyState(
                        title = "No drafts",
                        message = "Drafts you start appear here.",
                        icon = RuiIcons.Grid,
                        actionLabel = "New draft",
                        onAction = { drafts++ },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        } else {
            RuiListGroup {
                repeat(drafts) { index ->
                    RuiListItem(title = "Draft ${index + 1}")
                    if (index < drafts - 1) {
                        RuiDivider(inset = RuiTheme.spacing.lg)
                    }
                }
                RuiDivider(inset = RuiTheme.spacing.lg)
                RuiListItem(
                    title = "Clear all",
                    trailing = {
                        RuiButton(
                            onClick = { drafts = 0 },
                            variant = RuiButtonVariant.Tonal,
                        ) {
                            RuiText("Clear")
                        }
                    },
                )
            }
        }
    }
}
