/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence;

import android.database.sqlite.SQLiteConstraintException;

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

import java.util.Calendar;
import java.util.List;

import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.CategoryWithNotesCount;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.model.Capabilities;
import it.niedermann.owncloud.notes.shared.model.DBStatus;

import static it.niedermann.owncloud.notes.shared.model.DBStatus.LOCAL_DELETED;
import static it.niedermann.owncloud.notes.shared.model.DBStatus.LOCAL_EDITED;
import static it.niedermann.owncloud.notes.shared.model.DBStatus.VOID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class NotesDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @NonNull
    private NotesDatabase db;
    private Account account;

    @Before
    public void setupDB() {
        db = Room
                .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), NotesDatabase.class)
                .allowMainThreadQueries()
                .build();
        db.getAccountDao().insert(new Account("https://äöüß.example.com", "彼得", "彼得@äöüß.example.com", null, new Capabilities()));
        account = db.getAccountDao().getAccountByName("彼得@äöüß.example.com");
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void deleteNoteById() throws InterruptedException {
        db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "T", "C", "", false, "1", LOCAL_DELETED, account.getId(), "", 0));
        db.getNoteDao().deleteByNoteId(1, LOCAL_DELETED);
        assertNull(db.getNoteDao().getNoteById(1));
        assertNull(NotesTestingUtil.getOrAwaitValue(db.getNoteDao().getNoteById$(1)));

        db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "T", "C", "", false, "1", LOCAL_DELETED, account.getId(), "", 0));
        db.getNoteDao().deleteByNoteId(1, VOID);
        assertEquals(1, db.getNoteDao().getNoteById(1).getId());
        assertEquals(1, NotesTestingUtil.getOrAwaitValue(db.getNoteDao().getNoteById$(1)).getId());
    }

    @Test
    public void updateScrollY() {
        db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "T", "C", "", false, "1", LOCAL_DELETED, account.getId(), "", 0));
        db.getNoteDao().updateScrollY(1, 128);
        assertEquals(128, db.getNoteDao().getNoteById(1).getScrollY());
    }

    @Test
    public void updateStatus() {
        db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "T", "C", "", false, "1", LOCAL_DELETED, account.getId(), "", 0));
        db.getNoteDao().updateStatus(1, LOCAL_EDITED);
        assertEquals(LOCAL_EDITED, db.getNoteDao().getNoteById(1).getStatus());
    }

    @Test(expected = SQLiteConstraintException.class)
    public void updateStatus_NullConstraint() {
        db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "T", "C", "", false, "1", LOCAL_DELETED, account.getId(), "", 0));
        db.getNoteDao().updateStatus(1, null);
    }

    @Test
    public void updateCategory() {
        db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "T", "C", "", false, "1", LOCAL_DELETED, account.getId(), "", 0));
        db.getNoteDao().updateCategory(1, "日记");
        assertEquals("日记", db.getNoteDao().getNoteById(1).getCategory());
    }

    @Test(expected = SQLiteConstraintException.class)
    public void updateCategory_NullConstraint() {
        db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "T", "C", "", false, "1", LOCAL_DELETED, account.getId(), "", 0));
        db.getNoteDao().updateCategory(1, null);
    }

    @Test
    public void getRemoteIds() {
        final var secondAccount = setupSecondAccount();

        db.getNoteDao().addNote(new Note(1, 4711L, Calendar.getInstance(), "T", "C", "", false, "1", VOID, account.getId(), "", 0));
        db.getNoteDao().addNote(new Note(2, 1234L, Calendar.getInstance(), "T", "C", "", false, "1", LOCAL_EDITED, account.getId(), "", 0));
        db.getNoteDao().addNote(new Note(3, 1234L, Calendar.getInstance(), "T", "C", "", false, "1", LOCAL_EDITED, secondAccount.getId(), "", 0));
        db.getNoteDao().addNote(new Note(4, 6969L, Calendar.getInstance(), "T", "C", "", false, "1", LOCAL_DELETED, account.getId(), "", 0));

        final List<Long> remoteIdsOfAccount = db.getNoteDao().getRemoteIds(account.getId());
        assertTrue(remoteIdsOfAccount.stream().anyMatch(id -> 4711 == id));
        assertTrue(remoteIdsOfAccount.stream().anyMatch(id -> 1234 == id));
        assertEquals("Remote IDs can only occur a single time, like in a set.", 1, remoteIdsOfAccount.stream().filter(id -> 1234 == id).count());
        assertFalse("Remote IDs from notes of other accounts must not be returned.", remoteIdsOfAccount.stream().anyMatch(id -> 6969 == id));
    }

    @Test
    public void getRemoteIdAndId() {
        db.getNoteDao().addNote(new Note(815, 4711L, Calendar.getInstance(), "T", "C", "", false, "1", VOID, account.getId(), "", 0));
        db.getNoteDao().addNote(new Note(666, 1234L, Calendar.getInstance(), "T", "C", "", false, "1", LOCAL_EDITED, account.getId(), "", 0));
        db.getNoteDao().addNote(new Note(987, 6969L, Calendar.getInstance(), "T", "C", "", false, "1", LOCAL_DELETED, account.getId(), "", 0));

        final var pair = db.getNoteDao().getRemoteIdAndId(account.getId());
        assertEquals(2, pair.size());
        assertTrue(pair.stream().anyMatch(note -> 815 == note.getId() && Long.valueOf(4711).equals(note.getRemoteId())));
        assertTrue(pair.stream().anyMatch(note -> 666 == note.getId() && Long.valueOf(1234).equals(note.getRemoteId())));
        assertFalse("Result must not contain deleted note", pair.stream().anyMatch(note -> 987 == note.getId()));
        assertFalse("Result must not contain deleted note", pair.stream().anyMatch(note -> Long.valueOf(6969).equals(note.getRemoteId())));
    }

    @Test
    public void getLocalIdByRemoteId() {
        db.getNoteDao().addNote(new Note(815, 4711L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", VOID, account.getId(), "", 0));
        db.getNoteDao().addNote(new Note(666, 1234L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", LOCAL_EDITED, account.getId(), "", 0));
        db.getNoteDao().addNote(new Note(987, 6969L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", LOCAL_DELETED, account.getId(), "", 0));

        assertEquals(Long.valueOf(815), db.getNoteDao().getLocalIdByRemoteId(account.getId(), 4711));
        assertEquals(Long.valueOf(666), db.getNoteDao().getLocalIdByRemoteId(account.getId(), 1234));
        assertNull(db.getNoteDao().getLocalIdByRemoteId(account.getId(), 6969));
    }

    @Test
    public void getFavoritesCount() throws InterruptedException {
        final Account secondAccount = setupSecondAccountAndTestNotes();

        assertEquals(Integer.valueOf(1), db.getNoteDao().countFavorites(account.getId()));
        assertEquals(Integer.valueOf(1), db.getNoteDao().countFavorites(secondAccount.getId()));

        assertEquals(Integer.valueOf(1), NotesTestingUtil.getOrAwaitValue(db.getNoteDao().countFavorites$(account.getId())));
        assertEquals(Integer.valueOf(1), NotesTestingUtil.getOrAwaitValue(db.getNoteDao().countFavorites$(secondAccount.getId())));
    }

    @Test
    public void count() throws InterruptedException {
        final var secondAccount = setupSecondAccountAndTestNotes();

        assertEquals(Integer.valueOf(7), db.getNoteDao().count(account.getId()));
        assertEquals(Integer.valueOf(5), db.getNoteDao().count(secondAccount.getId()));

        assertEquals(Integer.valueOf(7), NotesTestingUtil.getOrAwaitValue(db.getNoteDao().count$(account.getId())));
        assertEquals(Integer.valueOf(5), NotesTestingUtil.getOrAwaitValue(db.getNoteDao().count$(secondAccount.getId())));
    }

    @Test
    public void getLocalModifiedNotes() {
        final var secondAccount = setupSecondAccountAndTestNotes();

        final var accountNotes = db.getNoteDao().getLocalModifiedNotes(account.getId());
        assertEquals(6, accountNotes.size());
        for (Note note : accountNotes) {
            assertNotEquals(VOID, note.getStatus());
        }

        final var secondAccountNotes = db.getNoteDao().getLocalModifiedNotes(secondAccount.getId());
        assertEquals(7, secondAccountNotes.size());
        for (Note note : secondAccountNotes) {
            assertNotEquals(VOID, note.getStatus());
        }
    }

    @Test
    public void toggleFavorite() {
        final var note = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", LOCAL_DELETED, account.getId(), "", 0);
        db.getNoteDao().addNote(note);
        db.getNoteDao().toggleFavorite(note.getId());
        assertTrue(db.getNoteDao().getNoteById(note.getId()).getFavorite());
        db.getNoteDao().toggleFavorite(note.getId());
        assertFalse(db.getNoteDao().getNoteById(note.getId()).getFavorite());
        db.getNoteDao().toggleFavorite(note.getId());
        assertTrue(db.getNoteDao().getNoteById(note.getId()).getFavorite());
    }

    @Test
    public void updateRemoteId() {
        final var note = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", LOCAL_DELETED, account.getId(), "", 0);
        db.getNoteDao().addNote(note);
        db.getNoteDao().updateRemoteId(1, 5L);
        assertEquals(Long.valueOf(5), db.getNoteDao().getNoteById(1).getRemoteId());
    }

    @Test
    public void updateIfNotModifiedLocallyDuringSync_NotModified() {
        final var localNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, VOID, account.getId(), "", 0);
        final var targetNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, VOID, account.getId(), "", 0);

        db.getNoteDao().addNote(localNote);

        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyDuringSync(localNote.getId(), targetNote.getModified().getTimeInMillis(), targetNote.getTitle(), targetNote.getFavorite(), targetNote.getETag(), targetNote.getContent(), targetNote.getExcerpt(), localNote.getContent(), localNote.getCategory(), localNote.getFavorite()));
    }

    @Test
    public void updateIfNotModifiedLocallyDuringSync_ModifiedContent() {
        final var localNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, VOID, account.getId(), "", 0);
        final var targetNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, VOID, account.getId(), "", 0);

        db.getNoteDao().addNote(localNote);

        localNote.setContent("My-Modified-Content");

        assertEquals(0, db.getNoteDao().updateIfNotModifiedLocallyDuringSync(localNote.getId(), targetNote.getModified().getTimeInMillis(), targetNote.getTitle(), targetNote.getFavorite(), targetNote.getETag(), targetNote.getContent(), targetNote.getExcerpt(), localNote.getContent(), localNote.getCategory(), localNote.getFavorite()));
    }

    @Test
    public void updateIfNotModifiedLocallyDuringSync_ModifiedFavorite() {
        final var localNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, VOID, account.getId(), "", 0);
        final var targetNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, VOID, account.getId(), "", 0);

        db.getNoteDao().addNote(localNote);

        localNote.setFavorite(true);

        assertEquals(0, db.getNoteDao().updateIfNotModifiedLocallyDuringSync(localNote.getId(), targetNote.getModified().getTimeInMillis(), targetNote.getTitle(), targetNote.getFavorite(), targetNote.getETag(), targetNote.getContent(), targetNote.getExcerpt(), localNote.getContent(), localNote.getCategory(), localNote.getFavorite()));
    }

    @Test
    public void updateIfNotModifiedLocallyDuringSync_ModifiedCategory() {
        final var localNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, VOID, account.getId(), "", 0);
        final var targetNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, VOID, account.getId(), "", 0);

        db.getNoteDao().addNote(localNote);

        localNote.setCategory("Modified-Category");

        assertEquals(0, db.getNoteDao().updateIfNotModifiedLocallyDuringSync(localNote.getId(), targetNote.getModified().getTimeInMillis(), targetNote.getTitle(), targetNote.getFavorite(), targetNote.getETag(), targetNote.getContent(), targetNote.getExcerpt(), localNote.getContent(), localNote.getCategory(), localNote.getFavorite()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_Nothing() {
        final var localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", VOID, account.getId(), "", 0)));
        assertEquals(0, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), localNote.getETag(), localNote.getContent(), localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_Nothing_ETagWasAndIsNull() {
        final var localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, VOID, account.getId(), "", 0)));
        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), null, localNote.getContent(), localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_Nothing_ETagWasNullButChanged() {
        final var localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, VOID, account.getId(), "", 0)));
        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), "1", localNote.getContent(), localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_Modified() {
        final var localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", VOID, account.getId(), "", 0)));
        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis() + 1000, localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), localNote.getETag(), localNote.getContent(), localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_Title() {
        final var localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", VOID, account.getId(), "", 0)));
        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle() + " ", localNote.getFavorite(), localNote.getCategory(), localNote.getETag(), localNote.getContent(), localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_Favorite() {
        final var localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", VOID, account.getId(), "", 0)));
        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), !localNote.getFavorite(), localNote.getCategory(), localNote.getETag(), localNote.getContent(), localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_Category() {
        final var localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", VOID, account.getId(), "", 0)));
        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory() + " ", localNote.getETag(), localNote.getContent(), localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_ETag() {
        final var localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", VOID, account.getId(), "", 0)));
        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), localNote.getETag() + " ", localNote.getContent(), localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_Content() {
        final var localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", VOID, account.getId(), "", 0)));
        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), localNote.getETag(), localNote.getContent() + " ", localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_Excerpt() {
        final var localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", VOID, account.getId(), "", 0)));
        assertEquals("Excerpt is a local property, and therefore should not prevent updating if different", 0, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), localNote.getETag(), localNote.getContent(), localNote.getExcerpt() + " "));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_ContentChangedButWasLocalEdited() {
        final var localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", LOCAL_EDITED, account.getId(), "", 0)));
        assertEquals(0, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), localNote.getETag(), localNote.getContent() + " ", localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_ContentChangedButWasLocalDeleted() {
        final var localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", LOCAL_DELETED, account.getId(), "", 0)));
        assertEquals(0, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), localNote.getETag(), localNote.getContent() + " ", localNote.getExcerpt()));
    }

    @Test
    public void getCategoriesLiveData() throws InterruptedException {
        final var secondAccount = setupSecondAccountAndTestNotes();

        final var accountCategories = NotesTestingUtil.getOrAwaitValue(db.getNoteDao().getCategories$(account.getId()));
        assertEquals(4, accountCategories.size());
        for (final var category : accountCategories) {
            assertEquals(account.getId(), category.getAccountId());
        }

        assertTrue(accountCategories.stream().anyMatch(cat -> "Movies".equals(cat.getCategory()) && Integer.valueOf(3).equals(cat.getTotalNotes())));
        assertTrue(accountCategories.stream().anyMatch(cat -> "Music".equals(cat.getCategory()) && Integer.valueOf(2).equals(cat.getTotalNotes())));
        assertTrue(accountCategories.stream().anyMatch(cat -> "ToDo".equals(cat.getCategory()) && Integer.valueOf(1).equals(cat.getTotalNotes())));
        assertTrue(accountCategories.stream().anyMatch(cat -> "日记".equals(cat.getCategory()) && Integer.valueOf(1).equals(cat.getTotalNotes())));

        final var secondAccountCategories = NotesTestingUtil.getOrAwaitValue(db.getNoteDao().getCategories$(secondAccount.getId()));
        assertEquals(2, secondAccountCategories.size());
        for (final var category : secondAccountCategories) {
            assertEquals(secondAccount.getId(), category.getAccountId());
        }
        assertTrue(secondAccountCategories.stream().anyMatch(cat -> "Movies".equals(cat.getCategory()) && Integer.valueOf(4).equals(cat.getTotalNotes())));
        assertTrue(secondAccountCategories.stream().anyMatch(cat -> "Music".equals(cat.getCategory()) && Integer.valueOf(1).equals(cat.getTotalNotes())));
        assertFalse(secondAccountCategories.stream().anyMatch(cat -> "ToDo".equals(cat.getCategory())));
        assertFalse(secondAccountCategories.stream().anyMatch(cat -> "日记".equals(cat.getCategory())));
    }

    @Test
    public void searchCategories() throws InterruptedException {
        final var secondAccount = setupSecondAccountAndTestNotes();

        assertEquals(2, NotesTestingUtil.getOrAwaitValue(db.getNoteDao().searchCategories$(account.getId(), "M%")).size());
        assertEquals(1, NotesTestingUtil.getOrAwaitValue(db.getNoteDao().searchCategories$(account.getId(), "Mo%")).size());
        assertEquals(1, NotesTestingUtil.getOrAwaitValue(db.getNoteDao().searchCategories$(account.getId(), "MO%")).size());
        assertEquals(1, NotesTestingUtil.getOrAwaitValue(db.getNoteDao().searchCategories$(account.getId(), "movie%")).size());
        assertEquals(1, NotesTestingUtil.getOrAwaitValue(db.getNoteDao().searchCategories$(account.getId(), "T%")).size());
        assertEquals(1, NotesTestingUtil.getOrAwaitValue(db.getNoteDao().searchCategories$(account.getId(), "日记")).size());
        assertEquals(2, NotesTestingUtil.getOrAwaitValue(db.getNoteDao().searchCategories$(secondAccount.getId(), "M%")).size());
        assertEquals(0, NotesTestingUtil.getOrAwaitValue(db.getNoteDao().searchCategories$(secondAccount.getId(), "T%")).size());
    }

    @Test
    public void searchRecentByModified() {
        final var secondAccount = setupSecondAccountAndTestNotes();
        final var result = db.getNoteDao().searchRecentByModified(secondAccount.getId(), "T");
        assertEquals(5, result.size());
        for (final var note : result) {
            assertNotEquals(DBStatus.LOCAL_DELETED, note.getStatus());
            assertEquals(secondAccount.getId(), note.getAccountId());
            assertTrue(note.getTitle().toLowerCase().contains("t") || note.getTitle().toLowerCase().contains("t"));
            assertTrue("should be sorted by favorite", isSortedByFavorite(result));
        }
    }

    private static boolean isSortedByFavorite(List<Note> notes) {
        for (int i = 0; i < notes.size() - 1; ++i) {
            if (Boolean.compare(notes.get(i).getFavorite(), notes.get(i + 1).getFavorite()) < 0)
                return false;
        }
        return true;
    }

    private Account setupSecondAccount() {
        db.getAccountDao().insert(new Account("https://example.org", "test", "test@example.org", null, new Capabilities()));
        return db.getAccountDao().getAccountByName("test@example.org");
    }

    private Account setupSecondAccountAndTestNotes() {
        final var secondAccount = setupSecondAccount();

        long uniqueId = 1;
        final var notes = new Note[]{
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Movies", false, null, VOID, account.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Movies", false, null, LOCAL_EDITED, account.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Movies", false, null, LOCAL_EDITED, account.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Music", false, null, VOID, account.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Music", false, null, LOCAL_EDITED, account.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Music", true, null, LOCAL_DELETED, account.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "ToDo", true, null, VOID, account.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "ToDo", true, null, LOCAL_DELETED, account.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "ToDo", true, null, LOCAL_DELETED, account.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "美好的一天", " 兄弟，这真是美好的一天。", "日记", false, null, VOID, account.getId(), "", 0),

                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Movies", false, null, VOID, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Movies", false, null, LOCAL_EDITED, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "t", "C", "Movies", false, null, LOCAL_EDITED, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Movies", false, null, LOCAL_EDITED, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Music", true, null, VOID, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "t", "C", "Music", true, null, LOCAL_DELETED, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "ToDo", true, null, LOCAL_DELETED, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "ToDo", true, null, LOCAL_DELETED, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId, Calendar.getInstance(), "T", "C", "ToDo", true, null, LOCAL_DELETED, secondAccount.getId(), "", 0)
        };
        for (final var note : notes) {
            db.getNoteDao().addNote(note);
        }
        return secondAccount;
    }
}
