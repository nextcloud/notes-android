package it.niedermann.owncloud.notes.persistence;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.Snackbar;
import com.nextcloud.android.sso.exceptions.NextcloudApiNotRespondingException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotSupportedException;
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;

import org.json.JSONException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.CloudNote;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.model.DBStatus;
import it.niedermann.owncloud.notes.model.LocalAccount;
import it.niedermann.owncloud.notes.model.LoginStatus;
import it.niedermann.owncloud.notes.util.ExceptionUtil;
import it.niedermann.owncloud.notes.util.ICallback;
import it.niedermann.owncloud.notes.util.ServerResponse;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Helps to synchronize the Database to the Server.
 */
public class NoteServerSyncHelper {

    private static final String TAG = NoteServerSyncHelper.class.getSimpleName();

    private static NoteServerSyncHelper instance;

    private NoteSQLiteOpenHelper dbHelper;
    private Context context;
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
        this.context = db.getContext();
        try {
            updateAccount();
        } catch (NextcloudFilesAppAccountNotFoundException e) {
            e.printStackTrace();
        }
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
    public static synchronized NoteServerSyncHelper getInstance(NoteSQLiteOpenHelper dbHelper) {
        if (instance == null) {
            instance = new NoteServerSyncHelper(dbHelper);
        }
        return instance;
    }

    public void updateAccount() throws NextcloudFilesAppAccountNotFoundException {
        try {
            this.localAccount = dbHelper.getLocalAccountByAccountName(SingleAccountHelper.getCurrentSingleSignOnAccount(context.getApplicationContext()).name);
            if (notesClient == null) {
                if (this.localAccount != null) {
                    notesClient = new NotesClient(context.getApplicationContext());
                }
            } else {
                notesClient.updateAccount();
            }
            Log.v(TAG, "NextcloudRequest account: " + localAccount);
        } catch (NoCurrentAccountSelectedException e) {
            e.printStackTrace();
        }
        Log.v(TAG, "Reinstanziation NotesClient because of SSO acc changed");
    }

    @Override
    protected void finalize() throws Throwable {
        context.getApplicationContext().unregisterReceiver(networkReceiver);
        super.finalize();
    }

    private static boolean isConfigured(Context context) {
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
        return networkConnected && isConfigured(context.getApplicationContext());
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
        Log.d(TAG, "Sync requested (" + (onlyLocalChanges ? "onlyLocalChanges" : "full") + "; " + (syncActive ? "sync active" : "sync NOT active") + ") ...");
        if (isSyncPossible() && (!syncActive || onlyLocalChanges)) {
            Log.d(TAG, "... starting now");
            SyncTask syncTask = new SyncTask(onlyLocalChanges);
            syncTask.addCallbacks(callbacksPush);
            callbacksPush = new ArrayList<>();
            if (!onlyLocalChanges) {
                syncTask.addCallbacks(callbacksPull);
                callbacksPull = new ArrayList<>();
            }
            syncTask.execute();
        } else if (!onlyLocalChanges) {
            Log.d(TAG, "... scheduled");
            syncScheduled = true;
            for (ICallback callback : callbacksPush) {
                callback.onScheduled();
            }
        } else {
            Log.d(TAG, "... do nothing");
            for (ICallback callback : callbacksPush) {
                callback.onScheduled();
            }
        }
    }

