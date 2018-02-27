package it.niedermann.owncloud.notes.android.activity;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.fragment.BaseNoteFragment;
import it.niedermann.owncloud.notes.android.fragment.NoteEditFragment;
import it.niedermann.owncloud.notes.android.fragment.NotePreviewFragment;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.util.NoteUtil;

public class EditNoteActivity extends AppCompatActivity implements BaseNoteFragment.NoteFragmentListener {

    public static final String PARAM_NOTE_ID = "noteId";

    private BaseNoteFragment fragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            createFragmentByPreference();
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
        Log.d(getClass().getSimpleName(), "onNewIntent: "+intent.getLongExtra(PARAM_NOTE_ID, 0));
        setIntent(intent);
        if(fragment != null) {
            getFragmentManager().beginTransaction().detach(fragment).commit();
            fragment = null;
        }
        createFragmentByPreference();
    }

    private long getNoteId() {
        return getIntent().getLongExtra(PARAM_NOTE_ID, 0);
    }

    private void createFragmentByPreference() {
        long noteId = getNoteId();

        final String prefKeyNoteMode = getString(R.string.pref_key_note_mode);
        final String prefKeyLastMode = getString(R.string.pref_key_last_note_mode);
        final String prefValueEdit = getString(R.string.pref_value_mode_edit);
        final String prefValuePreview = getString(R.string.pref_value_mode_preview);
        final String prefValueLast = getString(R.string.pref_value_mode_last);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String mode = preferences.getString(prefKeyNoteMode, prefValueEdit);
        String lastMode = preferences.getString(prefKeyLastMode, prefValueEdit);
        if (prefValuePreview.equals(mode) || (prefValueLast.equals(mode) && prefValuePreview.equals(lastMode))) {
            createFragment(noteId, false);
        /* TODO enhancement: store last mode in note
           for cross device functionality per note mode should be stored on the server.
        } else if(prefValueLast.equals(mode) && prefValuePreview.equals(note.getMode())) {
            createPreviewFragment(note);
         */
        } else {
            createFragment(noteId, true);
        }
    }

    private void createFragment(long noteId, boolean edit) {
        // save state of the fragment in order to resume with the same note and originalNote
        Fragment.SavedState savedState = null;
        if(fragment != null) {
            savedState = getFragmentManager().saveFragmentInstanceState(fragment);
        }
        if(edit) {
            fragment = NoteEditFragment.newInstance(noteId);
        } else {
            fragment = NotePreviewFragment.newInstance(noteId);
        }
        if(savedState != null) {
            fragment.setInitialSavedState(savedState);
        }
        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }

    @Override
    public void onBackPressed() {
        fragment.onPrepareClose();
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
                fragment.onPrepareClose();
                close();
                return true;
            case R.id.menu_preview:
                fragment.onPrepareClose();
                createFragment(getNoteId(), false);
                return true;
            case R.id.menu_edit:
                fragment.onPrepareClose();
                createFragment(getNoteId(), true);
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
        finish();
    }

    @Override
    public void onNoteUpdated(DBNote note) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(note.getTitle());
            String subtitle = note.getCategory().isEmpty() ? getString(R.string.action_uncategorized) : NoteUtil.extendCategory(note.getCategory());
            actionBar.setSubtitle(subtitle);
        }
    }
}