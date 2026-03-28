<!--
  - SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
  - SPDX-License-Identifier: GPL-3.0-or-later
-->
# Agents.md

This file provides guidance to all AI agents (Claude, Codex, Gemini, etc.) working with code in this repository.

You are an experienced engineer specialized on Java, Kotlin and familiar with the platform-specific details of Android.

## Your Role

- You implement features and fix bugs.
- Your documentation and explanations are written for less experienced contributors to ease understanding and learning.
- You work on an open source project and lowering the barrier for contributors is part of your work.

## Project Overview

Nextcloud Notes for Android — a notes management app that syncs with a Nextcloud server. Written primarily in Java (legacy) with new code in Kotlin. Targets API 24+ (minSdk 24, targetSdk 36). Uses Nextcloud Single Sign-On (SSO) for authentication.

## Build Commands

```bash
# Assemble debug APK (F-Droid flavor)
./gradlew assembleFdroidDebug

# Assemble Google Play flavor
./gradlew assemblePlayDebug

# Run unit tests
./gradlew test

# Run a single unit test class
./gradlew testFdroidDebugUnitTest --tests "it.niedermann.owncloud.notes.SomeTest"

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Static analysis
./gradlew check
./gradlew ktlintCheck
./gradlew lint

# Auto-fix ktlint issues
./gradlew ktlintFormat
```

Build output: `app/build/outputs/apk/`

## Build Flavors

| Flavor   | App ID                              | Purpose           |
|----------|-------------------------------------|-------------------|
| `fdroid` | `it.niedermann.owncloud.notes`      | F-Droid release   |
| `play`   | `it.niedermann.owncloud.notes`      | Google Play       |
| `dev`    | `it.niedermann.owncloud.notes.dev`  | Development builds|
| `qa`     | `it.niedermann.owncloud.notes.qa`   | Per-PR testing    |

## Architecture

The app follows MVVM with a clear layered structure. All source lives under `app/src/main/java/it/niedermann/owncloud/notes/`.

### Key Layers

- **Persistence layer** — `persistence/`: Room database (`NotesDatabase.java`, version 29 with migrations 9–29), `NotesRepository.java` as single source of truth, and `ApiProvider.java` which caches per-account Retrofit instances for `NotesAPI`, `OcsAPI`, `FilesAPI`, `ShareAPI`, and `UserStatusAPI`.
- **Sync** — `SyncWorker` (WorkManager background sync) and `NotesServerSyncTask` handle server synchronization.
- **ViewModel layer** — per-feature ViewModels (`MainViewModel`, `CategoryViewModel`, `ManageAccountsViewModel`, etc.) expose data to the UI via LiveData.
- **UI layer** — traditional XML layouts. Activities/Fragments per feature area. `MainActivity` is the main entry point post-login.

### Authentication

Uses Nextcloud Android-SingleSignOn (SSO). Accounts are managed via `SingleSignOnAccount` / `SingleAccountHelper`. `ApiProvider` creates and caches Retrofit instances per SSO account, avoiding repeated reflection overhead.

### Feature Package Structure

| Package | Purpose |
|---|---|
| `main/` | Note list, navigation drawer, multi-select, grid/list toggle |
| `edit/` | Note editor fragments, category/title editing |
| `persistence/` | Room DB entities/DAOs, repository, API interfaces, workers |
| `branding/` | Server-driven Nextcloud theming via `BrandingUtil` / `NotesViewThemeUtils` |
| `shared/` | Common models (`Capabilities`, `ApiVersion`), utils, RxJava extensions |
| `widget/` | Two widget types: single note preview and note list |
| `exception/` | Global exception handling with tip/suggestion system |
| `importaccount/` | SSO account import flow |

### Database

Room database version 29. Migrations 9–24 are manual Java migrations; 25–29 use Room auto-migrations. Schema JSON files are in `app/schemas/`. Entities: `Account`, `Note`, `CategoryOptions`, `Capabilities`, `ShareEntity`, `SingleNoteWidgetData`, `NotesListWidgetData`.

### Reactive Programming

RxJava 2 is used throughout for async operations. Kotlin extensions for RxJava live in `shared/`. New code should prefer coroutines/Flow where practical.

## General Guidance