    private void updateNetworkStatus() {
        ConnectivityManager connMgr = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();

        if (activeInfo != null && activeInfo.isConnected()) {
            networkConnected =
                    !syncOnlyOnWifi || ((ConnectivityManager) context.getApplicationContext()
                            .getSystemService(Context.CONNECTIVITY_SERVICE))
                            .getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();

            if (networkConnected) {
                Log.d(TAG, "Network connection established.");
            } else {
                Log.d(TAG, "Network connected, but not used because only synced on wifi.");
            }
        } else {
            networkConnected = false;
            Log.d(TAG, "No network connection.");
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

        SyncTask(boolean onlyLocalChanges) {
            this.onlyLocalChanges = onlyLocalChanges;
        }

        void addCallbacks(List<ICallback> callbacks) {
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
            Log.i(TAG, "STARTING SYNCHRONIZATION");
            //dbHelper.debugPrintFullDB();
            LoginStatus status = LoginStatus.OK;
            pushLocalChanges();
            if (!onlyLocalChanges) {
                status = pullRemoteChanges();
            }
            //dbHelper.debugPrintFullDB();
            Log.i(TAG, "SYNCHRONIZATION FINISHED");
            return status;
        }

        /**
         * Push local changes: for each locally created/edited/deleted Note, use NotesClient in order to push the changed to the server.
         */
        private void pushLocalChanges() {
            if (localAccount == null) {
                return;
            }
            Log.d(TAG, "pushLocalChanges()");
            List<DBNote> notes = dbHelper.getLocalModifiedNotes(localAccount.getId());
            for (DBNote note : notes) {
                Log.d(TAG, "   Process Local Note: " + note);
                try {
                    CloudNote remoteNote = null;
                    switch (note.getStatus()) {
                        case LOCAL_EDITED:
                            Log.v(TAG, "   ...create/edit");
                            // if note is not new, try to edit it.
                            if (note.getRemoteId() > 0) {
                                Log.v(TAG, "   ...try to edit");
                                remoteNote = notesClient.editNote(note).getNote();
                            }
                            // However, the note may be deleted on the server meanwhile; or was never synchronized -> (re)create
                            // Please note, thas dbHelper.updateNote() realizes an optimistic conflict resolution, which is required for parallel changes of this Note from the UI.
                            if (remoteNote == null) {
                                Log.v(TAG, "   ...Note does not exist on server -> (re)create");
                                remoteNote = notesClient.createNote(note).getNote();
                            }
                            dbHelper.updateNote(note.getId(), remoteNote, note);
                            break;
                        case LOCAL_DELETED:
                            if (note.getRemoteId() > 0) {
                                Log.v(TAG, "   ...delete (from server and local)");
                                notesClient.deleteNote(note.getRemoteId());
                            } else {
                                Log.v(TAG, "   ...delete (only local, since it was not synchronized)");
                            }
                            // Please note, thas dbHelper.deleteNote() realizes an optimistic conflict resolution, which is required for parallel changes of this Note from the UI.
                            dbHelper.deleteNote(note.getId(), DBStatus.LOCAL_DELETED);
                            break;
                        default:
                            throw new IllegalStateException("Unknown State of Note: " + note);
                    }
                } catch (NextcloudHttpRequestFailedException e) {
                    if (e.getStatusCode() == 304) {
                        Log.d(TAG, "Server returned HTTP Status Code 304 - Not Modified");
                    } else {
                        exceptions.add(e);
                    }
                } catch (Exception e) {
                    exceptions.add(e);
                }
            }
        }

        /**
         * Pull remote Changes: update or create each remote note (if local pendant has no changes) and remove remotely deleted notes.
         */
        private LoginStatus pullRemoteChanges() {
            if (localAccount == null) {
                return LoginStatus.NO_NETWORK;
            }
            Log.d(TAG, "pullRemoteChanges() for account " + localAccount.getAccountName());
            LoginStatus status;
            try {
                Map<Long, Long> idMap = dbHelper.getIdMap(localAccount.getId());
                ServerResponse.NotesResponse response = notesClient.getNotes(localAccount.getModified(), localAccount.getEtag());
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
                        dbHelper.updateNote(idMap.get(remoteNote.getRemoteId()), remoteNote, null);
                    } else {
                        Log.v(TAG, "   ... create");
                        dbHelper.addNote(localAccount.getId(), remoteNote);
                    }
                }
                Log.d(TAG, "   Remove remotely deleted Notes (only those without local changes)");
                // remove remotely deleted notes (only those without local changes)
                for (Map.Entry<Long, Long> entry : idMap.entrySet()) {
                    if (!remoteIDs.contains(entry.getKey())) {
                        Log.v(TAG, "   ... remove " + entry.getValue());
                        dbHelper.deleteNote(entry.getValue(), DBStatus.VOID);
                    }
                }

                // update ETag and Last-Modified in order to reduce size of next response
                localAccount.setETag(response.getETag());
                localAccount.setModified(response.getLastModified());
                dbHelper.updateETag(localAccount.getId(), localAccount.getEtag());
                dbHelper.updateModified(localAccount.getId(), localAccount.getModified());
                return LoginStatus.OK;
            } catch (JSONException | NullPointerException e) {
                exceptions.add(e);
                status = LoginStatus.JSON_FAILED;
            } catch (NextcloudHttpRequestFailedException e) {
                Log.d(TAG, "Server returned HTTP Status Code " + e.getStatusCode() + " - " + e.getMessage());
                if (e.getStatusCode() == 304) {
                    return LoginStatus.OK;
                } else {
                    exceptions.add(e);
                    return LoginStatus.JSON_FAILED;
                }
            } catch (NextcloudFilesAppNotSupportedException e) {
                exceptions.add(e);
                return LoginStatus.FILES_APP_VERSION_TOO_OLD;
            } catch (NextcloudApiNotRespondingException e) {
                exceptions.add(e);
                return LoginStatus.PROBLEM_WITH_FILES_APP;
            } catch (SocketTimeoutException | ConnectException e) {
                exceptions.add(e);
                return LoginStatus.NO_NETWORK;
            } catch (Exception e) {
                exceptions.add(e);
                return LoginStatus.UNKNOWN_PROBLEM;
            }
            return status;
        }

        @Override
        protected void onPostExecute(LoginStatus status) {
            super.onPostExecute(status);
            if (status != LoginStatus.OK) {
                for (Throwable e : exceptions) {
                    Log.e(TAG, e.getMessage(), e);
                }
                String statusMessage = context.getApplicationContext().getString(R.string.error_sync, context.getApplicationContext().getString(status.str));
                if (context instanceof ViewProvider && context instanceof Activity) {
                    Snackbar.make(((ViewProvider) context).getView(), statusMessage, Snackbar.LENGTH_LONG)
                            .setAction(R.string.simple_more, v -> {
                                String debugInfos = ExceptionUtil.getDebugInfos((Activity) context, exceptions);
                                new AlertDialog.Builder(context, R.style.ncAlertDialog)
                                        .setTitle(statusMessage)
                                        .setMessage(debugInfos)
                                        .setPositiveButton(android.R.string.copy, (a, b) -> {
                                            final ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                                            ClipData clipData = ClipData.newPlainText(context.getString(R.string.simple_exception), "```\n" + debugInfos + "\n```");
                                            clipboardManager.setPrimaryClip(clipData);
                                            Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
                                            a.dismiss();
                                        })
                                        .setNegativeButton(R.string.simple_close, null)
                                        .create()
                                        .show();
                            })
                            .show();
                } else {
                    Toast.makeText(context.getApplicationContext(), statusMessage, Toast.LENGTH_LONG).show();
                    for (Throwable e : exceptions) {
                        Toast.makeText(context.getApplicationContext(), e.getClass().getName() + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
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

    public interface ViewProvider {
        View getView();
    }
}
