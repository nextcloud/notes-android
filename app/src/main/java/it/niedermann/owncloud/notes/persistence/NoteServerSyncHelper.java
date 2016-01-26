package it.niedermann.owncloud.notes.persistence;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.android.activity.SettingsActivity;
import it.niedermann.owncloud.notes.model.DBStatus;
import it.niedermann.owncloud.notes.model.Note;
import it.niedermann.owncloud.notes.util.ICallback;
import it.niedermann.owncloud.notes.util.NotesClient;

/**
 * Helps to synchronize the Database to the Server.
 * <p>
 * Created by stefan on 20.09.15.
 */
public class NoteServerSyncHelper {

    private NotesClient client = null;
    private NoteSQLiteOpenHelper db = null;
    private final View.OnClickListener goToSettingsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Activity parent = (Activity) db.getContext();
            Intent intent = new Intent(parent, SettingsActivity.class);
            parent.startActivity(intent);
        }
    };
    private int operationsCount = 0;
    private int operationsFinished = 0;
    private Handler handler = null;
    private List<ICallback> callbacks = new ArrayList<>();

    public NoteServerSyncHelper(NoteSQLiteOpenHelper db) {
        this.db = db;
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                for (ICallback callback : callbacks) {
                    callback.onFinish();
                }
                callbacks.clear();
            }
        };
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(db.getContext().getApplicationContext());
        String url = preferences.getString(SettingsActivity.SETTINGS_URL,
                SettingsActivity.DEFAULT_SETTINGS);
        String username = preferences.getString(SettingsActivity.SETTINGS_USERNAME,
                SettingsActivity.DEFAULT_SETTINGS);
        String password = preferences.getString(SettingsActivity.SETTINGS_PASSWORD,
                SettingsActivity.DEFAULT_SETTINGS);
        client = new NotesClient(url, username, password);
    }

    /**
     * Adds a callback method to the NoteServerSyncHelper.
     * All callbacks will be executed once all synchronize operations are done.
     * After execution the callback will be deleted, so it has to be added again if it shall be
     * executed the next time all synchronize operations are finished.
     *
     * @param callback Implementation of ICallback, contains one method that shall be executed.
     */
    public void addCallback(ICallback callback) {
        callbacks.add(callback);
    }

    /**
     * Synchronizes Edited, New and Deleted Notes. After all changed content has been sent to the
     * server, it downloads all changes that happened on the server.
     */
    public void synchronize() {
        uploadEditedNotes();
        uploadNewNotes();
        uploadDeletedNotes();
        downloadNotes();
    }

    /**
     * Helper method to check if all synchronize operations are done yet.
     */
    private void asyncTaskFinished() {
        operationsFinished++;
        if (operationsFinished == operationsCount) {
            handler.obtainMessage(1).sendToTarget();
        }
    }

    // Not async anymore
    public void uploadEditedNotes() {
        List<Note> notes = db.getNotesByStatus(DBStatus.LOCAL_EDITED);
        for (Note note : notes) {
            operationsCount++;
            try {
                Note newNote = client.editNote(note.getId(), note.getContent());
                db.updateNote(newNote);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            asyncTaskFinished();
        }
    }

    // Not async anymore
    public void uploadNewNotes() {
        List<Note> notes = db.getNotesByStatus(DBStatus.LOCAL_CREATED);
        for (Note note : notes) {
            operationsCount++;
            try {
                Note newNote = client.createNote(note.getContent());
                //change Status of note
                db.deleteNote(note.getId());
                db.addNote(newNote);
                asyncTaskFinished();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            asyncTaskFinished();
        }
    }

    // Not async anymore
    public void uploadDeletedNotes() {
        List<Note> notes = db.getNotesByStatus(DBStatus.LOCAL_DELETED);
        for (Note note : notes) {
            operationsCount++;
            try {
                long id = note.getId();
                client.deleteNote(id);
                db.deleteNote(id);
            } catch (IOException e) {
                e.printStackTrace();
            }
            asyncTaskFinished();
        }
    }

    // Not async anymore
    public void downloadNotes() {
        operationsCount++;
        try {
            List<Note> notesFromServer = client.getNotes();
            for (Note note : notesFromServer) {
                db.addNote(note);
            }
        } catch (IOException | JSONException e) {
            // Clear Database only if there was no Server Error
            db.clearDatabase();
            e.printStackTrace();
        }
        asyncTaskFinished();
    }

}
