// Lucide icon "brush" (ISC), vector definition from Compose Icons (MIT).
// Regenerate with rui-icons/tools/generate.py; see NOTICE.

package rui.icons

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val lucideBrush: ImageVector
	get() {
		if (_Brush != null) {
			return _Brush!!
		}
		_Brush = ImageVector.Builder(
            name = "brush",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
			path(
    			fill = null,
    			fillAlpha = 1.0f,
    			stroke = SolidColor(Color(0xFF000000)),
    			strokeAlpha = 1.0f,
    			strokeLineWidth = 2f,
    			strokeLineCap = StrokeCap.Round,
    			strokeLineJoin = StrokeJoin.Round,
    			strokeLineMiter = 1.0f,
    			pathFillType = PathFillType.NonZero
			) {
				moveTo(9.06f, 11.9f)
				lineToRelative(8.07f, -8.06f)
				arcToRelative(2.85f, 2.85f, 0f, isMoreThanHalf = true, isPositiveArc = true, 4.03f, 4.03f)
				lineToRelative(-8.06f, 8.08f)
			}
			path(
    			fill = null,
    			fillAlpha = 1.0f,
    			stroke = SolidColor(Color(0xFF000000)),
    			strokeAlpha = 1.0f,
    			strokeLineWidth = 2f,
    			strokeLineCap = StrokeCap.Round,
    			strokeLineJoin = StrokeJoin.Round,
    			strokeLineMiter = 1.0f,
    			pathFillType = PathFillType.NonZero
			) {
				moveTo(7.07f, 14.94f)
				curveToRelative(-1.66f, 0f, -3f, 1.35f, -3f, 3.02f)
				curveToRelative(0f, 1.33f, -2.5f, 1.52f, -2f, 2.02f)
				curveToRelative(1.08f, 1.1f, 2.49f, 2.02f, 4f, 2.02f)
				curveToRelative(2.2f, 0f, 4f, -1.8f, 4f, -4.04f)
				arcToRelative(3.01f, 3.01f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3f, -3.02f)
				close()
			}
		}.build()
		return _Brush!!
	}

private var _Brush: ImageVector? = null
