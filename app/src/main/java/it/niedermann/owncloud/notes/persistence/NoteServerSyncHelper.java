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
 * <p/>
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

    public void uploadEditedNotes() {
        List<Note> notes = db.getNotesByStatus(DBStatus.LOCAL_EDITED);
        for (Note note : notes) {
            UploadEditedNotesTask editedNotesTask = new UploadEditedNotesTask();
            editedNotesTask.execute(note);
        }
    }

    public void uploadNewNotes() {
        List<Note> notes = db.getNotesByStatus(DBStatus.LOCAL_CREATED);
        for (Note note : notes) {
            UploadNewNoteTask newNotesTask = new UploadNewNoteTask();
            newNotesTask.execute(note);
        }
    }

    public void uploadDeletedNotes() {
        List<Note> notes = db.getNotesByStatus(DBStatus.LOCAL_DELETED);
        for (Note note : notes) {
            UploadDeletedNoteTask deletedNotesTask = new UploadDeletedNoteTask();
            deletedNotesTask.execute(note);
        }
    }

    public void downloadNotes() {
        DownloadNotesTask downloadNotesTask = new DownloadNotesTask();
        downloadNotesTask.execute();
    }

    private class UploadNewNoteTask extends AsyncTask<Object, Void, Object[]> {
        @Override
        protected Object[] doInBackground(Object... params) {
            operationsCount++;
            Note oldNote = (Note) params[0];
            try {
                Note note = client.createNote(oldNote.getContent());
                return new Object[]{note, oldNote.getId()};
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object[] params) {
            if (params != null) {
                Long id = (Long) params[1];
                if (id != null) {
                    db.deleteNote(((Long) params[1]));
                }
                db.addNote((Note) params[0]);
            }
            asyncTaskFinished();
        }
    }

    private class UploadEditedNotesTask extends AsyncTask<Object, Void, Note> {
        private String noteContent;

        @Override
        protected Note doInBackground(Object... params) {
            operationsCount++;
            Note oldNote = (Note) params[0];
            noteContent = oldNote.getContent();
            try {
                return client.editNote(oldNote.getId(), noteContent);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Note note) {
            if (note == null) {
                // Note has been deleted on server -> recreate
                db.addNoteAndSync(noteContent);
            } else {
                db.updateNote(note);
            }
            asyncTaskFinished();
        }
    }

    private class UploadDeletedNoteTask extends AsyncTask<Object, Void, Void> {
        Long id = null;

        @Override
        protected Void doInBackground(Object... params) {
            operationsCount++;
            try {
                id = ((Note) params[0]).getId();
                client.deleteNote(id);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            db.deleteNote(id);
            asyncTaskFinished();
        }
    }

    private class DownloadNotesTask extends AsyncTask<Object, Void, List<Note>> {
        private boolean serverError = false;

        @Override
        protected List<Note> doInBackground(Object... params) {
            operationsCount++;
            List<Note> notes = new ArrayList<>();
            try {
                notes = client.getNotes();
            } catch (IOException | JSONException e) {
                serverError = true;
                e.printStackTrace();
            }
            return notes;
        }

        @Override
        protected void onPostExecute(List<Note> result) {
            // Clear Database only if there was no Server Error
            if (!serverError) {
                db.clearDatabase();
            }
            for (Note note : result) {
                db.addNote(note);
            }
            asyncTaskFinished();
        }
    }
}
