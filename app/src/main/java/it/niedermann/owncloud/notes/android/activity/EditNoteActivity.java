package it.niedermann.owncloud.notes.android.activity;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.fragment.NoteEditFragment;
import it.niedermann.owncloud.notes.android.fragment.NoteFragmentI;
import it.niedermann.owncloud.notes.android.fragment.NotePreviewFragment;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;

public class EditNoteActivity extends AppCompatActivity {

    public static final String PARAM_NOTE = "note";
    public static final String PARAM_ORIGINAL_NOTE = "original_note";
    public static final String PARAM_NOTE_POSITION = "note_position";
    public static final String PARAM_WIDGET_SRC = "WIDGET_SRC";

    private static final String LOG_TAG = "EditNote/SAVE";
    private static final long DELAY = 2000; // in ms
    private static final long DELAY_AFTER_SYNC = 5000; // in ms

    private DBNote originalNote;
    private int notePosition = 0;
    private NoteSQLiteOpenHelper db;
    private NoteFragmentI fragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DBNote note = null;

        if (savedInstanceState == null) {
            Log.d(getClass().getSimpleName(), "Starting from Intent");
            note = originalNote = (DBNote) getIntent().getSerializableExtra(PARAM_NOTE);
            notePosition = getIntent().getIntExtra(PARAM_NOTE_POSITION, 0);
        } else {
            Log.d(getClass().getSimpleName(), "Starting from SavedState");
            note = (DBNote) savedInstanceState.getSerializable(PARAM_NOTE);
            originalNote = (DBNote) savedInstanceState.getSerializable(PARAM_ORIGINAL_NOTE);
            notePosition = savedInstanceState.getInt(PARAM_NOTE_POSITION);
        }

        db = NoteSQLiteOpenHelper.getInstance(this);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String mode = preferences.getString("noteMode", "edit");
        String lastMode = preferences.getString("noteLastMode", "edit");
        if ("preview".equals(mode)) {
            createPreviewFragment(note);
        } else if (mode.equals("last") && "preview".equals(lastMode)) {
            createPreviewFragment(note);
        /* TODO enhancement: store last mode in note
           for cross device functionality per note mode should be stored on the server.
        } else if(mode.equals("note") && "preview".equals(note.getMode())) {
            createPreviewFragment(note);
         */
        } else {
            createEditFragment(note);
        }
    }

    private void createEditFragment(DBNote note) {
        configureActionBar(note, false);
        fragment = NoteEditFragment.newInstance(note);
        getFragmentManager().beginTransaction().replace(android.R.id.content, (Fragment) fragment).commit();
    }

    private void createPreviewFragment(DBNote note) {
        configureActionBar(note, true);
        fragment = NotePreviewFragment.newInstance(note);
        getFragmentManager().beginTransaction().replace(android.R.id.content, (Fragment) fragment).commit();
    }

    private void configureActionBar(DBNote note, boolean timestamp) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(note.getTitle());
            if (timestamp) {
                actionBar.setSubtitle(DateUtils.getRelativeDateTimeString(getApplicationContext(), note.getModified().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0));
            } else {
                actionBar.setSubtitle(getString(R.string.action_edit_editing));
            }

            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(PARAM_NOTE, fragment.getNote());
        outState.putSerializable(PARAM_ORIGINAL_NOTE, originalNote);
        outState.putInt(PARAM_NOTE_POSITION, notePosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        close(fragment.getNote());
    }

    /**
     * Main-Menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note_list_view, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem itemFavorite = menu.findItem(R.id.menu_favorite);
        prepareFavoriteOption(itemFavorite);
        return super.onPrepareOptionsMenu(menu);
    }

    private void prepareFavoriteOption(MenuItem item) {
        DBNote note = fragment.getNote();
        item.setIcon(note.isFavorite() ? R.drawable.ic_star_white_24dp : R.drawable.ic_star_outline_white_24dp);
        item.setChecked(note.isFavorite());
    }

    /**
     * Main-Menu-Handler
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                close(fragment.getNote());
                return true;
            case R.id.menu_cancel:
                Log.d(LOG_TAG, "CANCEL: original: " + originalNote);
                db.updateNoteAndSync(originalNote, null, null);
                close(originalNote);
                return true;
            case R.id.menu_delete:
                db.deleteNoteAndSync(originalNote.getId());
                Intent data = new Intent();
                data.putExtra(PARAM_NOTE_POSITION, notePosition);
                setResult(RESULT_FIRST_USER, data);
                finish();
                return true;
            case R.id.menu_favorite:
                db.toggleFavorite(fragment.getNote(), null);
                prepareFavoriteOption(item);
                return true;
            case R.id.menu_preview:
                if (fragment instanceof NoteEditFragment) {
                    createPreviewFragment(fragment.getNote());
                } else {
                    createEditFragment(fragment.getNote());
                }
                return true;
            case R.id.menu_share:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                DBNote note = fragment.getNote();
                shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, note.getTitle());
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, note.getContent());
                startActivity(shareIntent);
                return true;
            /*case R.id.menu_copy:
                db = new NoteSQLiteOpenHelper(this);
                Note newNote = db.getNote(db.addNoteAndSync(note.getContent()));
                newNote.setTitle(note.getTitle() + " (" + getResources().getString(R.string.copy) + ")");
                db.updateNote(newNote);
                finish();
                return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Send result and closes the Activity
     */
    private void close(DBNote note) {
        /* TODO enhancement: store last mode in note
        * for cross device functionality per note mode should be stored on the server.
        */
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (fragment instanceof NoteEditFragment) {
            preferences.edit().putString("noteLastMode", "edit").commit();
        } else {
            preferences.edit().putString("noteLastMode", "preview").commit();
        }

        Intent data = new Intent();
        data.setAction(Intent.ACTION_VIEW);
        data.putExtra(PARAM_NOTE, note);
        data.putExtra(PARAM_NOTE_POSITION, notePosition);
        setResult(RESULT_OK, data);
        updateSingleNoteWidgets();
        finish();
    }


    public void updateSingleNoteWidgets() {

        Intent intent = new Intent(this, SingleNoteWidget.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        sendBroadcast(intent);
    }
}