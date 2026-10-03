package rui.foundation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.unit.dp

/**
 * The tiny icon set Rui components need themselves. Apps bring their own icons
 * (e.g. Compose Unstyled's Lucide set) and hand them to [RuiIcon].
 */
public object RuiIcons {
    public val Check: ImageVector by lazy {
        ImageVector.Builder(
            name = "RuiCheck",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathData {
                    moveTo(5f, 12.5f)
                    lineTo(9.5f, 17f)
                    lineTo(19f, 7f)
                },
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }.build()
    }
}
