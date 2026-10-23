// Lucide icon "music" (ISC), vector definition from Compose Icons (MIT).
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

internal val lucideMusic: ImageVector
	get() {
		if (_Music != null) {
			return _Music!!
		}
		_Music = ImageVector.Builder(
            name = "music",
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
				moveTo(9f, 18f)
				verticalLineTo(5f)
				lineToRelative(12f, -2f)
				verticalLineToRelative(13f)
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
				moveTo(9f, 18f)
				arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 6f, 21f)
				arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3f, 18f)
				arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 9f, 18f)
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
				moveTo(21f, 16f)
				arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 18f, 19f)
				arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 15f, 16f)
				arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 21f, 16f)
				close()
			}
		}.build()
		return _Music!!
	}

private var _Music: ImageVector? = null
