/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main

import androidx.annotation.StringRes
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.exception.NoteLockedException

object SyncErrorMessage {
    @JvmStatic
    @StringRes
    fun of(exceptions: Collection<Throwable>): Int = if (exceptions.all { it is NoteLockedException }) {
        R.string.error_note_locked
    } else {
        R.string.error_synchronization
    }
}
