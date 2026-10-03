package rui.sample

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.unit.dp

/** Extra icons used by the catalog demos (the library ships its own in RuiIcons). */
public object SampleIcons {
    public val Close: ImageVector by lazy {
        ImageVector.Builder(
            name = "SampleClose",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathData {
                    moveTo(6f, 6f)
                    lineTo(18f, 18f)
                    moveTo(18f, 6f)
                    lineTo(6f, 18f)
                },
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }.build()
    }
}
