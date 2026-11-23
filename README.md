# Bansify Browser

A fast, privacy-respecting web browser for Android phones, tablets and TVs, built with
Kotlin and Jetpack Compose. Chrome-style toolbar and omnibox, tab switcher, reading mode,
bookmarks, history, downloads and a local settings surface — with no account required to
browse.

<p align="left">
  <img alt="Platform" src="https://img.shields.io/badge/platform-Android%2024%2B-3DDC84">
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.4-7F52FF">
  <img alt="Compose" src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4">
  <img alt="Package" src="https://img.shields.io/badge/package-com.bansify.browser-0494F4">
</p>

## Features

- **Chrome-style UI** — toolbar with omnibox, page actions and overflow menu; the address
  bar can sit at the top or the bottom.
- **Tabs** — tab switcher with tab groups of one, incognito tabs, recent tabs, saved tabs.
- **New tab page** — shortcuts, most-visited tiles and a search box.
- **Reading mode** — strips a page down to readable text.
- **Bookmarks, history, downloads** — stored locally in Room, searchable and deletable.
- **Privacy controls** — incognito, safe browsing level, HTTPS-first, Do Not Track,
  third-party cookie policy, JavaScript and pop-up switches, "delete browsing data".
- **Bansify account** — sign-in surface and sync preferences (local placeholder today, see
  [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md#accounts)).
- **Material 3 Expressive** components with a Chrome-like colour system.

## Quick start

```bash
git clone https://github.com/Bansify/browser.git
cd browser
./gradlew :app:assembleDebug          # debug APK in app/build/outputs/apk/debug/
./gradlew :app:installDebug           # install on a connected device/emulator
```

Requirements: JDK 17+ (CI uses 21), Android SDK with platform 36, and the committed Gradle
wrapper (9.3.1). No keystore is needed for debug builds.

Full instructions: [docs/BUILD.md](docs/BUILD.md).

## Project structure

```text
app/src/main/java/com/bansify/browser/
├── MainActivity.kt              # single activity, edge-to-edge, handles VIEW intents
├── browser/                     # WebViewPool (tabs), UrlUtils
├── data/                        # Room (bookmarks/history/downloads/shortcuts/saved tabs),
│                                # DataStore settings, repository, account state
├── ui/                          # BrowserApp, components (toolbar, omnibox, menus),
│                                # screens (new tab, history, bookmarks, settings, …), theme
└── viewmodel/                   # BrowserViewModel: tabs, navigation, settings, history
```

## Documentation

| Document | What is inside |
|---|---|
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Layers, data flow, Room schema, tab/WebView model |
| [docs/BUILD.md](docs/BUILD.md) | Prerequisites, build commands, variants, troubleshooting |
| [docs/RELEASING.md](docs/RELEASING.md) | Versioning, signing secrets, store checklist |
| [docs/BRANDING.md](docs/BRANDING.md) | Colour tokens, launcher icon, typography |
| [docs/CODE_STYLE.md](docs/CODE_STYLE.md) | Kotlin/Compose conventions for this repo |
| [docs/TESTING.md](docs/TESTING.md) | Test layers and the manual QA checklist |
| [docs/ROADMAP.md](docs/ROADMAP.md) | What is done, next and later |
| [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) | Common build and runtime problems |
| [docs/FAQ.md](docs/FAQ.md) | Product and contributor questions |
| [CHANGELOG.md](CHANGELOG.md) | Release history |
| [CONTRIBUTING.md](CONTRIBUTING.md) | How to propose and land a change |
| [SECURITY.md](SECURITY.md) | How to report a vulnerability |

## Status

The app compiles and produces a debug APK on every pull request. The signed release
pipeline and the store listing are not wired yet — see [docs/RELEASING.md](docs/RELEASING.md).

## License

No license file is present in this repository yet. Until the owner adds one, the code is
all rights reserved: do not redistribute or reuse it. See
[docs/FAQ.md](docs/FAQ.md#can-i-reuse-this-code).
