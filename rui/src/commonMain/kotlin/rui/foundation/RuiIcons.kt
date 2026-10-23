package rui.foundation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.unit.dp

/**
 * The tiny icon set Rui components need themselves. Apps bring their own icons
 * (the optional rui-icons module wraps a curated Lucide set) and hand them to
 * [RuiIcon]. Direction-dependent glyphs carry [ImageVector.autoMirror], so they
 * flip in RTL layouts.
 */
public object RuiIcons {
    public val Check: ImageVector by lazy {
        strokedIcon("RuiCheck") {
            moveTo(5f, 12.5f)
            lineTo(9.5f, 17f)
            lineTo(19f, 7f)
        }
    }

    public val Close: ImageVector by lazy {
        strokedIcon("RuiClose") {
            moveTo(6f, 6f)
            lineTo(18f, 18f)
            moveTo(18f, 6f)
            lineTo(6f, 18f)
        }
    }

    public val ChevronLeft: ImageVector by lazy {
        strokedIcon("RuiChevronLeft", autoMirror = true) {
            moveTo(14.5f, 5.5f)
            lineTo(8f, 12f)
            lineTo(14.5f, 18.5f)
        }
    }

    public val ChevronRight: ImageVector by lazy {
        strokedIcon("RuiChevronRight", autoMirror = true) {
            moveTo(9.5f, 5.5f)
            lineTo(16f, 12f)
            lineTo(9.5f, 18.5f)
        }
    }

    public val Grid: ImageVector by lazy {
        strokedIcon("RuiGrid") {
            moveTo(4.5f, 4.5f)
            lineTo(10f, 4.5f)
            lineTo(10f, 10f)
            lineTo(4.5f, 10f)
            close()
            moveTo(14f, 4.5f)
            lineTo(19.5f, 4.5f)
            lineTo(19.5f, 10f)
            lineTo(14f, 10f)
            close()
            moveTo(4.5f, 14f)
            lineTo(10f, 14f)
            lineTo(10f, 19.5f)
            lineTo(4.5f, 19.5f)
            close()
            moveTo(14f, 14f)
            lineTo(19.5f, 14f)
            lineTo(19.5f, 19.5f)
            lineTo(14f, 19.5f)
            close()
        }
    }

    public val Swatch: ImageVector by lazy {
        strokedIcon("RuiSwatch") {
            moveTo(20f, 12f)
            arcTo(8f, 8f, 0f, true, true, 4f, 12f)
            arcTo(8f, 8f, 0f, true, true, 20f, 12f)
        }
    }

    public val Search: ImageVector by lazy {
        strokedIcon("RuiSearch") {
            moveTo(18f, 11f)
            arcTo(7f, 7f, 0f, true, true, 4f, 11f)
            arcTo(7f, 7f, 0f, true, true, 18f, 11f)
            moveTo(16.2f, 16.2f)
            lineTo(20.5f, 20.5f)
        }
    }

    public val Plus: ImageVector by lazy {
        strokedIcon("RuiPlus") {
            moveTo(12f, 5f)
            lineTo(12f, 19f)
            moveTo(5f, 12f)
            lineTo(19f, 12f)
        }
    }

    public val Minus: ImageVector by lazy {
        strokedIcon("RuiMinus") {
            moveTo(5f, 12f)
            lineTo(19f, 12f)
        }
    }

    public val Menu: ImageVector by lazy {
        strokedIcon("RuiMenu") {
            moveTo(4f, 7f)
            lineTo(20f, 7f)
            moveTo(4f, 12f)
            lineTo(20f, 12f)
            moveTo(4f, 17f)
            lineTo(20f, 17f)
        }
    }

    public val Layers: ImageVector by lazy {
        strokedIcon("RuiLayers") {
            moveTo(12f, 4f)
            lineTo(20f, 8.5f)
            lineTo(12f, 13f)
            lineTo(4f, 8.5f)
            close()
            moveTo(4.5f, 13.5f)
            lineTo(12f, 17.5f)
            lineTo(19.5f, 13.5f)
        }
    }

    /** Solid play triangle — the transport glyphs read filled, not stroked. */
    public val Play: ImageVector by lazy {
        filledIcon("RuiPlay") {
            moveTo(8.8f, 5.8f)
            lineTo(8.8f, 18.2f)
            lineTo(19f, 12f)
            close()
        }
    }

    public val Pause: ImageVector by lazy {
        strokedIcon("RuiPause", width = 3.6f) {
            moveTo(9.4f, 6.2f)
            lineTo(9.4f, 17.8f)
            moveTo(14.6f, 6.2f)
            lineTo(14.6f, 17.8f)
        }
    }

    public val ChevronDown: ImageVector by lazy {
        strokedIcon("RuiChevronDown") {
            moveTo(6f, 9.5f)
            lineTo(12f, 15.5f)
            lineTo(18f, 9.5f)
        }
    }

    public val SkipBack: ImageVector by lazy {
        transportIcon("RuiSkipBack", forward = false, autoMirror = true)
    }

    public val SkipForward: ImageVector by lazy {
        transportIcon("RuiSkipForward", forward = true, autoMirror = true)
    }

    private fun strokedIcon(
        name: String,
        width: Float = 2.2f,
        autoMirror: Boolean = false,
        path: PathBuilder.() -> Unit,
    ): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
            autoMirror = autoMirror,
        ).apply {
            addPath(
                pathData = PathData { path() },
                stroke = SolidColor(Color.Black),
                strokeLineWidth = width,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }.build()

    /** A filled glyph whose corners stay round: fill plus a matching stroke. */
    private fun filledIcon(name: String, path: PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathData { path() },
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.2f,
                strokeLineJoin = StrokeJoin.Round,
            )
        }.build()

    /** The music transport's filled triangle plus bar, mirrored without transforms. */
    private fun transportIcon(name: String, forward: Boolean, autoMirror: Boolean = false): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
            autoMirror = autoMirror,
        ).apply {
            addPath(
                pathData = PathData {
                    if (forward) {
                        moveTo(6.9f, 5.9f)
                        lineTo(6.9f, 18.1f)
                        lineTo(16.2f, 12f)
                        close()
                    } else {
                        moveTo(17.1f, 5.9f)
                        lineTo(17.1f, 18.1f)
                        lineTo(7.8f, 12f)
                        close()
                    }
                },
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.2f,
                strokeLineJoin = StrokeJoin.Round,
            )
            addPath(
                pathData = PathData {
                    if (forward) {
                        moveTo(19.4f, 5.9f)
                        lineTo(19.4f, 18.1f)
                    } else {
                        moveTo(4.6f, 5.9f)
                        lineTo(4.6f, 18.1f)
                    }
                },
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.6f,
                strokeLineCap = StrokeCap.Round,
            )
        }.build()
}
