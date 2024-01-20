package it.niedermann.owncloud.notes.persistence;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.api.ParsedResponse;
import com.nextcloud.android.sso.exceptions.NextcloudApiNotRespondingException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;
import com.nextcloud.android.sso.exceptions.TokenMismatchException;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.niedermann.owncloud.notes.BuildConfig;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.persistence.sync.NotesAPI;
import it.niedermann.owncloud.notes.shared.model.DBStatus;
import it.niedermann.owncloud.notes.shared.model.ISyncCallback;
import it.niedermann.owncloud.notes.shared.model.SyncResultStatus;
import it.niedermann.owncloud.notes.shared.util.ApiVersionUtil;
import retrofit2.Response;

import static it.niedermann.owncloud.notes.shared.model.DBStatus.LOCAL_DELETED;
import static it.niedermann.owncloud.notes.shared.util.NoteUtil.generateNoteExcerpt;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NOT_MODIFIED;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;


/**
 * {@link NotesServerSyncTask} is a {@link Thread} which performs the synchronization in a background thread.
 * Synchronization consists of two parts: {@link #pushLocalChanges()} and {@link #pullRemoteChanges}.
 */
abstract class NotesServerSyncTask extends Thread {

    private static final String TAG = NotesServerSyncTask.class.getSimpleName();

    private static final String HEADER_KEY_X_NOTES_API_VERSIONS = "X-Notes-API-Versions";
    private static final String HEADER_KEY_ETAG = "ETag";
    private static final String HEADER_KEY_LAST_MODIFIED = "Last-Modified";

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
        final var notes = repo.getLocalModifiedNotes(localAccount.getId());
        for (Note note : notes) {
            Log.d(TAG, "   Process Local Note: " + (BuildConfig.DEBUG ? note : note.getTitle()));
            try {
                Note remoteNote;
                switch (note.getStatus()) {
                    case LOCAL_EDITED -> {
                        Log.v(TAG, "   ...create/edit");
                        if (note.getRemoteId() != null) {
                            Log.v(TAG, "   ...Note has remoteId → try to edit");
                            final var editResponse = notesAPI.editNote(note).execute();
                            if (editResponse.isSuccessful()) {
                                remoteNote = editResponse.body();
                                if (remoteNote == null) {
                                    Log.e(TAG, "   ...Tried to edit \"" + note.getTitle() + "\" (#" + note.getId() + ") but the server response was null.");
                                    throw new Exception("Server returned null after editing \"" + note.getTitle() + "\" (#" + note.getId() + ")");
                                }
                            } else if (editResponse.code() == HTTP_NOT_FOUND) {
                                Log.v(TAG, "   ...Note does no longer exist on server → recreate");
                                final var createResponse = notesAPI.createNote(note).execute();
                                if (createResponse.isSuccessful()) {
                                    remoteNote = createResponse.body();
                                    if (remoteNote == null) {
                                        Log.e(TAG, "   ...Tried to recreate \"" + note.getTitle() + "\" (#" + note.getId() + ") but the server response was null.");
                                        throw new Exception("Server returned null after recreating \"" + note.getTitle() + "\" (#" + note.getId() + ")");
                                    }
                                } else {
                                    throw new Exception(createResponse.message());
                                }
                            } else {
                                throw new Exception(editResponse.message());
                            }
                        } else {
                            Log.v(TAG, "   ...Note does not have a remoteId yet → create");
                            final var createResponse = notesAPI.createNote(note).execute();
                            if (createResponse.isSuccessful()) {
                                remoteNote = createResponse.body();
                                if (remoteNote == null) {
                                    Log.e(TAG, "   ...Tried to create \"" + note.getTitle() + "\" (#" + note.getId() + ") but the server response was null.");
                                    throw new Exception("Server returned null after creating \"" + note.getTitle() + "\" (#" + note.getId() + ")");
                                }
                                repo.updateRemoteId(note.getId(), remoteNote.getRemoteId());
                            } else {
                                throw new Exception(createResponse.message());
                            }
                        }
                        // Please note, that db.updateNote() realized an optimistic conflict resolution, which is required for parallel changes of this Note from the UI.
                        repo.updateIfNotModifiedLocallyDuringSync(note.getId(), remoteNote.getModified().getTimeInMillis(), remoteNote.getTitle(), remoteNote.getFavorite(), remoteNote.getETag(), remoteNote.getContent(), generateNoteExcerpt(remoteNote.getContent(), remoteNote.getTitle()), note.getContent(), note.getCategory(), note.getFavorite());
                    }
                    case LOCAL_DELETED -> {
                        if (note.getRemoteId() == null) {
                            Log.v(TAG, "   ...delete (only local, since it has never been synchronized)");
                        } else {
                            Log.v(TAG, "   ...delete (from server and local)");
                            final var deleteResponse = notesAPI.deleteNote(note.getRemoteId()).execute();
                            if (!deleteResponse.isSuccessful()) {
                                if (deleteResponse.code() == HTTP_NOT_FOUND) {
                                    Log.v(TAG, "   ...delete (note has already been deleted remotely)");
                                } else {
                                    throw new Exception(deleteResponse.message());
                                }
                            }
                        }
                        // Please note, that db.deleteNote() realizes an optimistic conflict resolution, which is required for parallel changes of this Note from the UI.
                        repo.deleteByNoteId(note.getId(), LOCAL_DELETED);
                    }
                    default ->
                            throw new IllegalStateException("Unknown State of Note " + note + ": " + note.getStatus());
                }
            } catch (NextcloudHttpRequestFailedException e) {
                if (e.getStatusCode() == HTTP_NOT_MODIFIED) {
                    Log.d(TAG, "Server returned HTTP Status Code 304 - Not Modified");
                } else {
                    exceptions.add(e);
                    success = false;
                }
            } catch (Exception e) {
                if (e instanceof TokenMismatchException) {
                    apiProvider.invalidateAPICache(ssoAccount);
                }
                exceptions.add(e);
                success = false;
            }
        }
        return success;
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

