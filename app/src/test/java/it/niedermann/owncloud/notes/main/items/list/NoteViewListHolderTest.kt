/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main.items.list

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import androidx.test.core.app.ApplicationProvider
import com.nextcloud.android.common.core.utils.DateFormatter
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.databinding.ItemNotesListNoteItemWithExcerptBinding
import it.niedermann.owncloud.notes.shared.model.NoteClickListener
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockConstruction
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NoteViewListHolderTest {

    /**
     * Rows that are not recyclable stay in the list after their note is removed.
     */
    @Test
    fun isRecyclable() {
        val context = ContextThemeWrapper(ApplicationProvider.getApplicationContext<Context>(), R.style.AppTheme)
        val binding = ItemNotesListNoteItemWithExcerptBinding.inflate(LayoutInflater.from(context))
        mockConstruction(DateFormatter::class.java).use {
            val holder = NoteViewListHolder(binding, mock(NoteClickListener::class.java))
            assertTrue(holder.isRecyclable)
        }
    }
}
