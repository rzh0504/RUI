package rui.sample.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import rui.components.RuiAvatar
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.sample.CatalogPage
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

/** Real-world screen blocks composed from Rui components. */
@Composable
public fun BlocksScreen(
    onOpen: (CatalogPage) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val colors = RuiTheme.colors

    ContentColumn(contentPadding = contentPadding) {
        SectionLabel("Account")
        RuiListGroup {
            RuiListItem(
                title = "Sign in",
                subtitle = "Identity form with inline feedback",
                leading = { RuiIcon(RuiIcons.Check, contentDescription = null, tint = colors.primary) },
                showChevron = true,
                onClick = { onOpen(CatalogPage.SignIn) },
                showDividerAfter = true,
            )
            RuiListItem(
                title = "Account",
                subtitle = "Profile, preferences and privacy controls",
                leading = { RuiAvatar(initials = "RL") },
                showChevron = true,
                onClick = { onOpen(CatalogPage.Account) },
            )
        }

        SectionLabel("Shopping")
        RuiListGroup {
            RuiListItem(
                title = "Cart",
                subtitle = "Editable quantities, totals and checkout",
                leading = { RuiIcon(RuiIcons.Grid, contentDescription = null, tint = colors.primary) },
                showChevron = true,
                onClick = { onOpen(CatalogPage.Cart) },
            )
        }

        SectionLabel("Communication")
        RuiListGroup {
            RuiListItem(
                title = "Inbox",
                subtitle = "Unread markers, previews and timestamps",
                leading = { RuiAvatar(initials = "MK") },
                showChevron = true,
                onClick = { onOpen(CatalogPage.Inbox) },
            )
        }

        SectionLabel("Discovery")
        RuiListGroup {
            RuiListItem(
                title = "Search results",
                subtitle = "Empty state that transitions to results",
                leading = { RuiIcon(RuiIcons.Search, contentDescription = null, tint = colors.primary) },
                showChevron = true,
                onClick = { onOpen(CatalogPage.SearchResults) },
            )
        }
    }
}
