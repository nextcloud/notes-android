/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main

import android.accounts.NetworkErrorException
import com.nextcloud.android.sso.exceptions.NextcloudApiNotRespondingException
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException
import com.nextcloud.android.sso.exceptions.NextcloudNetworkException
import com.nextcloud.android.sso.exceptions.TokenMismatchException
import it.niedermann.owncloud.notes.persistence.NoteSyncException
import it.niedermann.owncloud.notes.persistence.SyncFailureReason

class SyncErrorMessage(private val exceptions: Collection<Throwable>) {
    fun reasonOf(): SyncFailureReason =
        exceptions.map { it.syncFailureReason() }.distinct().singleOrNull() ?: SyncFailureReason.UNKNOWN

    private fun Throwable.syncFailureReason(): SyncFailureReason =
        generateSequence(this) { it.cause }.firstNotNullOfOrNull { it.directSyncFailureReason() }
            ?: SyncFailureReason.UNKNOWN

    private fun Throwable.directSyncFailureReason(): SyncFailureReason? = when (this) {
        is NoteSyncException -> reason
        is NextcloudHttpRequestFailedException -> SyncFailureReason.fromStatusCode(statusCode)
        is NextcloudNetworkException, is NextcloudApiNotRespondingException -> SyncFailureReason.SERVER_UNREACHABLE
        is TokenMismatchException -> SyncFailureReason.TOKEN_MISMATCH
        is NextcloudFilesAppAccountNotFoundException -> SyncFailureReason.ACCOUNT_REMOVED
        is NetworkErrorException -> SyncFailureReason.NO_NETWORK
        else -> null
    }
}
