package it.niedermann.owncloud.notes.persistence;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.nextcloud.android.sso.AccountImporter;
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
import java.util.Objects;
import java.util.Set;

import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.persistence.sync.ApiProvider;
import it.niedermann.owncloud.notes.shared.model.DBStatus;
import it.niedermann.owncloud.notes.shared.model.ISyncCallback;
import it.niedermann.owncloud.notes.shared.model.SyncResultStatus;
import retrofit2.Response;

import static it.niedermann.owncloud.notes.shared.model.DBStatus.LOCAL_DELETED;
import static it.niedermann.owncloud.notes.shared.util.NoteUtil.generateNoteExcerpt;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NOT_MODIFIED;


/**
 * {@link NotesServerSyncTask} is a {@link Thread} which performs the synchronization in a background thread.
 * Synchronization consists of two parts: {@link #pushLocalChanges()} and {@link #pullRemoteChanges}.
 */
abstract class NotesServerSyncTask extends Thread {

    private static final String TAG = NotesServerSyncTask.class.getSimpleName();

    private static final String HEADER_KEY_X_NOTES_API_VERSIONS = "X-Notes-API-Versions";
    private static final String HEADER_KEY_ETAG = "ETag";
    private static final String HEADER_KEY_LAST_MODIFIED = "Last-Modified";
    @NonNull
    private final Context context;
    @NonNull
    private ApiProvider notesClient;
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

    NotesServerSyncTask(@NonNull Context context, @NonNull NotesRepository repo, @NonNull Account localAccount, boolean onlyLocalChanges) throws NextcloudFilesAppAccountNotFoundException {
        super(TAG);
        this.context = context;
        this.repo = repo;
        this.localAccount = localAccount;
        this.ssoAccount = AccountImporter.getSingleSignOnAccount(context, localAccount.getAccountName());
        this.onlyLocalChanges = onlyLocalChanges;
    }

    void addCallbacks(Account account, List<ISyncCallback> callbacks) {
        this.callbacks.put(account.getId(), callbacks);
    }

    @Override
    public void run() {
        onPreExecute();
        notesClient = new ApiProvider(context, ssoAccount);
        new Thread(() -> {
            Log.i(TAG, "STARTING SYNCHRONIZATION");
            final SyncResultStatus status = new SyncResultStatus();
            status.pushSuccessful = pushLocalChanges();
            if (!onlyLocalChanges) {
                status.pullSuccessful = pullRemoteChanges();
            }
            Log.i(TAG, "SYNCHRONIZATION FINISHED");

            onPostExecute(status);
        }).start();
    }

    abstract void onPreExecute();

    abstract void onPostExecute(SyncResultStatus status);

