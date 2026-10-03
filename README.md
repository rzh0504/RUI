# Rui

A neutral, cross-platform design system for **Compose Multiplatform**, built on [Compose Unstyled](https://composeunstyled.com) headless primitives.

Rui renders the same clean, restrained UI on Android and iOS — it is not a Material skin, not a Liquid Glass clone, and not a web framework's design language. The visual base sits closest to iOS (grouped backgrounds, filled controls, tight radii, hairline separators) without copying it: no platform-specific components, no system chrome imitation. It borrows Material's semantic tokens, state-layer feedback and accessibility discipline, and Liquid Glass's translucent material. Platforms converge because **all UI code lives in `commonMain`**.

## Architecture

```
compose-unstyled          behavior, state, keyboard, a11y (zero visuals)
        │
┌───────┴────────────────────────────────────────────┐
│  rui-ui                                            │
│  ├─ theme       tokens: colors / type / shapes /   │
│  │              spacing / motion  (RuiTheme)       │
│  ├─ foundation  Surface · Text · Icon · state      │
│  │              layer · FrostedSurface · BlurBackdrop│
│  │              utils: bounce overscroll · haptics │
│  └─ components  Scaffold · TopAppBar · NavBar ·    │
│                 Button · TextField · Switch ·      │
│                 Checkbox · Dialog · Tabs · List    │
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

The catalog is a structured showcase: a large-title app bar and a frosted-glass bottom navigation bar (real backdrop blur) over an iOS-style grouped index; one page per component, each organized into labeled demo groups. Lists use bounce overscroll and boundary haptics. Light/dark and frosted toggles live on the home page.

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

- Progressive blur and glass highlights for `FrostedSurface` (port miuix's runtime-shader pipeline)
- More primitives: DropdownMenu, BottomSheet, Slider, Progress, Avatar, Tooltip, Disclosure
- Continuous (squircle) corner shapes as a token swap
- `jvm` / `wasmJs` targets + a desktop demo
- Anthology of motion tokens (springs per component family)
