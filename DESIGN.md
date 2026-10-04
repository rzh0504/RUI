# Rui Design Language

Rui is a **neutral** design system: it belongs to no platform and no brand. On Android it does not pretend to be iOS; on iOS it does not pretend to be Material. It looks like a clean, well-made tool everywhere, because the same tokens, geometry and motion run on every platform.

## Principles

1. **Content first.** Chrome is quiet: flat surfaces, hairline borders, generous whitespace. Nothing shouts.
2. **One accent.** The default palette is a neutral gray ramp plus a single blue accent (`#3C87FF`, both modes). A brand is a single `copy(primary = …)` away, never a redesign.
3. **Feedback you can feel, not see.** Interactions answer with a subtle content-colored state layer and a keyboard-only focus ring — Material's discipline, without tonal shadows.
4. **One geometry.** The same radius scale, 4dp spacing grid and type scale on every platform. Geometry is a token decision, never a per-component pixel decision.
5. **Material is an enhancement, not a dependency.** Glass translucency degrades to solid surfaces when the platform can't support it. Nothing breaks without it.

## Token dictionary

### Colors — `RuiColorScheme`

Semantic roles only; light and dark factory defaults ship in `RuiColorScheme.light()/dark()`.

| Role | Used for |
|---|---|
| `background` | Page behind everything (the grouped background surfaces sit on) |
| `surface` | Cards, sheets, inputs |
| `surfaceMuted` | Wells, headers, disabled containers |
| `surfaceElevated` | Menus, dialogs |
| `outline` / `outlineSubtle` | Rare hairlines (error fields) / row separators |
| `content` / `contentMuted` / `contentDisabled` | Text & icon hierarchy |
| `primary` / `onPrimary` | Accent and emphasis — defaults to `#3C87FF` blue, light and dark |
| `danger` / `success` | Destructive / affirmative feedback |
| `scrim` | Overlay behind modal fragments |

### Typography — `RuiTypography`

Three levels × three sizes. Default family is the platform system font, so text renders natively while the scale stays identical.

`titleLg 22/28 SemiBold · titleMd 17/24 · titleSm 15/20`
`bodyLg 16/24 Regular · bodyMd 14/20 · bodySm 13/18`
`labelLg 14/18 Medium · labelMd 12/16 · labelSm 11/14`

### Shapes — `RuiShapes`

Every corner renders through `SmoothCornerShape` — continuous ("squircle") corners, adapted from miuix's squircle module (Apache-2.0, see NOTICE): the corner curve extends `smoothing × radius` into the edges (default 1.1), so curvature flows instead of a circle glued to a rectangle. Radii larger than half the shorter side clamp, producing smooth stadiums for pills.

Token values — `sm 6 · md 10 · lg 14 · xl 18 · sheet 14 (top only) · full (stadium)`:

- **md (10) is the workhorse tier and is shared**: buttons, icon buttons, text fields, grouped cards, glass surfaces. One tier, one value — corners never fight on the same screen, and a small control nested in a card reads as the same family.
- **sm (6)**: checkboxes, badges, swatches. **xl (18)**: dialogs and dropdown menu panels. **full**: switch and segmented tracks (smooth stadium).
- **sheet (14)**: `TopSmoothCornerShape` — the same 14 tier as `lg` but rounded at the top edge only, square at the bottom, for surfaces docked to the screen's bottom edge (bottom sheets).
- Never construct ad-hoc shapes in components; pick a token, and if a new role genuinely appears, add it to `ruiShapes()` and document it here.

### Spacing & metrics

`RuiSpacing`: 4 / 8 / 12 / 16 / 24 / 32 on a 4dp grid.
`RuiDimens`: touch target 44dp (between Material's 48 and HIG's 44), border 1dp, focus ring 2dp, icon 20dp.

### Motion — `RuiMotion`

State layers: 90ms in / 180ms out. Short transitions 120ms, medium 200ms. Component springs (switch thumb, dialog scale) live with the component and stay restrained.

## Interaction recipes

- **State layer** — pressed 10%, hovered 6%, focused 8%, dragged 12% alpha of the *component's content color* (`rememberRuiIndication`, `ColoredIndication` under the hood).
- **Focus** — 2dp ring in `primary`, drawn outside the shape (`focusRing`), visible only for keyboard focus (`FocusVisibilityProvider` at the app root enables this).
- **Selection** — checkboxes and switches fill with `primary`; marks animate with short springs.

## Frosted material

`FrostedSurface` is deliberately named: with a `BlurBackdrop` it renders the captured content blurred behind a translucent tint — frosted glass. Without a backdrop it is an honest translucent panel (tint + hairline), not glass; don't present it as one.

