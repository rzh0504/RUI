package rui.sample.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rui.components.RuiDivider
import rui.components.RuiDrawer
import rui.components.RuiIconButton
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.components.rememberRuiDrawerState
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

private val DrawerDestinations = listOf("Inbox", "Sent", "Trash")

@Composable
public fun DrawerScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    val drawerState = rememberRuiDrawerState()
    var selected by rememberSaveable { mutableStateOf("Inbox") }

    RuiDrawer(
        state = drawerState,
        modifier = Modifier.fillMaxSize(),
        drawerContent = {
            RuiText(
                text = "Rui drawer",
                style = RuiTheme.typography.titleMd,
                modifier = Modifier.padding(
                    horizontal = RuiTheme.spacing.lg,
                    vertical = RuiTheme.spacing.lg,
                ),
            )
            RuiListGroup(Modifier.padding(horizontal = RuiTheme.spacing.lg)) {
                DrawerDestinations.forEachIndexed { index, destination ->
                    RuiListItem(
                        title = destination,
                        onClick = {
                            selected = destination
                            drawerState.close()
                        },
                    )
                    if (index < DrawerDestinations.lastIndex) {
                        RuiDivider(inset = RuiTheme.spacing.lg)
                    }
                }
            }
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            Column(Modifier.fillMaxWidth()) {
                SectionLabel("Inbox")
                RuiListGroup(Modifier.padding(horizontal = RuiTheme.spacing.lg)) {
                    RuiListItem(
                        leading = {
                            RuiIconButton(onClick = { drawerState.open() }) {
                                RuiIcon(imageVector = RuiIcons.Menu, contentDescription = "Menu")
                            }
                        },
                        title = selected,
                        subtitle = "Menu button or edge swipe opens the drawer",
                    )
                    RuiDivider(inset = RuiTheme.spacing.lg)
                    RuiListItem(
                        title = "Panel drag",
                        subtitle = "Drag the panel back, tap the scrim, or press Back to close",
                    )
                }
            }
        }
    }
}
