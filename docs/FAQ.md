# FAQ

## Product

**Does it need an account?**
No. Browsing, tabs, bookmarks, history, downloads and every setting work without signing in.
The account screen exists so bookmarks and history can sync later — today it stores state on
the device only.

**Does it block ads?**
Not yet. Safe browsing, HTTPS-first, Do Not Track and third-party cookie controls are in.
Filter-list based blocking is on the roadmap, not in the build.

**Where is my history stored?**
In a local Room database inside the app's private storage. Nothing is uploaded. Incognito
tabs never write history.

**Can I set it as the default browser?**
Yes on Android 7+ once the app declares the HTTP/HTTPS intent filters for its build. If the
system picker does not offer it, the build is missing those filters — check
`AndroidManifest.xml` before filing a bug.

**Does it work on a TV?**
The UI is built for touch and D-pad; a remote can drive the toolbar and page focus. TV polish
is on the roadmap.

## Development

**Which Gradle/JDK do I need?**
Use the committed wrapper (9.3.1) and JDK 17–21. See [BUILD.md](BUILD.md).

**Why is `namespace` the same as `applicationId`?**
Both are `com.bansify.browser`; the sources live in `com/bansify/browser/**`, so the
generated `R` class matches the package.

**Why do debug builds not need a keystore?**
Debug builds are signed with the standard `~/.android/debug.keystore` that AGP creates on
first use. Release builds take the keystore from `KEYSTORE_PATH` (see [RELEASING.md](RELEASING.md)).

**Can I change a workflow file?**
No. `.github/workflows/**` is maintainer-owned; open an issue with the change you need. See
[../CONTRIBUTING.md](../CONTRIBUTING.md).

**Where do new strings go?**
`app/src/main/res/values/strings.xml`. English only, sentence case.

## Can I reuse this code?

There is no `LICENSE` file in this repository yet, so no licence is granted: the code is all
rights reserved and must not be copied, redistributed or reused until the owner adds one. If
you are the owner, adding a `LICENSE` file is the one step that makes reuse explicit.
