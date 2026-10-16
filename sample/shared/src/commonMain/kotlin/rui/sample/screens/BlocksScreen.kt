package rui.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import rui.components.RuiAvatar
import rui.components.RuiBadge
import rui.components.RuiButton
import rui.components.RuiDivider
import rui.components.RuiEmptyState
import rui.components.RuiInlineBanner
import rui.components.RuiInlineBannerVariant
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.components.RuiStepper
import rui.components.RuiSwitch
import rui.components.RuiTextField
import rui.foundation.RuiIcons
import rui.foundation.RuiSurface
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

/**
 * Real-world app blocks — sign-in, account settings, a cart, an inbox and a
 * search section — composed purely from Rui components, the way an app would
 * assemble them. Blocks are live: forms validate, steppers recompute totals.
 */
@Composable
public fun BlocksScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    val spacing = RuiTheme.spacing

    ContentColumn(contentPadding = contentPadding) {
        SectionLabel("Sign in")
        SignInBlock()

        SectionLabel("Account")
        AccountBlock()

        SectionLabel("Cart")
        CartBlock()

        SectionLabel("Inbox")
        InboxBlock()

        SectionLabel("Search results")
        SearchResultsBlock()
    }
}

/** Email + password form: inline validation banner, one primary action. */
@Composable
private fun SignInBlock() {
    val spacing = RuiTheme.spacing
    val email = rememberTextFieldState()
    val password = rememberTextFieldState()
    var attempted by remember { mutableStateOf(false) }
    var signedInAs by remember { mutableStateOf<String?>(null) }

    val fieldsFilled = email.text.isNotBlank() && password.text.isNotBlank()

    RuiSurface(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            RuiText(text = "Welcome back", style = RuiTheme.typography.titleMd)
            RuiTextField(
                state = email,
                label = "Email",
                placeholder = "you@example.com",
                isError = attempted && !fieldsFilled,
                supportingText = if (attempted && !fieldsFilled) "Enter your email and password." else null,
            )
            RuiTextField(
                state = password,
                label = "Password",
                isError = attempted && !fieldsFilled,
            )
            when {
                signedInAs != null -> RuiInlineBanner(
                    message = "Signed in as $signedInAs",
                    variant = RuiInlineBannerVariant.Neutral,
                    onDismiss = { signedInAs = null },
                )
                attempted && !fieldsFilled -> RuiInlineBanner(
                    message = "Couldn't sign in — check your details.",
                    variant = RuiInlineBannerVariant.Danger,
                )
            }
            RuiButton(
                onClick = {
                    attempted = true
                    if (fieldsFilled) {
                        signedInAs = email.text.toString()
                        attempted = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                RuiText(text = "Sign in")
            }
        }
    }
}

/** The iOS-settings account page: profile row, switch rows, a value row. */
@Composable
private fun AccountBlock() {
    var notifications by remember { mutableStateOf(true) }
    var analytics by remember { mutableStateOf(false) }

    RuiListGroup {
        RuiListItem(
            title = "Robin Lee",
            subtitle = "robin@rui.dev",
            leading = { RuiAvatar(initials = "RL") },
            showChevron = true,
            onClick = {},
        )
        RuiDivider(inset = RuiTheme.spacing.lg)
        RuiListItem(
            title = "Push notifications",
            trailing = { RuiSwitch(checked = notifications, onCheckedChange = { notifications = it }) },
        )
        RuiDivider(inset = RuiTheme.spacing.lg)
        RuiListItem(
            title = "Usage analytics",
            trailing = { RuiSwitch(checked = analytics, onCheckedChange = { analytics = it }) },
        )
        RuiDivider(inset = RuiTheme.spacing.lg)
        RuiListItem(
            title = "Storage",
            value = "128 MB",
            showChevron = true,
            onClick = {},
        )
    }
}

/** Line items with steppers; subtotal, shipping and total recompute live. */
@Composable
private fun CartBlock() {
    val spacing = RuiTheme.spacing
    val products = remember { listOf("Aurora desk lamp" to 4200, "Linen throw" to 2800, "Oak tray" to 1800) }
    var quantities by remember { mutableStateOf(listOf(1, 2, 0)) }
    var placed by remember { mutableStateOf(false) }

    val subtotalCents = products.indices.sumOf { quantities[it] * products[it].second }
    val shippingCents = if (subtotalCents >= 5000 || subtotalCents == 0) 0 else 500

    RuiListGroup {
        products.forEachIndexed { index, (name, unitCents) ->
            if (index > 0) RuiDivider(inset = spacing.lg)
            RuiListItem(
                title = name,
                subtitle = money(unitCents),
                trailing = {
                    RuiStepper(
                        value = quantities[index],
                        onValueChange = { value ->
                            quantities = quantities.toMutableList().also { it[index] = value }
                            placed = false
                        },
                        range = 0..9,
                    )
                },
            )
        }
        RuiDivider(inset = spacing.lg)
        RuiListItem(title = "Subtotal", value = money(subtotalCents))
        RuiListItem(title = "Shipping", value = if (shippingCents == 0) "Free" else money(shippingCents))
        RuiDivider(inset = spacing.lg)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.lg, vertical = spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RuiText(text = "Total", style = RuiTheme.typography.titleMd, modifier = Modifier.weight(1f))
            RuiText(
                text = money(subtotalCents + shippingCents),
                style = RuiTheme.typography.titleMd,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
    if (placed) {
        RuiInlineBanner(
            message = "Order placed — demo only.",
            variant = RuiInlineBannerVariant.Neutral,
            onDismiss = { placed = false },
        )
    }
    RuiButton(
        onClick = { placed = true },
        enabled = subtotalCents > 0,
        modifier = Modifier.fillMaxWidth(),
    ) {
        RuiText(text = "Checkout")
    }
}

/** Notification rows: avatar with an unread dot badge and a time stamp. */
@Composable
private fun InboxBlock() {
    val spacing = RuiTheme.spacing
    RuiListGroup {
        InboxRow(initials = "MK", name = "Mia Kim", message = "Re: the squircle radii", time = "2m", unread = true)
        RuiDivider(inset = spacing.lg)
        InboxRow(initials = "JT", name = "Jon Törn", message = "Shipped the wheel picker fix", time = "1h", unread = true)
        RuiDivider(inset = spacing.lg)
        InboxRow(initials = "AS", name = "Ana Silva", message = "Lunch tomorrow?", time = "Tue", unread = false)
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
            Box {
                RuiAvatar(initials = initials)
                if (unread) {
                    RuiBadge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp),
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
    )
}

/** An empty search section whose action populates sample result rows. */
@Composable
private fun SearchResultsBlock() {
    var hasResults by remember { mutableStateOf(false) }

    if (!hasResults) {
        RuiSurface(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                RuiEmptyState(
                    title = "No results",
                    message = "Nothing matches the current filters.",
                    icon = RuiIcons.Search,
                    actionLabel = "Load sample results",
                    onAction = { hasResults = true },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    } else {
        RuiListGroup {
            ResultRow("Squircle radii, revisited", "Design notes · 6 min read", "RL")
            RuiDivider(inset = RuiTheme.spacing.lg)
            ResultRow("Wheel picker physics", "Engineering · 11 min read", "JT")
            RuiDivider(inset = RuiTheme.spacing.lg)
            ResultRow("Tokens on a 4dp grid", "Foundations · 4 min read", "MK")
        }
        RuiText(
            text = "Clear the section to see the empty state again.",
            style = RuiTheme.typography.labelSm,
            color = RuiTheme.colors.contentMuted,
        )
    }
}

@Composable
private fun ResultRow(title: String, subtitle: String, initials: String) {
    RuiListItem(
        title = title,
        subtitle = subtitle,
        leading = { RuiAvatar(initials = initials) },
        showChevron = true,
        onClick = {},
    )
}

private fun money(cents: Int): String {
    val whole = cents / 100
    val fraction = (cents % 100).toString().padStart(2, '0')
    return "$$whole.$fraction"
}
