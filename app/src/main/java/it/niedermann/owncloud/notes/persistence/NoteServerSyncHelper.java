package it.niedermann.owncloud.notes.persistence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.LongSparseArray;
import android.widget.Toast;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.activity.SettingsActivity;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.model.DBStatus;
import it.niedermann.owncloud.notes.model.OwnCloudNote;
import it.niedermann.owncloud.notes.util.ICallback;
import it.niedermann.owncloud.notes.util.NotesClient;
import it.niedermann.owncloud.notes.util.NotesClientUtil.LoginStatus;

/**
 * Helps to synchronize the Database to the Server.
 * <p/>
 * Created by stefan on 20.09.15.
 */
public class NoteServerSyncHelper {

    private static NoteServerSyncHelper instance;

    /**
     * Get (or create) instance from NoteServerSyncHelper.
     * This has to be a singleton in order to realize correct registering and unregistering of
     * the BroadcastReceiver, which listens on changes of network connectivity.
     * @param dbHelper NoteSQLiteOpenHelper
     * @return NoteServerSyncHelper
     */
    public static synchronized NoteServerSyncHelper getInstance(NoteSQLiteOpenHelper dbHelper) {
        if(instance==null) {
            instance = new NoteServerSyncHelper(dbHelper);
        }
        return instance;
    }

    private NoteSQLiteOpenHelper dbHelper;
    private Context appContext;

