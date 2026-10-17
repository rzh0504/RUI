package rui.sample.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import rui.components.RuiDivider
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.sample.CatalogPage
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

/**
 * The Blocks tab's index: real-world app screens composed purely from Rui
 * components, one pushed page per block — the Components tab's pattern
 * applied to whole screens instead of single components.
 */
@Composable
public fun BlocksScreen(
    onOpen: (CatalogPage) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val spacing = RuiTheme.spacing

    ContentColumn(contentPadding = contentPadding) {
        SectionLabel("App blocks")
        RuiListGroup {
            RuiListItem(
                title = "Sign in",
                subtitle = "Form with inline validation and one action",
                showChevron = true,
                onClick = { onOpen(CatalogPage.SignIn) },
            )
            RuiDivider(inset = spacing.lg)
            RuiListItem(
                title = "Account",
                subtitle = "Profile row and grouped preferences",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Account) },
            )
            RuiDivider(inset = spacing.lg)
            RuiListItem(
                title = "Cart",
                subtitle = "Steppers with a live order summary",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Cart) },
            )
            RuiDivider(inset = spacing.lg)
            RuiListItem(
                title = "Inbox",
                subtitle = "Notification rows with unread badges",
                showChevron = true,
                onClick = { onOpen(CatalogPage.Inbox) },
            )
            RuiDivider(inset = spacing.lg)
            RuiListItem(
                title = "Search results",
                subtitle = "Empty state with a populate action",
                showChevron = true,
                onClick = { onOpen(CatalogPage.SearchResults) },
            )
        }
    }
}
