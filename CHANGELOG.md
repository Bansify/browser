# Changelog

All notable changes to this project are recorded here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project uses
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- `docs/` with architecture, build, releasing, branding, style, testing, roadmap,
  troubleshooting and FAQ pages.
- `CONTRIBUTING.md`, `SECURITY.md`, `CODE_OF_CONDUCT.md` and this changelog.

### Changed
- `.github/workflows/quality.yml`, `build_apk.yml` and `release.yml` adapted to the
  single-module layout: `app/common` tasks removed, the missing `scripts/` steps replaced
  with inline equivalents, desktop packaging steps dropped.
- `versionCode`/`versionName` now read `-PversionCode`/`-PversionName` from CI, with local
  fallbacks of `1` and `1.0`.

## [1.0.0] — 2026-11-23

First build of the rewritten app.

### Added
- New Compose browser: Chrome-style toolbar and omnibox, tab switcher, new tab page,
  reading mode, overflow menu, Material 3 Expressive components.
- Bookmarks, history, downloads, shortcuts and saved tabs in Room.
- Settings surface for search, appearance, privacy, content, downloads and language.
- Bansify account screen with sync preferences (local state only).
- Chrome-style launcher icon: white mark on the brand blue `#0494F4`, every density plus
  adaptive and monochrome layers.

### Changed
- Package and namespace are `com.bansify.browser`.
- The previous TV-Bro-derived multi-module code base was replaced with this single-module
  project.

### Removed
- `app/common` module, `buildSrc` convention plugin, `packaging/` desktop scripts and the
  `scripts/` CI helpers that belonged to the old layout.