    // Track network connection changes using a BroadcastReceiver
    private boolean networkConnected = false;
    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connMgr = (ConnectivityManager)appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
            if(activeInfo != null && activeInfo.isConnected()) {
                Log.d(NoteServerSyncHelper.class.getSimpleName(), "Network connection established.");
                networkConnected = true;
                scheduleSync(false);
            } else {
                networkConnected = false;
                Log.d(NoteServerSyncHelper.class.getSimpleName(), "No network connection.");
            }
        }
    };

    // current state of the synchronization
    private boolean syncActive = false;
    private boolean syncScheduled = false;


    private Handler handler = null;
    private List<ICallback> callbacks = new ArrayList<>();

    private NoteServerSyncHelper(NoteSQLiteOpenHelper db) {
        this.dbHelper = db;
        this.appContext = db.getContext().getApplicationContext();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                for (ICallback callback : callbacks) {
                    callback.onFinish();
                }
                callbacks.clear();
            }
        };
        // Registers BroadcastReceiver to track network connection changes.
        appContext.registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void finalize() throws Throwable {
        appContext.unregisterReceiver(networkReceiver);
        super.finalize();
    }

    /**
     * Adds a callback method to the NoteServerSyncHelper.
     * All callbacks will be executed once all synchronize operations are done.
     * After execution the callback will be deleted, so it has to be added again if it shall be
     * executed the next time all synchronize operations are finished.
     *
     * @param callback Implementation of ICallback, contains one method that shall be executed.
     */
    @Deprecated
    public void addCallback(ICallback callback) {
        callbacks.add(callback);
    }


    /**
     * Schedules a synchronization and start it directly, if the network is connected and no
     * synchronization is currently running.
     * @param onlyLocalChanges Whether to only push local changes to the server or to also load the whole list of notes from the server.
     */
    public void scheduleSync(boolean onlyLocalChanges) {
        Log.d(getClass().getSimpleName(), "Sync requested ("+(onlyLocalChanges?"onlyLocalChanges":"full")+"; "+(networkConnected?"network connected":"network NOT connected")+", "+(syncActive?"sync active":"sync NOT active")+") ...");
        if(networkConnected && (!syncActive || onlyLocalChanges)) {
            Log.d(getClass().getSimpleName(), "... starting now");
            new SyncTask(onlyLocalChanges).execute();
        } else if(!onlyLocalChanges) {
            Log.d(getClass().getSimpleName(), "... scheduled");
            syncScheduled = true;
        } else {
            Log.d(getClass().getSimpleName(), "... do nothing");
        }
    }

    private class SyncTask extends AsyncTask<Void, Void, Void> {
        private final boolean onlyLocalChanges;
        private NotesClient client;

        public SyncTask(boolean onlyLocalChanges) {
            this.onlyLocalChanges = onlyLocalChanges;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(!onlyLocalChanges && syncScheduled) {
                syncScheduled = false;
            }
            syncActive = true;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            client = createNotesClient(); // recreate NoteClients on every sync in case the connection settings was changed
            Log.d(getClass().getSimpleName(), "STARTING SYNCHRONIZATION");
            dbHelper.debugPrintFullDB();
            pushLocalChanges();
            pullRemoteChanges();
            dbHelper.debugPrintFullDB();
            Log.d(getClass().getSimpleName(), "SYNCHRONIZATION FINISHED");
            return null;
        }

        /**
         * Push local changes: for each locally created/edited/deleted Note, use NotesClient in order to push the changed to the server.
         */
        private void pushLocalChanges() {
            Log.d(getClass().getSimpleName(), "pushLocalChanges()");
            List<DBNote> notes = dbHelper.getLocalModifiedNotes();
            for (DBNote note : notes) {
                Log.d(getClass().getSimpleName(), "   Process Local Note: "+note);
                try {
                    OwnCloudNote remoteNote=null;
                    switch(note.getStatus()) {
                        case LOCAL_EDITED:
                            Log.d(getClass().getSimpleName(), "   ...create/edit");
                            // if note is not new, try to edit it.
                            if (note.getRemoteId()>0) {
                                Log.d(getClass().getSimpleName(), "   ...try to edit");
                                remoteNote = client.editNote(note.getRemoteId(), note.getContent());
                            }
                            // However, the note may be deleted on the server meanwhile; or was never synchronized -> (re)create
                            if (remoteNote == null) {
                                Log.d(getClass().getSimpleName(), "   ...Note does not exist on server -> (re)create");
                                remoteNote = client.createNote(note.getContent());
                                dbHelper.updateNote(note.getId(), remoteNote, note);
                            } else {
                                dbHelper.updateNote(note.getId(), remoteNote, note);
                            }
                            break;
                        case LOCAL_DELETED:
                            if(note.getRemoteId()>0) {
                                Log.d(getClass().getSimpleName(), "   ...delete (from server and local)");
                                try {
                                    client.deleteNote(note.getRemoteId());
                                } catch (FileNotFoundException e) {
                                    Log.d(getClass().getSimpleName(), "   ...Note does not exist on server (anymore?) -> delete locally");
                                }
                            } else {
                                Log.d(getClass().getSimpleName(), "   ...delete (only local, since it was not synchronized)");
                            }
                            dbHelper.deleteNote(note.getId(), DBStatus.LOCAL_DELETED);
                            break;
                        default:
                            throw new IllegalStateException("Unknown State of Note: "+note);
                    }
                } catch (IOException | JSONException e) {
                    // FIXME Fehlerbehandlung sichtbar machen
                    e.printStackTrace();
                }
            }
        }

        /**
         * Pull remote Changes: update or create each remote note (if local pendant has no changes) and remove remotely deleted notes.
         */
        private void pullRemoteChanges() {
            Log.d(getClass().getSimpleName(), "pullRemoteChanges()");
            LoginStatus status = null;
            try {
                List<DBNote> localNotes = dbHelper.getNotes();
                Map<Long, Long> localIDmap = new HashMap<>();
                for (DBNote note : localNotes) {
                    localIDmap.put(note.getRemoteId(), note.getId());
                }
                List<OwnCloudNote> remoteNotes = client.getNotes();
                Set<Long> remoteIDs = new HashSet<>();
                // pull remote changes: update or create each remote note
                for (OwnCloudNote remoteNote : remoteNotes) {
                    Log.d(getClass().getSimpleName(), "   Process Remote Note: "+remoteNote);
                    remoteIDs.add(remoteNote.getRemoteId());
                    if(localIDmap.containsKey(remoteNote.getRemoteId())) {
                        Log.d(getClass().getSimpleName(), "   ... found -> Update");
                        dbHelper.updateNote(localIDmap.get(remoteNote.getRemoteId()), remoteNote, null);
                    } else {
                        Log.d(getClass().getSimpleName(), "   ... create");
                        dbHelper.addNote(remoteNote);
                    }
                }
                Log.d(getClass().getSimpleName(), "   Remove remotely deleted Notes (only those without local changes)");
                // remove remotely deleted notes (only those without local changes)
                for (DBNote note : localNotes) {
                    if(note.getStatus()==DBStatus.VOID && !remoteIDs.contains(note.getRemoteId())) {
                        Log.d(getClass().getSimpleName(), "   ... remove "+note);
                        dbHelper.deleteNote(note.getId(), DBStatus.VOID);
                    }
                }
                status = LoginStatus.OK;
            } catch (IOException e) {
                e.printStackTrace();
                status = LoginStatus.CONNECTION_FAILED;
            } catch (JSONException e) {
                status = LoginStatus.JSON_FAILED;
            }
            if (status!=LoginStatus.OK) {
                Toast.makeText(appContext, appContext.getString(R.string.error_sync, appContext.getString(status.str)), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            syncActive = false;
            if(syncScheduled) {
                scheduleSync(false);
            } else {
                asyncTaskFinished();
            }
        }
    }

    private NotesClient createNotesClient() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext.getApplicationContext());
        String url = preferences.getString(SettingsActivity.SETTINGS_URL, SettingsActivity.DEFAULT_SETTINGS);
        String username = preferences.getString(SettingsActivity.SETTINGS_USERNAME, SettingsActivity.DEFAULT_SETTINGS);
        String password = preferences.getString(SettingsActivity.SETTINGS_PASSWORD, SettingsActivity.DEFAULT_SETTINGS);
        return new NotesClient(url, username, password);
    }

    /**
     * Helper method to check if all synchronize operations are done yet.
     */
    @Deprecated
    private void asyncTaskFinished() {
        handler.obtainMessage(1).sendToTarget();
    }
}
