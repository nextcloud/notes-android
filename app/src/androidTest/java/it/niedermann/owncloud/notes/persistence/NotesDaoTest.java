package it.niedermann.owncloud.notes.persistence;

import android.database.sqlite.SQLiteConstraintException;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.List;

import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.CategoryWithNotesCount;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.persistence.entity.NoteIdPair;
import it.niedermann.owncloud.notes.shared.model.Capabilities;
import kotlin.DeprecationLevel;

import static it.niedermann.owncloud.notes.persistence.NotesDatabaseTestUtil.getOrAwaitValue;
import static it.niedermann.owncloud.notes.shared.model.DBStatus.LOCAL_DELETED;
import static it.niedermann.owncloud.notes.shared.model.DBStatus.LOCAL_EDITED;
import static it.niedermann.owncloud.notes.shared.model.DBStatus.VOID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ConstantConditions")
@RunWith(AndroidJUnit4.class)
public class NotesDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @NonNull
    private NotesDatabase db;
    private Account account;

    @Before
    public void setupDB() throws NextcloudHttpRequestFailedException {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), NotesDatabase.class).build();
        db.addAccount("https://äöüß.example.com", "彼得", "彼得@äöüß.example.com", new Capabilities("", null));
        account = db.getAccountDao().getLocalAccountByAccountName("彼得@äöüß.example.com");
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void deleteNotebyId() {
        db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "T", "C", "", false, "1", LOCAL_DELETED, account.getId(), "", 0));
        db.getNoteDao().deleteByNoteId(1, LOCAL_DELETED);
        assertNull(db.getNoteDao().getNoteById(1));

        db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "T", "C", "", false, "1", LOCAL_DELETED, account.getId(), "", 0));
        db.getNoteDao().deleteByNoteId(1, VOID);
        assertEquals(1, db.getNoteDao().getNoteById(1).getId());
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
    public void getRemoteIds() throws NextcloudHttpRequestFailedException {
        final Account secondAccount = setupSecondAccount();

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

        final List<NoteIdPair> pair = db.getNoteDao().getRemoteIdAndId(account.getId());
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
    public void getFavoritesCount() throws NextcloudHttpRequestFailedException, InterruptedException {
        final Account secondAccount = setupSecondAccountAndTestNotes();

        assertEquals(Integer.valueOf(1), db.getNoteDao().getFavoritesCount(account.getId()));
        assertEquals(Integer.valueOf(1), db.getNoteDao().getFavoritesCount(secondAccount.getId()));

        assertEquals(Integer.valueOf(1), getOrAwaitValue(db.getNoteDao().getFavoritesCountLiveData(account.getId())));
        assertEquals(Integer.valueOf(1), getOrAwaitValue(db.getNoteDao().getFavoritesCountLiveData(secondAccount.getId())));
    }

    @Test
    public void count() throws NextcloudHttpRequestFailedException, InterruptedException {
        final Account secondAccount = setupSecondAccountAndTestNotes();

        assertEquals(Integer.valueOf(7), db.getNoteDao().count(account.getId()));
        assertEquals(Integer.valueOf(5), db.getNoteDao().count(secondAccount.getId()));

        assertEquals(Integer.valueOf(7), getOrAwaitValue(db.getNoteDao().countLiveData(account.getId())));
        assertEquals(Integer.valueOf(5), getOrAwaitValue(db.getNoteDao().countLiveData(secondAccount.getId())));
    }

    @Test
    public void getLocalModifiedNotes() throws NextcloudHttpRequestFailedException {
        final Account secondAccount = setupSecondAccountAndTestNotes();

        final List<Note> accountNotes = db.getNoteDao().getLocalModifiedNotes(account.getId());
        assertEquals(6, accountNotes.size());
        for (Note note : accountNotes) {
            assertNotEquals(VOID, note.getStatus());
        }

        final List<Note> secondAccountNotes = db.getNoteDao().getLocalModifiedNotes(secondAccount.getId());
        assertEquals(7, secondAccountNotes.size());
        for (Note note : secondAccountNotes) {
            assertNotEquals(VOID, note.getStatus());
        }
    }

    @Test
    public void toggleFavorite() {
        final Note note = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", LOCAL_DELETED, account.getId(), "", 0);
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
        final Note note = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", LOCAL_DELETED, account.getId(), "", 0);
        db.getNoteDao().addNote(note);
        db.getNoteDao().updateRemoteId(1, 5L);
        assertEquals(Long.valueOf(5), db.getNoteDao().getNoteById(1).getRemoteId());
    }

    @Test
    public void updateIfNotModifiedLocallyDuringSync_NotModified() {
        final Note localNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, VOID, account.getId(), "", 0);
        final Note targetNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, VOID, account.getId(), "", 0);

        db.getNoteDao().addNote(localNote);

        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyDuringSync(localNote.getId(), targetNote.getModified().getTimeInMillis(), targetNote.getTitle(), targetNote.getFavorite(), targetNote.getETag(), targetNote.getContent(), targetNote.getExcerpt(), localNote.getContent(), localNote.getCategory(), localNote.getFavorite()));
    }

    @Test
    public void updateIfNotModifiedLocallyDuringSync_ModifiedContent() {
        final Note localNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, VOID, account.getId(), "", 0);
        final Note targetNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, VOID, account.getId(), "", 0);

        db.getNoteDao().addNote(localNote);

        localNote.setContent("My-Modified-Content");

        assertEquals(0, db.getNoteDao().updateIfNotModifiedLocallyDuringSync(localNote.getId(), targetNote.getModified().getTimeInMillis(), targetNote.getTitle(), targetNote.getFavorite(), targetNote.getETag(), targetNote.getContent(), targetNote.getExcerpt(), localNote.getContent(), localNote.getCategory(), localNote.getFavorite()));
    }

    @Test
    public void updateIfNotModifiedLocallyDuringSync_ModifiedFavorite() {
        final Note localNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, VOID, account.getId(), "", 0);
        final Note targetNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, VOID, account.getId(), "", 0);

        db.getNoteDao().addNote(localNote);

        localNote.setFavorite(true);

        assertEquals(0, db.getNoteDao().updateIfNotModifiedLocallyDuringSync(localNote.getId(), targetNote.getModified().getTimeInMillis(), targetNote.getTitle(), targetNote.getFavorite(), targetNote.getETag(), targetNote.getContent(), targetNote.getExcerpt(), localNote.getContent(), localNote.getCategory(), localNote.getFavorite()));
    }

    @Test
    public void updateIfNotModifiedLocallyDuringSync_ModifiedCategory() {
        final Note localNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, VOID, account.getId(), "", 0);
        final Note targetNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, VOID, account.getId(), "", 0);

        db.getNoteDao().addNote(localNote);

        localNote.setCategory("Modified-Category");

        assertEquals(0, db.getNoteDao().updateIfNotModifiedLocallyDuringSync(localNote.getId(), targetNote.getModified().getTimeInMillis(), targetNote.getTitle(), targetNote.getFavorite(), targetNote.getETag(), targetNote.getContent(), targetNote.getExcerpt(), localNote.getContent(), localNote.getCategory(), localNote.getFavorite()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_Nothing() {
        final Note localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", VOID, account.getId(), "", 0)));
        assertEquals(0, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), localNote.getETag(), localNote.getContent(), localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_Nothing_ETagWasAndIsNull() {
        final Note localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, VOID, account.getId(), "", 0)));
        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), null, localNote.getContent(), localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_Nothing_ETagWasNullButChanged() {
        final Note localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, VOID, account.getId(), "", 0)));
        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), "1", localNote.getContent(), localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_Modified() {
        final Note localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", VOID, account.getId(), "", 0)));
        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis() + 1000, localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), localNote.getETag(), localNote.getContent(), localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_Title() {
        final Note localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", VOID, account.getId(), "", 0)));
        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle() + " ", localNote.getFavorite(), localNote.getCategory(), localNote.getETag(), localNote.getContent(), localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_Favorite() {
        final Note localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", VOID, account.getId(), "", 0)));
        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), !localNote.getFavorite(), localNote.getCategory(), localNote.getETag(), localNote.getContent(), localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_Category() {
        final Note localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", VOID, account.getId(), "", 0)));
        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory() + " ", localNote.getETag(), localNote.getContent(), localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_ETag() {
        final Note localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", VOID, account.getId(), "", 0)));
        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), localNote.getETag() + " ", localNote.getContent(), localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_Content() {
        final Note localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", VOID, account.getId(), "", 0)));
        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), localNote.getETag(), localNote.getContent() + " ", localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_Excerpt() {
        final Note localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", VOID, account.getId(), "", 0)));
        assertEquals("Excerpt is a local property, and therefore should not prevent updating if different", 0, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), localNote.getETag(), localNote.getContent(), localNote.getExcerpt() + " "));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_ContentChangedButWasLocalEdited() {
        final Note localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", LOCAL_EDITED, account.getId(), "", 0)));
        assertEquals(0, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), localNote.getETag(), localNote.getContent() + " ", localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_ContentChangedButWasLocalDeleted() {
        final Note localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", LOCAL_DELETED, account.getId(), "", 0)));
        assertEquals(0, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), localNote.getETag(), localNote.getContent() + " ", localNote.getExcerpt()));
    }

    @Test
    public void getContent() {
        final Note note = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", LOCAL_DELETED, account.getId(), "", 0);
        db.getNoteDao().addNote(note);
        assertEquals("My-Content", db.getNoteDao().getContent(note.getId()));
        assertNull(db.getNoteDao().getContent(note.getId() + 1));
    }

    @Test
    public void getCategoriesLiveData() throws InterruptedException, NextcloudHttpRequestFailedException {
        final Account secondAccount = setupSecondAccountAndTestNotes();

        final List<CategoryWithNotesCount> accountCategories = getOrAwaitValue(db.getNoteDao().getCategoriesLiveData(account.getId()));
        assertEquals(4, accountCategories.size());
        for (CategoryWithNotesCount category : accountCategories) {
            assertEquals(account.getId(), category.getAccountId());
        }

        assertTrue(accountCategories.stream().anyMatch(cat -> "Movies".equals(cat.getCategory()) && Integer.valueOf(3).equals(cat.getTotalNotes())));
        assertTrue(accountCategories.stream().anyMatch(cat -> "Music".equals(cat.getCategory()) && Integer.valueOf(2).equals(cat.getTotalNotes())));
        assertTrue(accountCategories.stream().anyMatch(cat -> "ToDo".equals(cat.getCategory()) && Integer.valueOf(1).equals(cat.getTotalNotes())));
        assertTrue(accountCategories.stream().anyMatch(cat -> "日记".equals(cat.getCategory()) && Integer.valueOf(1).equals(cat.getTotalNotes())));

        final List<CategoryWithNotesCount> secondAccountCategories = getOrAwaitValue(db.getNoteDao().getCategoriesLiveData(secondAccount.getId()));
        assertEquals(2, secondAccountCategories.size());
        for (CategoryWithNotesCount category : secondAccountCategories) {
            assertEquals(secondAccount.getId(), category.getAccountId());
        }
        assertTrue(secondAccountCategories.stream().anyMatch(cat -> "Movies".equals(cat.getCategory()) && Integer.valueOf(4).equals(cat.getTotalNotes())));
        assertTrue(secondAccountCategories.stream().anyMatch(cat -> "Music".equals(cat.getCategory()) && Integer.valueOf(1).equals(cat.getTotalNotes())));
        assertFalse(secondAccountCategories.stream().anyMatch(cat -> "ToDo".equals(cat.getCategory())));
        assertFalse(secondAccountCategories.stream().anyMatch(cat -> "日记".equals(cat.getCategory())));
    }

    @Test
    public void searchCategories() throws InterruptedException, NextcloudHttpRequestFailedException {
        final Account secondAccount = setupSecondAccountAndTestNotes();

        assertEquals(2, getOrAwaitValue(db.getNoteDao().searchCategories(account.getId(), "M%")).size());
        assertEquals(1, getOrAwaitValue(db.getNoteDao().searchCategories(account.getId(), "Mo%")).size());
        assertEquals(1, getOrAwaitValue(db.getNoteDao().searchCategories(account.getId(), "MO%")).size());
        assertEquals(1, getOrAwaitValue(db.getNoteDao().searchCategories(account.getId(), "movie%")).size());
        assertEquals(1, getOrAwaitValue(db.getNoteDao().searchCategories(account.getId(), "T%")).size());
        assertEquals(1, getOrAwaitValue(db.getNoteDao().searchCategories(account.getId(), "日记")).size());
        assertEquals(2, getOrAwaitValue(db.getNoteDao().searchCategories(secondAccount.getId(), "M%")).size());
        assertEquals(0, getOrAwaitValue(db.getNoteDao().searchCategories(secondAccount.getId(), "T%")).size());
    }

    private Account setupSecondAccount() throws NextcloudHttpRequestFailedException {
        db.addAccount("https://example.org", "test", "test@example.org", new Capabilities("", null));
        return db.getAccountDao().getLocalAccountByAccountName("test@example.org");
    }

    private Account setupSecondAccountAndTestNotes() throws NextcloudHttpRequestFailedException {
        final Account secondAccount = setupSecondAccount();

        long uniqueId = 1;
        final Note[] notes = new Note[]{
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
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Movies", false, null, LOCAL_EDITED, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Movies", false, null, LOCAL_EDITED, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Music", true, null, VOID, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Music", true, null, LOCAL_DELETED, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "ToDo", true, null, LOCAL_DELETED, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "ToDo", true, null, LOCAL_DELETED, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId, Calendar.getInstance(), "T", "C", "ToDo", true, null, LOCAL_DELETED, secondAccount.getId(), "", 0)
        };
        for (Note note : notes) {
            db.getNoteDao().addNote(note);
        }
        return secondAccount;
    }
}