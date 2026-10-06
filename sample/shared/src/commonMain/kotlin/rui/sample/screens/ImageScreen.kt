package rui.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import rui.components.RuiAsyncImage
import rui.components.RuiButton
import rui.components.RuiButtonSize
import rui.components.RuiButtonVariant
import rui.foundation.RuiSurface
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

// Demo photography from picsum's fixed-ID endpoints (stable content per ID).
private const val PhotoBase = "https://picsum.photos/id"

@Composable
public fun ImageScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    var retry by remember { mutableIntStateOf(0) }

    ContentColumn(contentPadding = contentPadding) {
        RuiText(
            text = "RuiAsyncImage loads over the network on Coil — memory and " +
                "disk caching, request cancellation and the crossfade come " +
                "with it. While a request is in flight a skeleton bone " +
                "breathes in the image's place; failures swap in a muted " +
                "placeholder unless you override the slot.",
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
            modifier = Modifier.fillMaxWidth(),
        )

        SectionLabel("Gallery")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(RuiTheme.spacing.sm),
        ) {
            listOf(1015, 1025, 1039).forEach { id ->
                RuiSurface(modifier = Modifier.weight(1f), shape = RuiTheme.shapes.md) {
                    RuiAsyncImage(
                        model = "$PhotoBase/$id/600/600",
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
        RuiSurface {
            RuiAsyncImage(
                model = "$PhotoBase/1036/1200/400",
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop,
            )
        }

        SectionLabel("Failure and retry")
        RuiText(
            text = "A request that cannot resolve falls into the error slot — " +
                "here overridden with an explainer and a retry that re-issues " +
                "the request.",
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
            modifier = Modifier.fillMaxWidth(),
        )
        RuiSurface {
            RuiAsyncImage(
                model = "https://unreachable.rui.test/broken.jpg?attempt=$retry",
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop,
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(RuiTheme.colors.surfaceMuted),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(RuiTheme.spacing.sm),
                        ) {
                            RuiText(
                                text = "The image could not be loaded.",
                                style = RuiTheme.typography.bodySm,
                                color = RuiTheme.colors.contentMuted,
                            )
                            RuiButton(
                                onClick = { retry++ },
                                variant = RuiButtonVariant.Tonal,
                                size = RuiButtonSize.Sm,
                            ) {
                                RuiText("Retry")
                            }
                        }
                    }
                },
            )
        }
    }
}
