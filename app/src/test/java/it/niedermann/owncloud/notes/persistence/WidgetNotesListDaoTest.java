/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData;
import it.niedermann.owncloud.notes.shared.model.Capabilities;

@RunWith(RobolectricTestRunner.class)
public class WidgetNotesListDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @NonNull
    private NotesDatabase db;

    private long accountId;

    @Before
    public void setupDB() {
        db = Room
                .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), NotesDatabase.class)
                .allowMainThreadQueries()
                .build();
        accountId = db.getAccountDao().insert(new Account("https://example.com", "user", "user@example.com", null, new Capabilities()));
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void insertThenReadWidgetData() {
        final var data = widgetData(1, NotesListWidgetData.MODE_DISPLAY_ALL);
        db.getWidgetNotesListDao().createOrUpdateNoteListWidgetData(data);

        final var loaded = db.getWidgetNotesListDao().getNoteListWidgetData(1);
        assertNotNull(loaded);
        assertEquals(NotesListWidgetData.MODE_DISPLAY_ALL, loaded.getMode());
    }

    /**
     * Reconfiguring an interactive widget saves again for the same appWidgetId (primary key).
     * The DAO must upsert; a plain insert would abort on the primary-key conflict.
     */
    @Test
    public void reconfigureUpdatesExistingWidgetData() {
        db.getWidgetNotesListDao().createOrUpdateNoteListWidgetData(widgetData(1, NotesListWidgetData.MODE_DISPLAY_ALL));

        db.getWidgetNotesListDao().createOrUpdateNoteListWidgetData(widgetData(1, NotesListWidgetData.MODE_DISPLAY_STARRED));

        final var loaded = db.getWidgetNotesListDao().getNoteListWidgetData(1);
        assertNotNull(loaded);
        assertEquals(NotesListWidgetData.MODE_DISPLAY_STARRED, loaded.getMode());
    }

    @NonNull
    private NotesListWidgetData widgetData(int appWidgetId, int mode) {
        final var data = new NotesListWidgetData();
        data.setId(appWidgetId);
        data.setAccountId(accountId);
        data.setMode(mode);
        data.setCategory(null);
        return data;
    }
}
