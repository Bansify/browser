# Build

## Prerequisites

| Tool | Version | Notes |
|---|---|---|
| JDK | 17 or newer | CI uses Temurin 21 |
| Android SDK | platform 36, build-tools latest | `compileSdk`/`targetSdk` 36, `minSdk` 24 |
| Gradle | via the committed wrapper (9.3.1) | never install Gradle globally |
| Kotlin | 2.4.21 | supplied by the build |

Set `ANDROID_HOME` (or `sdk.dir` in `local.properties`) before the first build.

## Commands

```bash
./gradlew :app:assembleDebug        # debug APK  → app/build/outputs/apk/debug/
./gradlew :app:installDebug         # install on the connected device
./gradlew :app:testDebugUnitTest    # JVM unit tests
./gradlew :app:lintDebug            # Android lint
./gradlew :app:assembleRelease      # release APK (signed only if KEYSTORE_PATH is set)
./gradlew :app:bundleRelease        # Play Store AAB
```

Clean build when the Gradle cache misbehaves:

```bash
./gradlew clean --no-daemon
rm -rf .gradle app/build build
```

## Variants and signing

- **debug** — signed with the standard `~/.android/debug.keystore` that AGP creates on first
  use. Nothing to configure; no keystore is committed.
- **release** — signed only when the environment provides a keystore:

  ```bash
  export KEYSTORE_PATH=/absolute/path/to/release.jks
  export KEYSTORE_PASSWORD=…   # STORE_PASSWORD also accepted
  export KEY_ALIAS=…           # defaults to "upload"
  export KEY_PASSWORD=…
  ./gradlew :app:assembleRelease
  ```

  Without those variables the release APK is produced **unsigned**, which is what the
  release workflow expects: it signs the artifact itself with `apksigner`.

CI passes `-PversionCode` and `-PversionName`; local builds fall back to `1` and `1.0`.

## Build configuration in one place

| Setting | Where |
|---|---|
| applicationId / namespace | `app/build.gradle.kts` → both `com.bansify.browser` |
| SDK levels, version code/name | `app/build.gradle.kts` |
| Dependency versions | `gradle/libs.versions.toml` |
| Optional secrets (`.env`) | `app/build.gradle.kts` → `secrets { }`, template in `.env.example` |

## What the build does not need

- No `google-services.json` — the Firebase plugin is configured with
  `MissingGoogleServicesStrategy.WARN`, so a missing file is a warning, not a failure.
- No `.env` file — the Secrets plugin falls back to `.env.example`.
- No network access beyond Maven repositories.

## Continuous integration

`.github/workflows/quality.yml` runs on every pull request and on `main`: a fast rules gate,
JVM unit tests, a debug assemble and Android lint. `build_apk.yml` (push to `main`) and
`release.yml` (tags `v*`) build and sign the release artifacts. Workflow files are owned by
the maintainer — see [../CONTRIBUTING.md](../CONTRIBUTING.md).
