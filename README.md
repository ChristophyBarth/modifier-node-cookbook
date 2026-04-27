# modifier-node-cookbook

A curated collection of canonical, production-ready Jetpack Compose modifiers, every one written using the modern **`Modifier.Node`** API. No `Modifier.composed`. Anywhere.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.christophybarth/modifier-node-cookbook.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.christophybarth/modifier-node-cookbook)
[![CI](https://github.com/ChristophyBarth/modifier-node-cookbook/actions/workflows/ci.yml/badge.svg)](https://github.com/ChristophyBarth/modifier-node-cookbook/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Install

`build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.christophybarth:modifier-node-cookbook:0.1.0")
}
```

Or via the Gradle version catalog (`gradle/libs.versions.toml`):

```toml
[versions]
modifier-node-cookbook = "0.1.0"

[libraries]
modifier-node-cookbook = { module = "io.github.christophybarth:modifier-node-cookbook", version.ref = "modifier-node-cookbook" }
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
        .tilt(maxAngleDeg = 12f)
        .clip(RoundedCornerShape(16.dp))
        .background(brush)
        .clickable { onClick() },
)
```

## What's in the box

**Standalone modifiers (20):**

| Modifier | What it does |
|---|---|
| `Modifier.pressScale(scale, spec)` | Scales the receiver down on press; springs back on release. |
| `Modifier.shimmer(colors, angle, …)` | Sweeps a translucent gradient over the content. Loading state in motion. |
| `Modifier.skeleton(shape, color)` | Opaque placeholder shape. Composes with `shimmer`. |
| `Modifier.hoverElevation(restingDp, hoveredDp, …)` | Animates shadow elevation on mouse / stylus hover. |
| `Modifier.fadeScrollEdges(scrollState, fadeLength)` | Fades leading & trailing edges of a scrollable based on position. |
| `Modifier.parallax(scrollState, factor)` | Translates a child relative to a parent scroll container. |
| `Modifier.tilt(maxAngleDeg, spec)` | 3D card-tilt that follows pointer position. |
| `Modifier.longPressProgress(durationMs, …, onComplete)` | Radial progress arc while long-press is held. |
| `Modifier.dropShadow(color, blur, offset, shape)` | Custom drop shadow untied from Material elevation. |
| `Modifier.gradientBorder(brush, width, …)` | Animated gradient stroke around the receiver. |
| `Modifier.colorPulse(colors, durationMs)` | Periodic colour cycling for badges and attention. |
| `Modifier.shake(trigger, intensity, …)` | Single-shot horizontal shake driven by a trigger key. |
| `Modifier.glassmorphism(blurRadius, tint)` | Frosted glass; real blur on API 31+, tint fallback below. |
| `Modifier.bounceOnAppear(initialScale, spring)` | Single-shot entry bounce on attach. |
| `Modifier.revealOnScroll(translationY, threshold, …)` | Fade + translate in as element enters the viewport. |
| `Modifier.marquee(direction, gap, fadeEdges, …)` | Auto-scroll on overflow with configurable velocity. |
| `Modifier.swipeToDismiss(onDismiss, threshold, axis)` | Drag past threshold dismisses; springs back otherwise. |
| `Modifier.magnetic(anchors, snapThreshold)` | Drag snaps to the nearest anchor on release. |
| `Modifier.pinchZoom(minScale, maxScale)` | Two-finger zoom with pan; clamped scale. |
| `Modifier.dragToReorder(state, key)` | Long-press to lift, drag to reorder; pairs with `ReorderableState`. |

**Composites (2):**

| Modifier | Composes |
|---|---|
| `Modifier.interactiveCard(...)` | `pressScale` + `tilt` + `hoverElevation` |
| `Modifier.loadingPlaceholder(shape)` | `skeleton` + `shimmer` |

Each one has a [doc page](https://christophybarth.github.io/modifier-node-cookbook/) with usage notes, parameters, and design notes.

## Why `Modifier.Node`?

Because `Modifier.composed { remember { … } }` allocates state in composition, ties animation lifecycles to composition, and shows up as opaque wrappers in the layout inspector. A `Modifier.Node` lives in the layout tree, has `onAttach` / `onDetach` / `update()` for free, and never triggers recomposition.

Read the long version: [Why Modifier.Node?](https://christophybarth.github.io/modifier-node-cookbook/philosophy/).

## Project layout

```
modifier-node-cookbook/
├── cookbook/        # the library module (published to Maven Central)
├── sample/          # showcase Compose app, one screen per modifier
├── docs/            # MkDocs Material site (gh-pages)
├── .github/workflows/   # ci.yml, publish.yml, docs.yml
└── gradle/libs.versions.toml
```

## Local commands

```bash
# Build everything (compiles, lints, ktlints, detekt, runs unit tests)
./gradlew build

# Just unit tests for the library
./gradlew :cookbook:test

# Run the showcase app on a connected device or emulator
./gradlew :sample:installDebug

# Serve the docs site locally (requires `pip install mkdocs-material`)
mkdocs serve

# Publish a snapshot to Maven Local for testing in another project
./gradlew :cookbook:publishToMavenLocal

# Cut a release (CI does this when you push a tag)
git tag v0.1.0 && git push --tags
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). The bar: every modifier ships with a node, an `update()`, KDoc, a unit test, a sample, and a doc page.

## License

```
Copyright 2026 Christophy Barth

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0
```
