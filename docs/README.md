# Documentation

One place per topic. If a document grows past a single responsibility, split it and add a
row here.

## Start here

| Document | Purpose |
|---|---|
| [../README.md](../README.md) | What the app is, features, quick start |
| [ARCHITECTURE.md](ARCHITECTURE.md) | How the code is organised and how data flows |
| [BUILD.md](BUILD.md) | Prerequisites, commands, variants, signing |
| [RELEASING.md](RELEASING.md) | Versioning, secrets, store checklist |
| [BRANDING.md](BRANDING.md) | Colours, icon, typography, naming |
| [CODE_STYLE.md](CODE_STYLE.md) | Kotlin and Compose conventions |
| [TESTING.md](TESTING.md) | Test layers and the manual QA checklist |
| [ROADMAP.md](ROADMAP.md) | Done, next, later |
| [TROUBLESHOOTING.md](TROUBLESHOOTING.md) | Build and runtime problems |
| [FAQ.md](FAQ.md) | Product and contributor questions |

## Repository-level documents

| Document | Purpose |
|---|---|
| [../CHANGELOG.md](../CHANGELOG.md) | Release history, newest first |
| [../CONTRIBUTING.md](../CONTRIBUTING.md) | Branch, commit and review rules |
| [../SECURITY.md](../SECURITY.md) | Reporting a vulnerability |
| [../CODE_OF_CONDUCT.md](../CODE_OF_CONDUCT.md) | Community expectations |

## Rules for these documents

1. **Keep them true.** A document that describes an old layout is worse than no document.
   When a change alters behaviour, update the matching file in the same pull request.
2. **One responsibility per file.** Build steps do not belong in the architecture page.
3. **No secrets.** Never paste a keystore, password, API key or token. Refer to the secret
   name instead, for example `KEYSTORE_PASSWORD`.
4. **English only**, same as the code and the UI.
