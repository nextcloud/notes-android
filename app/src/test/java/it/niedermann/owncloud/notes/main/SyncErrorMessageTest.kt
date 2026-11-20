/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.persistence.NoteSyncException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SyncErrorMessageTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun singleLockedNoteIsNamed() {
        val message = SyncErrorMessage.of(context, listOf(noteSyncException(GROCERIES, HTTP_LOCKED)))

        assertEquals(context.getString(R.string.error_sync_note_locked, GROCERIES), message)
    }

    @Test
    fun severalLockedNotesAreCounted() {
        val exceptions = listOf(
            noteSyncException(GROCERIES, HTTP_LOCKED),
            noteSyncException(INVOICES, HTTP_LOCKED),
        )

        val message = SyncErrorMessage.of(context, exceptions)

        assertEquals(context.resources.getQuantityString(R.plurals.error_sync_notes_locked, 2, 2), message)
    }

    @Test
    fun lockedNoteIsFoundInCauseChain() {
        val wrapped = RuntimeException(IllegalStateException(noteSyncException(GROCERIES, HTTP_LOCKED)))

        val message = SyncErrorMessage.of(context, wrapped)

        assertEquals(context.getString(R.string.error_sync_note_locked, GROCERIES), message)
    }

    @Test
    fun lockedTogetherWithOtherFailuresFallsBackToGenericMessage() {
        val exceptions = listOf(
            noteSyncException(GROCERIES, HTTP_LOCKED),
            noteSyncException(INVOICES, HTTP_INSUFFICIENT_STORAGE),
        )

        val message = SyncErrorMessage.of(context, exceptions)

        assertEquals(context.getString(R.string.error_synchronization), message)
    }

    @Test
    fun otherFailureReasonsKeepTheirMessage() {
        val message = SyncErrorMessage.of(context, noteSyncException(GROCERIES, HTTP_INSUFFICIENT_STORAGE))

        assertEquals(context.getString(R.string.error_sync_insufficient_storage), message)
    }

    private fun noteSyncException(title: String, statusCode: Int) =
        NoteSyncException(title, Response.error<Any>(statusCode, "".toResponseBody()))

    companion object {
        private const val GROCERIES = "Groceries"
        private const val INVOICES = "Invoices"
        private const val HTTP_LOCKED = 423
        private const val HTTP_INSUFFICIENT_STORAGE = 507
    }
}
