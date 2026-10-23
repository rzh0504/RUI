// Lucide icon "bluetooth" (ISC), vector definition from Compose Icons (MIT).
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

internal val lucideBluetooth: ImageVector
	get() {
		if (_Bluetooth != null) {
			return _Bluetooth!!
		}
		_Bluetooth = ImageVector.Builder(
            name = "bluetooth",
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
				moveTo(7f, 7f)
				lineToRelative(10f, 10f)
				lineToRelative(-5f, 5f)
				verticalLineTo(2f)
				lineToRelative(5f, 5f)
				lineTo(7f, 17f)
			}
		}.build()
		return _Bluetooth!!
	}

private var _Bluetooth: ImageVector? = null
