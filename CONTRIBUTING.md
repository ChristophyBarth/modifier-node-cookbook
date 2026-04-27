# Contributing

Thanks for considering a contribution. The cookbook has a deliberately narrow scope: a small, curated set of canonical Compose modifiers, all built on `Modifier.Node`. We'd rather ship 12 great modifiers than 50 mediocre ones.

## Before you start

- Open an issue first if you're proposing a new modifier. We'll talk through scope, name, and whether it belongs in `cookbook` (general-purpose) or somewhere else.
- Bug fixes and doc improvements: jump straight to a PR.

## The bar for a new modifier

Every modifier in the cookbook ships:

1. **A `Modifier.xxx(...)` extension** that validates inputs and returns `this then SomeElement(...)`.
2. **A `ModifierNodeElement` subclass** as a `data class` (so `equals`/`hashCode` are correct), with a meaningful `update()` that mutates the existing node, never recreates.
3. **A node class** mixing in the right `Modifier.Node` mixin(s). Pick the cheapest mixin that does the job.
4. **KDoc** on the public function: paragraph description, parameter docs, a `@sample` reference to a sample function in `cookbook/.../samples/`.
5. **A unit test** in `cookbook/src/test/...`. Use `createComposeRule` + Robolectric. Cover at minimum: parameter validation, parameter-change-without-recreation, and one behaviour assertion.
6. **A sample-app screen** in `sample/.../screens/` and entry in `CookbookCatalog.catalogEntries()`.
7. **A doc page** in `docs/modifiers/<name>.md` with usage, parameters, and design notes (which node subtype, why, any subtleties).

## Quality gates

The CI build must be green. Locally:

```bash
./gradlew build
```

That runs `ktlint`, `detekt`, lint, unit tests, and assembles both modules.

Conventions:

- **No `Modifier.composed`** anywhere in `cookbook/`. That's the entire point of the project.
- **No `LaunchedEffect`** in modifier impls; use the node's `coroutineScope`.
- **No reflection.** No `kotlin-reflect`, no `Class.forName`.
- **Public API is `@Stable`** (or `@Immutable` for value classes).
- **`explicitApi()` is on** in `cookbook`: every public symbol needs an explicit visibility modifier.
- Animations driven by `Animatable` / `AnimationState`, cancelled implicitly by node detach.

## Repo secrets (for maintainers)

Publishing to Maven Central is wired through GitHub Actions and reads these repo secrets:

| Secret | Purpose |
|---|---|
| `MAVEN_CENTRAL_USERNAME` | Central Portal user token name |
| `MAVEN_CENTRAL_PASSWORD` | Central Portal user token password |
| `SIGNING_IN_MEMORY_KEY` | ASCII-armored private GPG key (`gpg --armor --export-secret-keys <KEY_ID>`) |
| `SIGNING_IN_MEMORY_KEY_ID` | The 8-character short key ID (e.g. `0A1B2C3D`) |
| `SIGNING_IN_MEMORY_KEY_PASSWORD` | Passphrase for the GPG key |

Pushing a `vX.Y.Z` tag triggers `.github/workflows/publish.yml`, which strips `-SNAPSHOT` from `gradle.properties`, signs, and uploads to the Central Portal staging bundle (auto-released).

## Style

- Kotlin official style. ktlint enforces.
- `data class` for elements; small `internal class` for the node.
- Names: file = `Xxx.kt` (PascalCase), package = `xxx` (all lowercase, no separators), public function = `Modifier.xxx` (camelCase).
- Tests: `xxx_does_yyy()` (snake_case method names; backticks-with-spaces are nicer to read but ktlint and the Robolectric runner complain about characters like `[`/`]`).

## License

By contributing you agree your contribution is licensed under the Apache 2.0 License.