            final var fetchResponse = notesAPI.getNotes(localAccount.getModified(), localAccount.getETag()).blockingSingle();
            final var remoteNotes = fetchResponse.getResponse();
            final var remoteIDs = new HashSet<Long>();
            // pull remote changes: update or create each remote note
            for (final var remoteNote : remoteNotes) {
                Log.v(TAG, "   Process Remote Note: " + (BuildConfig.DEBUG ? remoteNote : remoteNote.getTitle()));
                remoteIDs.add(remoteNote.getRemoteId());
                if (remoteNote.getModified() == null) {
                    Log.v(TAG, "   ... unchanged");
                } else if (idMap.containsKey(remoteNote.getRemoteId())) {
                    Log.v(TAG, "   ... found → Update");
                    final Long localId = idMap.get(remoteNote.getRemoteId());
                    if (localId != null) {
                        repo.updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                                localId, remoteNote.getModified().getTimeInMillis(), remoteNote.getTitle(), remoteNote.getFavorite(), remoteNote.getCategory(), remoteNote.getETag(), remoteNote.getContent(), generateNoteExcerpt(remoteNote.getContent(), remoteNote.getTitle()));
                    } else {
                        Log.e(TAG, "Tried to update note from server, but local id of note is null. " + (BuildConfig.DEBUG ? remoteNote : remoteNote.getTitle()));
                    }
                } else {
                    Log.v(TAG, "   ... create");
                    repo.addNote(localAccount.getId(), remoteNote);
                }
            }
            Log.d(TAG, "   Remove remotely deleted Notes (only those without local changes)");
            // remove remotely deleted notes (only those without local changes)
            for (final var entry : idMap.entrySet()) {
                if (!remoteIDs.contains(entry.getKey())) {
                    Log.v(TAG, "   ... remove " + entry.getValue());
                    repo.deleteByNoteId(entry.getValue(), DBStatus.VOID);
                }
            }

            // update ETag and Last-Modified in order to reduce size of next response
            localAccount.setETag(fetchResponse.getHeaders().get(HEADER_KEY_ETAG));

            final var lastModified = Calendar.getInstance();
            lastModified.setTimeInMillis(0);
            final String lastModifiedHeader = fetchResponse.getHeaders().get(HEADER_KEY_LAST_MODIFIED);
            if (lastModifiedHeader != null)
                lastModified.setTimeInMillis(Date.parse(lastModifiedHeader));
            Log.d(TAG, "ETag: " + fetchResponse.getHeaders().get(HEADER_KEY_ETAG) + "; Last-Modified: " + lastModified + " (" + lastModified + ")");

            localAccount.setModified(lastModified);

            repo.updateETag(localAccount.getId(), localAccount.getETag());
            repo.updateModified(localAccount.getId(), localAccount.getModified().getTimeInMillis());

            final String newApiVersion = ApiVersionUtil.sanitize(fetchResponse.getHeaders().get(HEADER_KEY_X_NOTES_API_VERSIONS));
            localAccount.setApiVersion(newApiVersion);
            repo.updateApiVersion(localAccount.getId(), newApiVersion);
            Log.d(TAG, "ApiVersion: " + newApiVersion);
            return true;
        } catch (Throwable t) {
            final Throwable cause = t.getCause();
            if (t.getClass() == RuntimeException.class && cause != null) {
                if (cause.getClass() == NextcloudHttpRequestFailedException.class || cause instanceof NextcloudHttpRequestFailedException) {
                    final NextcloudHttpRequestFailedException httpException = (NextcloudHttpRequestFailedException) cause;
                    if (httpException.getStatusCode() == HTTP_NOT_MODIFIED) {
                        Log.d(TAG, "Server returned HTTP Status Code " + httpException.getStatusCode() + " - Notes not modified.");
                        return true;
                    } else if (httpException.getStatusCode() == HTTP_UNAVAILABLE) {
                        Log.d(TAG, "Server returned HTTP Status Code " + httpException.getStatusCode() + " - Server is in maintenance mode.");
                        return true;
                    }
                } else if (cause.getClass() == NextcloudApiNotRespondingException.class || cause instanceof NextcloudApiNotRespondingException) {
                    apiProvider.invalidateAPICache(ssoAccount);
                }
            }
            exceptions.add(t);
            return false;
        }
    }
}
