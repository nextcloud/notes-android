/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence

import androidx.annotation.StringRes
import it.niedermann.owncloud.notes.R
import java.net.HttpURLConnection.HTTP_FORBIDDEN
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.net.HttpURLConnection.HTTP_NOT_FOUND
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.net.HttpURLConnection.HTTP_UNAVAILABLE

enum class SyncFailureReason(@StringRes val message: Int) {
    UNAUTHORIZED(R.string.error_sync_unauthorized),
    PERMISSION(R.string.error_sync_permission),
    NOTES_APP_UNAVAILABLE(R.string.error_sync_notes_app_unavailable),
    LOCKED(R.string.error_sync_locked),
    SERVER_ERROR(R.string.error_sync_server_error),
    MAINTENANCE(R.string.error_sync_maintenance),
    INSUFFICIENT_STORAGE(R.string.error_sync_insufficient_storage),
    SERVER_UNREACHABLE(R.string.error_sync_server_unreachable),
    TOKEN_MISMATCH(R.string.error_sync_token_mismatch),
    UNKNOWN(R.string.error_synchronization);

    companion object {
        private const val HTTP_LOCKED = 423
        private const val HTTP_INSUFFICIENT_STORAGE = 507
        private const val SSO_LOCAL_FAILURE = 900

        fun fromStatusCode(statusCode: Int): SyncFailureReason = when (statusCode) {
            HTTP_UNAUTHORIZED -> UNAUTHORIZED
            HTTP_FORBIDDEN -> PERMISSION
            HTTP_NOT_FOUND -> NOTES_APP_UNAVAILABLE
            HTTP_LOCKED -> LOCKED
            HTTP_INTERNAL_ERROR -> SERVER_ERROR
            HTTP_UNAVAILABLE -> MAINTENANCE
            HTTP_INSUFFICIENT_STORAGE -> INSUFFICIENT_STORAGE
            SSO_LOCAL_FAILURE -> SERVER_UNREACHABLE
            else -> UNKNOWN
        }
    }
}
