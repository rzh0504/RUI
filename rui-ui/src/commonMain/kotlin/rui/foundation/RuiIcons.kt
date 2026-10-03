package rui.foundation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.unit.dp

/**
 * The tiny icon set Rui components need themselves. Apps bring their own icons
 * (e.g. Compose Unstyled's Lucide set) and hand them to [RuiIcon].
 */
public object RuiIcons {
    public val Check: ImageVector by lazy {
        strokedIcon("RuiCheck") {
            moveTo(5f, 12.5f)
            lineTo(9.5f, 17f)
            lineTo(19f, 7f)
        }
    }

    public val Close: ImageVector by lazy {
        strokedIcon("RuiClose") {
            moveTo(6f, 6f)
            lineTo(18f, 18f)
            moveTo(18f, 6f)
            lineTo(6f, 18f)
        }
    }

    public val ChevronLeft: ImageVector by lazy {
        strokedIcon("RuiChevronLeft") {
            moveTo(14.5f, 5.5f)
            lineTo(8f, 12f)
            lineTo(14.5f, 18.5f)
        }
    }

    public val ChevronRight: ImageVector by lazy {
        strokedIcon("RuiChevronRight") {
            moveTo(9.5f, 5.5f)
            lineTo(16f, 12f)
            lineTo(9.5f, 18.5f)
        }
    }

    private fun strokedIcon(name: String, path: PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathData { path() },
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }.build()
}
