package it.niedermann.owncloud.notes.android.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.Note;
import it.niedermann.owncloud.notes.model.NoteAdapter;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;
import it.niedermann.owncloud.notes.util.ICallback;

public class NotesListViewActivity extends AppCompatActivity implements View.OnClickListener, NoteAdapter.NoteClickListener {

    public final static String SELECTED_NOTE = "it.niedermann.owncloud.notes.clicked_note";
    public final static String CREATED_NOTE = "it.niedermann.owncloud.notes.created_notes";
    public final static String SELECTED_NOTE_POSITION = "it.niedermann.owncloud.notes.clicked_note_position";
    public final static String CREDENTIALS_CHANGED = "it.niedermann.owncloud.notes.CREDENTIALS_CHANGED";

    private final static int create_note_cmd = 0;
    private final static int show_single_note_cmd = 1;
    private final static int server_settings = 2;
    private final static int about = 3;

    private NoteAdapter adapter;
    private ActionMode mActionMode;
    private SwipeRefreshLayout swipeRefreshLayout = null;
    private NoteSQLiteOpenHelper db = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // First Run Wizard
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        if(preferences.getBoolean(SettingsActivity.SETTINGS_FIRST_RUN, true)) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivityForResult(settingsIntent, server_settings);
        }

        setContentView(R.layout.activity_notes_list_view);

        adapter = new NoteAdapter(this, this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        // Display Data
        db = new NoteSQLiteOpenHelper(this);
        db.synchronizeWithServer();
        setListView(db.getNotes());

        // Pull to Refresh
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefreshlayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("Swipe", "Refreshing Notes");
                db.synchronizeWithServer();
                db.getNoteServerSyncHelper().addCallback(new ICallback() {
                    @Override
                    public void onFinish() {
                        swipeRefreshLayout.setRefreshing(false);
                        setListView(db.getNotes());
                    }
                });
            }
        });

        // Floating Action Button
        findViewById(R.id.fab_create).setOnClickListener(this);
    }

    /**
     * Click listener for <strong>Floating Action Button</strong>
     * <p/>
     * Creates a new Instance of CreateNoteActivity.
     *
     * @param v View
     */
    @Override
    public void onClick(View v) {
        Intent createIntent = new Intent(this, CreateNoteActivity.class);
        startActivityForResult(createIntent, create_note_cmd);
    }

    /**
     * Allows other classes to set a List of Notes.
     *
     * @param noteList List&lt;Note&gt;
     */
    public void setListView(List<Note> noteList) {
        adapter.replaceNotes(noteList);
    }

    /**
     * Adds the Menu Items to the Action Bar.
     *
     * @param menu Menu
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_view, menu);
        return true;
    }

    /**
     * Handels click events on the Buttons in the Action Bar.
     *
     * @param item MenuItem - the clicked menu item
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(settingsIntent, server_settings);
                return true;
            case R.id.action_about:
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivityForResult(aboutIntent, about);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Handles the Results of started Sub Activities (Created Note, Edited Note)
     *
     * @param requestCode int to distinguish between the different Sub Activities
     * @param resultCode  int Return Code
     * @param data        Intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == create_note_cmd) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Note createdNote = (Note) data.getExtras().getSerializable(CREATED_NOTE);
                adapter.addNote(createdNote);
            }
        } else if (requestCode == NoteActivity.EDIT_NOTE_CMD) {
            if (resultCode == RESULT_OK) {
                Note editedNote = (Note) data.getExtras().getSerializable(
                        NoteActivity.EDIT_NOTE);
                int notePosition = data.getExtras().getInt(SELECTED_NOTE_POSITION);
                adapter.replaceNote(notePosition, editedNote);
            }
        } else if (requestCode == SettingsActivity.CREDENTIALS_CHANGED) {
            db = new NoteSQLiteOpenHelper(this);
            db.synchronizeWithServer(); // Needed to instanciate new NotesClient with new URL
            // TODO: should probably wait for a DB event?
            setListView(db.getNotes());
        }
    }

    @Override
    public void onNoteClicked(int position) {
        if (mActionMode != null) {
            toggleSelection(position);
        } else {
            Intent intent = new Intent(getApplicationContext(), NoteActivity.class);
            intent.putExtra(SELECTED_NOTE, adapter.getNote(position));
            intent.putExtra(SELECTED_NOTE_POSITION, position);
            Log.v("Note", "notePosition | NotesListViewActivity wurde abgesendet "
                    + position);
            startActivityForResult(intent, show_single_note_cmd);
        }
    }

    @Override
    public boolean onNoteLong(int position) {
        if (mActionMode == null) {
            // TODO differ if one or more items are selected
            mActionMode = startSupportActionMode(new MultiSelectedActionModeCallback());
        }

        toggleSelection(position);

        return true;
    }

    /**
     * Toggle the selection state of a note.
     *
     * If the note was the last one in the selection and is unselected, the selection is stopped.
     * Note that the selection must already be started (mActionMode must not be null).
     *
     * @param position Position of the item to toggle the selection state
     */
    private void toggleSelection(int position) {
        adapter.toggleSelection(position);
        int count = adapter.getSelectedItemCount();

        if (count == 0) {
            mActionMode.finish();
        } else {
            mActionMode.setTitle(String.valueOf(adapter.getSelectedItemCount()) + " " + getString(R.string.ab_selected));
        }
    }


    /**
     * Removes all selections.
     */
    private void removeSelection() {
        adapter.clearSelection();
    }

    /**
     * Handler for the MultiSelect Actions
     */
    private class MultiSelectedActionModeCallback implements
            ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // inflate contextual menu
            mode.getMenuInflater().inflate(R.menu.menu_list_context_multiple,
                    menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        /**
         * @param mode ActionMode - used to close the Action Bar after all work is done.
         * @param item MenuItem - the item in the List that contains the Node
         * @return boolean
         */
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_delete:
                    List<Integer> checkedItemPositions = adapter.getSelectedItems();
                    List<Note> notesToRemove = new ArrayList<>();
                    for (Integer position : checkedItemPositions) {
                        notesToRemove.add(adapter.getNote(position));
                    }
                    adapter.removeNotes(notesToRemove);
                    // TODO: sync only once?
                    for (Note note : notesToRemove) {
                        db.deleteNoteAndSync(note.getId());
                    }
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            removeSelection();
            mActionMode = null;
            adapter.notifyDataSetChanged();
        }
    }
}