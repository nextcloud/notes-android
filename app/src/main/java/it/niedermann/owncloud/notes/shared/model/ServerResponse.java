package it.niedermann.owncloud.notes.shared.model;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.niedermann.owncloud.notes.persistence.NotesClient;
import it.niedermann.owncloud.notes.persistence.entity.Note;

/**
 * Provides entity classes for handling server responses with a single note ({@link NoteResponse}) or a list of notes ({@link NotesResponse}).
 */
public class ServerResponse {

    public static class NoteResponse extends ServerResponse {
        public NoteResponse(NotesClient.ResponseData response) {
            super(response);
        }

        public Note getNote() throws JSONException {
            return getNoteFromJSON(new JSONObject(getContent()));
        }
    }

    public static class NotesResponse extends ServerResponse {
        public NotesResponse(NotesClient.ResponseData response) {
            super(response);
        }

        public List<Note> getNotes() throws JSONException {
            List<Note> notesList = new ArrayList<>();
            JSONArray notes = new JSONArray(getContent());
            for (int i = 0; i < notes.length(); i++) {
                JSONObject json = notes.getJSONObject(i);
                notesList.add(getNoteFromJSON(json));
            }
            return notesList;
        }
    }


    private final NotesClient.ResponseData response;

    ServerResponse(NotesClient.ResponseData response) {
        this.response = response;
    }

    protected String getContent() {
        return response == null ? null : response.getContent();
    }

    public String getETag() {
        return response.getETag();
    }

    public long getLastModified() {
        return response.getLastModified();
    }

    @Nullable
    public String getSupportedApiVersions() {
        return response.getSupportedApiVersions();
    }

    Note getNoteFromJSON(JSONObject json) throws JSONException {
        long id = 0;
        String title = "";
        String content = "";
        Calendar modified = null;
        boolean favorite = false;
        String category = "";
        String etag = null;
        if (!json.isNull(NotesClient.JSON_ID)) {
            id = json.getLong(NotesClient.JSON_ID);
        }
        if (!json.isNull(NotesClient.JSON_TITLE)) {
            title = json.getString(NotesClient.JSON_TITLE);
        }
        if (!json.isNull(NotesClient.JSON_CONTENT)) {
            content = json.getString(NotesClient.JSON_CONTENT);
        }
        if (!json.isNull(NotesClient.JSON_MODIFIED)) {
            modified = Calendar.getInstance();
            modified.setTimeInMillis(json.getLong(NotesClient.JSON_MODIFIED) * 1000);
        }
        if (!json.isNull(NotesClient.JSON_FAVORITE)) {
            favorite = json.getBoolean(NotesClient.JSON_FAVORITE);
        }
        if (!json.isNull(NotesClient.JSON_CATEGORY)) {
            category = json.getString(NotesClient.JSON_CATEGORY);
        }
        if (!json.isNull(NotesClient.JSON_ETAG)) {
            etag = json.getString(NotesClient.JSON_ETAG);
        }
        return new Note(id, modified, title, content, category, favorite, etag);
    }
}
