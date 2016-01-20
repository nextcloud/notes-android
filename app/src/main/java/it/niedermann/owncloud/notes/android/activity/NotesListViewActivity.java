package it.niedermann.owncloud.notes.android.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.Item;
import it.niedermann.owncloud.notes.model.ItemAdapter;
import it.niedermann.owncloud.notes.model.Note;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;
import it.niedermann.owncloud.notes.util.ICallback;
import it.niedermann.owncloud.notes.util.SimpleDividerItemDecoration;

public class NotesListViewActivity extends AppCompatActivity implements
         ItemAdapter.NoteClickListener,View.OnClickListener {

    public final static String SELECTED_NOTE = "it.niedermann.owncloud.notes.clicked_note";
    public final static String CREATED_NOTE = "it.niedermann.owncloud.notes.created_notes";
    public final static String SELECTED_NOTE_POSITION = "it.niedermann.owncloud.notes.clicked_note_position";
    public final static String CREDENTIALS_CHANGED = "it.niedermann.owncloud.notes.CREDENTIALS_CHANGED";

    private final static int create_note_cmd = 0;
    private final static int show_single_note_cmd = 1;
    private final static int server_settings = 2;
    private final static int about = 3;

    // Layout Settings
    public final static boolean CARDLAYOUT=false;
    private final int columns = 1;

    private RecyclerView recyclerView = null;
    private ItemAdapter adapter = null;
    private ActionMode mActionMode;
    private SwipeRefreshLayout swipeRefreshLayout = null;
    private NoteSQLiteOpenHelper db = null;

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
        db.synchronizeWithServer();
        setRecyclerView(db.getNotes());

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
                        setRecyclerView(db.getNotes());
                    }
                });
                db.synchronizeWithServer();
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
    @SuppressWarnings("WeakerAccess")
    public void setRecyclerView(List<Note> noteList) {
        List<Item> itemList = new ArrayList<>();
        /*
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
        */
        for (int i = 0; i < noteList.size(); i++) {
            Note currentNote = noteList.get(i);
            /*
            if (!todaySet && recent.getTimeInMillis() - currentNote.getModified().getTimeInMillis() >= 600000 && currentNote.getModified().getTimeInMillis() >= today.getTimeInMillis()) {
                // < 10 minutes but after 00:00 today
                if (i > 0) {
                    itemList.add(new SectionItem(getResources().getString(R.string.listview_updated_today)));
                }
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
            */
            itemList.add(currentNote);
        }

        adapter = new ItemAdapter(getApplicationContext(), itemList);
        adapter.setNoteClickListener(this);
        recyclerView = (RecyclerView) findViewById(R.id.list_view);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL));
        if(!NotesListViewActivity.CARDLAYOUT) {
            recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        }
        //recyclerView.setChoiceMode(CHOICE_MODE_MULTIPLE);
        recyclerView.setAdapter(adapter);
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
                Note createdNote = (Note) data.getExtras().getSerializable(
                        CREATED_NOTE);
                adapter.insert(createdNote, 0);
            }
        } else if (requestCode == show_single_note_cmd) {
            if (resultCode == RESULT_OK || resultCode == RESULT_FIRST_USER) {
                int notePosition = data.getExtras().getInt(
                        SELECTED_NOTE_POSITION);
                adapter.remove(adapter.getItem(notePosition));
                if (resultCode == RESULT_OK) {
                    Note editedNote = (Note) data.getExtras().getSerializable(
                            NoteActivity.EDIT_NOTE);
                    adapter.insert(editedNote, 0);
                }
            }
        } else if (requestCode == server_settings) {
            // Create new Instance with new URL and credentials
            db = new NoteSQLiteOpenHelper(this);
            db.getNoteServerSyncHelper().addCallback(new ICallback() {
                @Override
                public void onFinish() {
                    setRecyclerView(db.getNotes());
                }
            });
            db.synchronizeWithServer();
        }
    }

    /**
     * short click on a List Item
     * @param position Position of the Item in the List
     * @param v Viewholder of the item
     */
    @Override
    public void onNoteClick(int position, View v) {

        if (mActionMode != null) {
            selectNote(position,v);
        }else {


                Intent intent = new Intent(getApplicationContext(),
                        NoteActivity.class);

                Item item = adapter.getItem(position);
                intent.putExtra(SELECTED_NOTE, (Note) item);
                intent.putExtra(SELECTED_NOTE_POSITION, position);
                Log.v("Note",
                        "notePosition | NotesListViewActivity wurde abgesendet "
                                + position);
                //startActivityForResult(intent, show_single_note_cmd);
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, v, getString(R.string.noteTransition));
            ActivityCompat.startActivityForResult(this, intent, show_single_note_cmd, optionsCompat.toBundle());

        }
    }

    /** Helper function to reduce codeduplication, called from longClick and click
     * @param position Postion of the selected note
     * @return if Note was selected true, if Note was deselected false
     */
    private boolean selectNote(int position, View v){
        boolean selected=adapter.select(position);
        v.setSelected(selected);
        if(selected) {
            Log.v("Note", "notePosition | Note wurde ausgewaehlt "
                    + position);
            int checkedItemCount = adapter.getSelected().size();
            // first selection needs to start Action Mode
            if(mActionMode == null) {
                mActionMode = startSupportActionMode(new MultiSelectedActionModeCallback());
            }
            mActionMode.setTitle(String.valueOf(checkedItemCount)
                    + " " + getString(R.string.ab_selected));
        }else{
            adapter.deselect(position);
            Log.v("Note", "notePosition | Note wurde abgewaehlt "
                    + position);
            int checkedItemCount = adapter.getSelected().size();
            mActionMode.setTitle(String.valueOf(checkedItemCount)
                    + " " + getString(R.string.ab_selected));
            // last deselection needs to finish ActionMode
            if(checkedItemCount==0) {
                mActionMode.finish();
            }
        }
        return selected;
    }

    /**
     * Long click on a List Item in the RecyclerVIEW
     * @param position Position of the Item in the List
     * @param v Viewholder of the item
     * @return true if the item is selected otherwise false
     */
    @Override
    public boolean onNoteLongClick(int position, View v) {
        return selectNote(position,v);
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
                        //TODO not sync after every deletion, better sync after loop
                            db.deleteNoteAndSync(note.getId());
                            adapter.remove(note);

                    }
                    mode.finish(); // Action picked, so close the CAB
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