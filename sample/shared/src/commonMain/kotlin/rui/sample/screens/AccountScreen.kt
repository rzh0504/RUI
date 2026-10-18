package rui.sample.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import rui.components.RuiAvatar
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.components.RuiSwitch
import rui.sample.ui.ContentColumn
import rui.theme.RuiTheme

/** Account page: profile row, switch rows and a value row — settings form. */
@Composable
public fun AccountScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    var notifications by remember { mutableStateOf(true) }
    var analytics by remember { mutableStateOf(false) }

    ContentColumn(contentPadding = contentPadding) {
        RuiListGroup {
            RuiListItem(
                title = "Robin Lee",
                subtitle = "robin@rui.dev",
                leading = { RuiAvatar(initials = "RL") },
                showChevron = true,
                onClick = {},
                showDividerAfter = true,
            )
            RuiListItem(
                title = "Push notifications",
                trailing = { RuiSwitch(checked = notifications, onCheckedChange = { notifications = it }) },
                showDividerAfter = true,
            )
            RuiListItem(
                title = "Usage analytics",
                trailing = { RuiSwitch(checked = analytics, onCheckedChange = { analytics = it }) },
                showDividerAfter = true,
            )
            RuiListItem(
                title = "Storage",
                value = "128 MB",
                showChevron = true,
                onClick = {},
            )
        }
    }
}
