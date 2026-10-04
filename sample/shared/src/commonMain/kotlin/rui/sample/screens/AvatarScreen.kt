package rui.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import rui.components.RuiAvatar
import rui.components.RuiAvatarColors
import rui.components.RuiAvatarSize
import rui.components.RuiDivider
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

/** A stand-in "photo" for the demo: a soft accent gradient. */
private object DemoPhotoPainter : Painter() {
    override val intrinsicSize = Size(96f, 96f)

    override fun DrawScope.onDraw() {
        drawRect(
            brush = Brush.linearGradient(
                listOf(Color(0xFF3C87FF), Color(0xFF8FD6FF)),
            ),
        )
    }
}

@Composable
public fun AvatarScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    ContentColumn(contentPadding = contentPadding) {
        SectionLabel("Sizes")
        RuiListGroup {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = RuiTheme.spacing.lg, vertical = RuiTheme.spacing.md),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                SizeDemo(size = RuiAvatarSize.Sm, caption = "Sm")
                SizeDemo(size = RuiAvatarSize.Md, caption = "Md")
                SizeDemo(size = RuiAvatarSize.Lg, caption = "Lg")
            }
        }

        SectionLabel("In list rows")
        RuiListGroup {
            RuiListItem(
                title = "Image",
                subtitle = "A photo crops to a circle",
                leading = { RuiAvatar(painter = DemoPhotoPainter, size = RuiAvatarSize.Sm) },
                showChevron = true,
                onClick = {},
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Initials",
                subtitle = "Fallback when there is no photo",
                leading = { RuiAvatar(initials = "张", size = RuiAvatarSize.Sm) },
                showChevron = true,
                onClick = {},
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Placeholder",
                subtitle = "No photo and no initials",
                leading = { RuiAvatar(size = RuiAvatarSize.Sm) },
                showChevron = true,
                onClick = {},
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Accent",
                subtitle = "Fallback colors are a slot",
                leading = {
                    RuiAvatar(
                        initials = "ZR",
                        size = RuiAvatarSize.Sm,
                        colors = RuiAvatarColors(
                            container = RuiTheme.colors.primary,
                            content = RuiTheme.colors.onPrimary,
                        ),
                    )
                },
                showChevron = true,
                onClick = {},
            )
        }
    }
}

@Composable
private fun SizeDemo(size: RuiAvatarSize, caption: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        RuiAvatar(initials = "ZR", size = size)
        Spacer(Modifier.height(RuiTheme.spacing.sm))
        RuiText(
            text = caption,
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
        )
    }
}
