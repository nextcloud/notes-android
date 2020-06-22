package it.niedermann.owncloud.notes.persistence;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.nextcloud.android.sso.model.SingleSignOnAccount;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import it.niedermann.owncloud.notes.shared.model.CloudNote;
import it.niedermann.owncloud.notes.shared.model.ServerResponse.NoteResponse;
import it.niedermann.owncloud.notes.shared.model.ServerResponse.NotesResponse;

@WorkerThread
public class NotesClientV1 extends NotesClient {

    private static final String API_PATH = "/index.php/apps/notes/api/v1/";

    NotesClientV1(@NonNull Context appContext) {
        super(appContext);
    }

    NotesResponse getNotes(SingleSignOnAccount ssoAccount, long lastModified, String lastETag) throws Exception {
        Map<String, String> parameter = new HashMap<>();
        parameter.put(GET_PARAM_KEY_PRUNE_BEFORE, Long.toString(lastModified));
        return new NotesResponse(requestServer(ssoAccount, "notes", METHOD_GET, parameter, null, lastETag));
    }

    private NoteResponse putNote(SingleSignOnAccount ssoAccount, CloudNote note, String path, String method) throws Exception {
        JSONObject paramObject = new JSONObject();
        paramObject.accumulate(JSON_TITLE, note.getTitle());
        paramObject.accumulate(JSON_CONTENT, note.getContent());
        paramObject.accumulate(JSON_MODIFIED, note.getModified().getTimeInMillis() / 1000);
        paramObject.accumulate(JSON_FAVORITE, note.isFavorite());
        paramObject.accumulate(JSON_CATEGORY, note.getCategory());
        return new NoteResponse(requestServer(ssoAccount, path, method, null, paramObject, null));
    }

    NoteResponse createNote(SingleSignOnAccount ssoAccount, CloudNote note) throws Exception {
        return putNote(ssoAccount, note, "notes", METHOD_POST);
    }

    NoteResponse editNote(SingleSignOnAccount ssoAccount, CloudNote note) throws Exception {
        return putNote(ssoAccount, note, "notes/" + note.getRemoteId(), METHOD_PUT);
    }

    void deleteNote(SingleSignOnAccount ssoAccount, long noteId) throws Exception {
        this.requestServer(ssoAccount, "notes/" + noteId, METHOD_DELETE, null, null, null);
    }

    @Override
    protected String getApiPath() {
        return API_PATH;
    }
}
