package it.niedermann.owncloud.notes.persistence.sync;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.api.ParsedResponse;

import java.util.List;

import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.model.ApiVersion;
import retrofit2.Call;
import retrofit2.NextcloudRetrofitApiBuilder;

/**
 * Compatibility layer to support multiple API versions
 */
public class NotesAPI {

    private static final String TAG = NotesAPI.class.getSimpleName();

    private static final String API_ENDPOINT_NOTES_1_0 = "/index.php/apps/notes/api/v1/";
    private static final String API_ENDPOINT_NOTES_0_2 = "/index.php/apps/notes/api/v0.2/";

    @NonNull
    private final ApiVersion usedApiVersion;
    private final NotesAPI_0_2 notesAPI_0_2;
    private final NotesAPI_1_0 notesAPI_1_0;

    public NotesAPI(@NonNull NextcloudAPI nextcloudAPI, @Nullable ApiVersion preferredApiVersion) {
        if (preferredApiVersion == null) {
            Log.i(TAG, "Using " + ApiVersion.API_VERSION_0_2 + ", preferredApiVersion is null");
            usedApiVersion = ApiVersion.API_VERSION_0_2;
            notesAPI_0_2 = new NextcloudRetrofitApiBuilder(nextcloudAPI, API_ENDPOINT_NOTES_0_2).create(NotesAPI_0_2.class);
            notesAPI_1_0 = null;
        } else if (ApiVersion.API_VERSION_1_0.equals(preferredApiVersion)) {
            Log.i(TAG, "Using " + ApiVersion.API_VERSION_1_0);
            usedApiVersion = ApiVersion.API_VERSION_1_0;
            notesAPI_0_2 = null;
            notesAPI_1_0 = new NextcloudRetrofitApiBuilder(nextcloudAPI, API_ENDPOINT_NOTES_1_0).create(NotesAPI_1_0.class);
        } else if (ApiVersion.API_VERSION_0_2.equals(preferredApiVersion)) {
            Log.i(TAG, "Using " + ApiVersion.API_VERSION_0_2);
            usedApiVersion = ApiVersion.API_VERSION_0_2;
            notesAPI_0_2 = new NextcloudRetrofitApiBuilder(nextcloudAPI, API_ENDPOINT_NOTES_0_2).create(NotesAPI_0_2.class);
            notesAPI_1_0 = null;
        } else {
            Log.w(TAG, "Unsupported API version " + preferredApiVersion + " - try using " + ApiVersion.API_VERSION_0_2);
            usedApiVersion = ApiVersion.API_VERSION_0_2;
            notesAPI_0_2 = new NextcloudRetrofitApiBuilder(nextcloudAPI, API_ENDPOINT_NOTES_0_2).create(NotesAPI_0_2.class);
            notesAPI_1_0 = null;
        }
    }

    public Call<ParsedResponse<List<Note>>> getNotes(long lastModified, String lastETag) {
        if (ApiVersion.API_VERSION_1_0.equals(usedApiVersion)) {
            return notesAPI_1_0.getNotes(lastModified, lastETag);
        } else if (ApiVersion.API_VERSION_0_2.equals(usedApiVersion)) {
            return notesAPI_0_2.getNotes(lastModified, lastETag);
        } else {
            throw new UnsupportedOperationException("Used API version " + usedApiVersion + " does not support getNotes().");
        }
    }

    public Call<Note> createNote(Note note) {
        if (ApiVersion.API_VERSION_1_0.equals(usedApiVersion)) {
            return notesAPI_1_0.createNote(note);
        } else if (ApiVersion.API_VERSION_0_2.equals(usedApiVersion)) {
            return notesAPI_0_2.createNote(note);
        } else {
            throw new UnsupportedOperationException("Used API version " + usedApiVersion + " does not support createNote().");
        }
    }

    public Call<Note> editNote(Note note, long remoteId) {
        if (ApiVersion.API_VERSION_1_0.equals(usedApiVersion)) {
            return notesAPI_1_0.editNote(note, remoteId);
        } else if (ApiVersion.API_VERSION_0_2.equals(usedApiVersion)) {
            return notesAPI_0_2.editNote(note, remoteId);
        } else {
            throw new UnsupportedOperationException("Used API version " + usedApiVersion + " does not support editNote().");
        }
    }

    public Call<Note> deleteNote(long noteId) {
        if (ApiVersion.API_VERSION_1_0.equals(usedApiVersion)) {
            return notesAPI_1_0.deleteNote(noteId);
        } else if (ApiVersion.API_VERSION_0_2.equals(usedApiVersion)) {
            return notesAPI_0_2.deleteNote(noteId);
        } else {
            throw new UnsupportedOperationException("Used API version " + usedApiVersion + " does not support createNote().");
        }
    }
}
