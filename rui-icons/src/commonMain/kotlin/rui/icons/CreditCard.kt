// Lucide icon "creditcard" (ISC), vector definition from Compose Icons (MIT).
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

internal val lucideCreditCard: ImageVector
	get() {
		if (_CreditCard != null) {
			return _CreditCard!!
		}
		_CreditCard = ImageVector.Builder(
            name = "credit-card",
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
				moveTo(4f, 5f)
				horizontalLineTo(20f)
				arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22f, 7f)
				verticalLineTo(17f)
				arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 20f, 19f)
				horizontalLineTo(4f)
				arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2f, 17f)
				verticalLineTo(7f)
				arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4f, 5f)
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
				moveTo(2f, 10f)
				lineTo(22f, 10f)
			}
		}.build()
		return _CreditCard!!
	}

private var _CreditCard: ImageVector? = null
