# Architecture

Single-module Android app. One activity hosts all Compose UI; a single `BrowserViewModel`
owns browser state; Room and DataStore hold everything that survives a restart.

## Layers

```text
┌─────────────────────────────────────────────────────────────┐
│ ui/          Compose screens and components (stateless)     │
│              BrowserApp → Toolbar, Omnibox, Screens         │
├─────────────────────────────────────────────────────────────┤
│ viewmodel/   BrowserViewModel — tabs, navigation, settings   │
│              exposes StateFlow, performs no I/O on the UI   │
├─────────────────────────────────────────────────────────────┤
│ data/        BrowserRepository (Room + DataStore),           │
│              BrowserPreferences, BansifyAccountStore         │
├─────────────────────────────────────────────────────────────┤
│ browser/     WebViewPool — one WebView per tab, chrome       │
│              client, download and permission plumbing        │
└─────────────────────────────────────────────────────────────┘
```

## Entry point

`MainActivity` is the only activity. It calls `enableEdgeToEdge()`, forwards `ACTION_VIEW`
intents to the ViewModel (`openUrlInActiveTab` or `createNewTab`, depending on the
"open external links in new tab" setting) and renders `BrowserApp`. `onNewIntent` handles
links that arrive while the app is already running; `launchMode` is `singleTask`.

## Tabs and WebViews

`browser/WebViewPool.kt` keeps the live `WebView` instances:

- one `WebView` per tab, created lazily and cached by tab id;
- a `WebChromeClient` for progress, title, favicons, JS dialogs, file chooser and
  `target=_blank` pop-ups;
- a `WebViewClient` that maps URL loading, SSL errors, safe-browsing hits and download
  requests into the ViewModel;
- downloads go through Android's `DownloadManager`, so they survive the app being closed.

Incognito tabs are separate `WebView` instances with their own (cleared) data directory
behaviour and are dropped when the last incognito tab closes.

## State

`BrowserViewModel` exposes `StateFlow`s, so the UI recomposes without holding business
logic:

| Flow | Meaning |
|---|---|
| `tabs` | open tabs with title, url, favicon, incognito flag, active tab id |
| `settings` | every user preference (see `BrowserPreferences`) |
| `account` | Bansify account state and sync toggles |
| `addressBar` | omnibox text, focus, editing state |
| `pageState` | loading progress, title, url, canGoBack/canGoForward |

## Persistence

**Room** (`BrowserDatabase`, version 1) — five tables, all with a DAO in `BrowserDao`:

| Table | Holds |
|---|---|
| `bookmarks` | title, url, created timestamp |
| `history` | title, url, visited timestamp |
| `downloads` | file name, url, path, status, size, timestamp |
| `shortcuts` | new-tab page tiles |
| `saved_tabs` | tabs the user parked for later |

**DataStore** (`BrowserPreferences`) — search engine, address-bar position, theme mode,
homepage, safe-browsing level, HTTPS-first, Do Not Track, third-party cookie policy,
JavaScript/cookies/pop-ups/images switches, desktop-site default, media autoplay, text zoom,
force-enable zoom, download notifications, preferred language, offer-to-translate and the
incognito-on-leave lock. Every key is a typed `Preferences.Key`.

## Accounts

`data/BansifyAccount.kt` stores a **local** account state (display name, email, avatar
colour, sync toggles) in its own DataStore. It is a UI placeholder: it does not talk to the
Gothwad Accounts service and it does not sync anything yet. Real SSO is tracked in
[ROADMAP.md](ROADMAP.md).

## Conventions that keep the architecture honest

- Screens take state and callbacks; they never construct a repository or a `WebView`.
- The ViewModel never touches `Context` beyond what it is given at construction.
- Anything that must survive process death goes to Room or DataStore, never to a field.
- No network client of its own: pages are rendered by the platform `WebView`.
