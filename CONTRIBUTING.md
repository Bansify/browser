# Contributing

## Before you start

- Read [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) so a change lands in the right layer.
- Build once locally: `./gradlew :app:assembleDebug` (see [docs/BUILD.md](docs/BUILD.md)).
- For anything larger than a bug fix, open an issue first and describe the change.

## Ground rules

1. **Do not edit `.github/workflows/**`.** Workflow files are maintainer-owned. If a change
   needs one, open an issue with the exact diff you want.
2. **English only** — code, comments, UI strings, docs and commit messages.
3. **No secrets in the repository** — no keystores, passwords, API keys or tokens. Refer to
   a secret by name.
4. **Keep the package id** `com.bansify.browser` and the `namespace` in step with it.
5. **One concern per pull request.** A refactor and a feature in one PR cannot be reviewed.

## Branches and commits

```text
main                     always buildable
feat/<short-name>        new behaviour
fix/<short-name>         bug fix
docs/<short-name>        documentation only
```

Commit messages use the imperative mood and a scope where it helps:

```text
feat(tabs): keep the active tab after closing the last incognito tab
fix(downloads): resume the queue after a network drop
docs: add the releasing checklist
```

## Pull request checklist

- [ ] `./gradlew :app:assembleDebug` succeeds
- [ ] `./gradlew :app:testDebugUnitTest` passes
- [ ] new behaviour has a test, or the PR says why it cannot be tested
- [ ] user-visible strings are in `strings.xml`
- [ ] docs updated in the same PR (`docs/`, `CHANGELOG.md`)
- [ ] screenshots or a screen recording for any UI change
- [ ] no workflow, keystore or secret changes

## Code review

Reviewers look for: correct layer (UI vs ViewModel vs data), lifecycle-safe state, no
`!!`, no swallowed exceptions, string resources, and a test that would fail if the bug came
back. Style details are in [docs/CODE_STYLE.md](docs/CODE_STYLE.md).

## Reporting bugs

Include: app version (`Settings → About` or the APK name), Android version and device,
steps to reproduce, expected vs actual, and logcat lines around the failure. A screenshot of
a broken page is worth more than a paragraph.
