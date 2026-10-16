package rui.sample.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rui.components.RuiAvatar
import rui.components.RuiBadge
import rui.components.RuiBadgedBox
import rui.components.RuiDivider
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.theme.RuiTheme

/** Inbox page: notification rows with unread dot badges and time stamps. */
@Composable
public fun InboxScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    ContentColumn(contentPadding = contentPadding) {
        RuiListGroup {
            InboxRow("MK", "Mia Kim", "Re: the squircle radii", "2m", unread = true)
            RuiDivider(inset = RuiTheme.spacing.lg)
            InboxRow("JT", "Jon Törn", "Shipped the wheel picker fix", "1h", unread = true)
            RuiDivider(inset = RuiTheme.spacing.lg)
            InboxRow("AS", "Ana Silva", "Lunch tomorrow?", "Tue", unread = false)
            RuiDivider(inset = RuiTheme.spacing.lg)
            InboxRow("DP", "Dana Park", "Tokens page looks great now", "Mon", unread = false)
            RuiDivider(inset = RuiTheme.spacing.lg)
            InboxRow("RL", "Robin Lee", "Release notes draft", "Mon", unread = false)
        }
    }
}

@Composable
private fun InboxRow(
    initials: String,
    name: String,
    message: String,
    time: String,
    unread: Boolean,
) {
    RuiListItem(
        title = name,
        subtitle = message,
        leading = {
            RuiBadgedBox(badge = { if (unread) RuiBadge() }) {
                RuiAvatar(initials = initials)
            }
        },
        trailing = {
            RuiText(
                text = time,
                style = RuiTheme.typography.labelSm,
                color = if (unread) RuiTheme.colors.primary else RuiTheme.colors.contentMuted,
            )
        },
    )
}
