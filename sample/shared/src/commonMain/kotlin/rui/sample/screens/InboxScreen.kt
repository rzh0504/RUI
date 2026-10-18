package rui.sample.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rui.components.RuiAvatar
import rui.components.RuiAvatarDefaults
import rui.components.RuiAvatarSize
import rui.components.RuiBadge
import rui.components.RuiBadgeDefaults
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.circleRimDotOffset
import rui.theme.RuiTheme

/** Inbox page: notification rows with unread dot badges and time stamps. */
@Composable
public fun InboxScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    ContentColumn(contentPadding = contentPadding) {
        RuiListGroup {
            InboxRow("MK", "Mia Kim", "Re: the squircle radii", "2m", unread = true, showDividerAfter = true)
            InboxRow("JT", "Jon Törn", "Shipped the wheel picker fix", "1h", unread = true, showDividerAfter = true)
            InboxRow("AS", "Ana Silva", "Lunch tomorrow?", "Tue", unread = false, showDividerAfter = true)
            InboxRow("DP", "Dana Park", "Tokens page looks great now", "Mon", unread = false, showDividerAfter = true)
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
    showDividerAfter: Boolean = false,
) {
    RuiListItem(
        title = name,
        subtitle = message,
        leading = {
            // Unread dot riding the avatar's rim along the top-end diagonal —
            // circular anchors never reach their bounding-box corner, so
            // RuiBadgedBox's corner placement can't apply.
            Box {
                RuiAvatar(initials = initials)
                if (unread) {
                    RuiBadge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .circleRimDotOffset(
                                circleSize = RuiAvatarDefaults.diameter(RuiAvatarSize.Md),
                                dotSize = RuiBadgeDefaults.DotSize,
                                corner = Alignment.TopEnd,
                            ),
                    )
                }
            }
        },
        trailing = {
            RuiText(
                text = time,
                style = RuiTheme.typography.labelSm,
                color = if (unread) RuiTheme.colors.primary else RuiTheme.colors.contentMuted,
            )
        },
        showDividerAfter = showDividerAfter,
    )
}
