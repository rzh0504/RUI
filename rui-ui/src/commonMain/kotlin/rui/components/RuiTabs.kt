package rui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.composeunstyled.Tab
import com.composeunstyled.TabList
import com.composeunstyled.TabPanel
import com.composeunstyled.UnstyledTabGroup
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import rui.foundation.RuiText
import rui.theme.RuiTheme
import kotlin.math.roundToInt

/**
 * The Rui tab group: Unstyled's roving-focus, keyboard-navigable tab behavior
 * rendered as a segmented control — a muted track with a raised selection
 * pill that slides to the activated segment (the shared settle token) while
 * the label colors crossfade.
 *
 * With [fillWidth] the segments share the available width evenly (the common
 * case); otherwise each hugs its label. Labels wrap to up to [maxLines] lines
 * and ellipsize beyond that — the visible truncation never hides the text from
 * assistive technology, which reads the full label. All pills share the track
 * height (the tallest label's), so a wrapping tab never leaves its neighbors
 * shorter. Segments never scroll — iOS segmented controls truncate, they don't
 * pan; with [fillWidth] = false an oversized label group clips at the track.
 *
 * Tab panels are composed through [content] for the activated tab key.
 */
@Composable
public fun <T> RuiTabGroup(
    selectedTab: T,
    onSelectedTabChange: (T) -> Unit,
    tabs: List<T>,
    modifier: Modifier = Modifier,
    fillWidth: Boolean = true,
    labelStyle: TextStyle = RuiTheme.typography.labelLg,
    maxLines: Int = 2,
    label: (T) -> String,
    content: @Composable (T) -> Unit,
) {
    val colors = RuiTheme.colors
    val spacing = RuiTheme.spacing
    val motion = RuiTheme.motion
    val density = LocalDensity.current

    UnstyledTabGroup(
        selectedTab = selectedTab,
        onSelectedTabChange = onSelectedTabChange,
        tabs = tabs,
        modifier = modifier,
    ) {
        Column {
            TabList(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RuiTheme.shapes.full)
                        .background(colors.surfaceMuted)
                        .padding(2.dp),
                ) {
                    // Each tab's bounds in the track's content space. Measured
                    // in root space against the track's own origin because Tab
                    // hosts the content behind an internal wrapper whose
                    // parent is not the track; one geometry source covers both
                    // fillWidth modes.
                    var trackOriginX by remember { mutableFloatStateOf(0f) }
                    var trackHeightPx by remember { mutableFloatStateOf(0f) }
                    val tabMetrics = remember(tabs.size) {
                        mutableStateListOf<Pair<Float, Float>?>().apply {
                            repeat(tabs.size) { add(null) }
                        }
                    }

                    val selectedIndex = tabs.indexOf(selectedTab).coerceIn(0, tabs.size - 1)
                    val selectedGeometry = tabMetrics.getOrNull(selectedIndex)

                    // The pill appears with the first measured frame — no
                    // wipe-in; after that every selection change slides it.
                    val pillLeft = remember { Animatable(0f) }
                    val pillWidth = remember { Animatable(0f) }
                    var placed by remember { mutableStateOf(false) }
                    LaunchedEffect(selectedGeometry) {
                        val (left, width) = selectedGeometry ?: return@LaunchedEffect
                        if (placed) {
                            coroutineScope {
                                launch { pillLeft.animateTo(left, motion.settle) }
                                launch { pillWidth.animateTo(width, motion.settle) }
                            }
                        } else {
                            pillLeft.snapTo(left)
                            pillWidth.snapTo(width)
                            placed = true
                        }
                    }

                    if (selectedGeometry != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset { IntOffset(pillLeft.value.roundToInt(), 0) }
                                .width(with(density) { pillWidth.value.roundToInt().toDp() })
                                .height(with(density) { trackHeightPx.roundToInt().toDp() })
                                .clip(RuiTheme.shapes.full)
                                .background(colors.surface),
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .onGloballyPositioned {
                                trackOriginX = it.positionInRoot().x
                                trackHeightPx = it.size.height.toFloat()
                            },
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        tabs.forEachIndexed { index, key ->
                            Tab(
                                key = key,
                                modifier = if (fillWidth) Modifier.weight(1f) else Modifier,
                            ) {
                                val labelColor by animateColorAsState(
                                    targetValue = if (selected) colors.content else colors.contentMuted,
                                    animationSpec = motion.colorFade,
                                    label = "rui_tab_label_color",
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight()
                                        .onGloballyPositioned {
                                            tabMetrics[index] =
                                                it.positionInRoot().x - trackOriginX to
                                                    it.size.width.toFloat()
                                        }
                                        .padding(vertical = spacing.sm, horizontal = spacing.md),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    RuiText(
                                        text = label(key),
                                        style = labelStyle,
                                        color = labelColor,
                                        textAlign = TextAlign.Center,
                                        maxLines = maxLines,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            tabs.forEach { key ->
                TabPanel(key = key) {
                    content(key)
                }
            }
        }
    }
}
