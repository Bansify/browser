# Testing

## Layers

| Layer | Location | Runs |
|---|---|---|
| JVM unit tests | `app/src/test/java/com/bansify/browser/` | `./gradlew :app:testDebugUnitTest` |
| Robolectric | same folder (`ExampleRobolectricTest`) | part of the unit test task |
| Instrumented | `app/src/androidTest/java/com/bansify/browser/` | `./gradlew :app:connectedDebugAndroidTest` (device needed) |
| CI | `.github/workflows/quality.yml` | rules gate, unit tests, debug assemble, lint |

The three test classes that ship today are the template examples. Replace them with real
coverage as behaviour lands — start with `UrlUtils` (pure functions, cheapest to test) and
`BrowserViewModel` tab operations.

## What is worth testing first

1. `browser/UrlUtils.kt` — search-vs-URL detection, scheme handling, display URL.
2. `BrowserViewModel` — new tab / close tab / active tab switching, incognito isolation.
3. Room DAOs — bookmark and history insert, delete, ordering.
4. `BrowserPreferences` — defaults when the DataStore is empty.

## Manual QA checklist

Run this on a phone and, where possible, on a TV/tablet before a release.

**Startup**
- [ ] Cold start shows the new tab page, no flicker, no crash
- [ ] Rotating the device keeps the page and the tab strip state

**Browsing**
- [ ] Type a URL → loads; type a search term → goes to the configured search engine
- [ ] Back / forward / reload / stop behave correctly
- [ ] Long press a link → open in new tab, copy link, download
- [ ] `target=_blank` pop-up opens as a new tab, not a dead page
- [ ] File chooser (upload) opens and returns a file
- [ ] Video plays inline and in fullscreen; audio stops when the tab closes

**Tabs**
- [ ] Open several tabs, switch, close; the correct tab stays active
- [ ] Incognito tab does not appear in history and is gone after closing all incognito tabs
- [ ] Saved tabs restore after a restart

**Data**
- [ ] Bookmark / unbookmark from the toolbar menu
- [ ] History records visits and can be cleared
- [ ] Download completes, appears in Downloads, opens from there
- [ ] "Delete browsing data" clears what it says it clears

**Settings**
- [ ] Address bar position switches top/bottom and survives a restart
- [ ] Theme mode (system/light/dark) applies immediately
- [ ] JavaScript, images, pop-ups and cookies switches take effect on the next load
- [ ] Text zoom and force-enable-zoom affect page rendering

**Account surface**
- [ ] Sign-in screen opens, back works, no crash with no network

**Platform**
- [ ] `ACTION_VIEW` from another app opens the link in this browser
- [ ] Back gesture at the start of history leaves the app instead of looping
- [ ] TV remote D-pad can move through the toolbar and page links (TV builds)
