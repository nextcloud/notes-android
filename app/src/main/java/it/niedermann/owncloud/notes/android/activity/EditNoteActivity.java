package it.niedermann.owncloud.notes.android.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.fragment.CategoryDialogFragment;
import it.niedermann.owncloud.notes.android.fragment.NoteEditFragment;
import it.niedermann.owncloud.notes.android.fragment.NoteFragmentI;
import it.niedermann.owncloud.notes.android.fragment.NotePreviewFragment;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;

public class EditNoteActivity extends AppCompatActivity implements CategoryDialogFragment.CategoryDialogListener {

    public static final String PARAM_NOTE = "note";
    public static final String PARAM_ORIGINAL_NOTE = "original_note";
    public static final String PARAM_NOTE_POSITION = "note_position";
    public static final String PARAM_WIDGET_SRC = "WIDGET_SRC";

    private static final String LOG_TAG = "EditNote/SAVE";

    /**
     * Preference key to store the open mode set by the user.
     */
    private static final String PREF_NOTE_MODE = "noteMode";
    /**
     * Preference key to store last mode used by the user.
     * Value is updated when the Activity is closed.
     * Supported values: PREF_MODE_EDIT, PREF_MODE_PREVIEW
     */
    private static final String PREF_NOTE_LAST_MODE = "noteLastMode";
    /**
     * Preference value indicating that the note should be opened in edit mode.
     */
    private static final String PREF_MODE_EDIT = "edit";
    /**
     * Preference value indicating that the note should be opened in preview mode.
     */
    private static final String PREF_MODE_PREVIEW = "preview";
    /**
     * Preference value indicating that the note should be opened according to the mode stored in PREF_NOTE_LAST_MODE.
     */
    private static final String PREF_MODE_LAST = "last";
    /**
     * Preference value indicating that the note should be opened according to the mode stored in note itself (on the server).
     * Possible enhancement. Currently not implemented.
     */
    private static final String PREF_MODE_NOTE = "note";

    private DBNote originalNote;
    private int notePosition = 0;
    private NoteSQLiteOpenHelper db;
    private NoteFragmentI fragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DBNote note;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String mode = preferences.getString(PREF_NOTE_MODE, PREF_MODE_EDIT);
        String lastMode = preferences.getString(PREF_NOTE_LAST_MODE, PREF_MODE_EDIT);

        if (savedInstanceState == null) {
            Log.d(getClass().getSimpleName(), "Starting from Intent");
            note = originalNote = (DBNote) getIntent().getSerializableExtra(PARAM_NOTE);
            notePosition = getIntent().getIntExtra(PARAM_NOTE_POSITION, 0);
        } else {
            Log.d(getClass().getSimpleName(), "Starting from SavedState");
            note = (DBNote) savedInstanceState.getSerializable(PARAM_NOTE);
            originalNote = (DBNote) savedInstanceState.getSerializable(PARAM_ORIGINAL_NOTE);
            notePosition = savedInstanceState.getInt(PARAM_NOTE_POSITION);
            mode = savedInstanceState.getString(PREF_NOTE_MODE);
        }

        db = NoteSQLiteOpenHelper.getInstance(this);

        if (PREF_MODE_PREVIEW.equals(mode)) {
            createPreviewFragment(note);
        } else if (PREF_MODE_LAST.equals(mode) && PREF_MODE_PREVIEW.equals(lastMode)) {
            createPreviewFragment(note);
        /* TODO enhancement: store last mode in note
           for cross device functionality per note mode should be stored on the server.
        } else if(PREF_MODE_NOTE.equals(mode) && PREF_MODE_PREVIEW.equals(note.getMode())) {
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
        if(fragment instanceof  NotePreviewFragment)
            outState.putString(PREF_NOTE_MODE, PREF_MODE_PREVIEW);
        else
            outState.putString(PREF_NOTE_MODE, PREF_MODE_EDIT);
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
            case R.id.menu_category:
                showCategorySelector();
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
     * Opens a dialog in order to chose a category
     */
    private void showCategorySelector() {
        final String fragmentId = "fragment_category";
        FragmentManager manager = getFragmentManager();
        Fragment frag = manager.findFragmentByTag(fragmentId);
        if(frag!=null) {
            manager.beginTransaction().remove(frag).commit();
        }
        Bundle arguments = new Bundle();
        arguments.putString(CategoryDialogFragment.PARAM_CATEGORY, fragment.getNote().getCategory());
        CategoryDialogFragment categoryFragment = new CategoryDialogFragment();
        categoryFragment.setArguments(arguments);
        categoryFragment.show(manager, fragmentId);
    }

    @Override
    public void onCategoryChosen(String category) {
        DBNote note = fragment.getNote();
        note.setCategory(category);
        db.updateNoteAndSync(note, note.getContent(), null);
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
            preferences.edit().putString(PREF_NOTE_LAST_MODE, PREF_MODE_EDIT).apply();
        } else {
            preferences.edit().putString(PREF_NOTE_LAST_MODE, PREF_MODE_PREVIEW).apply();
        }

        Intent data = new Intent();
        data.setAction(Intent.ACTION_VIEW);
        data.putExtra(PARAM_NOTE, note);
        data.putExtra(PARAM_NOTE_POSITION, notePosition);
        setResult(RESULT_OK, data);
        db.updateSingleNoteWidgets();
        finish();
    }
}