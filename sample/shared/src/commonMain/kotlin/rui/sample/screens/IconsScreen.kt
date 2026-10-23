package rui.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import rui.components.RuiListGroup
import rui.foundation.RuiIcon
import rui.foundation.RuiText
import rui.icons.RuiCommonIcons
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

/**
 * The catalogue's icon gallery: every curated glyph with its semantic name.
 * Serves as the visual audit for the optional rui-icons module.
 */
@Composable
public fun IconsScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    val icons = RuiCommonIcons.All
    ContentColumn(contentPadding = contentPadding) {
        RuiText(
            text = "A curated Lucide set: ${icons.size} common glyphs with " +
                "semantic Rui names. Every icon is a 24dp vector with 2dp rounded " +
                "strokes; directional glyphs mirror in RTL.",
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
        )

        SectionLabel("Common icons")
        RuiListGroup {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(RuiTheme.spacing.lg),
            ) {
                val columns = (maxWidth / 92.dp).toInt().coerceIn(3, 6)
                Column(verticalArrangement = Arrangement.spacedBy(RuiTheme.spacing.md)) {
                    icons.chunked(columns).forEach { rowIcons ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(RuiTheme.spacing.sm),
                            verticalAlignment = Alignment.Top,
                        ) {
                            rowIcons.forEach { icon ->
                                IconCell(
                                    name = icon.name,
                                    image = icon.image,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            repeat(columns - rowIcons.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IconCell(
    name: String,
    image: ImageVector,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(RuiTheme.spacing.xs),
    ) {
        RuiIcon(imageVector = image, contentDescription = null, size = 24.dp)
        RuiText(
            text = name,
            style = RuiTheme.typography.labelSm,
            color = RuiTheme.colors.contentMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}
