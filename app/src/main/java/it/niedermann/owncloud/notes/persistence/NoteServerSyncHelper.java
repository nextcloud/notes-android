package it.niedermann.owncloud.notes.persistence;

import android.accounts.NetworkErrorException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.fragment.ExceptionDialogFragment;
import it.niedermann.owncloud.notes.model.CloudNote;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.model.DBStatus;
import it.niedermann.owncloud.notes.model.ISyncCallback;
import it.niedermann.owncloud.notes.model.LocalAccount;
import it.niedermann.owncloud.notes.model.SyncResultStatus;
import it.niedermann.owncloud.notes.util.SSOUtil;
import it.niedermann.owncloud.notes.util.ServerResponse;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NOT_MODIFIED;

/**
 * Helps to synchronize the Database to the Server.
 */
public class NoteServerSyncHelper {

    private static final String TAG = NoteServerSyncHelper.class.getSimpleName();

    private static NoteServerSyncHelper instance;

    private final NotesDatabase db;
    private final Context context;

    // Track network connection changes using a BroadcastReceiver
    private boolean isSyncPossible = false;
    private boolean networkConnected = false;
    private String syncOnlyOnWifiKey;
    private boolean syncOnlyOnWifi;

    /**
     * @see <a href="https://stackoverflow.com/a/3104265">Do not make this a local variable.</a>
     */
    @SuppressWarnings("FieldCanBeLocal")
    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = (SharedPreferences prefs, String key) -> {
        if (syncOnlyOnWifiKey.equals(key)) {
            syncOnlyOnWifi = prefs.getBoolean(syncOnlyOnWifiKey, false);
            updateNetworkStatus();
        }
    };

    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateNetworkStatus();
            if (isSyncPossible() && SSOUtil.isConfigured(context)) {
                try {
                    scheduleSync(SingleAccountHelper.getCurrentSingleSignOnAccount(context), false);
                } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
                    Log.v(TAG, "Can not select current SingleSignOn account after network changed, do not sync.");
                }
            }
        }
    };

    // current state of the synchronization
    private final Map<String, Boolean> syncActive = new HashMap<>();
    private final Map<String, Boolean> syncScheduled = new HashMap<>();
    private final NotesClient notesClient;

    // list of callbacks for both parts of synchronziation
    private final Map<String, List<ISyncCallback>> callbacksPush = new HashMap<>();
    private final Map<String, List<ISyncCallback>> callbacksPull = new HashMap<>();

    private NoteServerSyncHelper(NotesDatabase db) {
        this.db = db;
        this.context = db.getContext();
        notesClient = new NotesClient(context.getApplicationContext());
        this.syncOnlyOnWifiKey = context.getApplicationContext().getResources().getString(R.string.pref_key_wifi_only);

        // Registers BroadcastReceiver to track network connection changes.
        context.getApplicationContext().registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.context.getApplicationContext());
        prefs.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        syncOnlyOnWifi = prefs.getBoolean(syncOnlyOnWifiKey, false);

        updateNetworkStatus();
    }

    /**
     * Get (or create) instance from NoteServerSyncHelper.
     * This has to be a singleton in order to realize correct registering and unregistering of
     * the BroadcastReceiver, which listens on changes of network connectivity.
     *
     * @param dbHelper NoteSQLiteOpenHelper
     * @return NoteServerSyncHelper
     */
    public static synchronized NoteServerSyncHelper getInstance(NotesDatabase dbHelper) {
        if (instance == null) {
            instance = new NoteServerSyncHelper(dbHelper);
        }
        return instance;
    }

    @Override
    protected void finalize() throws Throwable {
        context.getApplicationContext().unregisterReceiver(networkReceiver);
        super.finalize();
    }

    /**
     * Synchronization is only possible, if there is an active network connection.
     * <p>
     * This method respects the user preference "Sync on Wi-Fi only".
     * <p>
     * NoteServerSyncHelper observes changes in the network connection.
     * The current state can be retrieved with this method.
     *
     * @return true if sync is possible, otherwise false.
     */
    public boolean isSyncPossible() {
        return isSyncPossible;
    }

    public boolean isNetworkConnected() {
        return networkConnected;
    }

    public boolean isSyncOnlyOnWifi() {
        return syncOnlyOnWifi;
    }

    /**
     * Adds a callback method to the NoteServerSyncHelper for the synchronization part push local changes to the server.
     * All callbacks will be executed once the synchronization operations are done.
     * After execution the callback will be deleted, so it has to be added again if it shall be
     * executed the next time all synchronize operations are finished.
     *
     * @param callback Implementation of ISyncCallback, contains one method that shall be executed.
     */
    public void addCallbackPush(SingleSignOnAccount ssoAccount, ISyncCallback callback) {
        if (ssoAccount == null) {
            Log.i(TAG, "ssoAccount is null. Is this a local account?");
            callback.onScheduled();
            callback.onFinish();
        } else {
            if (!callbacksPush.containsKey(ssoAccount.name)) {
                callbacksPush.put(ssoAccount.name, new ArrayList<>());
            }
            Objects.requireNonNull(callbacksPush.get(ssoAccount.name)).add(callback);
        }
    }

    /**
     * Adds a callback method to the NoteServerSyncHelper for the synchronization part pull remote changes from the server.
     * All callbacks will be executed once the synchronization operations are done.
     * After execution the callback will be deleted, so it has to be added again if it shall be
     * executed the next time all synchronize operations are finished.
     *
     * @param callback Implementation of ISyncCallback, contains one method that shall be executed.
     */
    public void addCallbackPull(SingleSignOnAccount ssoAccount, ISyncCallback callback) {
        if (ssoAccount == null) {
            Log.i(TAG, "ssoAccount is null. Is this a local account?");
            callback.onScheduled();
            callback.onFinish();
        } else {
            if (!callbacksPull.containsKey(ssoAccount.name)) {
                callbacksPull.put(ssoAccount.name, new ArrayList<>());
            }
            Objects.requireNonNull(callbacksPull.get(ssoAccount.name)).add(callback);
        }
    }


    /**
     * Schedules a synchronization and start it directly, if the network is connected and no
     * synchronization is currently running.
     *
     * @param onlyLocalChanges Whether to only push local changes to the server or to also load the whole list of notes from the server.
     */
    public void scheduleSync(SingleSignOnAccount ssoAccount, boolean onlyLocalChanges) {
        if (ssoAccount == null) {
            Log.i(TAG, "ssoAccount is null. Is this a local account?");
        } else {
            if (syncActive.get(ssoAccount.name) == null) {
                syncActive.put(ssoAccount.name, false);
            }
            Log.d(TAG, "Sync requested (" + (onlyLocalChanges ? "onlyLocalChanges" : "full") + "; " + (Boolean.TRUE.equals(syncActive.get(ssoAccount.name)) ? "sync active" : "sync NOT active") + ") ...");
            if (isSyncPossible() && (!Boolean.TRUE.equals(syncActive.get(ssoAccount.name)) || onlyLocalChanges)) {
                Log.d(TAG, "... starting now");
                final LocalAccount account = db.getLocalAccountByAccountName(ssoAccount.name);
                if (account == null) {
                    Log.e(TAG, "LocalAccount for ssoAccount \"" + ssoAccount.name + "\" is null. Cannot synchronize.", new IllegalStateException());
                    return;
                }
                SyncTask syncTask = new SyncTask(account, ssoAccount, onlyLocalChanges);
                syncTask.addCallbacks(ssoAccount, callbacksPush.get(ssoAccount.name));
                callbacksPush.put(ssoAccount.name, new ArrayList<>());
                if (!onlyLocalChanges) {
                    syncTask.addCallbacks(ssoAccount, callbacksPull.get(ssoAccount.name));
                    callbacksPull.put(ssoAccount.name, new ArrayList<>());
                }
                syncTask.execute();
            } else if (!onlyLocalChanges) {
                Log.d(TAG, "... scheduled");
                syncScheduled.put(ssoAccount.name, true);
                if (callbacksPush.containsKey(ssoAccount.name) && callbacksPush.get(ssoAccount.name) != null) {
                    final List<ISyncCallback> callbacks = callbacksPush.get(ssoAccount.name);
                    if (callbacks != null) {
                        for (ISyncCallback callback : callbacks) {
                            callback.onScheduled();
                        }
                    } else {
                        Log.w(TAG, "List of push-callbacks was set for account \"" + ssoAccount.name + "\" but it was null");
                    }
                }
            } else {
                Log.d(TAG, "... do nothing");
                if (callbacksPush.containsKey(ssoAccount.name) && callbacksPush.get(ssoAccount.name) != null) {
                    final List<ISyncCallback> callbacks = callbacksPush.get(ssoAccount.name);
                    if (callbacks != null) {
                        for (ISyncCallback callback : callbacks) {
                            callback.onScheduled();
                        }
                    } else {
                        Log.w(TAG, "List of push-callbacks was set for account \"" + ssoAccount.name + "\" but it was null");
                    }
                }
            }
        }
    }

    private void updateNetworkStatus() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connMgr == null) {
                throw new NetworkErrorException("ConnectivityManager is null");
            }

            NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();

            if (activeInfo == null) {
                throw new NetworkErrorException("NetworkInfo is null");
            }

            if (activeInfo.isConnected()) {
                networkConnected = true;

                NetworkInfo networkInfo = connMgr.getNetworkInfo((ConnectivityManager.TYPE_WIFI));

                if (networkInfo == null) {
                    throw new NetworkErrorException("connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI) is null");
                }

                isSyncPossible = !syncOnlyOnWifi || networkInfo.isConnected();

                if (isSyncPossible) {
                    Log.d(TAG, "Network connection established.");
                } else {
                    Log.d(TAG, "Network connected, but not used because only synced on wifi.");
                }
            } else {
                networkConnected = false;
                isSyncPossible = false;
                Log.d(TAG, "No network connection.");
            }
        } catch (NetworkErrorException e) {
            e.printStackTrace();
            networkConnected = false;
            isSyncPossible = false;
        }
    }

    /**
     * SyncTask is an AsyncTask which performs the synchronization in a background thread.
     * Synchronization consists of two parts: pushLocalChanges and pullRemoteChanges.
     */
    private class SyncTask extends AsyncTask<Void, Void, SyncResultStatus> {
        @NonNull
        private final LocalAccount localAccount;
        private final SingleSignOnAccount ssoAccount;
        private final boolean onlyLocalChanges;
        @NonNull
        private final Map<String, List<ISyncCallback>> callbacks = new HashMap<>();
        @NonNull
        private final ArrayList<Throwable> exceptions = new ArrayList<>();

        SyncTask(@NonNull LocalAccount localAccount, @NonNull SingleSignOnAccount ssoAccount, boolean onlyLocalChanges) {
            this.localAccount = localAccount;
            this.ssoAccount = ssoAccount;
            this.onlyLocalChanges = onlyLocalChanges;
        }

        private void addCallbacks(SingleSignOnAccount ssoAccount, List<ISyncCallback> callbacks) {
            this.callbacks.put(ssoAccount.name, callbacks);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!syncScheduled.containsKey(ssoAccount.name) || syncScheduled.get(ssoAccount.name) == null) {
                syncScheduled.put(ssoAccount.name, false);
            }
            if (!onlyLocalChanges && Boolean.TRUE.equals(syncScheduled.get(ssoAccount.name))) {
                syncScheduled.put(ssoAccount.name, false);
            }
            syncActive.put(ssoAccount.name, true);
        }

        @Override
        protected SyncResultStatus doInBackground(Void... voids) {
            Log.i(TAG, "STARTING SYNCHRONIZATION");
            final SyncResultStatus status = new SyncResultStatus();
            status.pushSuccessful = pushLocalChanges();
            if (!onlyLocalChanges) {
                status.pullSuccessful = pullRemoteChanges();
            }
            Log.i(TAG, "SYNCHRONIZATION FINISHED");
            return status;
        }

        /**
         * Push local changes: for each locally created/edited/deleted Note, use NotesClient in order to push the changed to the server.
         */
        private boolean pushLocalChanges() {
            Log.d(TAG, "pushLocalChanges()");

            boolean success = true;
            List<DBNote> notes = db.getLocalModifiedNotes(localAccount.getId());
            for (DBNote note : notes) {
                Log.d(TAG, "   Process Local Note: " + note);
                try {
                    CloudNote remoteNote;
                    switch (note.getStatus()) {
                        case LOCAL_EDITED:
                            Log.v(TAG, "   ...create/edit");
                            if (note.getRemoteId() > 0) {
                                Log.v(TAG, "   ...Note has remoteId -> try to edit");
                                try {
                                    remoteNote = notesClient.editNote(ssoAccount, note).getNote();
                                } catch (NextcloudHttpRequestFailedException e) {
                                    if (e.getStatusCode() == HTTP_NOT_FOUND) {
                                        Log.v(TAG, "   ...Note does no longer exist on server -> recreate");
                                        remoteNote = notesClient.createNote(ssoAccount, note).getNote();
                                    } else {
                                        throw e;
                                    }
                                }
                            } else {
                                Log.v(TAG, "   ...Note does not have a remoteId yet -> create");
                                remoteNote = notesClient.createNote(ssoAccount, note).getNote();
                            }
                            // Please note, that db.updateNote() realizes an optimistic conflict resolution, which is required for parallel changes of this Note from the UI.
                            db.updateNote(note.getId(), remoteNote, note);
                            break;
                        case LOCAL_DELETED:
                            if (note.getRemoteId() > 0) {
                                Log.v(TAG, "   ...delete (from server and local)");
                                try {
                                    notesClient.deleteNote(ssoAccount, note.getRemoteId());
                                } catch (NextcloudHttpRequestFailedException e) {
                                    if (e.getStatusCode() == HTTP_NOT_FOUND) {
                                        Log.v(TAG, "   ...delete (note has already been deleted remotely)");
                                    } else {
                                        throw e;
                                    }
                                }
                            } else {
                                Log.v(TAG, "   ...delete (only local, since it has never been synchronized)");
                            }
                            // Please note, that db.deleteNote() realizes an optimistic conflict resolution, which is required for parallel changes of this Note from the UI.
                            db.deleteNote(note.getId(), DBStatus.LOCAL_DELETED);
                            break;
                        default:
                            throw new IllegalStateException("Unknown State of Note: " + note);
                    }
                } catch (NextcloudHttpRequestFailedException e) {
                    if (e.getStatusCode() == HTTP_NOT_MODIFIED) {
                        Log.d(TAG, "Server returned HTTP Status Code 304 - Not Modified");
                    } else {
                        exceptions.add(e);
                        success = false;
                    }
                } catch (Exception e) {
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
                Map<Long, Long> idMap = db.getIdMap(localAccount.getId());
                ServerResponse.NotesResponse response = notesClient.getNotes(ssoAccount, localAccount.getModified(), localAccount.getEtag());
                List<CloudNote> remoteNotes = response.getNotes();
                Set<Long> remoteIDs = new HashSet<>();
                // pull remote changes: update or create each remote note
                for (CloudNote remoteNote : remoteNotes) {
                    Log.v(TAG, "   Process Remote Note: " + remoteNote);
                    remoteIDs.add(remoteNote.getRemoteId());
                    if (remoteNote.getModified() == null) {
                        Log.v(TAG, "   ... unchanged");
                    } else if (idMap.containsKey(remoteNote.getRemoteId())) {
                        Log.v(TAG, "   ... found -> Update");
                        Long remoteId = idMap.get(remoteNote.getRemoteId());
                        if (remoteId != null) {
                            db.updateNote(remoteId, remoteNote, null);
                        } else {
                            Log.e(TAG, "Tried to update note from server, but remoteId of note is null. " + remoteNote);
                        }
                    } else {
                        Log.v(TAG, "   ... create");
                        db.addNote(localAccount.getId(), remoteNote);
                    }
                }
                Log.d(TAG, "   Remove remotely deleted Notes (only those without local changes)");
                // remove remotely deleted notes (only those without local changes)
                for (Map.Entry<Long, Long> entry : idMap.entrySet()) {
                    if (!remoteIDs.contains(entry.getKey())) {
                        Log.v(TAG, "   ... remove " + entry.getValue());
                        db.deleteNote(entry.getValue(), DBStatus.VOID);
                    }
                }

                // update ETag and Last-Modified in order to reduce size of next response
                localAccount.setETag(response.getETag());
                localAccount.setModified(response.getLastModified());
                db.updateETag(localAccount.getId(), localAccount.getEtag());
                db.updateModified(localAccount.getId(), localAccount.getModified());
                return true;
            } catch (NextcloudHttpRequestFailedException e) {
                Log.d(TAG, "Server returned HTTP Status Code " + e.getStatusCode() + " - " + e.getMessage());
                if (e.getStatusCode() == HTTP_NOT_MODIFIED) {
                    return true;
                } else {
                    exceptions.add(e);
                    return false;
                }
            } catch (Exception e) {
                exceptions.add(e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(SyncResultStatus status) {
            super.onPostExecute(status);
            for (Throwable e : exceptions) {
                Log.e(TAG, e.getMessage(), e);
            }
            if (!status.pullSuccessful || !status.pushSuccessful) {
                if (context instanceof ViewProvider && context instanceof AppCompatActivity) {
                    Snackbar.make(((ViewProvider) context).getView(), R.string.error_synchronization, Snackbar.LENGTH_LONG)
                            .setAction(R.string.simple_more, v -> ExceptionDialogFragment.newInstance(exceptions)
                                    .show(((AppCompatActivity) context).getSupportFragmentManager(), ExceptionDialogFragment.class.getCanonicalName()))
                            .show();
                }
            }
            syncActive.put(ssoAccount.name, false);
            // notify callbacks
            if (callbacks.containsKey(ssoAccount.name) && callbacks.get(ssoAccount.name) != null) {
                for (ISyncCallback callback : Objects.requireNonNull(callbacks.get(ssoAccount.name))) {
                    callback.onFinish();
                }
            }
            db.notifyNotesChanged();
            db.updateDynamicShortcuts(localAccount.getId());
            // start next sync if scheduled meanwhile
            if (syncScheduled.containsKey(ssoAccount.name) && syncScheduled.get(ssoAccount.name) != null && Boolean.TRUE.equals(syncScheduled.get(ssoAccount.name))) {
                scheduleSync(ssoAccount, false);
            }
        }
    }

    public interface ViewProvider {
        View getView();
    }
}
