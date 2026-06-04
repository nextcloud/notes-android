/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import it.niedermann.owncloud.notes.persistence.entity.Account
import it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData
import it.niedermann.owncloud.notes.shared.model.Capabilities
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WidgetNotesListDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: NotesDatabase
    private var accountId: Long = 0

    @Before
    fun setupDB() {
        db = Room
            .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), NotesDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        val account = Account(ACCOUNT_URL, ACCOUNT_USERNAME, ACCOUNT_NAME, null, Capabilities())
        accountId = db.accountDao.insert(account)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertThenReadWidgetData() {
        db.widgetNotesListDao.createOrUpdateNoteListWidgetData(
            widgetData(NotesListWidgetData.MODE_DISPLAY_ALL)
        )

        val loaded = db.widgetNotesListDao.getNoteListWidgetData(WIDGET_ID)
        assertNotNull(loaded)
        assertEquals(NotesListWidgetData.MODE_DISPLAY_ALL, loaded.mode)
    }

    /**
     * Reconfiguring an interactive widget saves again for the same appWidgetId (primary key).
     * The DAO must upsert; a plain insert would abort on the primary-key conflict.
     */
    @Test
    fun reconfigureUpdatesExistingWidgetData() {
        db.widgetNotesListDao.createOrUpdateNoteListWidgetData(
            widgetData(NotesListWidgetData.MODE_DISPLAY_ALL)
        )
        db.widgetNotesListDao.createOrUpdateNoteListWidgetData(
            widgetData(NotesListWidgetData.MODE_DISPLAY_STARRED)
        )

        val loaded = db.widgetNotesListDao.getNoteListWidgetData(WIDGET_ID)
        assertNotNull(loaded)
        assertEquals(NotesListWidgetData.MODE_DISPLAY_STARRED, loaded.mode)
    }

    private fun widgetData(mode: Int) = NotesListWidgetData().apply {
        id = WIDGET_ID
        accountId = this@WidgetNotesListDaoTest.accountId
        this.mode = mode
        category = null
    }

    companion object {
        private const val WIDGET_ID = 1
        private const val ACCOUNT_URL = "https://example.com"
        private const val ACCOUNT_USERNAME = "user"
        private const val ACCOUNT_NAME = "user@example.com"
    }
}
