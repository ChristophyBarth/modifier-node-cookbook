# Changelog

All notable changes to this project will be documented in this file. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.1] - 2026-04-29

### Fixed

- `Modifier.loadingPlaceholder` now actually shimmers. The internal chain was reversed to `shimmer().skeleton(...)` — `Modifier.skeleton` does not call `drawContent()` (it replaces content), so anything chained inner of it never runs. Direct `.skeleton().shimmer()` users should swap to `.shimmer().skeleton()`; the constraint is now documented on `Modifier.skeleton`.

## [0.1.0] - 2026-04-27

Initial release. Twenty production-ready standalone modifiers and two composites, all built on `Modifier.Node`. No `Modifier.composed` anywhere.

**Standalone**

- `Modifier.pressScale(scale, animationSpec)`: scales the receiver down on press, springs back.
- `Modifier.shimmer(colors, angleDegrees, durationMillis, repeatMode)`: animated gradient sweep.
- `Modifier.skeleton(shape, color)`: opaque placeholder shape.
- `Modifier.hoverElevation(restingDp, hoveredDp, shape, animationSpec)`: animated elevation on hover.
- `Modifier.fadeScrollEdges(scrollState, fadeLength)`: leading/trailing fade based on scroll.
- `Modifier.parallax(scrollState, factor)`: child translates relative to a parent scroll.
- `Modifier.tilt(maxAngleDeg, animationSpec)`: pointer-following 3D tilt.
- `Modifier.longPressProgress(durationMs, strokeWidth, color, onComplete)`: radial hold progress.
- `Modifier.dropShadow(color, blur, offset, shape)`: custom drop shadow untied from Material elevation.
- `Modifier.gradientBorder(brush, width, animationSpec)`: animated gradient stroke around the receiver.
- `Modifier.colorPulse(colors, durationMs)`: periodic color cycling for attention/badges.
- `Modifier.shake(trigger, intensity, durationMs)`: single-shot horizontal shake driven by a trigger key.
- `Modifier.glassmorphism(blurRadius, tint)`: frosted glass. `RenderEffect` blur on API 31+, translucent tint fallback below.
- `Modifier.bounceOnAppear(initialScale, spring)`: single-shot entry bounce when the node first attaches.
- `Modifier.revealOnScroll(translationY, threshold, durationMs)`: fade + translate in as the element enters the viewport.
- `Modifier.marquee(direction, gap, fadeEdges, fadeColor, velocity)`: auto-scroll on overflow with edge fades and configurable velocity.
- `Modifier.swipeToDismiss(onDismiss, threshold, axis)`: drag past threshold dismisses, otherwise springs back.
- `Modifier.magnetic(anchors, snapThreshold)`: drag snaps to the nearest anchor on release.
- `Modifier.pinchZoom(minScale, maxScale)`: two-finger zoom with pan support, pivots around the gesture centroid.
- `Modifier.dragToReorder(state, key)`: gesture-driven reorder for list items, paired with a `ReorderableState` helper.

**Composite**

- `Modifier.interactiveCard(...)`: `pressScale + tilt + hoverElevation` with sensible defaults.
- `Modifier.loadingPlaceholder(shape)`: `skeleton + shimmer` in one call.

Sample app with one screen per modifier. MkDocs Material doc site. CI on PRs, doc deploy on `main`, Maven Central publish on tag.

[0.1.1]: https://github.com/ChristophyBarth/modifier-node-cookbook/releases/tag/v0.1.1
[0.1.0]: https://github.com/ChristophyBarth/modifier-node-cookbook/releases/tag/v0.1.0
