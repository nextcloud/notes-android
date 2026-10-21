/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main

import androidx.annotation.StringRes
import com.nextcloud.android.sso.exceptions.NextcloudApiNotRespondingException
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException
import com.nextcloud.android.sso.exceptions.NextcloudNetworkException
import com.nextcloud.android.sso.exceptions.TokenMismatchException
import it.niedermann.owncloud.notes.persistence.NoteSyncException
import it.niedermann.owncloud.notes.persistence.SyncFailureReason

object SyncErrorMessage {
    @JvmStatic
    @StringRes
    fun of(exceptions: Collection<Throwable>): Int =
        (exceptions.map { it.syncFailureReason() }.distinct().singleOrNull() ?: SyncFailureReason.UNKNOWN).message

    @JvmStatic
    @StringRes
    fun of(exception: Throwable): Int = exception.syncFailureReason().message

    private fun Throwable.syncFailureReason(): SyncFailureReason =
        generateSequence(this) { it.cause }.firstNotNullOfOrNull { it.directSyncFailureReason() }
            ?: SyncFailureReason.UNKNOWN

    private fun Throwable.directSyncFailureReason(): SyncFailureReason? = when (this) {
        is NoteSyncException -> reason
        is NextcloudHttpRequestFailedException -> SyncFailureReason.fromStatusCode(statusCode)
        is NextcloudNetworkException, is NextcloudApiNotRespondingException -> SyncFailureReason.SERVER_UNREACHABLE
        is TokenMismatchException -> SyncFailureReason.TOKEN_MISMATCH
        else -> null
    }
}
