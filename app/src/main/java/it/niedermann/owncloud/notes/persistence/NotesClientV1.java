package it.niedermann.owncloud.notes.persistence;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.nextcloud.android.sso.model.SingleSignOnAccount;

import it.niedermann.owncloud.notes.model.CloudNote;
import it.niedermann.owncloud.notes.util.ServerResponse.NoteResponse;
import it.niedermann.owncloud.notes.util.ServerResponse.NotesResponse;

@WorkerThread
public class NotesClientV1 extends NotesClient {

    private static final String API_PATH = "/index.php/apps/notes/api/v1/";

    NotesClientV1(@NonNull Context appContext) {
        super(appContext);
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    NotesResponse getNotes(SingleSignOnAccount ssoAccount, long lastModified, String lastETag) throws Exception {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    NoteResponse createNote(SingleSignOnAccount ssoAccount, CloudNote note) throws Exception {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    NoteResponse editNote(SingleSignOnAccount ssoAccount, CloudNote note) throws Exception {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    void deleteNote(SingleSignOnAccount ssoAccount, long noteId) throws Exception {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    protected String getApiPath() {
        return API_PATH;
    }
}
