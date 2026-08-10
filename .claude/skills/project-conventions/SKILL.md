---
name: project-conventions
description: Nextcloud Notes house conventions for any file you add, rename, or edit — SPDX header form, the 300-line and one-top-level-type-per-file limits, magic numbers, string/color/dimen resources, Java-interop annotations, and the verification commands that actually exist in this repository. Use when creating or renaming a file, and before reporting any change as done.
---

<!--
 ~ SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 ~ SPDX-License-Identifier: GPL-3.0-or-later
-->

# Project Conventions (Nextcloud Notes)

Apply these to every file you write so the change passes review.

## SPDX Header (every new/renamed file)

The IDE keeps the old license block. Replace it with the current template. The year is the
year the Kotlin file is created. New contributions are `AGPL-3.0-or-later`; keep
`OR GPL-2.0-only` only if the original file carried it.

```kotlin
/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: <YEAR> Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
```

If the developer wants personal attribution, the form
`SPDX-FileCopyrightText: <YEAR> <Name> <email>` is also used — match what the developer
asks for; default to the "Nextcloud GmbH and Nextcloud contributors" line.

## Structural Rules

- **≤300 lines per file**, and a file already at or above 300 lines must not grow. If
  decomposition pushes past it, split responsibilities into separate files/collaborators and
  tell the developer. Do not reach for `@Suppress("LargeClass", "TooManyFunctions")` — those
  are detekt rule names and this repository has no detekt, so the annotation suppresses
  nothing and merely hides the problem from the reader.
- **≤120 columns per line.**
- **One top-level type per file.** Extract models, states, sealed classes, and listener
  interfaces into their own files rather than nesting many types in one.
- **Exactly one trailing newline** at end of file.

## No Magic Numbers / Hardcoded Resources

- Extract literals into named `const val` in a `companion object`
  (`MIN_SHOW_ALL_VISIBLE_ITEM_COUNT = 3`, `INTERNAL_LINK_PATH_PRETTY = "/f/"`).
- Strings, colors, dimens come from resources (`R.string.*`, `R.dimen.*`), never inline.
- Only `app/src/main/res/values/strings.xml` may be edited for strings; never touch
  `values-*` translation folders.

## Comments & Naming

- No decorative divider comments (`// ==== ====`, `// ---- Title ----`). `// region` /
  `// endregion` for IDE folding is allowed and should match the file's existing style.
- Prefer self-explanatory names over per-function KDoc. Preserve genuinely informative
  Javadoc as KDoc (invariant 4); drop noise.
- Do not use multiple boolean flags to model state — use an `enum`/sealed class.

## Modern Java Interop

When the file still has Java callers, keep the Java-facing API clean:
`@JvmStatic` for factory/companion functions, `@JvmField` for exposed constants,
`@JvmOverloads` for defaulted params, `@Throws` for checked exceptions.

## Git & Commits (developer-driven)

- Preserve history: the rename `git mv Foo.java Foo.kt` should be a **separate commit**
  from the content change so `git blame` follows through.
- Conventional Commits (`refactor(sharing): convert FileDetailSharingFragment to Kotlin`).
- Every AI-assisted commit needs an `Assisted-by: <agent>:<model>` trailer.
- Only the human contributor adds `Signed-off-by` (DCO). You must never add it, and never
  open PRs/issues autonomously (AI policy).

## Quality Gate

These are the tasks this project actually has. `gplay` is not a flavor here — the flavors
are `fdroid`, `play`, `dev`, and `qa`.

```bash
./gradlew lintFdroidDebug
./gradlew testFdroidDebugUnitTest
./gradlew createFdroidDebugUnitTestCoverageReport   # JaCoCo, debug builds only
./gradlew check                                     # lint + unit tests for all variants
```

Fix every finding in the files you changed before declaring done. Style rules that no task
enforces — line length, trailing newline, import order — are on you to check by reading the
diff.
