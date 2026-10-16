package rui.sample.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import rui.components.RuiButton
import rui.components.RuiDivider
import rui.components.RuiInlineBanner
import rui.components.RuiInlineBannerVariant
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.components.RuiStepper
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.theme.RuiTheme

/** Cart page: line items with steppers, a live order summary and checkout. */
@Composable
public fun CartScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    val spacing = RuiTheme.spacing
    val products = remember { listOf("Aurora desk lamp" to 4200, "Linen throw" to 2800, "Oak tray" to 1800) }
    var quantities by remember { mutableStateOf(listOf(1, 2, 0)) }
    var placed by remember { mutableStateOf(false) }

    val subtotalCents = products.indices.sumOf { quantities[it] * products[it].second }
    val shippingCents = if (subtotalCents >= 5000 || subtotalCents == 0) 0 else 500

    ContentColumn(contentPadding = contentPadding) {
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
}

private fun money(cents: Int): String {
    val whole = cents / 100
    val fraction = (cents % 100).toString().padStart(2, '0')
    return "$$whole.$fraction"
}
