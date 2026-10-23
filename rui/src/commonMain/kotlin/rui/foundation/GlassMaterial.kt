package rui.foundation

/**
 * The glass material of a surface that can render frosted glass — the code
 * author's choice, made per component at the call site. There is no in-app
 * switching and no theme-level default to override it: which material an app
 * uses is a design decision, not a runtime setting.
 *
 * - [Frosted] — the miuix-style recipe: the captured content blurred behind
 *   a heavy tint (0.72 alpha) with a hairline border. The material every
 *   glass component shipped with.
 * - [Liquid] — the refraction look of iOS 26's Liquid Glass, rendered by the
 *   Backdrop library (https://github.com/Kyant0/AndroidLiquidGlass, Apache
 *   License 2.0): saturation lift (vibrancy), a light blur, and a lens
 *   shader refracting the content near the shape's edges, topped with a
 *   specular highlight instead of a border, over a much lighter tint
 *   (0.30 alpha) that keeps the glass clear.
 *
 * Requirements and degradation:
 * - [Liquid] samples a second capture that only exists when the screen's
 *   backdrop is created with `rememberBlurBackdrop(liquid = true)`. Without
 *   it, components fall back to the [Frosted] path.
 * - Both materials need a RenderEffect (Android 12+; Skia platforms are
 *   always fine). Below that, components fall back to the honest translucent
 *   panel.
 * - The lens refraction needs a runtime shader — Android 13+ or any Skia
 *   platform (desktop, iOS, web, all via Skia's own runtime effects). On
 *   Android 12 the liquid material renders without refraction: vibrancy and
 *   blur remain.
 */
public enum class RuiGlassMaterial {
    Frosted,
    Liquid,
}
