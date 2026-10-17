package rui.sample.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rui.components.AnchorAlignment
import rui.components.RuiDivider
import rui.components.RuiDropdownMenu
import rui.components.RuiDropdownMenuItem
import rui.components.RuiDropdownSubmenu
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.foundation.BlurBackdrop
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

@Composable
public fun DropdownMenuScreen(
    backdrop: BlurBackdrop,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var editMenu by remember { mutableStateOf(false) }
    var viewsMenu by remember { mutableStateOf(false) }
    var shareMenu by remember { mutableStateOf(false) }

    ContentColumn(contentPadding = contentPadding) {
        RuiText(
            text = "Menus anchor to their trigger, portal above the page, and " +
                "dismiss on outside tap, back, or selection.",
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
        )

        SectionLabel("Pull-down")
        RuiListGroup {
            RuiDropdownMenu(
                expanded = editMenu,
                onDismissRequest = { editMenu = false },
                backdrop = backdrop,
                alignment = AnchorAlignment.End,
                anchor = {
                    RuiListItem(
                        title = "Edit",
                        subtitle = "Frosted panel with a destructive action",
                        onClick = { editMenu = !editMenu },
                    )
                },
            ) {
                RuiDropdownMenuItem(text = "Rename", onClick = { editMenu = false })
                RuiDropdownMenuItem(text = "Duplicate", onClick = { editMenu = false })
                RuiDivider()
                RuiDropdownMenuItem(text = "Delete", onClick = { editMenu = false }, danger = true)
            }
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiDropdownMenu(
                expanded = viewsMenu,
                onDismissRequest = { viewsMenu = false },
                backdrop = backdrop,
                anchor = {
                    RuiListItem(
                        title = "Choose view",
                        subtitle = "Leading icons and a checkmark",
                        onClick = { viewsMenu = !viewsMenu },
                    )
                },
            ) {
                RuiDropdownMenuItem(
                    text = "Components",
                    onClick = { viewsMenu = false },
                    leading = { RuiIcon(imageVector = RuiIcons.Grid, contentDescription = null) },
                )
                RuiDropdownMenuItem(
                    text = "Tokens",
                    onClick = { viewsMenu = false },
                    leading = { RuiIcon(imageVector = RuiIcons.Swatch, contentDescription = null) },
                    trailing = { RuiIcon(imageVector = RuiIcons.Check, contentDescription = null) },
                )
                RuiDivider()
                RuiDropdownMenuItem(
                    text = "Close",
                    onClick = { viewsMenu = false },
                    leading = { RuiIcon(imageVector = RuiIcons.Close, contentDescription = null) },
                )
            }
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiDropdownMenu(
                expanded = shareMenu,
                onDismissRequest = { shareMenu = false },
                backdrop = backdrop,
                alignment = AnchorAlignment.End,
                anchor = {
                    RuiListItem(
                        title = "Share",
                        subtitle = "Cascading submenus, two levels deep",
                        onClick = { shareMenu = !shareMenu },
                    )
                },
            ) {
                RuiDropdownMenuItem(
                    text = "Copy link",
                    onClick = { shareMenu = false },
                    leading = { RuiIcon(imageVector = RuiIcons.Check, contentDescription = null) },
                )
                RuiDropdownSubmenu(
                    text = "Share to…",
                    leading = { RuiIcon(imageVector = RuiIcons.Search, contentDescription = null) },
                ) {
                    RuiDropdownMenuItem(text = "Message", onClick = { shareMenu = false })
                    RuiDropdownMenuItem(text = "Mail", onClick = { shareMenu = false })
                    RuiDropdownSubmenu(text = "More…") {
                        RuiDropdownMenuItem(text = "Notes", onClick = { shareMenu = false })
                        RuiDropdownMenuItem(text = "Reminders", onClick = { shareMenu = false })
                    }
                }
                RuiDivider()
                RuiDropdownMenuItem(text = "Delete", onClick = { shareMenu = false }, danger = true)
            }
        }
    }
}
