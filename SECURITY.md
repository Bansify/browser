# Security policy

## Supported versions

Only the latest build on `main` is supported. Older builds are not patched.

## Reporting a vulnerability

**Do not open a public issue.** Report privately to the maintainer
(`gothwadtech@gmail.com`) with:

- what the issue is and which component (UI, WebView plumbing, storage, account)
- steps to reproduce, or a proof of concept
- the app version, Android version and device
- the impact you believe it has

You can expect an acknowledgement within a few days. Please give time for a fix before any
public disclosure.

## Scope

In scope:

- the app's own code (Kotlin, manifest, exported components, intents)
- storage of browsing data, bookmarks, history and downloads
- the account surface and any tokens it handles
- unsafe handling of web content (file access, intent URLs, downloads)

Out of scope:

- vulnerabilities in Chromium/WebView itself — report those to Google
- sites the user visits
- a rooted or otherwise compromised device
- missing hardening that requires an unlocked bootloader to matter

## Handling secrets

- No keystore, password, API key or token is ever committed. Signing material lives in
  GitHub Actions secrets (`RELEASE_KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`,
  `KEY_PASSWORD`).
- `.env` is git-ignored; only `.env.example` is tracked, and it contains placeholders.
- If a secret leaks, rotate it first and treat any artifact signed with it as untrusted.
