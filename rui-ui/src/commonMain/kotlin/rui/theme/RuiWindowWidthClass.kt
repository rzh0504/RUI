package rui.theme

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The window width buckets Rui's adaptive decisions key on. The tiers and
 * 600dp/840dp breakpoints are borrowed from Material's window size classes —
 * the one place Rui borrows Material semantics beyond tokens, because there is
 * no neutral convention to copy.
 */
public enum class RuiWindowWidthClass { Compact, Medium, Expanded }

/**
 * Resolves [widthDp] to its window width class: [RuiWindowWidthClass.Compact]
 * below 600dp, [RuiWindowWidthClass.Medium] from 600dp,
 * [RuiWindowWidthClass.Expanded] from 840dp.
 *
 * A pure function, so adaptive code can resolve widths from any source (tests,
 * desktop resizing, previews) without a composition.
 */
public fun windowWidthClassAt(widthDp: Int): RuiWindowWidthClass = when {
    widthDp >= 840 -> RuiWindowWidthClass.Expanded
    widthDp >= 600 -> RuiWindowWidthClass.Medium
    else -> RuiWindowWidthClass.Compact
}

internal val LocalRuiWindowWidthClass = staticCompositionLocalOf { RuiWindowWidthClass.Compact }
