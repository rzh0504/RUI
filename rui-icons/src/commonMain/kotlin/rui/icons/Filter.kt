// Lucide icon "filter" (ISC), vector definition from Compose Icons (MIT).
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

internal val lucideFilter: ImageVector
	get() {
		if (_Filter != null) {
			return _Filter!!
		}
		_Filter = ImageVector.Builder(
            name = "filter",
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
				moveTo(22f, 3f)
				lineTo(2f, 3f)
				lineTo(10f, 12.46f)
				lineTo(10f, 19f)
				lineTo(14f, 21f)
				lineTo(14f, 12.46f)
				lineTo(22f, 3f)
				close()
			}
		}.build()
		return _Filter!!
	}

private var _Filter: ImageVector? = null
