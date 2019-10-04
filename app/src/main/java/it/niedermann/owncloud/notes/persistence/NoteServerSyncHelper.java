package it.niedermann.owncloud.notes.persistence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.activity.AccountActivity;
import it.niedermann.owncloud.notes.model.CloudNote;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.model.DBStatus;
import it.niedermann.owncloud.notes.model.LocalAccount;
import it.niedermann.owncloud.notes.util.ICallback;
import it.niedermann.owncloud.notes.util.NotesClient;
import it.niedermann.owncloud.notes.util.NotesClientUtil.LoginStatus;
import it.niedermann.owncloud.notes.util.ServerResponse;

/**
 * Helps to synchronize the Database to the Server.
 */
public class NoteServerSyncHelper {

    private static NoteServerSyncHelper instance;

    /**
     * Get (or create) instance from NoteServerSyncHelper.
     * This has to be a singleton in order to realize correct registering and unregistering of
     * the BroadcastReceiver, which listens on changes of network connectivity.
     *
     * @param dbHelper NoteSQLiteOpenHelper
     * @return NoteServerSyncHelper
     */
    public static synchronized NoteServerSyncHelper getInstance(NoteSQLiteOpenHelper dbHelper) {
        if (instance == null) {
            instance = new NoteServerSyncHelper(dbHelper);
        }
        return instance;
    }

    private final NoteSQLiteOpenHelper dbHelper;
    private final Context appContext;
    private LocalAccount localAccount;

    // Track network connection changes using a BroadcastReceiver
    private boolean networkConnected = false;
    private String syncOnlyOnWifiKey;
    private boolean syncOnlyOnWifi;

