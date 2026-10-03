# Rui

A neutral, cross-platform design system for **Compose Multiplatform**, built on [Compose Unstyled](https://composeunstyled.com) headless primitives.

Rui renders the same clean, restrained UI on Android and iOS — it is not a Material skin and not a Liquid Glass clone. It borrows the best of both: Material's semantic tokens, state-layer feedback and accessibility discipline; Liquid Glass's translucent material and content-first chrome. Platforms converge because **all UI code lives in `commonMain`**.

## Architecture

```
compose-unstyled          behavior, state, keyboard, a11y (zero visuals)
        │
┌───────┴────────────────────────────────────────────┐
│  rui-ui                                            │
│  ├─ theme       tokens: colors / type / shapes /   │
│  │              spacing / motion  (RuiTheme)       │
│  ├─ foundation  Surface · Text · Icon · state      │
│  │              layer · GlassSurface               │
│  └─ components  Button · IconButton · TextField ·  │
│                 Switch · Checkbox · Dialog · Tabs  │
└───────┬────────────────────────────────────────────┘
        │
  sample/          catalog app: shared UI + androidApp + iosApp
```

Rules that keep it neutral and extensible:

- `rui-ui` depends only on Compose (ui/foundation/runtime) + Compose Unstyled — **no `material3`, ever**.
- Components are slot-based and hoist state; every visual slot can be overridden by the caller.
- `expect/actual` is limited to a tiny capability surface (`isBlurSupported`), so adding a target later touches only build files.

## Modules

| Path | What it is |
|---|---|
| `rui-ui` | The design system library (KMP) |
| `sample/shared` | Catalog app UI, shared by both platforms |
| `sample/androidApp` | Android entry point |
| `sample/iosApp` | Xcode project + Swift entry point |

Targets today: `android`, `iosArm64`, `iosSimulatorArm64`. Desktop (`jvm`) and Web (`wasmJs`) are a build-config change away — see [DESIGN.md](DESIGN.md).

## Quick start

### Run the catalog app

```bash
# Android
./gradlew :sample:androidApp:installDebug

# iOS — open sample/iosApp/iosApp.xcodeproj in Xcode and run,
# or verify remotely: see .github/workflows/ios-ci.yml
```

The catalog exercises every component, with light/dark and glass toggles.

### Use Rui in an app

```kotlin
dependencies {
    implementation(project(":rui-ui"))   // or a published artifact later
}
```

```kotlin
RuiTheme {                       // picks light/dark from the system
    // rebrand in one line: RuiTheme(colorScheme = RuiColorScheme.light().copy(primary = brand)) { … }
    ScaffoldLikeChrome {
        RuiButton(onClick = ::save, leading = { RuiIcon(RuiIcons.Check, null) }) {
            RuiText("Save")
        }
    }
}
```

## Building

- JDK 17+, Android SDK (platform 37), Gradle wrapper (9.8).
- On Windows, iOS targets are configured but not compiled (`kotlin.native.ignoreDisabledTargets=true`); build them on macOS or via the `ios-ci` GitHub Actions workflow.

## Design language

See [DESIGN.md](DESIGN.md) for principles, the token dictionary and the recipes: adding a component, rebranding colors, and enabling desktop/web targets.

## Roadmap

- Backdrop blur for `GlassSurface` (Haze integration) behind the existing `isBlurSupported` seam
- More primitives: DropdownMenu, BottomSheet, Slider, Progress, Avatar, Tooltip, Disclosure
- Continuous (squircle) corner shapes as a token swap
- `jvm` / `wasmJs` targets + a desktop demo
- Anthology of motion tokens (springs per component family)
