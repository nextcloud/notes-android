/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NOT_MODIFIED;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;
import static it.niedermann.owncloud.notes.shared.model.DBStatus.LOCAL_DELETED;
import static it.niedermann.owncloud.notes.shared.util.NoteUtil.generateNoteExcerpt;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.exceptions.NextcloudApiNotRespondingException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;
import com.nextcloud.android.sso.exceptions.NextcloudNetworkException;
import com.nextcloud.android.sso.exceptions.TokenMismatchException;
import com.nextcloud.android.sso.model.SingleSignOnAccount;
import com.owncloud.android.lib.common.utils.Log_OC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.niedermann.owncloud.notes.BuildConfig;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.persistence.sync.NotesAPI;
import it.niedermann.owncloud.notes.shared.model.ISyncCallback;
import it.niedermann.owncloud.notes.shared.model.SyncResultStatus;
import it.niedermann.owncloud.notes.shared.util.ApiVersionUtil;
import retrofit2.Response;


/**
 * {@link NotesServerSyncTask} is a {@link Thread} which performs the synchronization in a background thread.
 * Synchronization consists of two parts: {@link #pushLocalChanges()} and {@link #pullRemoteChanges}.
 */
abstract class NotesServerSyncTask extends Thread {

    private static final String TAG = NotesServerSyncTask.class.getSimpleName();

    private NotesAPI notesAPI;
    @NonNull
    private final ApiProvider apiProvider;
    @NonNull
    private final Context context;
    @NonNull
    private final NotesRepository repo;
    @NonNull
    protected final Account localAccount;
    @NonNull
    private final SingleSignOnAccount ssoAccount;
    private final boolean onlyLocalChanges;
    @NonNull
    protected final Map<Long, List<ISyncCallback>> callbacks = new HashMap<>();
    @NonNull
    protected final ArrayList<Throwable> exceptions = new ArrayList<>();

    NotesServerSyncTask(@NonNull Context context, @NonNull NotesRepository repo, @NonNull Account localAccount, boolean onlyLocalChanges, @NonNull ApiProvider apiProvider) throws NextcloudFilesAppAccountNotFoundException {
        super(TAG);
        this.context = context;
        this.repo = repo;
        this.localAccount = localAccount;
        this.ssoAccount = AccountImporter.getSingleSignOnAccount(context, localAccount.getAccountName());
        this.onlyLocalChanges = onlyLocalChanges;
        this.apiProvider = apiProvider;
    }

    void addCallbacks(Account account, List<ISyncCallback> callbacks) {
        this.callbacks.put(account.getId(), callbacks);
    }

    @Override
    public void run() {
        onPreExecute();

        notesAPI = apiProvider.getNotesAPI(context, ssoAccount, ApiVersionUtil.getPreferredApiVersion(localAccount.getApiVersion()));

        Log.i(TAG, "STARTING SYNCHRONIZATION");

        final var status = new SyncResultStatus();
        status.pushSuccessful = pushLocalChanges();
        if (!onlyLocalChanges) {
            status.pullSuccessful = pullRemoteChanges();
        }

        Log.i(TAG, "SYNCHRONIZATION FINISHED");

        onPostExecute(status);
    }

    abstract void onPreExecute();

    abstract void onPostExecute(SyncResultStatus status);

    /**
     * Push local changes: for each locally created/edited/deleted Note, use NotesClient in order to push the changed to the server.
     */
    private boolean pushLocalChanges() {
        Log.d(TAG, "pushLocalChanges()");

        boolean success = true;
        for (final var note : repo.getLocalModifiedNotes(localAccount.getId())) {
            Log.d(TAG, "   Process Local Note: " + (BuildConfig.DEBUG ? note : note.getTitle()));
            success &= pushLocalChange(note);
        }
        return success;
    }

    private boolean pushLocalChange(@NonNull Note note) {
        try {
            switch (note.getStatus()) {
                case LOCAL_EDITED -> pushEditedNote(note);
                case LOCAL_DELETED -> pushDeletedNote(note);
                default ->
                        throw new IllegalStateException("Unknown State of Note " + note + ": " + note.getStatus());
            }
            return true;
        } catch (NextcloudHttpRequestFailedException e) {
            if (e.getStatusCode() == HTTP_NOT_MODIFIED) {
                Log.d(TAG, "Server returned HTTP Status Code 304 - Not Modified");
                return true;
            }
            return recordFailure(e);
        } catch (Exception e) {
            if (e instanceof TokenMismatchException) {
                apiProvider.invalidateAPICache(ssoAccount);
            }
            return recordFailure(e);
        }
    }

    private void pushEditedNote(@NonNull Note note) throws Exception {
        Log.v(TAG, "   ...create/edit");
        final Note remoteNote = note.getRemoteId() == null ? createRemoteNote(note) : editRemoteNote(note);
        // Please note, that db.updateNote() realized an optimistic conflict resolution, which is required for parallel changes of this Note from the UI.
        repo.updateIfNotModifiedLocallyDuringSync(note.getId(), remoteNote.getModified().getTimeInMillis(), remoteNote.getTitle(), remoteNote.getFavorite(), remoteNote.getETag(), remoteNote.getContent(), generateNoteExcerpt(remoteNote.getContent(), remoteNote.getTitle()), note.getContent(), note.getCategory(), note.getFavorite());
    }