    /**
     * @see <a href="https://stackoverflow.com/a/3104265">Do not make this a local variable.</a>
     */
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
            if (isSyncPossible()) {
                scheduleSync(false);
            }
        }
    };

    // current state of the synchronization
    private boolean syncActive = false;
    private boolean syncScheduled = false;
    private NotesClient notesClient;

    // list of callbacks for both parts of synchronziation
    private List<ICallback> callbacksPush = new ArrayList<>();
    private List<ICallback> callbacksPull = new ArrayList<>();


    private NoteServerSyncHelper(NoteSQLiteOpenHelper db) {
        this.dbHelper = db;
        this.appContext = db.getContext().getApplicationContext();
        try {
            this.localAccount = db.getLocalAccountByAccountName(SingleAccountHelper.getCurrentSingleSignOnAccount(appContext).name);
            Log.v("Notes", "NextcloudRequest account: " + localAccount);
        } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
            e.printStackTrace();
        }
        this.syncOnlyOnWifiKey = appContext.getResources().getString(R.string.pref_key_wifi_only);

        // Registers BroadcastReceiver to track network connection changes.
        appContext.registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        notesClient = new NotesClient(appContext);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.appContext);
        prefs.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        syncOnlyOnWifi = prefs.getBoolean(syncOnlyOnWifiKey, false);

        updateNetworkStatus();
    }

    @Override
    protected void finalize() throws Throwable {
        appContext.unregisterReceiver(networkReceiver);
        super.finalize();
    }

    public static boolean isConfigured(Context context) {
        try {
            SingleAccountHelper.getCurrentSingleSignOnAccount(context);
            return true;
        } catch (NextcloudFilesAppAccountNotFoundException e) {
            return false;
        } catch (NoCurrentAccountSelectedException e) {
            return false;
        }
    }

    /**
     * Synchronization is only possible, if there is an active network connection and
     * SingleSignOn is available
     * NoteServerSyncHelper observes changes in the network connection.
     * The current state can be retrieved with this method.
     *
     * @return true if sync is possible, otherwise false.
     */
    public boolean isSyncPossible() {
        return networkConnected && isConfigured(appContext);
    }

    /**
     * Adds a callback method to the NoteServerSyncHelper for the synchronization part push local changes to the server.
     * All callbacks will be executed once the synchronization operations are done.
     * After execution the callback will be deleted, so it has to be added again if it shall be
     * executed the next time all synchronize operations are finished.
     *
     * @param callback Implementation of ICallback, contains one method that shall be executed.
     */
    public void addCallbackPush(ICallback callback) {
        callbacksPush.add(callback);
    }

    /**
     * Adds a callback method to the NoteServerSyncHelper for the synchronization part pull remote changes from the server.
     * All callbacks will be executed once the synchronization operations are done.
     * After execution the callback will be deleted, so it has to be added again if it shall be
     * executed the next time all synchronize operations are finished.
     *
     * @param callback Implementation of ICallback, contains one method that shall be executed.
     */
    public void addCallbackPull(ICallback callback) {
        callbacksPull.add(callback);
    }


    /**
     * Schedules a synchronization and start it directly, if the network is connected and no
     * synchronization is currently running.
     *
     * @param onlyLocalChanges Whether to only push local changes to the server or to also load the whole list of notes from the server.
     */
    public void scheduleSync(boolean onlyLocalChanges) {
        Log.d(getClass().getSimpleName(), "Sync requested (" + (onlyLocalChanges ? "onlyLocalChanges" : "full") + "; " + (syncActive ? "sync active" : "sync NOT active") + ") ...");
        if (isSyncPossible() && (!syncActive || onlyLocalChanges)) {
            Log.d(getClass().getSimpleName(), "... starting now");
            SyncTask syncTask = new SyncTask(onlyLocalChanges);
            syncTask.addCallbacks(callbacksPush);
            callbacksPush = new ArrayList<>();
            if (!onlyLocalChanges) {
                syncTask.addCallbacks(callbacksPull);
                callbacksPull = new ArrayList<>();
            }
            syncTask.execute();
        } else if (!onlyLocalChanges) {
            Log.d(getClass().getSimpleName(), "... scheduled");
            syncScheduled = true;
            for (ICallback callback : callbacksPush) {
                callback.onScheduled();
            }
        } else {
            Log.d(getClass().getSimpleName(), "... do nothing");
            for (ICallback callback : callbacksPush) {
                callback.onScheduled();
            }
        }
    }

    private void updateNetworkStatus() {
        ConnectivityManager connMgr = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();

        if (activeInfo != null && activeInfo.isConnected()) {
            networkConnected =
                    !syncOnlyOnWifi || ((ConnectivityManager) appContext
                            .getSystemService(Context.CONNECTIVITY_SERVICE))
                            .getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();

            if (networkConnected) {
                Log.d(NoteServerSyncHelper.class.getSimpleName(), "Network connection established.");
            } else {
                Log.d(NoteServerSyncHelper.class.getSimpleName(), "Network connected, but not used because only synced on wifi.");
            }
        } else {
            networkConnected = false;
            Log.d(NoteServerSyncHelper.class.getSimpleName(), "No network connection.");
        }
    }

    /**
     * SyncTask is an AsyncTask which performs the synchronization in a background thread.
     * Synchronization consists of two parts: pushLocalChanges and pullRemoteChanges.
     */
    private class SyncTask extends AsyncTask<Void, Void, LoginStatus> {
        private final boolean onlyLocalChanges;
        private final List<ICallback> callbacks = new ArrayList<>();
        private List<Throwable> exceptions = new ArrayList<>();

        public SyncTask(boolean onlyLocalChanges) {
            this.onlyLocalChanges = onlyLocalChanges;
        }

        public void addCallbacks(List<ICallback> callbacks) {
            this.callbacks.addAll(callbacks);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!onlyLocalChanges && syncScheduled) {
                syncScheduled = false;
            }
            syncActive = true;
        }

        @Override
        protected LoginStatus doInBackground(Void... voids) {
            Log.i(getClass().getSimpleName(), "STARTING SYNCHRONIZATION");
            //dbHelper.debugPrintFullDB();
            LoginStatus status = LoginStatus.OK;
            pushLocalChanges();
            if (!onlyLocalChanges) {
                status = pullRemoteChanges();
            }
            //dbHelper.debugPrintFullDB();
            Log.i(getClass().getSimpleName(), "SYNCHRONIZATION FINISHED");
            return status;
        }

        /**
         * Push local changes: for each locally created/edited/deleted Note, use NotesClient in order to push the changed to the server.
         */
        private void pushLocalChanges() {
            Log.d(getClass().getSimpleName(), "pushLocalChanges()");
            List<DBNote> notes = dbHelper.getLocalModifiedNotes(localAccount.getId());
            for (DBNote note : notes) {
                Log.d(getClass().getSimpleName(), "   Process Local Note: " + note);
                try {
                    CloudNote remoteNote = null;
                    switch (note.getStatus()) {
                        case LOCAL_EDITED:
                            Log.v(getClass().getSimpleName(), "   ...create/edit");
                            // if note is not new, try to edit it.
                            if (note.getRemoteId() > 0) {
                                Log.v(getClass().getSimpleName(), "   ...try to edit");
                                remoteNote = notesClient.editNote(note).getNote();
                            }
                            // However, the note may be deleted on the server meanwhile; or was never synchronized -> (re)create
                            // Please note, thas dbHelper.updateNote() realizes an optimistic conflict resolution, which is required for parallel changes of this Note from the UI.
                            if (remoteNote == null) {
                                Log.v(getClass().getSimpleName(), "   ...Note does not exist on server -> (re)create");
                                remoteNote = notesClient.createNote(note).getNote();
                            }
                            dbHelper.updateNote(note.getId(), remoteNote, note);
                            break;
                        case LOCAL_DELETED:
                            if (note.getRemoteId() > 0) {
                                Log.v(getClass().getSimpleName(), "   ...delete (from server and local)");
                                notesClient.deleteNote(note.getRemoteId());
                            } else {
                                Log.v(getClass().getSimpleName(), "   ...delete (only local, since it was not synchronized)");
                            }
                            // Please note, thas dbHelper.deleteNote() realizes an optimistic conflict resolution, which is required for parallel changes of this Note from the UI.
                            dbHelper.deleteNote(note.getId(), DBStatus.LOCAL_DELETED);
                            break;
                        default:
                            throw new IllegalStateException("Unknown State of Note: " + note);
                    }
                } catch (JSONException e) {
                    Log.e(getClass().getSimpleName(), "Exception", e);
                    exceptions.add(e);
                }
            }
        }

        /**
         * Pull remote Changes: update or create each remote note (if local pendant has no changes) and remove remotely deleted notes.
         */
        private LoginStatus pullRemoteChanges() {
            Log.d(getClass().getSimpleName(), "pullRemoteChanges()");
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
            String lastETag = preferences.getString(AccountActivity.SETTINGS_KEY_ETAG, null);
            long lastModified = preferences.getLong(AccountActivity.SETTINGS_KEY_LAST_MODIFIED, 0);
            LoginStatus status;
            try {
                Map<Long, Long> idMap = dbHelper.getIdMap();
                ServerResponse.NotesResponse response = notesClient.getNotes(lastModified, lastETag);
                List<CloudNote> remoteNotes = response.getNotes();
                Set<Long> remoteIDs = new HashSet<>();
                // pull remote changes: update or create each remote note
                for (CloudNote remoteNote : remoteNotes) {
                    Log.v(getClass().getSimpleName(), "   Process Remote Note: " + remoteNote);
                    remoteIDs.add(remoteNote.getRemoteId());
                    if (remoteNote.getModified() == null) {
                        Log.v(getClass().getSimpleName(), "   ... unchanged");
                    } else if (idMap.containsKey(remoteNote.getRemoteId())) {
                        Log.v(getClass().getSimpleName(), "   ... found -> Update");
                        dbHelper.updateNote(idMap.get(remoteNote.getRemoteId()), remoteNote, null);
                    } else {
                        Log.v(getClass().getSimpleName(), "   ... create");
                        dbHelper.addNote(localAccount.getId(), remoteNote);
                    }
                }
                Log.d(getClass().getSimpleName(), "   Remove remotely deleted Notes (only those without local changes)");
                // remove remotely deleted notes (only those without local changes)
                for (Map.Entry<Long, Long> entry : idMap.entrySet()) {
                    if (!remoteIDs.contains(entry.getKey())) {
                        Log.v(getClass().getSimpleName(), "   ... remove " + entry.getValue());
                        dbHelper.deleteNote(entry.getValue(), DBStatus.VOID);
                    }
                }

                // update ETag and Last-Modified in order to reduce size of next response
                SharedPreferences.Editor editor = preferences.edit();
                String etag = response.getETag();
                if (etag != null && !etag.isEmpty()) {
                    editor.putString(AccountActivity.SETTINGS_KEY_ETAG, etag);
                } else {
                    editor.remove(AccountActivity.SETTINGS_KEY_ETAG);
                }
                long modified = response.getLastModified();
                if (modified != 0) {
                    editor.putLong(AccountActivity.SETTINGS_KEY_LAST_MODIFIED, modified);
                } else {
                    editor.remove(AccountActivity.SETTINGS_KEY_LAST_MODIFIED);
                }
                editor.apply();
                return LoginStatus.OK;
            } catch (JSONException e) {
                Log.e(getClass().getSimpleName(), "Exception", e);
                exceptions.add(e);
                status = LoginStatus.JSON_FAILED;
            }
            return status;
        }

        @Override
        protected void onPostExecute(LoginStatus status) {
            super.onPostExecute(status);
            if (status != LoginStatus.OK) {
                Toast.makeText(appContext, appContext.getString(R.string.error_sync, appContext.getString(status.str)), Toast.LENGTH_LONG).show();
                for (Throwable e : exceptions) {
                    Toast.makeText(appContext, e.getClass().getName() + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            syncActive = false;
            // notify callbacks
            for (ICallback callback : callbacks) {
                callback.onFinish();
            }
            dbHelper.notifyNotesChanged();
            // start next sync if scheduled meanwhile
            if (syncScheduled) {
                scheduleSync(false);
            }
        }
    }
}
