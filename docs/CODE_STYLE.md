# Code style

Kotlin + Jetpack Compose, Material 3. Follow the existing files rather than introducing a
second way of doing the same thing.

## Naming

| Kind | Convention | Example |
|---|---|---|
| Class / composable | PascalCase | `ChromeToolbar`, `BrowserViewModel` |
| Function / property | camelCase | `openUrlInActiveTab` |
| Composable that emits UI | PascalCase noun | `TabSwitcherScreen` |
| Constant | UPPER_SNAKE_CASE | `MAX_TAB_COUNT` |
| Room entity | `<Thing>Entity` | `BookmarkEntity` |
| Screen file | `<Thing>Screen.kt` | `HistoryScreen.kt` |

## Compose

- Screens are **stateless**: they receive state and lambdas, they do not reach for a
  repository, `Context` or `WebView`.
- Collect state with `collectAsStateWithLifecycle()`, never `collectAsState()` in a screen.
- Hoist state to the ViewModel; `remember` is only for pure UI state (menu open, scroll).
- Every interactive element gets a `contentDescription` or a visible label; icons without
  text need one.
- Prefer `MaterialTheme.colorScheme` / `MaterialTheme.typography` over literal values.
- Keep one screen per file. If a file passes ~500 lines, split the pieces out.

## Kotlin

- No `!!`. Use `?.let`, early returns or `requireNotNull` with a message.
- Prefer immutable `data class` state and `StateFlow` over mutable fields.
- Coroutines: `viewModelScope` in the ViewModel, `Dispatchers.IO` for disk work, no
  blocking calls on the main thread.
- Catch only what you can handle; never swallow an exception with an empty `catch`.
- Public functions get a KDoc line only when the name does not already say it.

## Resources

- All user-visible strings in `res/values/strings.xml`.
- Colours in `ui/theme/Color.kt` (Compose) or `res/values/colors.xml` (XML themes), not
  inline hex in composables.
- No hard-coded dimensions for anything that must adapt to font scale; use `sp`/`dp` tokens.

## Comments

Explain **why**, not what. A comment that repeats the code is noise; a comment that records a
platform quirk (WebView, downloads, permission handling) saves the next person an afternoon.

## Commits and branches

See [../CONTRIBUTING.md](../CONTRIBUTING.md).
