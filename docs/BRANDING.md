# Branding

## Name

- Product: **Bansify Browser**
- Package / namespace: `com.bansify.browser`
- Short form in UI: "Browser" only where space is tight (tab strip, notifications)

## Colour

| Token | Value | Use |
|---|---|---|
| Brand blue | `#0494F4` | launcher icon background, primary accents, links |
| Chrome blue (current theme primary) | `#0B57D0` | `ChromeBluePrimary` in `ui/theme/Color.kt` |
| Surface / background | `#FFFFFF` light, dark palette in `Theme.kt` | page chrome |

Changing the accent to the brand blue means editing `ChromeBluePrimary` and its
`onPrimary`/`primaryContainer` siblings in `ui/theme/Color.kt` — the rest of the theme reads
those tokens.

## Launcher icon

The mark is the Chrome-style three-blade pinwheel, drawn in **solid white on `#0494F4`**.

Files (all generated from one master, never hand-edited):

```text
app/src/main/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/
├── ic_launcher.png               # legacy square
├── ic_launcher_round.png         # legacy round
├── ic_launcher_background.png    # adaptive background: solid #0494F4
├── ic_launcher_foreground.png    # adaptive foreground: white mark, inside the safe zone
└── ic_launcher_monochrome.png    # themed icons
app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml
```

Rules that keep the icon correct on every launcher:

- The foreground mark stays inside the inner 66% of the canvas; anything outside is cropped
  by the launcher mask.
- The background layer is a flat colour, so no scaling artefacts appear.
- Density sizes: legacy 48/72/96/144/192 px, adaptive layers 108/162/216/324/432 px.

A 512×512 store icon uses the same plate without the adaptive padding.

## Typography

`ui/theme/Type.kt` defines the type scale; the bundled fonts are `inter.ttf` and
`plus_jakarta_sans.ttf` in `res/font/`. Use the Compose `MaterialTheme.typography` roles
rather than hard-coded sizes.

## Writing style

- Sentence case for labels and buttons ("New tab", not "New Tab").
- No exclamation marks, no marketing superlatives inside the app.
- UI strings live in `res/values/strings.xml`; never inline a user-visible literal in Kotlin.
