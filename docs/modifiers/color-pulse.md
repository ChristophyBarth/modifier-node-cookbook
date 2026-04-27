# Modifier.colorPulse

> Cycles the receiver's fill colour through a sequence on a fixed period.

## When to use

- Notification dots, "new" badges, "live" indicators, recording dots.
- Anywhere a static colour reads as "ignorable" but motion would be too much.

## When _not_ to use

- Text. The cycling fill will compete with the glyph colour.
- Large fills. The pulse becomes overwhelming past about 24 dp.

## Usage

```kotlin
Box(
    modifier = Modifier
        .size(16.dp)
        .colorPulse(
            colors = listOf(Color(0xFFEF4444), Color(0xFFFB923C), Color(0xFFEF4444)),
            shape = CircleShape,
        ),
)
```

## Parameters

| Name | Type | Default | Notes |
|---|---|---|---|
| `colors` | `List<Color>` | red / amber / red | Cycle wraps from the last back to the first. Two minimum. |
| `durationMs` | `Int` | `1_600` | Time for a full pass through every colour. |
| `shape` | `Shape` | `CircleShape` | Filled with the current interpolated colour. |

## Design notes

- **Node subtype:** `DrawModifierNode`. A single `Animatable<Float>` cycles `0f → 1f` infinitely; we map the phase onto the colour list with `lerp` between adjacent stops. `FastOutSlowInEasing` softens transitions, preventing the visible "wraparound flash" you'd get with linear easing.
- **Cleanup:** the animation is cancelled in `onDetach()`. Adding `colorPulse` to a list cell that scrolls off doesn't keep frames coming.
- **Why not `targetBased` per-segment?** A single infinite animation with phase-mapping is cheaper than a chain of `animateTo` calls and has no jank at the boundary between segments.
