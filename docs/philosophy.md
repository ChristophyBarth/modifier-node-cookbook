# Why `Modifier.Node`?

A short, opinionated take on why this cookbook never reaches for `Modifier.composed`.

## What `composed` actually does

`Modifier.composed { … }` runs its body inside the composition that hosts the modified composable. That body usually contains a `remember { … }` for state and a `LaunchedEffect` for animation. It looks ergonomic, but it has costs:

- **A new modifier instance per composition.** `composed` materializes a fresh `Modifier` every time the host recomposes. The Compose runtime is good at de-duplicating, but the abstraction is "make a new modifier each frame", which means every parameter capture lives in composition.
- **Coroutine scopes tied to composition.** A `LaunchedEffect` keyed inside `composed` restarts when its keys change or when the composable leaves and re-enters composition. In-flight animations get cancelled and started over.
- **No inspector identity.** `composed` modifiers don't show up cleanly in the layout inspector. You see a wrapper, not the actual modifier name and parameters.
- **No lifecycle hooks.** There's no `onAttach` / `onDetach`. You can fake one with `DisposableEffect`, but its lifecycle is composition's, not the layout node's.

## What `Modifier.Node` gives you

A `Modifier.Node` is a real object that the layout owns. Its lifecycle is anchored to the layout tree, not to composition. That gives you:

- **`onAttach()` and `onDetach()`** for setting up and tearing down work: coroutines, observers, listeners, anything.
- **`coroutineScope`** that gets cancelled exactly when the node detaches. No leaked frames, no zombie animations.
- **An `update()` method** invoked when the wrapping `ModifierNodeElement` changes. You re-target in-flight animations instead of recreating the node from scratch.
- **Mixin interfaces:** `DrawModifierNode`, `LayoutModifierNode`, `PointerInputModifierNode`, `FocusEventModifierNode`, `ObserverModifierNode`, `KeyInputModifierNode`. Pick the ones you need.
- **`inspectableProperties { … }`** so the layout inspector shows your modifier with its parameter values.
- **Cheaper.** Nothing in a node triggers recomposition. Modifier stacks become a layout-tree concern, not a composition concern.

## The mental model

> A `Composable` is "what should be drawn given this state." A `Modifier.Node` is "a behaviour attached to this layout."

If your modifier reads composition state (a derived state, a flow), you want it to be a composable. If your modifier *is* a behaviour (animation, observation, drawing, gesture handling), it wants to be a node.

## When to still use `composed`?

Honestly, in 2026: rarely. The Compose team's guidance is to migrate. If you're targeting Compose < 1.6 or you genuinely can't get the API surface you need from a node mixin, sure. Otherwise: write a node.

## How to write your own

See the [Writing your own](guide/writing-your-own.md) guide; it walks through `pressScale` step by step.