    @NonNull
    private Note editRemoteNote(@NonNull Note note) throws Exception {
        Log.v(TAG, "   ...Note has remoteId → try to edit");
        final var editResponse = notesAPI.editNote(note).execute();
        if (editResponse.isSuccessful()) {
            return requireRemoteNote(note, editResponse, "editing");
        }
        if (editResponse.code() != HTTP_NOT_FOUND) {
            throw new NoteSyncException(note.getTitle(), editResponse);
        }
        Log.v(TAG, "   ...Note does no longer exist on server → recreate");
        return postNote(note, "recreating");
    }

    @NonNull
    private Note createRemoteNote(@NonNull Note note) throws Exception {
        Log.v(TAG, "   ...Note does not have a remoteId yet → create");
        return postNote(note, "creating");
    }

    @NonNull
    private Note postNote(@NonNull Note note, @NonNull String action) throws Exception {
        final var createResponse = notesAPI.createNote(note).execute();
        if (!createResponse.isSuccessful()) {
            throw new NoteSyncException(note.getTitle(), createResponse);
        }
        final var remoteNote = requireRemoteNote(note, createResponse, action);
        repo.updateRemoteId(note.getId(), remoteNote.getRemoteId());
        return remoteNote;
    }

    @NonNull
    private Note requireRemoteNote(@NonNull Note note, @NonNull Response<Note> response, @NonNull String action) throws Exception {
        final var remoteNote = response.body();
        if (remoteNote == null) {
            final String message = "Server returned null after " + action + " \"" + note.getTitle() + "\" (#" + note.getId() + ")";
            Log.e(TAG, "   ..." + message);
            throw new Exception(message);
        }
        return remoteNote;
    }

    private void pushDeletedNote(@NonNull Note note) throws Exception {
        deleteRemoteNote(note);
        // Please note, that db.deleteNote() realizes an optimistic conflict resolution, which is required for parallel changes of this Note from the UI.
        repo.deleteByNoteId(note.getId(), LOCAL_DELETED);
    }

    private void deleteRemoteNote(@NonNull Note note) throws Exception {
        final Long remoteId = note.getRemoteId();
        if (remoteId == null) {
            Log.v(TAG, "   ...delete (only local, since it has never been synchronized)");
            return;
        }

        Log.v(TAG, "   ...delete (from server and local)");
        final var deleteResponse = notesAPI.deleteNote(remoteId).execute();
        if (deleteResponse.isSuccessful()) {
            return;
        }
        if (deleteResponse.code() != HTTP_NOT_FOUND) {
            throw new NoteSyncException(note.getTitle(), deleteResponse);
        }
        Log.v(TAG, "   ...delete (note has already been deleted remotely)");
    }

    /**
     * Pull remote Changes: update or create each remote note (if local pendant has no changes) and remove remotely deleted notes.
     */
    private boolean pullRemoteChanges() {
        Log.d(TAG, "pullRemoteChanges() for account " + localAccount.getAccountName());
        try {
            final var idMap = repo.getIdMap(localAccount.getId());

            // FIXME re-reading the localAccount is only a workaround for a not-up-to-date eTag in localAccount.
            final var accountFromDatabase = repo.getAccountById(localAccount.getId());
            if (accountFromDatabase == null) {
                callbacks.remove(localAccount.getId());
                return true;
            }
            localAccount.setModified(accountFromDatabase.getModified());
            localAccount.setETag(accountFromDatabase.getETag());
            final var modified = localAccount.getModified();
            if (modified == null) {
                Log_OC.e(TAG, "modified is null cannot fetch notes");
                return false;
            }

            final var fetchResponse = notesAPI.getNotes(modified, localAccount.getETag()).blockingSingle();
            new RemoteNotesMerger(repo, localAccount).merge(fetchResponse, idMap);
            return true;
        } catch (Throwable t) {
            return handlePullFailure(t);
        }
    }

    private boolean handlePullFailure(@NonNull Throwable t) {
        final Throwable cause = t.getCause();
        if (t.getClass() != RuntimeException.class || cause == null) {
            return recordFailure(t);
        }
        if (isIgnoredPullFailure(cause)) {
            return true;
        }
        if (cause instanceof NextcloudApiNotRespondingException) {
            apiProvider.invalidateAPICache(ssoAccount);
        }
        return recordFailure(t);
    }

    private boolean isIgnoredPullFailure(@NonNull Throwable cause) {
        if (cause instanceof NextcloudHttpRequestFailedException httpException) {
            return isIgnoredPullStatus(httpException.getStatusCode());
        }
        if (cause.getClass() == NextcloudNetworkException.class) {
            Log.w(TAG, "Network connectivity issue during sync");
            return true;
        }
        return false;
    }

    private boolean isIgnoredPullStatus(int statusCode) {
        if (statusCode == HTTP_NOT_MODIFIED) {
            Log.d(TAG, "Server returned HTTP Status Code " + statusCode + " - Notes not modified.");
            return true;
        }
        if (statusCode == HTTP_UNAVAILABLE) {
            Log.d(TAG, "Server returned HTTP Status Code " + statusCode + " - Server is in maintenance mode.");
            return true;
        }
        return false;
    }

    private boolean recordFailure(@NonNull Throwable t) {
        exceptions.add(t);
        return false;
    }
}
