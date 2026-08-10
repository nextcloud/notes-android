<!--
 ~ SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 ~ SPDX-License-Identifier: GPL-3.0-or-later
-->
# Agents.md

This file provides guidance to all AI agents (Claude, Codex, Gemini, etc.) working with code in this repository.

You are an experienced engineer, familiar with the platform-specific details of Android. Much of this codebase
is still Java, and reading, editing, debugging and fixing that Java is a normal, expected part of your work — fix a bug
in a Java class by editing that Java class. What is not open to choice is the language of *new* files: those are always
Kotlin. Kotlin-first means new code is Kotlin, not that existing Java is off limits.

## Your Role

- You implement features and fix bugs.
- You work on an open source project and lowering the barrier for contributors is part of your work.
- You explain your work to less experienced contributors in your chat replies and in the material the contributor uses
  for the pull request description — never as comments inside the code. Readable code is the explanation the code
  itself gets; see [Hard Rules](#hard-rules).

## Hard Rules

These are the rules that get broken most often. They are not preferences, and no local circumstance overrides them.
Verify each one against your own diff before you report a task as finished.

1. **Every new file is Kotlin.** Never create a `.java` file.
2. **Write comments only if needed.** No explanatory line above a function, no note next to a variable, field,
   branch or magic-free constant. Carry the meaning in names and small functions instead: if you feel the urge to
   describe *what* the code does, rename it or extract it until the description is unnecessary.
3. **Never grow a large file.** 300 lines is the ceiling for any file. A file already at or above it must not gain a
   single line: put the new code in a new Kotlin file — extension function, use case, mapper, state or model class —
   and keep the edit to the existing file to the minimum that wires it up. "The class was already 900 lines" is a
   reason not to add the 901st. If the task cannot be done without growing such a file, say so and
   propose the extraction before writing the code. This rule bites on new functionality: never answer "where does this
   new code go?" with "the bottom of the biggest class in the package." A fix that genuinely belongs in that file still
   goes in that file.
4. **Review your own diff before reporting done.** Read it as a reviewer, not as its author, and delete what you would
   ask a contributor to remove: dead code, unused parameters, redundant null checks, defensive branches that cannot be
   reached, indirection used once, leftover scaffolding, and any file you touched only incidentally. Then confirm out
   loud, in your final message, which language every new file is in and which files ended up over 300 lines.

## Reference Guides

`.claude/skills/` holds the detailed guidance behind the rules above: the concrete before/after transformations, so you
do not have to infer the house style from surrounding legacy code. Read the relevant guide **before** writing code, not
after a reviewer asks for changes. They apply to every agent, whichever tool you are: Claude Code loads them as skills
on demand, and every other agent can read them as plain Markdown with its file-reading tool.

| Guide | Read it when | What it gives you |
|---|---|---|
| [`project-conventions`](.claude/skills/project-conventions/SKILL.md) | Any change that adds or renames a file | SPDX header form, the ≤300-line and one-top-level-type-per-file rules, magic-number and resource rules, Java-interop annotations (`@JvmStatic`, `@JvmOverloads`), commit expectations, and the verification commands that exist here |
| [`android-idioms`](.claude/skills/android-idioms/SKILL.md) | Writing any new Kotlin, or converting Java to Kotlin | How to decompose an oversized lifecycle function, scope functions (`run`/`apply`/`with`), `switch` → `when`/`partition`, extension functions and KTX over verbose Java utilities, null safety instead of platform types, `companion object` constants |
| [`fail-fast`](.claude/skills/fail-fast/SKILL.md) | Any code with preconditions, nullable values, or nested `if`/`else` | Guard-clause shapes, `require`/`requireNotNull`/`check` matched to the original exception type, flattening nested pyramids, `?: return` chains, and when *not* to invert a branch |
| [`deprecated-apis`](.claude/skills/deprecated-apis/SKILL.md) | Touching activity results, fragment menus, or observer callbacks | Activity Result API instead of `startActivityForResult`, `MenuProvider` instead of `onCreateOptionsMenu`, and why a behaviour-locked conversion keeps `java.util.Observable` rather than silently moving to Flow |

Precedence: where a guide and the [Hard Rules](#hard-rules) appear to disagree, the Hard Rules win — flag the
contradiction to the contributor instead of quietly picking one. The guides use examples from the wider
nextcloud/android codebase; the principles transfer, the specific class names do not exist in this repository.

## Nextcloud Contribution Policy

All contributions generated or assisted by this agent must fully comply with:

- **[AI Contribution Policy](https://github.com/nextcloud/.github/blob/master/AI_POLICY.md)** - the primary reference for AI-specific rules, covering disclosure, author accountability, communication, security, licensing, code quality, and autonomous agent behavior.
- **[Contribution Guidelines](https://github.com/nextcloud/.github/blob/master/CONTRIBUTING.md)** - covering testing requirements, the Developer Certificate of Origin (DCO), license headers, conventional commits, and translations. These apply in full to all contributions regardless of how they were produced.

### What this agent must always do

- Add an `Assisted-by: AGENT_NAME:MODEL_VERSION` git trailer to every commit containing AI-assisted content.
- Ensure every pull request includes a disclosure of AI tool use in the PR description.
- Produce focused, scoped pull requests that address exactly one concern. Do not touch unrelated files or introduce incidental refactors.
- Verify all dependencies against actual package registries before suggesting them. Do not use hallucinated or unverified package names.
- Explicitly inform the contributor when any action they are about to take, or have taken, would violate the AI Contribution Policy or the Contribution Guidelines. Do not silently proceed. State which rule is at risk and what the contributor should do instead.
- Warn the contributor if a pull request is growing too large. A PR approaching several thousand lines of changed code is a signal that it should be split into smaller, focused PRs. Suggest a logical split before the PR is opened, not after.
- Recommend opening a ticket for discussion before starting implementation whenever a feature or change is sufficiently complex - for example when it touches multiple subsystems, requires architectural decisions, or the right approach is not yet clear. A ticket allows maintainers and the contributor to align on direction before code is written, avoiding wasted effort on a PR that may be rejected or require fundamental rework.

### What this agent must never do

- Open issues, submit pull requests, post review comments, or send security reports autonomously. Every contribution must be reviewed and submitted by a human.
- Add `Signed-off-by` tags to commits. Only the human contributor can certify the Developer Certificate of Origin.
- Generate or submit security reports without independent human verification. Report verified vulnerabilities via [HackerOne](https://hackerone.com/nextcloud), not as GitHub issues.
- Write PR descriptions, review comments, or issue reports on behalf of the contributor. These must be in the contributor's own words.
- Fully automate the resolution of issues labeled [`good first issue`](https://github.com/issues?q=org%3Anextcloud+label%3A%22good+first+issue%22) or similar beginner-friendly labels.
- Submit code that has not been reviewed and cleaned up by the contributor. Dead code, redundant logic, excessive comments, and unrelated changes must be removed before submission.

## Project Overview

Nextcloud Notes for Android — a notes management app that syncs with a Nextcloud server. Kotlin is the language of the
project; the large amount of Java still present is legacy that is being migrated away from, not a style to follow.
Targets API 28+ (minSdk 28, targetSdk 36). Uses Nextcloud Single Sign-On (SSO) for authentication.

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

# Android lint (per variant, or the default variant)
./gradlew lintFdroidDebug
./gradlew lint

# Lint + unit tests across variants
./gradlew check

# JaCoCo coverage report (debug builds only)
./gradlew createFdroidDebugUnitTestCoverageReport
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

The bullets below are the summary; [`project-conventions`](.claude/skills/project-conventions/SKILL.md) and the other
[Reference Guides](#reference-guides) show what each one looks like in practice.

[//]: # (REUSE-IgnoreStart)
- Every new file is Kotlin, no file exceeds 300 lines, and code carries no comments — see [Hard Rules](#hard-rules).
- Line length: **120 characters**
- Standard Android Studio formatter with EditorConfig.
- Indentation: 4 spaces, UTF-8 encoding
- Do not use decorative section-divider comments of any kind (e.g. `// ── Title ───`, `// ------`, `// ======`).
- Every new file must end with exactly one empty trailing line (no more, no less).
- All new files must include an SPDX license header: ` SPDX-License-Identifier: GPL-3.0-or-later `
- Translations: only modify `values/strings.xml`; never the translated `values-*/strings.xml` files
- Create models, states in different files instead of doing it one single file.
- Do not use magic number.
- Apply fail fast principle instead of using nested if-else statements.
- Do not use multiple boolean flags to determine states instead use enums or sealed classes.
- When you must edit an existing Java class, use modern Java — Optionals, records, streams where they genuinely help.
  This applies to edits inside files that are already Java; it is never a reason to create a new Java file.
- Avoid hardcoded strings, colors, dimensions. Use resources.
- Run `./gradlew lintFdroidDebug` and `./gradlew testFdroidDebugUnitTest`, and fix every finding inside the files you
  changed.

[//]: # (REUSE-IgnoreEnd)

## Testing

- **Unit tests**: `app/src/test/` — JUnit 4, Mockito, Robolectric. Uses `includeAndroidResources = true`.
- **Instrumented tests**: `app/src/androidTest/` — Espresso, requires running device/emulator.
- Parallel test execution enabled (max forks = processors / 2).
- Code coverage via JaCoCo (enabled for debug builds).
