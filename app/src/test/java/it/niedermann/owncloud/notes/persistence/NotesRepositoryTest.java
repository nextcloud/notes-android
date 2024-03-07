/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static it.niedermann.owncloud.notes.persistence.NotesTestingUtil.getOrAwaitValue;
import static it.niedermann.owncloud.notes.shared.model.DBStatus.LOCAL_DELETED;
import static it.niedermann.owncloud.notes.shared.model.DBStatus.LOCAL_EDITED;
import static it.niedermann.owncloud.notes.shared.model.DBStatus.VOID;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.google.common.util.concurrent.MoreExecutors;
import com.nextcloud.android.sso.api.ParsedResponse;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

import io.reactivex.Observable;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.persistence.sync.NotesAPI;
import it.niedermann.owncloud.notes.shared.model.Capabilities;
import it.niedermann.owncloud.notes.shared.model.IResponseCallback;

@RunWith(RobolectricTestRunner.class)
public class NotesRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private NotesRepository repo = null;
    private Account account = null;
    private Account secondAccount = null;
    private NotesDatabase db;

    @Before
    public void setupDB() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException {
        final var context = ApplicationProvider.getApplicationContext();
        db = Room
                .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), NotesDatabase.class)
                .allowMainThreadQueries()
                .build();

        final var constructor = NotesRepository.class.getDeclaredConstructor(Context.class, NotesDatabase.class, ExecutorService.class, ExecutorService.class, ExecutorService.class, ApiProvider.class);
        constructor.setAccessible(true);
        final var executor = MoreExecutors.newDirectExecutorService();
        final var apiProviderSpy = mock(ApiProvider.class);
        final var notesApiSpy = mock(NotesAPI.class);
        repo = constructor.newInstance(context, db, executor, executor, executor, apiProviderSpy);

        doReturn(notesApiSpy).when(apiProviderSpy).getNotesAPI(any(), any(), any());
        when(notesApiSpy.getNotesIDs()).thenReturn(Observable.just(Collections.emptyList()));
        when(notesApiSpy.getNote(anyLong())).thenReturn(Observable.just(ParsedResponse.of(new Note())));

        NotesTestingUtil.mockSingleSignOn(new SingleSignOnAccount("彼得@äöüß.example.com", "彼得", "1337", "https://äöüß.example.com", ""));
        repo.addAccount("https://äöüß.example.com", "彼得", "彼得@äöüß.example.com", new Capabilities(), null, new IResponseCallback<>() {
            @Override
            public void onSuccess(Account result) {

            }

            @Override
            public void onError(@NonNull Throwable t) {
                fail(t.getMessage());
            }
        });
        account = repo.getAccountByName("彼得@äöüß.example.com");

        NotesTestingUtil.mockSingleSignOn(new SingleSignOnAccount("test@example.org", "test", "1337", "https://example.org", ""));
        repo.addAccount("https://example.org", "test", "test@example.org", new Capabilities(), "Herbert", new IResponseCallback<>() {
            @Override
            public void onSuccess(Account result) {

            }

            @Override
            public void onError(@NonNull Throwable t) {
                fail(t.getMessage());
            }
        });
        secondAccount = repo.getAccountByName("test@example.org");

        Arrays.stream(new Note[]{
                new Note(1, 1001L, Calendar.getInstance(), "美好的一天", "C", "Movies", false, null, VOID, account.getId(), "", 0),
                new Note(2, null, Calendar.getInstance(), "T", "C", "Movies", false, null, LOCAL_EDITED, account.getId(), "", 0),
                new Note(3, 1003L, Calendar.getInstance(), "美好的一天", "C", "Movies", false, null, LOCAL_EDITED, account.getId(), "", 0),
                new Note(4, null, Calendar.getInstance(), "T", "C", "Music", false, null, VOID, account.getId(), "", 0),
                new Note(5, 1005L, Calendar.getInstance(), "美好的一天", "C", " 兄弟，这真是美好的一天。", false, null, LOCAL_EDITED, account.getId(), "", 0),
                new Note(6, 1006L, Calendar.getInstance(), "美好的一天", "C", " 兄弟，这真是美好的一天。", false, null, LOCAL_DELETED, account.getId(), "", 0),
                new Note(7, null, Calendar.getInstance(), "T", "C", "Music", true, null, LOCAL_EDITED, secondAccount.getId(), "", 0),
                new Note(8, 1008L, Calendar.getInstance(), "美好的一天", "C", "ToDo", true, null, LOCAL_EDITED, secondAccount.getId(), "", 0),
                new Note(9, 1009L, Calendar.getInstance(), "美好的一天", "C", "ToDo", true, null, LOCAL_DELETED, secondAccount.getId(), "", 0)
        }).forEach(note -> db.getNoteDao().addNote(note));
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void testGetInstance() {
        final var repo = NotesRepository.getInstance(ApplicationProvider.getApplicationContext());
        assertNotNull("Result of NotesRepository.getInstance() must not be null", repo);
        assertSame("Result of NotesRepository.getInstance() must always return the same instance", repo, NotesRepository.getInstance(ApplicationProvider.getApplicationContext()));
    }

    @Test
    public void testGetIdMap() {
        final var idMapOfFirstAccount = repo.getIdMap(account.getId());
        assertEquals(3, idMapOfFirstAccount.size());
        assertEquals(Long.valueOf(1L), idMapOfFirstAccount.get(1001L));
        assertEquals(Long.valueOf(3L), idMapOfFirstAccount.get(1003L));
        assertEquals(Long.valueOf(5L), idMapOfFirstAccount.get(1005L));

        final var idMapOfSecondAccount = repo.getIdMap(secondAccount.getId());
        assertEquals(1, idMapOfSecondAccount.size());
        assertEquals(Long.valueOf(8L), idMapOfSecondAccount.get(1008L));
    }

    @Test
    public void testAddAccount() throws IOException {
        NotesTestingUtil.mockSingleSignOn(new SingleSignOnAccount("彼得@äöüß.example.com", "彼得", "1337", "https://äöüß.example.com", ""));
        repo.addAccount("https://äöüß.example.com", "彼得", "彼得@äöüß.example.com", new Capabilities(), "", new IResponseCallback<>() {
            @Override
            public void onSuccess(Account createdAccount) {
                assertEquals("https://äöüß.example.com", createdAccount.getUrl());
                assertEquals("彼得", createdAccount.getUserName());
                assertEquals("彼得@äöüß.example.com", createdAccount.getAccountName());
            }

            @Override
            public void onError(@NonNull Throwable t) {
                fail(t.getMessage());
            }
        });
    }

    @Test
    public void testDeleteAccount() throws IOException {
        NotesTestingUtil.mockSingleSignOn(new SingleSignOnAccount(account.getAccountName(), account.getUserName(), "1337", account.getUrl(), ""));

        assertNotNull(repo.getAccountById(account.getId()));

        repo.deleteAccount(account);

        assertNull(repo.getAccountById(account.getId()));
    }

    @Test
    public void testAddNote() {
        final var localNote = new Note(null, Calendar.getInstance(), "Fancy Title", "MyContent", "Samples", false, "123");
        localNote.setId(99);
        localNote.setStatus(LOCAL_EDITED);
        final var createdNoteFromLocal = repo.addNote(account.getId(), localNote);
        assertEquals(LOCAL_EDITED, createdNoteFromLocal.getStatus());
        assertEquals("MyContent", createdNoteFromLocal.getExcerpt());

        final var createdNoteFromRemote = repo.addNote(account.getId(), new Note(null, Calendar.getInstance(), "Fancy Title", "MyContent", "Samples", false, "123"));
        assertEquals(VOID, createdNoteFromRemote.getStatus());
        assertEquals("MyContent", createdNoteFromRemote.getExcerpt());
    }

    @Test
    public void updateApiVersion() {
        repo.updateApiVersion(account.getId(), "");
        assertNull(repo.getAccountById(account.getId()).getApiVersion());

        repo.updateApiVersion(account.getId(), "foo");
        assertNull(repo.getAccountById(account.getId()).getApiVersion());

        repo.updateApiVersion(account.getId(), "{}");
        assertNull(repo.getAccountById(account.getId()).getApiVersion());

        repo.updateApiVersion(account.getId(), null);
        assertNull(repo.getAccountById(account.getId()).getApiVersion());

        repo.updateApiVersion(account.getId(), "[]");
        assertNull(repo.getAccountById(account.getId()).getApiVersion());

        repo.updateApiVersion(account.getId(), "[1.0]");
        assertEquals("[1.0]", repo.getAccountById(account.getId()).getApiVersion());

        repo.updateApiVersion(account.getId(), "[0.2, 1.0]");
        assertEquals("[0.2,1.0]", repo.getAccountById(account.getId()).getApiVersion());

        repo.updateApiVersion(account.getId(), "[0.2, foo]");
        assertEquals("[0.2]", repo.getAccountById(account.getId()).getApiVersion());
    }

    @Test
    public void moveNoteToAnotherAccount() throws InterruptedException {
        final var repoSpy = spy(repo);
        final var noteToMove = repoSpy.getNoteById(1);

        assertEquals(VOID, noteToMove.getStatus());
        assertEquals(3, repoSpy.getLocalModifiedNotes(secondAccount.getId()).size());

        doNothing().when(repoSpy).deleteNoteAndSync(any(), anyLong());
        doNothing().when(repoSpy).scheduleSync(any(), anyBoolean());

        final var movedNote = getOrAwaitValue(repoSpy.moveNoteToAnotherAccount(secondAccount, noteToMove));

        assertEquals(4, repoSpy.getLocalModifiedNotes(secondAccount.getId()).size());
        assertEquals("美好的一天", movedNote.getTitle());
        assertEquals("C", movedNote.getContent());
        assertEquals("Movies", movedNote.getCategory());
        assertEquals(LOCAL_EDITED, movedNote.getStatus());

        verify(repoSpy, times(1)).deleteNoteAndSync(any(), anyLong());
        verify(repoSpy, times(1)).addNoteAndSync(any(), any());
    }

    @Test
    public void testSyncStatusLiveData() throws InterruptedException, IOException {
        NotesTestingUtil.mockSingleSignOn(new SingleSignOnAccount(account.getAccountName(), account.getUserName(), "1337", account.getUrl(), ""));

        assertFalse(NotesTestingUtil.getOrAwaitValue(repo.getSyncStatus()));
        repo.addCallbackPull(account, () -> {
            try {
                assertTrue(NotesTestingUtil.getOrAwaitValue(repo.getSyncStatus()));
            } catch (InterruptedException e) {
                e.printStackTrace();
                fail(e.getMessage());
            }
        });
        repo.scheduleSync(account, false);
        assertFalse(NotesTestingUtil.getOrAwaitValue(repo.getSyncStatus()));
    }

    @Test
    public void testSyncErrorsLiveData() throws InterruptedException, IOException {
        NotesTestingUtil.mockSingleSignOn(new SingleSignOnAccount(account.getAccountName(), account.getUserName(), "1337", account.getUrl(), ""));

        assertThrows("The very first time, this LiveData should never have been set", RuntimeException.class, () -> NotesTestingUtil.getOrAwaitValue(repo.getSyncErrors()));
        repo.scheduleSync(account, true);
        assertEquals("In our scenario, we expect 4 failed note syncs to be handled.", 4, getOrAwaitValue(repo.getSyncErrors()).size());
    }

    @Test
    public void updateDisplayName() {
        final var account = db.getAccountDao().getAccountById(db.getAccountDao().insert(new Account("https://äöüß.example.com", "彼得", "彼得@äöüß.example.com", null, new Capabilities())));
        assertEquals("Should read userName in favor of displayName if displayName is NULL", "彼得", account.getDisplayName());

        repo.updateDisplayName(account.getId(), "");
        assertEquals("Should properly update the displayName, even if it is blank", "", db.getAccountDao().getAccountById(account.getId()).getDisplayName());

        repo.updateDisplayName(account.getId(), "Foo Bar");
        assertEquals("Foo Bar", db.getAccountDao().getAccountById(account.getId()).getDisplayName());

        repo.updateDisplayName(account.getId(), null);
        assertEquals("Should read userName in favor of displayName if displayName is NULL", "彼得", db.getAccountDao().getAccountById(account.getId()).getDisplayName());
    }

    @Config(qualifiers = "de")
    @Test
    @Ignore("Language is properly set to DE, but LOCALIZED SQL query does not work")
    public void searchLexicographically() throws InterruptedException, IOException {
        repo.searchRecentByModified(account.getId(), "").forEach(note -> repo.deleteByNoteId(note.getId(), note.getStatus()));
        Arrays.stream(new Note[]{
                new Note(10, 1001L, Calendar.getInstance(), "Baaa", "", "Špagety", false, null, VOID, account.getId(), "", 0),
                new Note(11, null, Calendar.getInstance(), "Aaaa", "", "Svíčková", false, null, VOID, account.getId(), "", 0),
                new Note(12, 1003L, Calendar.getInstance(), "Äaaa", "", "Zelí", false, null, VOID, account.getId(), "", 0),
        }).forEach(note -> db.getNoteDao().addNote(note));

        final var recent = NotesTestingUtil.getOrAwaitValue(repo.searchRecentLexicographically$(account.getId(), ""));
        assertEquals(3, recent.size());
        assertEquals(11, recent.get(0).getId());
        assertEquals(12, recent.get(1).getId());
        assertEquals(10, recent.get(2).getId());
    }
}
