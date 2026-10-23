// Lucide icon "pause" (ISC), vector definition from Compose Icons (MIT).
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

internal val lucidePause: ImageVector
	get() {
		if (_Pause != null) {
			return _Pause!!
		}
		_Pause = ImageVector.Builder(
            name = "pause",
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
				moveTo(15f, 4f)
				horizontalLineTo(17f)
				arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 18f, 5f)
				verticalLineTo(19f)
				arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 17f, 20f)
				horizontalLineTo(15f)
				arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 14f, 19f)
				verticalLineTo(5f)
				arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 15f, 4f)
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
				moveTo(7f, 4f)
				horizontalLineTo(9f)
				arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 10f, 5f)
				verticalLineTo(19f)
				arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 9f, 20f)
				horizontalLineTo(7f)
				arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 6f, 19f)
				verticalLineTo(5f)
				arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 7f, 4f)
				close()
			}
		}.build()
		return _Pause!!
	}

private var _Pause: ImageVector? = null
