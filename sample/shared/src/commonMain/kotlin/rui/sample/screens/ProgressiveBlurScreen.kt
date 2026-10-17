package rui.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import rui.components.RuiAsyncImage
import rui.foundation.ProgressiveBlur
import rui.foundation.ProgressiveEdge
import rui.foundation.RuiText
import rui.foundation.blurBackdrop
import rui.foundation.progressiveBlurSurface
import rui.foundation.rememberBlurBackdrop
import rui.foundation.utils.overScrollVertical
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

/**
 * Progressive blur over locally captured backdrops: scrolling content dies
 * into a glass edge instead of stopping under a uniform veil — strongest at
 * the overlaid edge, easing to clear. The Bottom preset arms the bottom edge,
 * Top the status-bar side; the media block fades across its leading edge so
 * a title can sit on it.
 *
 * Each glass surface samples a backdrop whose capture node it stands *next
 * to*, never inside — a surface inside its own capture's subtree would make
 * the captured layer draw itself.
 */
@Composable
public fun ProgressiveBlurScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    val colors = RuiTheme.colors
    val spacing = RuiTheme.spacing
    val scrollState = rememberScrollState()
    val columnBackdrop = rememberBlurBackdrop capture@{
        drawRect(colors.background)
        // The scroll moves its content as a draw-time canvas translation
        // outside this capture — a layer record starts at identity and would
        // freeze the scroll-0 layout — so the capture applies the scroll by
        // hand to match what is actually on screen behind the ramps.
        translate(0f, -scrollState.value.toFloat()) {
            this@capture.drawContent()
        }
    }
    val imageBackdrop = rememberBlurBackdrop {
        drawRect(colors.surfaceMuted)
        drawContent()
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .overScrollVertical()
                .verticalScroll(scrollState)
                .blurBackdrop(columnBackdrop, redrawKey = { scrollState.value })
                .padding(
                    top = contentPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding(),
                )
                .padding(horizontal = spacing.xl, vertical = spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.lg),
        ) {
            SectionLabel("Bottom edge")
            RuiText(
                text = "Scroll — the content below dies into the bottom edge: " +
                    "blur strongest there, easing to clear over the band. The " +
                    "old uniform scrim becomes a gradient of blur strength.",
                style = RuiTheme.typography.bodySm,
                color = colors.contentMuted,
            )
            ColorStripRow()

            SectionLabel("Top edge")
            RuiText(
                text = "The same ramp inverted: full strength at the top, for " +
                    "content passing under a status bar or app bar.",
                style = RuiTheme.typography.bodySm,
                color = colors.contentMuted,
            )
            ColorStripRow()

            SectionLabel("Leading edge of media")
            RuiText(
                text = "On an image, a leading-edge ramp carries a title that " +
                    "stays readable without covering the picture.",
                style = RuiTheme.typography.bodySm,
                color = colors.contentMuted,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RuiTheme.shapes.md),
            ) {
                RuiAsyncImage(
                    model = "https://picsum.photos/id/1011/800/400",
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .blurBackdrop(imageBackdrop),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .width(150.dp)
                        .progressiveBlurSurface(
                            backdrop = imageBackdrop,
                            progression = ProgressiveBlur(edge = ProgressiveEdge.Left),
                            radius = 20.dp,
                            tint = colors.background.copy(alpha = 0.2f),
                        ),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    RuiText(
                        text = "Fading leading edge",
                        style = RuiTheme.typography.titleLg,
                        color = colors.content,
                        modifier = Modifier.padding(horizontal = spacing.lg),
                    )
                }
            }

            // Extra length so the edge fades read while scrolling.
            ColorStripRow()
            ColorStripRow()
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(130.dp)
                .progressiveBlurSurface(
                    backdrop = columnBackdrop,
                    progression = ProgressiveBlur(edge = ProgressiveEdge.Top),
                    radius = 22.dp,
                    tint = colors.background.copy(alpha = 0.25f),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(150.dp)
                .progressiveBlurSurface(
                    backdrop = columnBackdrop,
                    progression = ProgressiveBlur(edge = ProgressiveEdge.Bottom),
                    radius = 24.dp,
                    tint = colors.background.copy(alpha = 0.25f),
                ),
        )
    }
}

/** Tall tinted blocks whose edges visibly smear as they pass under a ramp. */
@Composable
private fun ColorStripRow() {
    val swatches = listOf(
        Color(0xFF3C87FF),
        Color(0xFF34C77B),
        Color(0xFFFF9F45),
        Color(0xFFAF6BFF),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.spacedBy(RuiTheme.spacing.sm),
    ) {
        swatches.forEachIndexed { index, color ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RuiTheme.shapes.md)
                    .background(color.copy(alpha = if (index % 2 == 0) 0.85f else 0.55f)),
            )
        }
    }
}
