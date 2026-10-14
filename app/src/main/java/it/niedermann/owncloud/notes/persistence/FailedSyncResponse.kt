/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence

import it.niedermann.owncloud.notes.exception.NoteLockedException
import it.niedermann.owncloud.notes.persistence.entity.Note
import retrofit2.Response

object FailedSyncResponse {
    private const val HTTP_LOCKED = 423

    @JvmStatic
    fun toException(note: Note, response: Response<*>): Exception = if (response.code() == HTTP_LOCKED) {
        NoteLockedException(note.title)
    } else {
        Exception(response.message())
    }
}
