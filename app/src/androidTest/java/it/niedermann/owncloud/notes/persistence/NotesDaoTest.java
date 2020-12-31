package it.niedermann.owncloud.notes.persistence;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.model.Capabilities;
import it.niedermann.owncloud.notes.shared.model.DBStatus;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class NotesDaoTest {

    @NonNull
    private NotesDatabase db;
    private Account account;

    @Before
    public void setupDB() throws NextcloudHttpRequestFailedException {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), NotesDatabase.class).build();
        db.addAccount("https://example.com", "example", "example@example.com", new Capabilities("", null));
        account = db.getAccountDao().getLocalAccountByAccountName("example@example.com");
    }

    @Test
    public void updateIfNotModifiedLocallyDuringSync_NotModified() {
        final Note localNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, DBStatus.VOID, account.getId(), "", 0);
        final Note targetNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, DBStatus.VOID, account.getId(), "", 0);

        db.getNoteDao().addNote(localNote);

        assert targetNote.getModified() != null;
        assertEquals(1, db.getNoteDao().updateIfNotModifiedLocallyDuringSync(localNote.getId(), targetNote.getModified().getTimeInMillis(), targetNote.getTitle(), targetNote.getFavorite(), targetNote.getETag(), targetNote.getContent(), targetNote.getExcerpt(), localNote.getContent(), localNote.getCategory(), localNote.getFavorite()));
    }

    @Test
    public void updateIfNotModifiedLocallyDuringSync_ModifiedContent() {
        final Note localNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, DBStatus.VOID, account.getId(), "", 0);
        final Note targetNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, DBStatus.VOID, account.getId(), "", 0);

        db.getNoteDao().addNote(localNote);

        localNote.setContent("My-Modified-Content");

        assert targetNote.getModified() != null;
        assertEquals(0, db.getNoteDao().updateIfNotModifiedLocallyDuringSync(localNote.getId(), targetNote.getModified().getTimeInMillis(), targetNote.getTitle(), targetNote.getFavorite(), targetNote.getETag(), targetNote.getContent(), targetNote.getExcerpt(), localNote.getContent(), localNote.getCategory(), localNote.getFavorite()));
    }

    @Test
    public void updateIfNotModifiedLocallyDuringSync_ModifiedFavorite() {
        final Note localNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, DBStatus.VOID, account.getId(), "", 0);
        final Note targetNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, DBStatus.VOID, account.getId(), "", 0);

        db.getNoteDao().addNote(localNote);

        localNote.setFavorite(true);

        assert targetNote.getModified() != null;
        assertEquals(0, db.getNoteDao().updateIfNotModifiedLocallyDuringSync(localNote.getId(), targetNote.getModified().getTimeInMillis(), targetNote.getTitle(), targetNote.getFavorite(), targetNote.getETag(), targetNote.getContent(), targetNote.getExcerpt(), localNote.getContent(), localNote.getCategory(), localNote.getFavorite()));
    }

    @Test
    public void updateIfNotModifiedLocallyDuringSync_ModifiedCategory() {
        final Note localNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, DBStatus.VOID, account.getId(), "", 0);
        final Note targetNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, null, DBStatus.VOID, account.getId(), "", 0);

        db.getNoteDao().addNote(localNote);

        localNote.setCategory("Modified-Category");

        assert targetNote.getModified() != null;
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
        Note localNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", DBStatus.VOID, account.getId(), "", 0);
        localNote.setStatus(DBStatus.LOCAL_EDITED);
        db.getNoteDao().addNote(localNote);
        localNote = db.getNoteDao().getNoteById(1);
        assertEquals(0, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), localNote.getETag(), localNote.getContent() + " ", localNote.getExcerpt()));
    }

    @Test
    public void updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged_ContentChangedButWasLocalDeleted() {
        Note localNote = new Note(1, 1L, Calendar.getInstance(), "My-Title", "My-Content", "", false, "1", DBStatus.VOID, account.getId(), "", 0);
        localNote.setStatus(DBStatus.LOCAL_DELETED);
        db.getNoteDao().addNote(localNote);
        localNote = db.getNoteDao().getNoteById(1);
        assertEquals(0, db.getNoteDao().updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                localNote.getId(), localNote.getModified().getTimeInMillis(), localNote.getTitle(), localNote.getFavorite(), localNote.getCategory(), localNote.getETag(), localNote.getContent() + " ", localNote.getExcerpt()));
    }
}