/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence

import com.nextcloud.android.sso.helper.Retrofit2Helper
import retrofit2.Response

class NoteSyncException(noteTitle: String, response: Response<*>) :
    Exception(
        "Could not sync \"$noteTitle\" (HTTP ${response.code()}): ${response.message()}",
        Retrofit2Helper.getException(response)
    ) {
    val reason = SyncFailureReason.fromStatusCode(response.code())
}
