# Troubleshooting

## Build

**`SDK location not found`**
Create `local.properties` with `sdk.dir=/path/to/Android/sdk`, or export `ANDROID_HOME`.

**`Unsupported class file major version`**
The JDK is too new or too old for the Gradle/AGP pair. Use JDK 17–21; CI uses 21.

**`Could not resolve …` / timeouts**
Check the network/proxy, then `./gradlew --refresh-dependencies`. Corporate proxies need
`gradle.properties` entries, not shell variables.

**`Execution failed for task ':app:processDebugGoogleServices'`**
No `google-services.json`. The build is configured to warn instead of fail, so if it does
fail, check that `googleServices { missingGoogleServicesStrategy = … }` is still in
`app/build.gradle.kts`.

**KSP / Room errors after a dependency bump**
`./gradlew clean` then rebuild. Room's generated code lives in `app/build/generated/ksp/`.

## Runtime

**Blank page or `net::ERR_…`**
Usually TLS or a blocked host. Check logcat for the `WebViewClient.onReceivedError` entry;
the app surfaces SSL and safe-browsing errors through the ViewModel.

**Downloads do not start**
Android 10+ needs a writable location and, on some OEM builds, the storage permission for
the chosen folder. Check `DownloadManager` in logcat.

**Incognito tab keeps state**
Closing the last incognito tab should drop its `WebView`. If it does not, note the tab id and
the `WebViewPool` cache entry in a bug report.

**Settings do not stick**
Every preference goes through DataStore. If one resets, check that its key exists in
`BrowserPreferences.Keys` and that the flow is collected with
`collectAsStateWithLifecycle()`.

## CI

**`quality.yml` red on the rules gate**
The gate greps for `com.example` and requires `namespace`/`applicationId` to be
`com.bansify.browser`. Fix the source; do not relax the gate.

**`build_apk.yml` / `release.yml` red at signing**
The four signing secrets are missing or the alias does not exist in the keystore. The
workflow prints the detected alias — compare it with `KEY_ALIAS`.

**Workflow changes**
Workflow files are owned by the maintainer. Do not edit them in a feature pull request; open
an issue instead.
