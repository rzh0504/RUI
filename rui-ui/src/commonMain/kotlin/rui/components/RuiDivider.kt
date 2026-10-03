package rui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import rui.theme.RuiTheme

/**
 * Hairline separator between rows or sections. [inset] shifts the line past
 * the row's leading edge inside grouped lists.
 */
@Composable
public fun RuiDivider(modifier: Modifier = Modifier, inset: Dp = 0.dp) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = inset)
            .height(0.8.dp)
            .background(RuiTheme.colors.outlineSubtle),
    )
}
