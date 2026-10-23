// Lucide icon "shrink" (ISC), vector definition from Compose Icons (MIT).
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

internal val lucideShrink: ImageVector
	get() {
		if (_Shrink != null) {
			return _Shrink!!
		}
		_Shrink = ImageVector.Builder(
            name = "shrink",
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
				moveTo(15f, 15f)
				lineToRelative(6f, 6f)
				moveToRelative(-6f, -6f)
				verticalLineToRelative(4.8f)
				moveToRelative(0f, -4.8f)
				horizontalLineToRelative(4.8f)
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
				moveTo(9f, 19.8f)
				verticalLineTo(15f)
				moveToRelative(0f, 0f)
				horizontalLineTo(4.2f)
				moveTo(9f, 15f)
				lineToRelative(-6f, 6f)
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
				moveTo(15f, 4.2f)
				verticalLineTo(9f)
				moveToRelative(0f, 0f)
				horizontalLineToRelative(4.8f)
				moveTo(15f, 9f)
				lineToRelative(6f, -6f)
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
				moveTo(9f, 4.2f)
				verticalLineTo(9f)
				moveToRelative(0f, 0f)
				horizontalLineTo(4.2f)
				moveTo(9f, 9f)
				lineTo(3f, 3f)
			}
		}.build()
		return _Shrink!!
	}

private var _Shrink: ImageVector? = null
