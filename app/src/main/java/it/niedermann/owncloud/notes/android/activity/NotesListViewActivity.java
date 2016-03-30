package it.niedermann.owncloud.notes.android.activity;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.Item;
import it.niedermann.owncloud.notes.model.ItemAdapter;
import it.niedermann.owncloud.notes.model.Note;
import it.niedermann.owncloud.notes.model.SectionItem;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;
import it.niedermann.owncloud.notes.util.ICallback;

public class NotesListViewActivity extends AppCompatActivity implements
        ItemAdapter.NoteClickListener, View.OnClickListener {

    public final static String SELECTED_NOTE = "it.niedermann.owncloud.notes.clicked_note";
    public final static String CREATED_NOTE = "it.niedermann.owncloud.notes.created_notes";
    public final static String SELECTED_NOTE_POSITION = "it.niedermann.owncloud.notes.clicked_note_position";
    public final static String CREDENTIALS_CHANGED = "it.niedermann.owncloud.notes.CREDENTIALS_CHANGED";

    private final static int create_note_cmd = 0;
    private final static int show_single_note_cmd = 1;
    private final static int server_settings = 2;
    private final static int about = 3;

    private RecyclerView listView = null;
    private ItemAdapter adapter = null;
    private ActionMode mActionMode;
    private SwipeRefreshLayout swipeRefreshLayout = null;
    private NoteSQLiteOpenHelper db = null;
    private SearchView searchView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // First Run Wizard
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        if (preferences.getBoolean(SettingsActivity.SETTINGS_FIRST_RUN, true)) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivityForResult(settingsIntent, server_settings);
        }

        setContentView(R.layout.activity_notes_list_view);

        // Display Data
        db = new NoteSQLiteOpenHelper(this);
        setListView(db.getNotes());

        // Pull to Refresh
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefreshlayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                db.getNoteServerSyncHelper().addCallback(new ICallback() {
                    @Override
                    public void onFinish() {
                        swipeRefreshLayout.setRefreshing(false);
                        adapter.clearSelection();
                        if (mActionMode != null) {
                            mActionMode.finish();
                        }
                        setListView(db.getNotes());
                    }
                });
                db.getNoteServerSyncHelper().downloadNotes();
            }
        });

        // Floating Action Button
        findViewById(R.id.fab_create).setOnClickListener(this);
    }

    /**
     * Perform refresh on every time the activity gets visible
     */
    @Override
    protected void onResume() {
        db.getNoteServerSyncHelper().addCallback(new ICallback() {
            @Override
            public void onFinish() {
                swipeRefreshLayout.setRefreshing(false);
                adapter.clearSelection();
                if (mActionMode != null) {
                    mActionMode.finish();
                }
                adapter.checkForUpdates(db.getNotes());
            }
        });
        db.getNoteServerSyncHelper().downloadNotes();
        super.onResume();
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
    @SuppressWarnings("WeakerAccess")
    public void setListView(List<Note> noteList) {
        List<Item> itemList = new ArrayList<>();
        // #12 Create Sections depending on Time
        // TODO Move to ItemAdapter?
        boolean todaySet, yesterdaySet, weekSet, monthSet, earlierSet;
        todaySet = yesterdaySet = weekSet = monthSet = earlierSet = false;
        Calendar recent = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        Calendar yesterday = Calendar.getInstance();
        yesterday.set(Calendar.DAY_OF_YEAR, yesterday.get(Calendar.DAY_OF_YEAR) - 1);
        yesterday.set(Calendar.HOUR_OF_DAY, 0);
        yesterday.set(Calendar.MINUTE, 0);
        yesterday.set(Calendar.SECOND, 0);
        yesterday.set(Calendar.MILLISECOND, 0);
        Calendar week = Calendar.getInstance();
        week.set(Calendar.DAY_OF_WEEK, week.getFirstDayOfWeek());
        week.set(Calendar.HOUR_OF_DAY, 0);
        week.set(Calendar.MINUTE, 0);
        week.set(Calendar.SECOND, 0);
        week.set(Calendar.MILLISECOND, 0);
        Calendar month = Calendar.getInstance();
        month.set(Calendar.DAY_OF_MONTH, 0);
        month.set(Calendar.HOUR_OF_DAY, 0);
        month.set(Calendar.MINUTE, 0);
        month.set(Calendar.SECOND, 0);
        month.set(Calendar.MILLISECOND, 0);
        for (int i = 0; i < noteList.size(); i++) {
            Note currentNote = noteList.get(i);
            if (!todaySet && recent.getTimeInMillis() - currentNote.getModified().getTimeInMillis() >= 600000 && currentNote.getModified().getTimeInMillis() >= today.getTimeInMillis()) {
                // < 10 minutes but after 00:00 today
                //if (i > 0) {
                    //itemList.add(new SectionItem(getResources().getString(R.string.listview_updated_today)));
                //}
                todaySet = true;
            } else if (!yesterdaySet && currentNote.getModified().getTimeInMillis() < today.getTimeInMillis() && currentNote.getModified().getTimeInMillis() >= yesterday.getTimeInMillis()) {
                // between today 00:00 and yesterday 00:00
                if (i > 0) {
                    itemList.add(new SectionItem(getResources().getString(R.string.listview_updated_yesterday)));
                }
                yesterdaySet = true;
            } else if (!weekSet && currentNote.getModified().getTimeInMillis() < yesterday.getTimeInMillis() && currentNote.getModified().getTimeInMillis() >= week.getTimeInMillis()) {
                // between yesterday 00:00 and start of the week 00:00
                if (i > 0) {
                    itemList.add(new SectionItem(getResources().getString(R.string.listview_updated_this_week)));
                }
                weekSet = true;
            } else if (!monthSet && currentNote.getModified().getTimeInMillis() < week.getTimeInMillis() && currentNote.getModified().getTimeInMillis() >= month.getTimeInMillis()) {
                // between start of the week 00:00 and start of the month 00:00
                if (i > 0) {
                    itemList.add(new SectionItem(getResources().getString(R.string.listview_updated_this_month)));
                }
                monthSet = true;
            } else if (!earlierSet && currentNote.getModified().getTimeInMillis() < month.getTimeInMillis()) {
                // before start of the month 00:00
                if (i > 0) {
                    itemList.add(new SectionItem(getResources().getString(R.string.listview_updated_earlier)));
                }
                earlierSet = true;
            }
            itemList.add(currentNote);
        }

        adapter = new ItemAdapter(itemList);
        ItemAdapter.setNoteClickListener(this);
        listView = (RecyclerView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        listView.setLayoutManager(new LinearLayoutManager(this));
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
        // Associate searchable configuration with the SearchView
        final MenuItem item = menu.findItem(R.id.search);
        searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText.trim());
                return true;
            }
        });
        return true;
    }

    private void search(final String query) {
        new Thread() {
            @Override
            public void run() {
                if (query.length() > 0) {
                    setListView(db.searchNotes(query));
                } else {
                    setListView(db.getNotes());
                }
                listView.scrollToPosition(0);
            }
        }.run();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            search(intent.getStringExtra(SearchManager.QUERY));
        }
        super.onNewIntent(intent);
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
                //not need because of db.synchronisation in createActivity

                Note createdNote = (Note) data.getExtras().getSerializable(
                        CREATED_NOTE);
                adapter.add(createdNote);
                //setListView(db.getNotes());
            }
        } else if (requestCode == show_single_note_cmd) {
            if (resultCode == RESULT_OK || resultCode == RESULT_FIRST_USER) {
                int notePosition = data.getExtras().getInt(
                        SELECTED_NOTE_POSITION);
                adapter.remove(adapter.getItem(notePosition));
                if (resultCode == RESULT_OK) {
                    Note editedNote = (Note) data.getExtras().getSerializable(
                            NoteActivity.EDIT_NOTE);
                    adapter.add(editedNote);
                }
            }
        } else if (requestCode == server_settings) {
            // Create new Instance with new URL and credentials
            db = new NoteSQLiteOpenHelper(this);
            db.getNoteServerSyncHelper().addCallback(new ICallback() {
                @Override
                public void onFinish() {
                    setListView(db.getNotes());
                }
            });
            db.synchronizeWithServer();
        }
    }

    @Override
    public void onNoteClick(int position, View v) {
        if (mActionMode != null) {
            if (!adapter.select(position)) {
                v.setSelected(false);
                adapter.deselect(position);
            } else {
                v.setSelected(true);
            }
            int size = adapter.getSelected().size();
            mActionMode.setTitle(String.valueOf(getResources().getQuantityString(R.plurals.ab_selected, size, size)));
            int checkedItemCount = adapter.getSelected().size();
            boolean hasCheckedItems = checkedItemCount > 0;

            if (hasCheckedItems && mActionMode == null) {
                // TODO differ if one or more items are selected
                // if (checkedItemCount == 1) {
                // mActionMode = startActionMode(new
                // SingleSelectedActionModeCallback());
                // } else {
                // there are some selected items, start the actionMode
                mActionMode = startSupportActionMode(new MultiSelectedActionModeCallback());
                // }
            } else if (!hasCheckedItems && mActionMode != null) {
                // there no selected items, finish the actionMode
                mActionMode.finish();
            }
        } else {


            Intent intent = new Intent(getApplicationContext(),
                    NoteActivity.class);

            Item item = adapter.getItem(position);
            intent.putExtra(SELECTED_NOTE, (Note) item);
            intent.putExtra(SELECTED_NOTE_POSITION, position);
            startActivityForResult(intent, show_single_note_cmd);

        }
    }

    @Override
    public boolean onNoteLongClick(int position, View v) {
        boolean selected = adapter.select(position);
        if (selected) {
            v.setSelected(true);
            mActionMode = startSupportActionMode(new MultiSelectedActionModeCallback());
            int checkedItemCount = adapter.getSelected().size();
            mActionMode.setTitle(getResources().getQuantityString(R.plurals.ab_selected, checkedItemCount, checkedItemCount));
        }
        return selected;
    }

    @Override
    public void onBackPressed() {
        if (searchView.isIconified()) {
            super.onBackPressed();
        } else {
            searchView.setIconified(true);
        }
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
                    List<Integer> selection = adapter.getSelected();
                    for (Integer i : selection) {
                        Note note = (Note) adapter.getItem(i);
                        db.deleteNoteAndSync(note.getId());
                        // Not needed because of dbsync
                        //adapter.remove(note);
                    }
                    mode.finish(); // Action picked, so close the CAB
                    //after delete selection has to be cleared
                    searchView.setIconified(true);
                    setListView(db.getNotes());
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapter.clearSelection();
            mActionMode = null;
            adapter.notifyDataSetChanged();
        }
    }
}