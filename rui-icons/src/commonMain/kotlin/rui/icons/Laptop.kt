// Lucide icon "laptop" (ISC), vector definition from Compose Icons (MIT).
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

internal val lucideLaptop: ImageVector
	get() {
		if (_Laptop != null) {
			return _Laptop!!
		}
		_Laptop = ImageVector.Builder(
            name = "laptop",
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
				moveTo(20f, 16f)
				verticalLineTo(7f)
				arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2f, -2f)
				horizontalLineTo(6f)
				arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2f, 2f)
				verticalLineToRelative(9f)
				moveToRelative(16f, 0f)
				horizontalLineTo(4f)
				moveToRelative(16f, 0f)
				lineToRelative(1.28f, 2.55f)
				arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.9f, 1.45f)
				horizontalLineTo(3.62f)
				arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.9f, -1.45f)
				lineTo(4f, 16f)
			}
		}.build()
		return _Laptop!!
	}

private var _Laptop: ImageVector? = null
