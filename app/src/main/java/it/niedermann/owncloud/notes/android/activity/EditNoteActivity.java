package it.niedermann.owncloud.notes.android.activity;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Calendar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.fragment.BaseNoteFragment;
import it.niedermann.owncloud.notes.android.fragment.NoteEditFragment;
import it.niedermann.owncloud.notes.android.fragment.NotePreviewFragment;
import it.niedermann.owncloud.notes.model.Category;
import it.niedermann.owncloud.notes.model.CloudNote;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.util.NoteUtil;

public class EditNoteActivity extends AppCompatActivity implements BaseNoteFragment.NoteFragmentListener {

    private static final String INTENT_GOOGLE_ASSISTANT = "com.google.android.gm.action.AUTO_SEND";
    private static final String MIMETYPE_TEXT_PLAIN = "text/plain";
    public static final String PARAM_NOTE_ID = "noteId";
    public static final String PARAM_CATEGORY = "category";

    private BaseNoteFragment fragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            launchNoteFragment();
        } else {
            fragment = (BaseNoteFragment) getFragmentManager().findFragmentById(android.R.id.content);
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(getClass().getSimpleName(), "onNewIntent: " + intent.getLongExtra(PARAM_NOTE_ID, 0));
        setIntent(intent);
        if (fragment != null) {
            getFragmentManager().beginTransaction().detach(fragment).commit();
            fragment = null;
        }
        launchNoteFragment();
    }

    private long getNoteId() {
        return getIntent().getLongExtra(PARAM_NOTE_ID, 0);
    }

    /**
     * Starts the note fragment for an existing note or a new note.
     * The actual behavior is triggered by the activity's intent.
     */
    private void launchNoteFragment() {
        long noteId = getNoteId();
        if (noteId > 0) {
            launchExistingNote(noteId);
        } else {
            launchNewNote();
        }
    }

    /**
     * Starts a {@link NoteEditFragment} or {@link NotePreviewFragment} for an existing note.
     * The type of fragment (view-mode) is chosen based on the user preferences.
     *
     * @param noteId ID of the existing note.
     */
    private void launchExistingNote(long noteId) {
        final String prefKeyNoteMode = getString(R.string.pref_key_note_mode);
        final String prefKeyLastMode = getString(R.string.pref_key_last_note_mode);
        final String prefValueEdit = getString(R.string.pref_value_mode_edit);
        final String prefValuePreview = getString(R.string.pref_value_mode_preview);
        final String prefValueLast = getString(R.string.pref_value_mode_last);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String mode = preferences.getString(prefKeyNoteMode, prefValueEdit);
        String lastMode = preferences.getString(prefKeyLastMode, prefValueEdit);
        boolean editMode = true;
        if (prefValuePreview.equals(mode) || (prefValueLast.equals(mode) && prefValuePreview.equals(lastMode))) {
            editMode = false;
        }
        launchExistingNote(noteId, editMode);
    }

    /**
     * Starts a {@link NoteEditFragment} or {@link NotePreviewFragment} for an existing note.
     *
     * @param noteId ID of the existing note.
     * @param edit   View-mode of the fragment:
     *               <code>true</code> for {@link NoteEditFragment},
     *               <code>false</code> for {@link NotePreviewFragment}.
     */
    private void launchExistingNote(long noteId, boolean edit) {
        // save state of the fragment in order to resume with the same note and originalNote
        Fragment.SavedState savedState = null;
        if (fragment != null) {
            savedState = getFragmentManager().saveFragmentInstanceState(fragment);
        }
        if (edit) {
            fragment = NoteEditFragment.newInstance(noteId);
        } else {
            fragment = NotePreviewFragment.newInstance(noteId);
        }

        if (savedState != null) {
            fragment.setInitialSavedState(savedState);
        }
        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }

    /**
     * Starts the {@link NoteEditFragment} with a new note.
     * Content ("share" functionality), category and favorite attribute can be preset.
     */
    private void launchNewNote() {
        Intent intent = getIntent();

        String category = null;
        boolean favorite = false;
        if (intent.hasExtra(PARAM_CATEGORY)) {
            Category categoryPreselection = (Category) intent.getSerializableExtra(PARAM_CATEGORY);
            category = categoryPreselection.category;
            favorite = categoryPreselection.favorite != null ? categoryPreselection.favorite : false;
        }

        String content = "";
        if (
                MIMETYPE_TEXT_PLAIN.equals(intent.getType()) &&
                        (Intent.ACTION_SEND.equals(intent.getAction()) ||
                                INTENT_GOOGLE_ASSISTANT.equals(intent.getAction()))
                ) {
            content = intent.getStringExtra(Intent.EXTRA_TEXT);
        }

        CloudNote newNote = new CloudNote(0, Calendar.getInstance(), NoteUtil.generateNonEmptyNoteTitle(content, this), content, favorite, category, null);
        fragment = NoteEditFragment.newInstanceWithNewNote(newNote);
        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }

    @Override
    public void onBackPressed() {
        close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                close();
                return true;
            case R.id.menu_preview:
                launchExistingNote(getNoteId(), false);
                return true;
            case R.id.menu_edit:
                launchExistingNote(getNoteId(), true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Send result and closes the Activity
     */
    public void close() {
        /* TODO enhancement: store last mode in note
         * for cross device functionality per note mode should be stored on the server.
         */
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final String prefKeyLastMode = getString(R.string.pref_key_last_note_mode);
        if (fragment instanceof NoteEditFragment) {
            preferences.edit().putString(prefKeyLastMode, getString(R.string.pref_value_mode_edit)).apply();
        } else {
            preferences.edit().putString(prefKeyLastMode, getString(R.string.pref_value_mode_preview)).apply();
        }
        fragment.onCloseNote();
        finish();
    }

    @Override
    public void onNoteUpdated(DBNote note) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(note.getTitle());
            if (!note.getCategory().isEmpty()) {
                actionBar.setSubtitle(NoteUtil.extendCategory(note.getCategory()));
            }
        }
    }
}