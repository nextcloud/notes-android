package it.niedermann.owncloud.notes.persistence;

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
import it.niedermann.owncloud.notes.shared.model.Capabilities;
import it.niedermann.owncloud.notes.shared.model.DBStatus;

import static it.niedermann.owncloud.notes.persistence.NotesDatabaseTestUtil.getOrAwaitValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
        db.addAccount("https://example.com", "example", "example@example.com", new Capabilities("", null));
        account = db.getAccountDao().getLocalAccountByAccountName("example@example.com");
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void updateIfNotModifiedLocallyDuringSync_NotModified() {
        final Note localNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, DBStatus.VOID, account.getId(), "", 0);
        final Note targetNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, DBStatus.VOID, account.getId(), "", 0);

        db.getNoteDao().addNote(localNote);

        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyDuringSync(localNote.getId(), targetNote.getModified().getTimeInMillis(), targetNote.getTitle(), targetNote.getFavorite(), targetNote.getETag(), targetNote.getContent(), targetNote.getExcerpt(), localNote.getContent(), localNote.getCategory(), localNote.getFavorite()));
    }

    @Test
    public void updateIfNotModifiedLocallyDuringSync_ModifiedContent() {
        final Note localNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, DBStatus.VOID, account.getId(), "", 0);
        final Note targetNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, DBStatus.VOID, account.getId(), "", 0);

        db.getNoteDao().addNote(localNote);

        localNote.setContent("My-Modified-Content");

        assertEquals(0, db.getNoteDao().updateIfNotModifiedLocallyDuringSync(localNote.getId(), targetNote.getModified().getTimeInMillis(), targetNote.getTitle(), targetNote.getFavorite(), targetNote.getETag(), targetNote.getContent(), targetNote.getExcerpt(), localNote.getContent(), localNote.getCategory(), localNote.getFavorite()));
    }

    @Test
    public void updateIfNotModifiedLocallyDuringSync_ModifiedFavorite() {
        final Note localNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, DBStatus.VOID, account.getId(), "", 0);
        final Note targetNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, DBStatus.VOID, account.getId(), "", 0);

        db.getNoteDao().addNote(localNote);

        localNote.setFavorite(true);

        assertEquals(0, db.getNoteDao().updateIfNotModifiedLocallyDuringSync(localNote.getId(), targetNote.getModified().getTimeInMillis(), targetNote.getTitle(), targetNote.getFavorite(), targetNote.getETag(), targetNote.getContent(), targetNote.getExcerpt(), localNote.getContent(), localNote.getCategory(), localNote.getFavorite()));
    }

    @Test
    public void updateIfNotModifiedLocallyDuringSync_ModifiedCategory() {
        final Note localNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, DBStatus.VOID, account.getId(), "", 0);
        final Note targetNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, DBStatus.VOID, account.getId(), "", 0);

        db.getNoteDao().addNote(localNote);

        localNote.setCategory("Modified-Category");

        assertEquals(0, db.getNoteDao().updateIfNotModifiedLocallyDuringSync(localNote.getId(), targetNote.getModified().getTimeInMillis(), targetNote.getTitle(), targetNote.getFavorite(), targetNote.getETag(), targetNote.getContent(), targetNote.getExcerpt(), localNote.getContent(), localNote.getCategory(), localNote.getFavorite()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_Nothing() {
        final Note localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", DBStatus.VOID, account.getId(), "", 0)));
        assertEquals(0, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), localNote.getETag(), localNote.getContent(), localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_Nothing_ETagWasAndIsNull() {
        final Note localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, DBStatus.VOID, account.getId(), "", 0)));
        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), null, localNote.getContent(), localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_Nothing_ETagWasNullButChanged() {
        final Note localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, DBStatus.VOID, account.getId(), "", 0)));
        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), "1", localNote.getContent(), localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_Modified() {
        final Note localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", DBStatus.VOID, account.getId(), "", 0)));
        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis() + 1000, localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), localNote.getETag(), localNote.getContent(), localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_Title() {
        final Note localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", DBStatus.VOID, account.getId(), "", 0)));
        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle() + " ", localNote.getFavorite(), localNote.getCategory(), localNote.getETag(), localNote.getContent(), localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_Favorite() {
        final Note localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", DBStatus.VOID, account.getId(), "", 0)));
        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), !localNote.getFavorite(), localNote.getCategory(), localNote.getETag(), localNote.getContent(), localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_Category() {
        final Note localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", DBStatus.VOID, account.getId(), "", 0)));
        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory() + " ", localNote.getETag(), localNote.getContent(), localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_ETag() {
        final Note localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", DBStatus.VOID, account.getId(), "", 0)));
        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), localNote.getETag() + " ", localNote.getContent(), localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_Content() {
        final Note localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", DBStatus.VOID, account.getId(), "", 0)));
        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), localNote.getETag(), localNote.getContent() + " ", localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_Excerpt() {
        final Note localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", DBStatus.VOID, account.getId(), "", 0)));
        // Excerpt is a local property, not a remote property
        assertEquals(0, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), localNote.getETag(), localNote.getContent(), localNote.getExcerpt() + " "));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_ContentChangedButWasLocalEdited() {
        final Note localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", DBStatus.LOCAL_EDITED, account.getId(), "", 0)));
        assertEquals(0, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), localNote.getETag(), localNote.getContent() + " ", localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_ContentChangedButWasLocalDeleted() {
        final Note localNote = db.getNoteDao().getNoteById(db.getNoteDao().addNote(new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", DBStatus.LOCAL_DELETED, account.getId(), "", 0)));
        assertEquals(0, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), localNote.getETag(), localNote.getContent() + " ", localNote.getExcerpt()));
    }

    @Test
    public void toggleFavorite() {
        final Note note = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", DBStatus.LOCAL_DELETED, account.getId(), "", 0);
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
        final Note note = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", DBStatus.LOCAL_DELETED, account.getId(), "", 0);
        db.getNoteDao().addNote(note);
        db.getNoteDao().updateRemoteId(1, 5L);
        assertEquals(Long.valueOf(5), db.getNoteDao().getNoteById(1).getRemoteId());
    }

    @Test
    public void getContent() {
        final Note note = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", DBStatus.LOCAL_DELETED, account.getId(), "", 0);
        db.getNoteDao().addNote(note);
        assertEquals("My-Content", db.getNoteDao().getContent(note.getId()));
        assertNull(db.getNoteDao().getContent(note.getId() + 1));
    }

    @Test
    public void getCategoriesLiveData() throws NextcloudHttpRequestFailedException, InterruptedException {
        db.addAccount("https://example.org", "test", "test@example.org", new Capabilities("", null));
        final Account secondAccount = db.getAccountDao().getLocalAccountByAccountName("test@example.org");

        long uniqueId = 1;
        final Note[] notes = new Note[]{
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Movies", false, null, DBStatus.VOID, account.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Movies", false, null, DBStatus.LOCAL_EDITED, account.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Movies", false, null, DBStatus.LOCAL_EDITED, account.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Music", false, null, DBStatus.VOID, account.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Music", false, null, DBStatus.LOCAL_EDITED, account.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Music", false, null, DBStatus.LOCAL_DELETED, account.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "ToDo", false, null, DBStatus.VOID, account.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "ToDo", false, null, DBStatus.LOCAL_DELETED, account.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "ToDo", false, null, DBStatus.LOCAL_DELETED, account.getId(), "", 0),

                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Movies", false, null, DBStatus.VOID, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Movies", false, null, DBStatus.LOCAL_EDITED, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Movies", false, null, DBStatus.LOCAL_EDITED, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Movies", false, null, DBStatus.LOCAL_EDITED, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Music", false, null, DBStatus.VOID, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Music", false, null, DBStatus.LOCAL_DELETED, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "ToDo", false, null, DBStatus.LOCAL_DELETED, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "ToDo", false, null, DBStatus.LOCAL_DELETED, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId, Calendar.getInstance(), "T", "C", "ToDo", false, null, DBStatus.LOCAL_DELETED, secondAccount.getId(), "", 0)
        };
        for (Note note : notes) {
            db.getNoteDao().addNote(note);
        }

        final List<CategoryWithNotesCount> accountCategories = getOrAwaitValue(db.getNoteDao().getCategoriesLiveData(account.getId()));
        assertEquals(3, accountCategories.size());
        for (CategoryWithNotesCount category : accountCategories) {
            assertEquals(account.getId(), category.getAccountId());
        }
        assertEquals("Movies", accountCategories.get(0).getCategory());
        assertEquals(Integer.valueOf(3), accountCategories.get(0).getTotalNotes());
        assertEquals("Music", accountCategories.get(1).getCategory());
        assertEquals(Integer.valueOf(2), accountCategories.get(1).getTotalNotes());
        assertEquals("ToDo", accountCategories.get(2).getCategory());
        assertEquals(Integer.valueOf(1), accountCategories.get(2).getTotalNotes());

        final List<CategoryWithNotesCount> secondAccountCategories = getOrAwaitValue(db.getNoteDao().getCategoriesLiveData(secondAccount.getId()));
        assertEquals(2, secondAccountCategories.size());
        for (CategoryWithNotesCount category : secondAccountCategories) {
            assertEquals(secondAccount.getId(), category.getAccountId());
        }
        assertEquals("Movies", secondAccountCategories.get(0).getCategory());
        assertEquals(Integer.valueOf(4), secondAccountCategories.get(0).getTotalNotes());
        assertEquals("Music", secondAccountCategories.get(1).getCategory());
        assertEquals(Integer.valueOf(1), secondAccountCategories.get(1).getTotalNotes());
    }

    @Test
    public void searchCategories() throws NextcloudHttpRequestFailedException, InterruptedException {
        db.addAccount("https://example.org", "test", "test@example.org", new Capabilities("", null));
        final Account secondAccount = db.getAccountDao().getLocalAccountByAccountName("test@example.org");

        long uniqueId = 1;
        final Note[] notes = new Note[]{
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Movies", false, null, DBStatus.VOID, account.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Movies", false, null, DBStatus.LOCAL_EDITED, account.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Movies", false, null, DBStatus.LOCAL_EDITED, account.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Music", false, null, DBStatus.VOID, account.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Music", false, null, DBStatus.LOCAL_EDITED, account.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Music", false, null, DBStatus.LOCAL_DELETED, account.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "ToDo", false, null, DBStatus.VOID, account.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "ToDo", false, null, DBStatus.LOCAL_DELETED, account.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "ToDo", false, null, DBStatus.LOCAL_DELETED, account.getId(), "", 0),

                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Movies", false, null, DBStatus.VOID, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Movies", false, null, DBStatus.LOCAL_EDITED, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Movies", false, null, DBStatus.LOCAL_EDITED, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Movies", false, null, DBStatus.LOCAL_EDITED, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Music", false, null, DBStatus.VOID, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "Music", false, null, DBStatus.LOCAL_DELETED, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "ToDo", false, null, DBStatus.LOCAL_DELETED, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId++, Calendar.getInstance(), "T", "C", "ToDo", false, null, DBStatus.LOCAL_DELETED, secondAccount.getId(), "", 0),
                new Note(uniqueId++, uniqueId, Calendar.getInstance(), "T", "C", "ToDo", false, null, DBStatus.LOCAL_DELETED, secondAccount.getId(), "", 0)
        };
        for (Note note : notes) {
            db.getNoteDao().addNote(note);
        }

        assertEquals(2, getOrAwaitValue(db.getNoteDao().searchCategories(account.getId(), "M%")).size());
        assertEquals(1, getOrAwaitValue(db.getNoteDao().searchCategories(account.getId(), "Mo%")).size());
        assertEquals(1, getOrAwaitValue(db.getNoteDao().searchCategories(account.getId(), "MO%")).size());
        assertEquals(1, getOrAwaitValue(db.getNoteDao().searchCategories(account.getId(), "movie%")).size());
        assertEquals(1, getOrAwaitValue(db.getNoteDao().searchCategories(account.getId(), "T%")).size());
        assertEquals(2, getOrAwaitValue(db.getNoteDao().searchCategories(secondAccount.getId(), "M%")).size());
        assertEquals(0, getOrAwaitValue(db.getNoteDao().searchCategories(secondAccount.getId(), "T%")).size());

    }
}