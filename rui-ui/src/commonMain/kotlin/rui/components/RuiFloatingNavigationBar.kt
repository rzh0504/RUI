package rui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import rui.foundation.BlurBackdrop
import rui.foundation.RuiIcon
import rui.foundation.RuiText
import rui.foundation.blurSurface
import rui.foundation.platform.isBlurSupported
import rui.foundation.ruiMinimumTouchTarget
import rui.theme.RuiTheme
import rui.theme.toShadowShape
import kotlin.math.roundToInt

/** Geometry and chrome constants of [RuiFloatingNavigationBar] — the bar's own visuals, beside the component. */
public object RuiFloatingNavigationBarDefaults {

    /** Height of the floating capsule. */
    public val BarHeight: Dp = 56.dp

    /** Height of one tab slot inside the capsule. */
    public val RowHeight: Dp = 48.dp

    /** Air between the capsule edge and the tab slots. */
    public val InnerPadding: Dp = 4.dp

    /** Air between the capsule and the screen edges. */
    public val HorizontalMargin: Dp = 36.dp

    /** Air above the capsule. */
    public val TopMargin: Dp = 6.dp

    /** Air between the capsule and the navigation-bar inset — the floating look. */
    public val FloatingGap: Dp = 12.dp

    /** The capsule never widens past this, however wide the window (tablets, desktop). */
    public val MaxWidth: Dp = 480.dp

    /** How far the bar slides down when hidden. */
    public val ExitOffset: Dp = 84.dp

    /** Elevation of the capsule's drop shadow. */
    public val ShadowElevation: Dp = 7.dp

    /** Alpha of the black shadow tint at rest. */
    public val ShadowAlpha: Float = 0.2f

    /** Tint of the selection pill: the accent washed over the capsule. */
    public const val IndicatorAlpha: Float = 0.12f

    /** Blur radius of the frosted glass. */
    public val BlurRadius: Dp = 32.dp

    /** Tab icon size — the nav bar's icon metric, shared with the docked bar. */
    public val IconSize: Dp = 22.dp
}

/**
 * A floating bottom navigation bar: a capsule hovering above the content,
 * frosted like the docked [RuiNavigationBar] — with a [backdrop] it blurs the
 * content behind itself, without one it is an honest translucent capsule.
 * Place inside [RuiScaffold]'s `bottomBar` slot; the scaffold reports the
 * capsule plus its surrounding air as content padding, so lists scroll under
 * the floating bar and never end up hidden behind it.
 *
 * The current destination is marked by a pill in the accent color sliding
 * between the equal-width slots; the selected slot's icon and label read in
 * the accent, the rest in the muted content color. Items share the
 * [RuiNavigationBarItem] model of the docked bar: a slot with a `null` icon
 * renders label only, a blank label renders icon only.
 *
 * [visible] = false hides the bar: it slides down under the navigation-bar
 * inset while tint, border and items fade, and its slots stop responding —
 * the reserved space stays, so the layout never jumps. Tapping the
 * already-selected tab re-invokes [onSelect] with its index, so
 * scroll-to-top patterns keep working; [selected] outside `items.indices`
 * selects nothing and parks the pill; an empty [items] renders an empty
 * capsule.
 */
