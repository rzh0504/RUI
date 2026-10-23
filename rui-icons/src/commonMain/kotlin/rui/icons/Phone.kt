// Lucide icon "phone" (ISC), vector definition from Compose Icons (MIT).
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

internal val lucidePhone: ImageVector
	get() {
		if (_Phone != null) {
			return _Phone!!
		}
		_Phone = ImageVector.Builder(
            name = "phone",
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
				moveTo(22f, 16.92f)
				verticalLineToRelative(3f)
				arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2.18f, 2f)
				arcToRelative(19.79f, 19.79f, 0f, isMoreThanHalf = false, isPositiveArc = true, -8.63f, -3.07f)
				arcToRelative(19.5f, 19.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, -6f, -6f)
				arcToRelative(19.79f, 19.79f, 0f, isMoreThanHalf = false, isPositiveArc = true, -3.07f, -8.67f)
				arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4.11f, 2f)
				horizontalLineToRelative(3f)
				arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2f, 1.72f)
				arcToRelative(12.84f, 12.84f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.7f, 2.81f)
				arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.45f, 2.11f)
				lineTo(8.09f, 9.91f)
				arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6f, 6f)
				lineToRelative(1.27f, -1.27f)
				arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2.11f, -0.45f)
				arcToRelative(12.84f, 12.84f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.81f, 0.7f)
				arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22f, 16.92f)
				close()
			}
		}.build()
		return _Phone!!
	}

private var _Phone: ImageVector? = null
