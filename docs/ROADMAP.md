# Roadmap

Ordered by what unblocks the most work. Nothing here is a promise of a date.

## Done

- Single-module Compose app with Chrome-style toolbar, omnibox, tab switcher, reading mode.
- New tab page with shortcuts, bookmarks, history, downloads, saved tabs (Room).
- Settings surface covering privacy, content, appearance and downloads.
- Bansify package id (`com.bansify.browser`) and the white-on-#0494F4 launcher icon.
- Debug builds and CI on every pull request.

## Next

1. **Real Gothwad Accounts sign-in** — the account screen currently stores local state only.
   Wire it to `accounts.bansify.com` (native `/api/mobile/auth/*` flow) so the browser uses
   the same account as Mail, Drive and Store.
2. **Sync** — bookmarks, history and open tabs against the account, opt-in per category
   (the toggles already exist in the account model).
3. **Signed release pipeline** — set the four signing secrets, tag a build, publish the APK
   and AAB from `release.yml`.
4. **Test coverage** — replace the template tests with real coverage for `UrlUtils`,
   `BrowserViewModel` and the DAOs (see [TESTING.md](TESTING.md)).

## Later

- Ad and tracker blocking with a user-editable filter list.
- Reader-mode typography controls and text-to-speech.
- Tab groups and a grid/list tab switcher layout option.
- Desktop-mode per site, remembered.
- Downloads: pause/resume, share sheet integration, storage-location choice.
- TV: focus ring polish, remote shortcuts, leanback launcher entry.
- Translations (the string resources are ready for `values-<lang>`).

## Not planned

- Bundled ad-blocking engine that bypasses site paywalls.
- Any feature that requires collecting browsing history on a server without explicit opt-in.
