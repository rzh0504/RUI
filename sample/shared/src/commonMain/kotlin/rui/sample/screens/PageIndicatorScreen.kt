package rui.sample.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import rui.components.RuiPageIndicator
import rui.foundation.RuiSurface
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

@Composable
public fun PageIndicatorScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 1) { 5 }

    ContentColumn(contentPadding = contentPadding) {
        RuiText(
            text = "Dots for paged content — the current page stretches into a " +
                "primary pill. Swipe the pages, or tap a dot to jump; the " +
                "indicator is wired to the pager's state.",
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
            modifier = Modifier.fillMaxWidth(),
        )

        SectionLabel("Pager")
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
        ) { page ->
            Box(Modifier.fillMaxSize().padding(horizontal = RuiTheme.spacing.sm)) {
                RuiSurface(modifier = Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        RuiText(
                            text = "Page ${page + 1}",
                            style = RuiTheme.typography.titleMd,
                            color = RuiTheme.colors.contentMuted,
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RuiPageIndicator(
                pageCount = 5,
                currentPage = pagerState.currentPage,
                onPageSelect = { page ->
                    scope.launch { pagerState.animateScrollToPage(page) }
                },
                modifier = Modifier.padding(top = RuiTheme.spacing.md),
            )
        }
    }
}
