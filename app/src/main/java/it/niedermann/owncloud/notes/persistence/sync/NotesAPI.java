/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.sync;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.api.ParsedResponse;

import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.Observable;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.model.ApiVersion;
import it.niedermann.owncloud.notes.shared.model.NotesSettings;
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

    public Observable<ParsedResponse<List<Note>>> getNotes(@NonNull Calendar lastModified, String lastETag) {
        if (ApiVersion.API_VERSION_1_0.equals(usedApiVersion)) {
            return notesAPI_1_0.getNotes(lastModified.getTimeInMillis() / 1_000, lastETag);
        } else if (ApiVersion.API_VERSION_0_2.equals(usedApiVersion)) {
            return notesAPI_0_2.getNotes(lastModified.getTimeInMillis() / 1_000, lastETag);
        } else {
            throw new UnsupportedOperationException("Used API version " + usedApiVersion + " does not support getNotes().");
        }
    }

    public Observable<List<Long>> getNotesIDs() {
        if (ApiVersion.API_VERSION_1_0.equals(usedApiVersion)) {
            return notesAPI_1_0.getNotesIDs().map(response -> response.getResponse().stream().map(Note::getRemoteId).collect(Collectors.toList()));
        } else if (ApiVersion.API_VERSION_0_2.equals(usedApiVersion)) {
            return notesAPI_0_2.getNotesIDs().map(response -> response.getResponse().stream().map(Note::getRemoteId).collect(Collectors.toList()));
        } else {
            throw new UnsupportedOperationException("Used API version " + usedApiVersion + " does not support getNotesIDs().");
        }
    }

    public Observable<ParsedResponse<Note>> getNote(long remoteId) {
        if (ApiVersion.API_VERSION_1_0.equals(usedApiVersion)) {
            return notesAPI_1_0.getNote(remoteId);
        } else if (ApiVersion.API_VERSION_0_2.equals(usedApiVersion)) {
            return notesAPI_0_2.getNote(remoteId);
        } else {
            throw new UnsupportedOperationException("Used API version " + usedApiVersion + " does not support getNote().");
        }
    }

    public Call<Note> createNote(Note note) {
        if (ApiVersion.API_VERSION_1_0.equals(usedApiVersion)) {
            return notesAPI_1_0.createNote(note);
        } else if (ApiVersion.API_VERSION_0_2.equals(usedApiVersion)) {
            return notesAPI_0_2.createNote(new Note_0_2(note));
        } else {
            throw new UnsupportedOperationException("Used API version " + usedApiVersion + " does not support createNote().");
        }
    }

    public Call<Note> editNote(@NonNull Note note) {
        final Long remoteId = note.getRemoteId();
        if (remoteId == null) {
            throw new IllegalArgumentException("remoteId of a " + Note.class.getSimpleName() + " must not be null if this object is used for editing a remote note.");
        }
        if (ApiVersion.API_VERSION_1_0.equals(usedApiVersion)) {
            return notesAPI_1_0.editNote(note, remoteId);
        } else if (ApiVersion.API_VERSION_0_2.equals(usedApiVersion)) {
            return notesAPI_0_2.editNote(new Note_0_2(note), remoteId);
        } else {
            throw new UnsupportedOperationException("Used API version " + usedApiVersion + " does not support editNote().");
        }
    }

    public Call<Void> deleteNote(long noteId) {
        if (ApiVersion.API_VERSION_1_0.equals(usedApiVersion)) {
            return notesAPI_1_0.deleteNote(noteId);
        } else if (ApiVersion.API_VERSION_0_2.equals(usedApiVersion)) {
            return notesAPI_0_2.deleteNote(noteId);
        } else {
            throw new UnsupportedOperationException("Used API version " + usedApiVersion + " does not support createNote().");
        }
    }


    public Call<NotesSettings> getSettings() {
        if (ApiVersion.API_VERSION_1_0.equals(usedApiVersion)) {
            return notesAPI_1_0.getSettings();
        } else {
            throw new UnsupportedOperationException("Used API version " + usedApiVersion + " does not support getSettings().");
        }
    }

    public Call<NotesSettings> putSettings(NotesSettings settings) {
        if (ApiVersion.API_VERSION_1_0.equals(usedApiVersion)) {
            return notesAPI_1_0.putSettings(settings);
        } else {
            throw new UnsupportedOperationException("Used API version " + usedApiVersion + " does not support putSettings().");
        }
    }

    /**
     * {@link ApiVersion#API_VERSION_0_2} didn't have a separate <code>title</code> property.
     */
    static class Note_0_2 {
        @Expose
        public final String category;
        @Expose
        public final Calendar modified;
        @Expose
        public final String content;
        @Expose
        public final boolean favorite;

        private Note_0_2(Note note) {
            if (note == null) {
                throw new IllegalArgumentException(Note.class.getSimpleName() + " can not be converted to " + Note_0_2.class.getSimpleName() + " because it is null.");
            }
            this.category = note.getCategory();
            this.modified = note.getModified();
            this.content = note.getContent();
            this.favorite = note.getFavorite();
        }
    }
}
