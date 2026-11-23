# Releasing

## Versioning

`versionCode` and `versionName` come from `-PversionCode` / `-PversionName`, which the
workflows fill in automatically:

- `build_apk.yml` — `versionCode = github.run_number`, `versionName = 1.0.<run_number>`
  unless you pass the manual inputs.
- `release.yml` — same, except a `v*` tag supplies the name (`v1.2.0` → `1.2.0`).

Local builds fall back to `1` / `1.0`.

## Signing

The release keystore is **never** committed. The workflows read four repository secrets:

| Secret | Meaning |
|---|---|
| `RELEASE_KEYSTORE_BASE64` | `base64 -w0 release.jks` |
| `KEYSTORE_PASSWORD` | keystore password |
| `KEY_ALIAS` | key alias inside the keystore |
| `KEY_PASSWORD` | key password |

If `RELEASE_KEYSTORE_BASE64` is missing the workflow generates a **temporary** keystore and
warns — useful for a smoke build, useless for the store, because every build then has a
different signing identity. Set the four secrets before shipping anything.

The keystore for this app must stay the same for the whole life of the listing: Android only
accepts an update signed by the same key. Keep a copy outside GitHub (password manager plus
an offline backup).

## Steps for a release

1. Merge the change into `main` and confirm the `Quality Gate & Tests` run is green.
2. Push a tag: `git tag v1.2.0 && git push origin v1.2.0` — `release.yml` builds the APK,
   the debug APK and the AAB, signs them and publishes a GitHub Release.
3. Download the `-Release.apk` from the release page and check the certificate:

   ```bash
   apksigner verify --print-certs app-release.apk
   ```

   The SHA-256 must match the fingerprint kept with the keystore backup.
4. Upload the `-PlayStore.aab` to the Play Console, or side-load the APK on a device and run
   the checklist in [TESTING.md](TESTING.md).
5. Update [../CHANGELOG.md](../CHANGELOG.md).

## Current state

`build_apk.yml` and `release.yml` have been adapted to the single-module layout (no
`app/common` module, no `packaging/` desktop scripts). They still expect the four signing
secrets above; until those exist, treat their output as a smoke build only.

## Store checklist

- [ ] `applicationId` unchanged (`com.bansify.browser`)
- [ ] `versionCode` strictly greater than the published build
- [ ] signed with the same key as the live listing
- [ ] target SDK meets the current Play requirement
- [ ] privacy policy URL and data-safety answers match what the app actually does
- [ ] screenshots and the 512×512 store icon (from the icon kit) updated
- [ ] release notes written in [../CHANGELOG.md](../CHANGELOG.md)
