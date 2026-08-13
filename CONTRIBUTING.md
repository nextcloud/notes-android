<!--
 ~ SPDX-FileCopyrightText: 2016-2026 Nextcloud GmbH and Nextcloud contributors
 ~ SPDX-FileCopyrightText: 2016-2024 Stefan Niedermann <info@niedermann.it>
 ~ SPDX-License-Identifier: GPL-3.0-or-later
-->
# Contributing guide

Thanks for helping out with Nextcloud Notes for Android. This guide covers how to report a bug, how to set up the
project, and what a pull request has to satisfy before it can be merged.

## Submitting bug reports

Please [open an issue](https://github.com/nextcloud/notes-android/issues/new/choose) and pick the **🐞 Bug report**
template. It asks for everything maintainers need, and filling it in completely is the fastest way to get your report
triaged:

- Notes Android version, Notes server version, Nextcloud Android (Files) app version and Nextcloud server version
- Android version and device
- Where you installed the app from (Google Play, F-Droid or self-built)
- Steps to reproduce, expected behaviour and, if possible, screenshots or a log

Before opening an issue, please read the [FAQ](FAQ.md) and search the existing issues. Note that this repository is only
about the **Android app** — problems with the [Notes server app](https://github.com/nextcloud/notes/issues) or the
[Nextcloud Android app](https://github.com/nextcloud/android/issues) belong in their own repositories.

## Adding new features

For anything beyond a small fix, please [open an issue](https://github.com/nextcloud/notes-android/issues/new/choose)
first and ask whether the feature is wanted. It would be disappointing to build something and then have the pull request
rejected for a reason you could not have known about. This is especially true for changes that touch several subsystems
or require an architectural decision.

## Project setup

| Setting | Value |
|---|---|
| Language for new code | **Kotlin** (see [Code style](#code-style)) |
| minSdk / targetSdk / compileSdk | 28 / 36 / 37 |
| Authentication | Nextcloud [Android SingleSignOn](https://github.com/nextcloud/Android-SingleSignOn) |

Import the project into Android Studio (a recent stable release that supports AGP 9) or build from the command line
with the Gradle wrapper. To run the app you need a Nextcloud server with the
[Notes server app](https://github.com/nextcloud/notes) installed, and the Nextcloud Files app on the device or emulator
to provide the SSO account.

### Build flavors

| Flavor | Application ID | Purpose |
|---|---|---|
| `fdroid` | `it.niedermann.owncloud.notes` | F-Droid release |
| `play` | `it.niedermann.owncloud.notes` | Google Play release |
| `dev` | `it.niedermann.owncloud.notes.dev` | Development builds, installable next to a release build |
| `qa` | `it.niedermann.owncloud.notes.qa` | Per-pull-request test builds |

### Common commands

```bash
# Assemble a debug APK
./gradlew assembleDevDebug          # or assembleFdroidDebug / assemblePlayDebug

# Unit tests
./gradlew test                      # all variants
./gradlew testFdroidDebugUnitTest   # one variant

# Android lint
./gradlew lintDevDebug              # the variant CI checks
./gradlew lintFdroidDebug

# Lint plus unit tests
./gradlew check

# Instrumented tests (device or emulator required)
./gradlew connectedAndroidTest
```

APKs are written to `app/build/outputs/apk/`.

## Code style

Formatting is defined by [`.editorconfig`](.editorconfig): 4 spaces, UTF-8, a maximum line length of 120 characters, no
trailing whitespace, and exactly one trailing newline at the end of every file. The standard Android Studio formatter
picks these up automatically.

### New files are Kotlin

Kotlin is the language of this project. **Every new source file must be a `.kt` file** — a dedicated
[CI check](.github/workflows/detectNewJavaFiles.yml) fails the pull request when a new `.java` file is added.

A large amount of Java is still present as legacy that is being migrated away from. Editing, debugging and fixing that
Java is normal and expected: fix a bug in a Java class by editing that Java class rather than converting it as a side
effect of an unrelated change.

### Conventions

- Keep files small — 300 lines is the ceiling. A file already at that size must not grow; put new code in a new Kotlin
  file (extension function, use case, mapper, state or model class) and keep the edit to the existing file to the
  minimum that wires it up.
- One top-level type per file, and separate files for models and states.
- Let names and small functions carry the meaning instead of comments. Do not add decorative section dividers
  (`// ---- Title ----`).
- 
### Design

- Follow Material Design 3 and the
  [Nextcloud wording guidelines](https://docs.nextcloud.com/server/latest/developer_manual/design/foundations.html#wording).
- Make sure the change works in light and dark theme, and with different server primary colors by using the
  `viewThemeUtils` color theme instead of fixed colors.

### License headers

The repository is [REUSE](https://reuse.software) compliant and CI enforces it. Every new file needs an SPDX header
whose year is the year the file was created:

```kotlin
/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
```

```xml
<!--
  ~ Nextcloud Notes - Android Client
  ~
  ~ SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
  ~ SPDX-License-Identifier: GPL-3.0-or-later
-->
```

Use the comment syntax of the respective file type for other kinds of files.

## Testing

- Unit tests live in `app/src/test/` and use JUnit 4, Mockito and Robolectric (with `includeAndroidResources`).
- Instrumented tests live in `app/src/androidTest/` and use Espresso; they need a running device or emulator.
- Coverage is collected with JaCoCo for debug builds, e.g.
  `./gradlew createFdroidDebugUnitTestCoverageReport`.

Add tests for your change, or state in the pull request why they are not needed.

## Translations

Translations are managed through [Transifex](https://explore.transifex.com/nextcloud/nextcloud/) and synced into the
repository automatically. Only ever edit `app/src/main/res/values/strings.xml`; never touch the translated
`values-*/strings.xml` files.

## Commits and pull requests

### Commits

- Sign off every commit (`git commit -s`) to certify the
  [Developer Certificate of Origin](https://developercertificate.org/). Only you as a human contributor can do this.
- Follow [Conventional Commits v1.0.0](https://www.conventionalcommits.org/en/v1.0.0/#specification), for example
  `feat(editor): add checkbox toggling` or `fix(sync): handle expired SSO token`.
- Every commit that contains AI-assisted content needs a trailer identifying the agent and model, which the
  [AI Policy workflow](.github/workflows/ai-policy.yml) checks for:

  ```
  AI-assistant: Claude Code 2.1.80 (Claude Sonnet 4.6)
  ```

  With distinct roles: `AI-assistant: OpenCode v1.0.203 (plan: Claude Opus 4.5, edit: Claude Sonnet 4.5)`.
  A coding agent must never appear in `Signed-off-by` or `Co-Authored-By`.

### Pull requests

Pull requests target **`main`**. Backports are requested by commenting `/backport to stable-xx.x` on the merged pull
request.

The [pull request template](.github/pull_request_template.md) asks you to confirm that tests are included or not
needed, that backports and the milestone are set, that the title is meaningful, and — if applicable — that the content
was partly or fully generated using AI. Please disclose AI use there; it is required by the Nextcloud
[AI Contribution Policy](https://github.com/nextcloud/.github/blob/master/AI_POLICY.md).

Beyond that:

- Describe what changed and why, and reference the issue it addresses (`Closes #123`).
- Add before/after screenshots for user-visible changes.
- Keep each pull request focused on one concern. No unrelated files, no incidental refactors. If it grows towards
  several thousand changed lines, split it.
- Before you push, run `./gradlew lintDevDebug` and `./gradlew test`, and fix every finding in the files you changed.
  CI runs lint, unit tests, a debug APK build, the new-Java-file check and the REUSE check.
- Review your own diff first and remove dead code, unused parameters, unreachable defensive branches, leftover
  scaffolding and superfluous comments.

Contributions must comply with the Nextcloud
[Contribution Guidelines](https://github.com/nextcloud/.github/blob/master/CONTRIBUTING.md) and, where AI tools were
involved, the [AI Contribution Policy](https://github.com/nextcloud/.github/blob/master/AI_POLICY.md).

If you use an AI coding agent on this repository, see [AGENTS.md](AGENTS.md) — it holds the rules the agent is expected
to follow, with detailed guides in `.claude/skills/`.
