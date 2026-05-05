# Modifier Node Cookbook

A curated collection of canonical, production-ready Jetpack Compose modifiers, every one written using the modern **`Modifier.Node`** API. No `Modifier.composed`. Anywhere.

## Install

```kotlin
// settings.gradle.kts (already on Maven Central)
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

// app/build.gradle.kts
dependencies {
    implementation("io.github.christophybarth:modifier-node-cookbook:0.2.0")
}
```

## Quick example

```kotlin
import io.github.christophybarth.cookbook.pressscale.pressScale
import io.github.christophybarth.cookbook.shimmer.shimmer
import io.github.christophybarth.cookbook.tilt.tilt

Box(
    modifier = Modifier
        .size(160.dp)
        .pressScale()
        .tilt()
        .clip(RoundedCornerShape(16.dp))
        .background(brush)
        .clickable { onClick() },
)
```

## The cookbook philosophy

Every modifier in this library is built on `Modifier.Node`, never `Modifier.composed`. That choice buys real things:

- **Lifecycle clarity.** A node has `onAttach` / `onDetach`; animations, coroutines, and observers tie to the node, not to a composition that may rebuild for unrelated reasons.
- **Update over recreate.** Changing a parameter calls the node's `update()` and re-targets in-flight animations, instead of throwing the old node away.
- **No composition cost.** Nothing in a modifier triggers a recomposition. Stack ten of them on a list cell; the cell still recomposes only when its data changes.
- **Inspectability.** Each modifier names itself in the layout inspector via `inspectableProperties`, so production users can see them in tooling.
- **Testability.** A node is a plain object. You can construct it, drive it, and assert on it without spinning up a composition tree if you want to.

If your modifier reaches for `composed { remember { … } }`, you have a node-shaped problem.

## Modifiers

| Modifier | What it does |
|---|---|
| [`pressScale`](modifiers/press-scale.md) | Scales the receiver down on press; springs back. |
| [`shimmer`](modifiers/shimmer.md) | Sweeps a translucent gradient. Loading state in motion. |
| [`skeleton`](modifiers/skeleton.md) | Opaque placeholder shape. Pairs with `shimmer`. |
| [`hoverElevation`](modifiers/hover-elevation.md) | Animates shadow elevation on mouse / stylus hover. |
| [`fadeScrollEdges`](modifiers/fade-scroll-edges.md) | Fades the leading / trailing edges of a scrollable based on position. |
| [`parallax`](modifiers/parallax.md) | Translates a child relative to a parent scroll. |
| [`tilt`](modifiers/tilt.md) | 3D card tilt that follows pointer position. |
| [`longPressProgress`](modifiers/long-press-progress.md) | Radial progress arc while long-press is held. |
| [`marquee`](modifiers/marquee.md) | Auto-scroll on overflow with configurable velocity and edge fades. |
| [`pinchZoom`](modifiers/pinch-zoom.md) | Two-finger zoom with pan; clamped scale. |
| [`glass`](modifiers/glass.md) | Backdrop sampler that reads as glass. Clear by default; raise saturation, sheen and border for frosted. Supersedes `glassmorphism`. |

See the **Modifiers** nav for the full list (21 standalone + 2 composites).

## Contributing

See [CONTRIBUTING.md](https://github.com/ChristophyBarth/modifier-node-cookbook/blob/main/CONTRIBUTING.md). The bar: every modifier ships with a node, an `update()`, KDoc, a unit test, a sample, and a doc page.
