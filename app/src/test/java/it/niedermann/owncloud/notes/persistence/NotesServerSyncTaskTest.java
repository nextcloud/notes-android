package it.niedermann.owncloud.notes.persistence;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannedString;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.core.text.HtmlCompat;

import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.api.ParsedResponse;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.persistence.sync.NotesAPI;
import it.niedermann.owncloud.notes.shared.model.SyncResultStatus;
import it.niedermann.owncloud.notes.shared.util.ApiVersionUtil;

import static it.niedermann.owncloud.notes.shared.model.DBStatus.LOCAL_EDITED;
import static it.niedermann.owncloud.notes.shared.model.DBStatus.VOID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@SuppressWarnings("CallToThreadRun")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ApiProvider.class, AccountImporter.class, TextUtils.class, Log.class, Color.class, ApiVersionUtil.class, HtmlCompat.class})
public class NotesServerSyncTaskTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private NotesServerSyncTask task;

    private final Account account = mock(Account.class);
    private final NotesRepository repo = mock(NotesRepository.class);
    private final NotesAPI notesAPI = mock(NotesAPI.class);

    @Before
    public void setup() throws NextcloudFilesAppAccountNotFoundException {
        mockStatic(ApiProvider.class, invocation -> notesAPI);
        mockStatic(AccountImporter.class, invocation -> mock(SingleSignOnAccount.class));
        mockStatic(Log.class);
        mockStatic(TextUtils.class);
        PowerMockito.when(TextUtils.join(anyString(), any(Object[].class))).thenReturn("");
        mockStatic(HtmlCompat.class);
        PowerMockito.when(HtmlCompat.fromHtml(anyString(), anyInt())).thenReturn(mock(SpannedString.class));
        mockStatic(ApiVersionUtil.class);
        mockStatic(Color.class);
        this.task = new NotesServerSyncTask(mock(Context.class), repo, account, false) {
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
        mockStatic(ApiProvider.class, invocation -> new NotesAPI(any(), any()) {
            @Override
            public Observable<ParsedResponse<List<Note>>> getNotes(@NonNull Calendar a, String b) {
                return Observable.just(ParsedResponse.of(Arrays.asList(
                        new Note(0, 1000L, Calendar.getInstance(), "RemoteId is in the idMap, therefore", "This note should be updated locally", "", false, "1", VOID, 0, "", 0),
                        new Note(0, 3000L, Calendar.getInstance(), "Is a new RemoteId, therefore", "This note should be created locally", "", false, "1", VOID, 0, "", 0)
                )));
            }
        });

        this.task.run();

        verify(repo).addNote(ArgumentMatchers.anyLong(), argThat(argument -> "This note should be created locally".equals(argument.getContent())));
        verify(repo).updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(anyLong(), anyLong(), anyString(), anyBoolean(), anyString(), anyString(), argThat("This note should be updated locally"::equals), anyString());
    }
}