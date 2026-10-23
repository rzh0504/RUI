// Lucide icon "key" (ISC), vector definition from Compose Icons (MIT).
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

internal val lucideKey: ImageVector
	get() {
		if (_Key != null) {
			return _Key!!
		}
		_Key = ImageVector.Builder(
            name = "key",
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
				moveTo(15.5f, 7.5f)
				lineToRelative(2.3f, 2.3f)
				arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.4f, 0f)
				lineToRelative(2.1f, -2.1f)
				arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -1.4f)
				lineTo(19f, 4f)
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
				moveTo(21f, 2f)
				lineToRelative(-9.6f, 9.6f)
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
				moveTo(13f, 15.5f)
				arcTo(5.5f, 5.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 7.5f, 21f)
				arcTo(5.5f, 5.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2f, 15.5f)
				arcTo(5.5f, 5.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 13f, 15.5f)
				close()
			}
		}.build()
		return _Key!!
	}

private var _Key: ImageVector? = null