    /**
     * Push local changes: for each locally created/edited/deleted Note, use NotesClient in order to push the changed to the server.
     */
    private boolean pushLocalChanges() {
        Log.d(TAG, "pushLocalChanges()");

        boolean success = true;
        final List<Note> notes = repo.getLocalModifiedNotes(localAccount.getId());
        for (Note note : notes) {
            Log.d(TAG, "   Process Local Note: " + note);
            try {
                Note remoteNote;
                switch (note.getStatus()) {
                    case LOCAL_EDITED:
                        Log.v(TAG, "   ...create/edit");
                        if (note.getRemoteId() != null) {
                            Log.v(TAG, "   ...Note has remoteId → try to edit");
                            final Response<Note> editResponse = notesClient.getNotesAPI().editNote(note, note.getRemoteId()).execute();
                            if (editResponse.isSuccessful()) {
                                remoteNote = editResponse.body();
                            } else {
                                if (editResponse.code() == HTTP_NOT_FOUND) {
                                    Log.v(TAG, "   ...Note does no longer exist on server → recreate");
                                    final Response<Note> createResponse = notesClient.getNotesAPI().createNote(note).execute();
                                    if (createResponse.isSuccessful()) {
                                        remoteNote = createResponse.body();
                                    } else {
                                        throw new Exception(createResponse.errorBody().string());
                                    }
                                } else {
                                    throw new Exception(editResponse.errorBody().string());
                                }
                            }
                        } else {
                            Log.v(TAG, "   ...Note does not have a remoteId yet → create");
                            final Response<Note> createResponse = notesClient.getNotesAPI().createNote(note).execute();
                            if (createResponse.isSuccessful()) {
                                remoteNote = createResponse.body();
                                repo.updateRemoteId(note.getId(), remoteNote.getRemoteId());
                            } else {
                                throw new Exception(createResponse.errorBody().string());
                            }
                        }
                        // Please note, that db.updateNote() realized an optimistic conflict resolution, which is required for parallel changes of this Note from the UI.
                        repo.updateIfNotModifiedLocallyDuringSync(note.getId(), remoteNote.getModified().getTimeInMillis(), remoteNote.getTitle(), remoteNote.getFavorite(), remoteNote.getETag(), remoteNote.getContent(), generateNoteExcerpt(remoteNote.getContent(), remoteNote.getTitle()), note.getContent(), note.getCategory(), note.getFavorite());
                        break;
                    case LOCAL_DELETED:
                        if (note.getRemoteId() == null) {
                            Log.v(TAG, "   ...delete (only local, since it has never been synchronized)");
                        } else {
                            Log.v(TAG, "   ...delete (from server and local)");
                            final Response<Note> deleteResponse = notesClient.getNotesAPI().deleteNote(note.getRemoteId()).execute();
                            if (!deleteResponse.isSuccessful()) {
                                if (deleteResponse.code() == HTTP_NOT_FOUND) {
                                    Log.v(TAG, "   ...delete (note has already been deleted remotely)");
                                } else {
                                    throw new Exception(deleteResponse.errorBody().string());
                                }
                            }
                        }
                        // Please note, that db.deleteNote() realizes an optimistic conflict resolution, which is required for parallel changes of this Note from the UI.
                        repo.deleteByNoteId(note.getId(), LOCAL_DELETED);
                        break;
                    default:
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
                    SSOClient.invalidateAPICache(ssoAccount);
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
            final Map<Long, Long> idMap = repo.getIdMap(localAccount.getId());

            // FIXME re-reading the localAccount is only a workaround for a not-up-to-date eTag in localAccount.
            final Account accountFromDatabase = repo.getAccountById(localAccount.getId());
            if (accountFromDatabase == null) {
                callbacks.remove(localAccount.getId());
                return true;
            }
            localAccount.setModified(accountFromDatabase.getModified());
            localAccount.setETag(accountFromDatabase.getETag());

            final Response<List<Note>> fetchResponse = notesClient.getNotesAPI().getNotes(localAccount.getModified().getTimeInMillis() / 1_000, localAccount.getETag()).execute();
            if (fetchResponse.isSuccessful()) {
                final List<Note> remoteNotes = fetchResponse.body();
                final Set<Long> remoteIDs = new HashSet<>();
                // pull remote changes: update or create each remote note
                for (Note remoteNote : remoteNotes) {
                    Log.v(TAG, "   Process Remote Note: " + remoteNote);
                    remoteIDs.add(remoteNote.getRemoteId());
                    if (remoteNote.getModified() == null) {
                        Log.v(TAG, "   ... unchanged");
                    } else if (idMap.containsKey(remoteNote.getRemoteId())) {
                        Log.v(TAG, "   ... found → Update");
                        Long localId = idMap.get(remoteNote.getRemoteId());
                        if (localId != null) {
                            repo.updateIfNotModifiedLocallyAndAnyRemoteColumnHasChanged(
                                    localId, remoteNote.getModified().getTimeInMillis(), remoteNote.getTitle(), remoteNote.getFavorite(), remoteNote.getCategory(), remoteNote.getETag(), remoteNote.getContent(), generateNoteExcerpt(remoteNote.getContent(), remoteNote.getTitle()));
                        } else {
                            Log.e(TAG, "Tried to update note from server, but local id of note is null. " + remoteNote);
                        }
                    } else {
                        Log.v(TAG, "   ... create");
                        repo.addNote(localAccount.getId(), remoteNote);
                    }
                }
                Log.d(TAG, "   Remove remotely deleted Notes (only those without local changes)");
                // remove remotely deleted notes (only those without local changes)
                for (Map.Entry<Long, Long> entry : idMap.entrySet()) {
                    if (!remoteIDs.contains(entry.getKey())) {
                        Log.v(TAG, "   ... remove " + entry.getValue());
                        repo.deleteByNoteId(entry.getValue(), DBStatus.VOID);
                    }
                }

                // update ETag and Last-Modified in order to reduce size of next response
                localAccount.setETag(fetchResponse.headers().get(HEADER_KEY_ETAG));

                final Calendar lastModified = Calendar.getInstance();
                lastModified.setTimeInMillis(0);
                final String lastModifiedHeader = fetchResponse.headers().get(HEADER_KEY_LAST_MODIFIED);
                if (lastModifiedHeader != null)
                    lastModified.setTimeInMillis(Date.parse(lastModifiedHeader));
                Log.d(TAG, "ETag: " + fetchResponse.headers().get(HEADER_KEY_ETAG) + "; Last-Modified: " + lastModified + " (" + lastModified + ")");

                localAccount.setModified(lastModified);

                repo.updateETag(localAccount.getId(), localAccount.getETag());
                repo.updateModified(localAccount.getId(), localAccount.getModified().getTimeInMillis());


                String supportedApiVersions = null;
                final String supportedApiVersionsHeader = fetchResponse.headers().get(HEADER_KEY_X_NOTES_API_VERSIONS);
                if (supportedApiVersionsHeader != null) {
                    supportedApiVersions = "[" + Objects.requireNonNull(supportedApiVersionsHeader) + "]";
                }
                try {
                    if (repo.updateApiVersion(localAccount.getId(), supportedApiVersions)) {
                        localAccount.setApiVersion(supportedApiVersions);
                    }
                } catch (Exception e) {
                    exceptions.add(e);
                }
                return true;
            } else {
                throw new Exception(fetchResponse.errorBody().string());
            }
        } catch (NextcloudHttpRequestFailedException e) {
            Log.d(TAG, "Server returned HTTP Status Code " + e.getStatusCode() + " - " + e.getMessage());
            if (e.getStatusCode() == HTTP_NOT_MODIFIED) {
                return true;
            } else {
                exceptions.add(e);
                return false;
            }
        } catch (Exception e) {
            if (e instanceof TokenMismatchException) {
                SSOClient.invalidateAPICache(ssoAccount);
            }
            exceptions.add(e);
            return false;
        }
    }
}
