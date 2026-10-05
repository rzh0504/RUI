package rui.sample.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rui.components.RuiDivider
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.components.RuiPullToRefresh
import rui.foundation.RuiText
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

@Composable
public fun PullToRefreshScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    var refreshing by remember { mutableStateOf(false) }
    var refreshCount by rememberSaveable { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        RuiPullToRefresh(
            isRefreshing = refreshing,
            onRefresh = {
                scope.launch {
                    delay(1500)
                    refreshCount++
                    refreshing = false
                }
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                RuiText(
                    text = "Pull the list down past the trigger — the arc grows with " +
                        "the pull, release to refresh. The content is a plain scrollable " +
                        "so the gesture owns the overscroll.",
                    style = RuiTheme.typography.bodySm,
                    color = RuiTheme.colors.contentMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = RuiTheme.spacing.lg, vertical = RuiTheme.spacing.md),
                )
                RuiListGroup(Modifier.padding(horizontal = RuiTheme.spacing.lg)) {
                    RuiListItem(
                        title = "Programmatic refresh",
                        subtitle = "Raises isRefreshing without a gesture",
                        showChevron = true,
                        onClick = {
                            scope.launch {
                                refreshing = true
                                delay(2000)
                                refreshing = false
                            }
                        },
                    )
                    RuiDivider(inset = RuiTheme.spacing.lg)
                    RuiListItem(
                        title = "Refreshed $refreshCount times",
                        subtitle = "Each refresh bumps this counter",
                    )
                }
                Spacer(Modifier.height(RuiTheme.spacing.md))
                RuiListGroup(Modifier.padding(horizontal = RuiTheme.spacing.lg)) {
                    repeat(8) { index ->
                        RuiListItem(title = "Item ${index + 1}")
                        if (index < 7) RuiDivider(inset = RuiTheme.spacing.lg)
                    }
                }
                Spacer(Modifier.height(RuiTheme.spacing.xl))
            }
        }
    }
}
