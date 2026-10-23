// Lucide icon "battery" (ISC), vector definition from Compose Icons (MIT).
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

internal val lucideBattery: ImageVector
	get() {
		if (_Battery != null) {
			return _Battery!!
		}
		_Battery = ImageVector.Builder(
            name = "battery",
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
				moveTo(4f, 7f)
				horizontalLineTo(16f)
				arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 18f, 9f)
				verticalLineTo(15f)
				arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16f, 17f)
				horizontalLineTo(4f)
				arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2f, 15f)
				verticalLineTo(9f)
				arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4f, 7f)
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
				moveTo(22f, 11f)
				lineTo(22f, 13f)
			}
		}.build()
		return _Battery!!
	}

private var _Battery: ImageVector? = null
