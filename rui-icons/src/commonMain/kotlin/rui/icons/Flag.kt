// Lucide icon "flag" (ISC), vector definition from Compose Icons (MIT).
// Regenerate with rui-icons/tools/generate.py; see NOTICE.

package rui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val lucideFlag: ImageVector
	get() {
		if (_Flag != null) {
			return _Flag!!
		}
		_Flag = ImageVector.Builder(
            name = "flag",
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
				moveTo(4f, 15f)
				reflectiveCurveToRelative(1f, -1f, 4f, -1f)
				reflectiveCurveToRelative(5f, 2f, 8f, 2f)
				reflectiveCurveToRelative(4f, -1f, 4f, -1f)
				verticalLineTo(3f)
				reflectiveCurveToRelative(-1f, 1f, -4f, 1f)
				reflectiveCurveToRelative(-5f, -2f, -8f, -2f)
				reflectiveCurveToRelative(-4f, 1f, -4f, 1f)
				close()
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
				moveTo(4f, 22f)
				lineTo(4f, 15f)
			}
		}.build()
		return _Flag!!
	}

private var _Flag: ImageVector? = null
