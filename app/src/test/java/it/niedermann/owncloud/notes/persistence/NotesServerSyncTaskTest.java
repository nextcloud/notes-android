/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static it.niedermann.owncloud.notes.shared.model.DBStatus.LOCAL_EDITED;
import static it.niedermann.owncloud.notes.shared.model.DBStatus.VOID;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.nextcloud.android.sso.api.ParsedResponse;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;

import io.reactivex.Observable;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.persistence.sync.NotesAPI;
import it.niedermann.owncloud.notes.shared.model.SyncResultStatus;

@SuppressWarnings("CallToThreadRun")
@RunWith(RobolectricTestRunner.class)
public class NotesServerSyncTaskTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private NotesServerSyncTask task;

    private final Account account = mock(Account.class);
    private final NotesRepository repo = mock(NotesRepository.class);
    private final NotesAPI notesAPI = mock(NotesAPI.class);
    private final ApiProvider apiProvider = mock(ApiProvider.class);

    @Before
    public void setup() throws NextcloudFilesAppAccountNotFoundException, IOException {
        when(apiProvider.getNotesAPI(any(), any(), any())).thenReturn(notesAPI);
        NotesTestingUtil.mockSingleSignOn(new SingleSignOnAccount(account.getAccountName(), account.getUserName(), "", account.getUrl(), ""));
        this.task = new NotesServerSyncTask(mock(Context.class), repo, account, false, apiProvider) {
            @Override
            void onPreExecute() {

            }

            @Override
            void onPostExecute(SyncResultStatus status) {

            }
        };
    }

    @Test
    public void testPushLocalChanges() {
        when(repo.getLocalModifiedNotes(anyLong())).thenReturn(Arrays.asList(
                new Note(1, null, Calendar.getInstance(), "Does not has a remoteId yet, therefore", "This note should be created on the server", "", false, "1", LOCAL_EDITED, 0, "", 0),
                new Note(1, 2L, Calendar.getInstance(), "Has already a remoteId, therefore", "This note should be updated on the server", "", false, "1", LOCAL_EDITED, 0, "", 0)
        ));

        this.task.run();

        verify(notesAPI).createNote(argThat(argument -> "This note should be created on the server".equals(argument.getContent())));
        verify(notesAPI).editNote(argThat(argument -> "This note should be updated on the server".equals(argument.getContent())));
    }

    @Test
    public void testPullRemoteChanges() {
        when(repo.getAccountById(anyLong())).thenReturn(account);
        when(repo.getIdMap(anyLong())).thenReturn(Map.of(1000L, 1L, 2000L, 2L));
        when(repo.updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(anyLong(), anyLong(), anyString(), anyBoolean(), anyString(), anyString(), anyString(), anyString())).thenReturn(1);
        when(notesAPI.getNotes(any(), any())).thenReturn(Observable.just(ParsedResponse.of(Arrays.asList(
                new Note(0, 1000L, Calendar.getInstance(), "RemoteId is in the idMap, therefore", "This note should be updated locally", "", false, "1", VOID, 0, "", 0),
                new Note(0, 3000L, Calendar.getInstance(), "Is a new RemoteId, therefore", "This note should be created locally", "", false, "1", VOID, 0, "", 0)
        ))));

        this.task.run();

        verify(repo).addNote(anyLong(), argThat(argument -> "This note should be created locally".equals(argument.getContent())));
        verify(repo).updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(anyLong(), anyLong(), anyString(), anyBoolean(), anyString(), anyString(), argThat("This note should be updated locally"::equals), anyString());
    }
}