The backdrop mechanism follows miuix's blur module (Apache-2.0, see NOTICE): one composable captures itself via `Modifier.blurBackdrop(backdrop)` (`rememberBlurBackdrop()` + opaque background first), and any surface applies `Modifier.blurSurface(...)` — a GraphicsLayer record drawn with the cross-platform `BlurEffect`, translated into local coordinates. No pixels are read back.

The catalog wires it end to end: `RuiScaffold(backdrop = …)` captures the content area; `RuiTopAppBar` and `RuiNavigationBar` sample it as frosted bars, and the frosted dialog samples it through the portal. Frosted surfaces are marked `@ExperimentalRuiApi`.

## Utilities

`rui.foundation.utils` carries the platform-feel utilities, mostly adapted from miuix (Apache-2.0, see NOTICE):

- `overScrollVertical()` / `overScrollHorizontal()` — iOS-style bounce overscroll (touch/trackpad only; wheel and keyboard pass through). The catalog's `ContentColumn` uses it everywhere.
- `scrollEndHaptic()` — a haptic tick when a fling hits a scroll boundary.
- `pressScale(interactionSource)` — sink-while-pressed scale for large tiles that pair with (or replace) the state layer.

## Component conventions

Visual signatures worth keeping consistent:

- **No chrome borders.** Surfaces separate by contrast (`surface` on `background`); a hairline only marks errors.
- **Filled button hierarchy** — `Primary` (accent fill) > `Tonal` (muted fill) > `Plain` (no fill) > `Danger`; no outlined buttons.
- **Filled text field** with label above; error is red label + hairline, never a filled red box.
- **Segmented control** for tabs: muted track, raised surface pill marks selection.
- **Progress is thin** — a 4dp capsule (linear) or 4dp ring (circular) in `outlineSubtle` with a `primary` fill, like the slider's rest track; `progress = null` renders the indeterminate state (sweeping segment / rotating arc, never a blink).
- **Inset grouped lists** for any index of items (`RuiListGroup` + `RuiListItem` + `RuiDivider(inset = …)`).
- **App chrome is frosted-capable**: `RuiScaffold` overlays `RuiTopAppBar` / `RuiNavigationBar` on the content; with a backdrop they render frosted, without it they are plain surfaces.
- **Switches stay circular**: the switch track and thumb use pure `CircleShape`, exempt from corner smoothing — circles read as circles.
- **List item titles sit at the title level** (`titleSm`), matching how real apps treat settings rows as section headlines; subtitles stay `bodySm`.

Every component in `rui.components`:

- wraps an Unstyled primitive (behavior, semantics, keyboard come free);
- reads tokens from `RuiTheme`, never hardcodes values;
- exposes every visual slot as a parameter with a token-driven default;
- hoists state (`Boolean`/`TextFieldState` in, callback out);
- lives in a single file named after it.

## Recipes

### Add a component

1. Pick the Unstyled primitive; read its source (each is one file, MIT).
2. Create `rui-ui/src/commonMain/kotlin/rui/components/Rui<Name>.kt`.
3. Defaults come from tokens; visuals only in the default parameter values.
4. Add a demo card in `sample/shared/.../demos/<Section>Demos.kt`.
5. Run `./gradlew :rui-ui:allTests :sample:androidApp:installDebug` and eyeball it in the catalog, light and dark.
6. Document any new token in this file.

### Rebrand (colors)

```kotlin
val Brand = RuiColorScheme.light().copy(primary = Color(0xFF3B6EF2))
val BrandDark = RuiColorScheme.dark().copy(primary = Color(0xFF7CA3FF))

RuiTheme(colorScheme = if (dark) BrandDark else Brand) { … }
```

New roles (e.g. a `warning`) mean adding a field to `RuiColorScheme`, both factories, and the catalog's color demo — then it exists on every component at once.

### Enable desktop / web later

1. In `rui-ui/build.gradle.kts` and `sample/shared`: add `jvm()` / `wasmJs { … }` targets (Unstyled already publishes them).
2. Create a `sample/desktopApp` module: `org.jetbrains.compose` plugin + `Window { RuiTheme { CatalogScreen(…) } }`.
3. For web: a `sample/webApp` with `CanvasBasedWindow`.
4. No library code changes — that is the point of commonMain-only UI and the tiny expect/actual surface.

## Non-goals

- Platform-faithful skins (that's Material and Liquid Glass's job).
- Theming through Unstyled's `ThemeProperty` registry — Rui owns ergonomics; the bridge provides Unstyled defaults instead.
- Pixel-perfect parity with any native framework.
