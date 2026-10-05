package rui.sample.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rui.components.RuiActionOption
import rui.components.RuiButton
import rui.components.RuiButtonVariant
import rui.components.RuiDivider
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.components.RuiSwipeActions
import rui.components.RuiSwipeValue
import rui.components.rememberRuiSwipeActionsState
import rui.components.rememberRuiSwipeGroup
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

private data class Message(val id: Int, val title: String, val subtitle: String)

@Composable
public fun SwipeActionsScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    val group = rememberRuiSwipeGroup()
    val messages = remember {
        mutableStateListOf(
            Message(1, "Weekly report", "Draft due Friday"),
            Message(2, "Team offsite", "Venue shortlist attached"),
            Message(3, "Design review", "Moved to 14:00"),
        )
    }

    ContentColumn(contentPadding = contentPadding) {
        RuiText(
            text = "Drag a row sideways to reveal the actions behind it — past half " +
                "their width the row settles open, a fling opens it regardless. " +
                "Opening one row closes the other; tapping an action closes first.",
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
            modifier = Modifier.fillMaxWidth(),
        )

        SectionLabel("Trailing actions")
        RuiListGroup {
            messages.forEachIndexed { index, message ->
                key(message.id) {
                    val state = rememberRuiSwipeActionsState(group)
                    RuiSwipeActions(
                        state = state,
                        endActions = listOf(
                            RuiActionOption(
                                label = "Flag",
                                onSelect = { },
                            ),
                            RuiActionOption(
                                label = "Delete",
                                danger = true,
                                onSelect = { messages.remove(message) },
                            ),
                        ),
                    ) {
                        RuiListItem(
                            title = message.title,
                            subtitle = message.subtitle,
                        )
                    }
                }
                if (index < messages.lastIndex) {
                    RuiDivider(inset = RuiTheme.spacing.lg)
                }
            }
            if (messages.isEmpty()) {
                RuiListItem(title = "All clear", subtitle = "Deleted messages leave the list")
            }
        }

        SectionLabel("Both edges")
        RuiListGroup {
            var archived by remember { mutableStateOf(false) }
            val state = rememberRuiSwipeActionsState(group)
            RuiSwipeActions(
                state = state,
                startActions = listOf(
                    RuiActionOption(
                        label = if (archived) "Unarchive" else "Archive",
                        onSelect = { archived = !archived },
                    ),
                ),
                endActions = listOf(
                    RuiActionOption(
                        label = "Mute",
                        enabled = false,
                        onSelect = { },
                    ),
                    RuiActionOption(
                        label = "Delete",
                        danger = true,
                        onSelect = { },
                    ),
                ),
            ) {
                RuiListItem(
                    title = "Flight confirmation",
                    subtitle = if (archived) "Archived" else "Boarding pass in wallet",
                )
            }
        }

        SectionLabel("Programmatic")
        RuiListGroup {
            val state = rememberRuiSwipeActionsState(group)
            RuiSwipeActions(
                state = state,
                endActions = listOf(
                    RuiActionOption(
                        label = "Snooze",
                        onSelect = { },
                    ),
                ),
            ) {
                RuiListItem(title = "Standup notes", subtitle = "Opens from the row or the buttons")
            }
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Open programmatically",
                trailing = {
                    RuiButton(
                        onClick = { state.open(RuiSwipeValue.EndOpen) },
                        variant = RuiButtonVariant.Tonal,
                    ) {
                        RuiText("Open")
                    }
                },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Close programmatically",
                trailing = {
                    RuiButton(
                        onClick = { state.close() },
                        variant = RuiButtonVariant.Tonal,
                    ) {
                        RuiText("Close")
                    }
                },
            )
        }
    }
}