@Composable
public fun RuiFloatingNavigationBar(
    items: List<RuiNavigationBarItem>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    backdrop: BlurBackdrop? = null,
    containerColor: Color = RuiTheme.colors.surfaceElevated,
    indicatorColor: Color = RuiTheme.colors.primary.copy(alpha = RuiFloatingNavigationBarDefaults.IndicatorAlpha),
) {
    val colors = RuiTheme.colors
    val typography = RuiTheme.typography
    val motion = RuiTheme.motion
    val density = LocalDensity.current
    val capsule = RuiTheme.shapes.full
    val shadowShape = capsule.toShadowShape()

    // Hide/show: the capsule slides under the navigation-bar inset while
    // tint, border and items fade. The shadow fades through its color, never
    // through a layer alpha — an alpha'd layer composites offscreen and crops
    // the shadow square (the snackbar's lesson).
    val exitPx = remember(density) {
        with(density) { RuiFloatingNavigationBarDefaults.ExitOffset.toPx() }
    }
    val slide = remember { Animatable(if (visible) 0f else exitPx) }
    LaunchedEffect(visible, exitPx) {
        slide.animateTo(if (visible) 0f else exitPx, animationSpec = motion.settle)
    }
    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = motion.normal,
        label = "rui_floating_navigation_bar_alpha",
    )

    // Selection pill: the items are equal-width slots, so the pill is one
    // slot wide and slides by index; RTL walks backwards from the start edge.
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    var capsuleWidthPx by remember { mutableIntStateOf(0) }
    val innerPx = with(density) { RuiFloatingNavigationBarDefaults.InnerPadding.toPx() }
    val slotWidthPx = floatingNavBarSlotWidthPx(capsuleWidthPx.toFloat(), innerPx, items.size)
    val position = animateFloatAsState(
        targetValue = selected.coerceIn(0, (items.size - 1).coerceAtLeast(0)).toFloat(),
        animationSpec = motion.settle,
        label = "rui_floating_navigation_bar_indicator",
    )

    // The glass recipe of FrostedSurface: blur + 72% tint with a backdrop, an
    // honest 85% translucent tint without one.
    val useBlur = backdrop != null && isBlurSupported
    val tint = if (useBlur) {
        containerColor.copy(alpha = 0.72f)
    } else {
        containerColor.copy(alpha = 0.85f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                start = RuiFloatingNavigationBarDefaults.HorizontalMargin,
                end = RuiFloatingNavigationBarDefaults.HorizontalMargin,
                top = RuiFloatingNavigationBarDefaults.TopMargin,
                bottom = RuiFloatingNavigationBarDefaults.FloatingGap,
            )
            .graphicsLayer { translationY = slide.value },
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .widthIn(max = RuiFloatingNavigationBarDefaults.MaxWidth),
        ) {
            Box(
                modifier = Modifier
                    .onSizeChanged { capsuleWidthPx = it.width }
                    .fillMaxWidth()
                    .height(RuiFloatingNavigationBarDefaults.BarHeight)
                    .graphicsLayer {
                        shape = shadowShape
                        clip = false
                        shadowElevation =
                            with(density) { RuiFloatingNavigationBarDefaults.ShadowElevation.toPx() }
                        val shadowAlpha = RuiFloatingNavigationBarDefaults.ShadowAlpha * contentAlpha
                        ambientShadowColor = Color.Black.copy(alpha = shadowAlpha)
                        spotShadowColor = Color.Black.copy(alpha = shadowAlpha)
                    }
                    .graphicsLayer { alpha = contentAlpha }
                    .then(
                        if (useBlur) {
                            Modifier.blurSurface(
                                backdrop = backdrop,
                                radius = RuiFloatingNavigationBarDefaults.BlurRadius,
                                tint = tint,
                                shape = capsule,
                            )
                        } else {
                            Modifier.background(tint, capsule)
                        },
                    )
                    .border(RuiTheme.dimens.borderWidth, colors.outlineSubtle, capsule),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (slotWidthPx > 0f && selected in items.indices) {
                    Box(
                        modifier = Modifier
                            .offset {
                                val x = floatingNavBarIndicatorX(
                                    fraction = position.value,
                                    slotWidthPx = slotWidthPx,
                                    isLtr = isLtr,
                                )
                                IntOffset((innerPx + x).roundToInt(), 0)
                            }
                            .width(with(density) { slotWidthPx.toDp() })
                            .height(RuiFloatingNavigationBarDefaults.RowHeight)
                            .background(indicatorColor, capsule),
                    )
                }

                Row(
                    modifier = Modifier
                        .padding(RuiFloatingNavigationBarDefaults.InnerPadding)
                        .height(RuiFloatingNavigationBarDefaults.RowHeight)
                        .fillMaxWidth()
                        .selectableGroup(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    items.forEachIndexed { index, item ->
                        val isSelected = index == selected
                        val itemColor = if (isSelected) colors.primary else colors.contentMuted
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .ruiMinimumTouchTarget()
                                // No press indication: iOS tab bars don't ripple.
                                .selectable(
                                    selected = isSelected,
                                    enabled = visible,
                                    interactionSource = null,
                                    indication = null,
                                    role = Role.Tab,
                                ) { onSelect(index) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                if (item.icon != null) {
                                    RuiIcon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        size = RuiFloatingNavigationBarDefaults.IconSize,
                                        tint = itemColor,
                                    )
                                }
                                RuiText(
                                    text = item.label,
                                    style = typography.labelSm,
                                    color = itemColor,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else null,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Width of one tab slot: the capsule minus the [innerPaddingPx] ring, split
 * evenly; never negative, and zero without items.
 */
internal fun floatingNavBarSlotWidthPx(capsuleWidthPx: Float, innerPaddingPx: Float, itemCount: Int): Float =
    if (itemCount > 0) {
        ((capsuleWidthPx - innerPaddingPx * 2) / itemCount).coerceAtLeast(0f)
    } else {
        0f
    }

/**
 * X offset of the selection pill for [fraction] slots, measured from the
 * capsule's start edge — positive along the reading direction, mirrored
 * (negative screen x, the pill leaving the start edge leftwards) in RTL.
 */
internal fun floatingNavBarIndicatorX(fraction: Float, slotWidthPx: Float, isLtr: Boolean): Float =
    if (isLtr) fraction * slotWidthPx else -fraction * slotWidthPx
