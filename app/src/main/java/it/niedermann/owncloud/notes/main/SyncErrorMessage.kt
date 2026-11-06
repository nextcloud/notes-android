/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main

import android.content.Context
import com.nextcloud.android.sso.exceptions.NextcloudApiNotRespondingException
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException
import com.nextcloud.android.sso.exceptions.NextcloudNetworkException
import com.nextcloud.android.sso.exceptions.TokenMismatchException
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.persistence.NoteSyncException
import it.niedermann.owncloud.notes.persistence.SyncFailureReason

object SyncErrorMessage {
    @JvmStatic
    fun of(context: Context, exceptions: Collection<Throwable>): String {
        val reasons = exceptions.map { it.syncFailureReason() }.distinct()
        val reason = reasons.singleOrNull() ?: SyncFailureReason.UNKNOWN
        if (reason != SyncFailureReason.LOCKED) {
            return context.getString(reason.message)
        }
        return lockedMessage(context, exceptions.mapNotNull { it.lockedNoteTitle() }.distinct())
    }

    @JvmStatic
    fun of(context: Context, exception: Throwable): String = of(context, listOf(exception))

    private fun lockedMessage(context: Context, lockedNoteTitles: List<String>): String = when (lockedNoteTitles.size) {
        0 -> context.getString(SyncFailureReason.LOCKED.message)
        1 -> context.getString(R.string.error_sync_note_locked, lockedNoteTitles.single())
        else -> context.resources.getQuantityString(
            R.plurals.error_sync_notes_locked,
            lockedNoteTitles.size,
            lockedNoteTitles.size,
        )
    }

    private fun Throwable.lockedNoteTitle(): String? = causeChain()
        .filterIsInstance<NoteSyncException>()
        .firstOrNull { it.reason == SyncFailureReason.LOCKED }
        ?.noteTitle

    private fun Throwable.syncFailureReason(): SyncFailureReason =
        causeChain().firstNotNullOfOrNull { it.directSyncFailureReason() } ?: SyncFailureReason.UNKNOWN

    private fun Throwable.causeChain(): Sequence<Throwable> = generateSequence(this) { it.cause }

    private fun Throwable.directSyncFailureReason(): SyncFailureReason? = when (this) {
        is NoteSyncException -> reason
        is NextcloudHttpRequestFailedException -> SyncFailureReason.fromStatusCode(statusCode)
        is NextcloudNetworkException, is NextcloudApiNotRespondingException -> SyncFailureReason.SERVER_UNREACHABLE
        is TokenMismatchException -> SyncFailureReason.TOKEN_MISMATCH
        else -> null
    }
}