Every new file needs to get a SPDX header in the first rows according to this template. 
The year in the first line must be replaced with the year when the file is created (for example, 2026 for files first added in 2026).
The commenting signs need to be used depending on the file type.

```plaintext
SPDX-FileCopyrightText: <YEAR> Nextcloud GmbH and Nextcloud contributors
SPDX-License-Identifier: AGPL-3.0-or-later
```
Kotlin/Java:
```kotlin
/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: <year> Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
```

XML:
```xml
<!--
  ~ Nextcloud - Android Client
  ~
  ~ SPDX-FileCopyrightText: <year> Nextcloud GmbH and Nextcloud contributors
  ~ SPDX-License-Identifier: AGPL-3.0-or-later
-->
```

## Design

- Follow Material Design 3 guidelines
- In addition to any Material Design wording guidelines, follow the Nextcloud wording guidelines at https://docs.nextcloud.com/server/latest/developer_manual/design/foundations.html#wording
- Ensure the app works in both light and dark theme
- Ensure the app works with different server primary colors by using the colorTheme of viewThemeUtils

## Commit and Pull Request Guidelines

### Commits

- All commits must be signed off (`git commit -s`) per the Developer Certificate of Origin (DCO). All PRs target `master`. Backports use `/backport to stable-X.Y` in a PR comment.

- Commit messages must follow the [Conventional Commits v1.0.0 specification](https://www.conventionalcommits.org/en/v1.0.0/#specification) — e.g. `feat(chat): add voice message playback`, `fix(call): handle MCU disconnect gracefully`.

- Every commit made with AI assistance must include an `AI-assistant` trailer identifying the coding agent, its version, and the model(s) used:

  ```
  AI-assistant: Claude Code 2.1.80 (Claude Sonnet 4.6)
  AI-assistant: Copilot 1.0.6 (Claude Sonnet 4.6)
  ```

  General pattern: `AI-assistant: <coding-agent> <agent-version> (<model-name> <model-version>)`

  If multiple models are used for different roles, extend the trailer with named roles:

  ```
  AI-assistant: OpenCode v1.0.203 (plan: Claude Opus 4.5, edit: Claude Sonnet 4.5)
  ```

  Pattern with roles: `AI-assistant: <coding-agent> <agent-version> (<role>: <model-name> <model-version>, <role>: <model-name> <model-version>)`

### Pull Requests

- Include a short summary of what changed. *Example:* `fix: prevent crash on empty todo title`.
- **Pull Request**: When the agent creates a PR, it should include a description summarizing the changes and why they were made. If a GitHub issue exists, reference it (e.g., “Closes #123”).

## Code Style

- Do not exceed 300 line of code per file.
- Line length: **120 characters**
- Standard Android Studio formatter with EditorConfig.
- Indentation: 4 spaces, UTF-8 encoding
- Kotlin preferred for new code; legacy Java still present
- Do not use decorative section-divider comments of any kind (e.g. `// ── Title ───`, `// ------`, `// ======`).
- Every new file must end with exactly one empty trailing line (no more, no less).
- Do not add comments, documentation for every function you created instead make it self explanatory as much as possible.
- `ktlint_code_style = android_studio`; disabled ktlint rules: `import-ordering`, `no-consecutive-comments`; trailing commas disallowed
- All new files must include an SPDX license header: `SPDX-License-Identifier: GPL-3.0-or-later`
- Translations: only modify `values/strings.xml`; never the translated `values-*/strings.xml` files
- Create models, states in different files instead of doing it one single file.
- Do not use magic number.
- Apply fail fast principle instead of using nested if-else statements.
- Do not use multiple boolean flags to determine states instead use enums or sealed classes.
- Use modern Java for Java classes. Optionals, virtual threads, records, streams if necessary.
- Avoid hardcoded strings, colors, dimensions. Use resources.
- Run lint, spotbugsGplayDebug, detekt, spotlessKotlinCheck and fix findings inside the files that have been changed.

## Testing

- **Unit tests**: `app/src/test/` — JUnit 4, Mockito, Robolectric. Uses `includeAndroidResources = true`.
- **Instrumented tests**: `app/src/androidTest/` — Espresso, requires running device/emulator.
- Parallel test execution enabled (max forks = processors / 2).
- Code coverage via JaCoCo (enabled for debug builds).
