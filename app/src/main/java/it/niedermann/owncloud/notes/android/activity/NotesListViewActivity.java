package it.niedermann.owncloud.notes.android.activity;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.helper.ItemTouchHelper.SimpleCallback;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.model.Item;
import it.niedermann.owncloud.notes.model.ItemAdapter;
import it.niedermann.owncloud.notes.model.SectionItem;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;
import it.niedermann.owncloud.notes.persistence.NoteServerSyncHelper;
import it.niedermann.owncloud.notes.util.ICallback;
import it.niedermann.owncloud.notes.util.NotesClientUtil;

public class NotesListViewActivity extends AppCompatActivity implements
        ItemAdapter.NoteClickListener, View.OnClickListener {

    public final static String CREATED_NOTE = "it.niedermann.owncloud.notes.created_notes";
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
    private Vibrator vibrator = null;
    private SearchView searchView = null;
    private ICallback syncCallBack = new ICallback() {
        @Override
        public void onFinish() {
            adapter.clearSelection();
            if (mActionMode != null) {
                mActionMode.finish();
            }
            refreshList();
            swipeRefreshLayout.setRefreshing(false);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // First Run Wizard
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        if (!NoteServerSyncHelper.isConfigured(this)) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivityForResult(settingsIntent, server_settings);
        }

        setContentView(R.layout.activity_notes_list_view);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Display Data
        db = NoteSQLiteOpenHelper.getInstance(this);
        initList();
        refreshList();

        // Pull to Refresh
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefreshlayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(db.getNoteServerSyncHelper().isSyncPossible()) {
                    synchronize();
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getApplicationContext(), getString(R.string.error_sync, getString(NotesClientUtil.LoginStatus.NO_NETWORK.str)), Toast.LENGTH_LONG).show();
                }
            }
        });
        db.getNoteServerSyncHelper().addCallbackPull(syncCallBack);

        // Floating Action Button
        findViewById(R.id.fab_create).setOnClickListener(this);

        // Show persistant notification for creating a new note
        checkNotificationSetting();
    }

    protected void checkNotificationSetting(){
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        Boolean showNotification = preferences.getBoolean("showNotification", false);
        if(showNotification==true){
            // add notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            PendingIntent newNoteIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, CreateNoteActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    0);
            builder.setSmallIcon(R.drawable.ic_action_new)
                    .setContentTitle(getString(R.string.action_create))
                    .setContentText("")
                    .setOngoing(true)
                    .setContentIntent(newNoteIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_SECRET);
            NotificationManager notificationManager = (NotificationManager) getSystemService(
                    NOTIFICATION_SERVICE);

            notificationManager.notify(10, builder.build());
        }
        else{
            // remove notification
            NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
            nMgr.cancel(10);
        }
    }

    /**
     * Perform refresh on every time the activity gets visible
     */
    @Override
    protected void onResume() {
        // Show persistant notification for creating a new note
        checkNotificationSetting();
        if (db.getNoteServerSyncHelper().isSyncPossible()) {
            synchronize();
        } else {
            refreshList();
        }
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
     * Allows other classes to refresh the List of Notes. Starts an AsyncTask which loads the data in the background.
     */
    public void refreshList() {
        new RefreshListTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class RefreshListTask extends AsyncTask<Void, Void, List<Item>> {

        private CharSequence query = null;

        @Override
        protected void onPreExecute() {
            if(searchView != null && !searchView.isIconified() && searchView.getQuery().length() != 0) {
                query = searchView.getQuery();
            }
        }

        @Override
        protected List<Item> doInBackground(Void... voids) {
            List<DBNote> noteList;
            if (query==null) {
                noteList = db.getNotes();
            } else {
                noteList = db.searchNotes(query);
            }

            final List<Item> itemList = new ArrayList<>();
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
                DBNote currentNote = noteList.get(i);
                if (currentNote.isFavorite()) {
                    // don't show as new section
                } else if (!todaySet && currentNote.getModified().getTimeInMillis() >= today.getTimeInMillis()) {
                    // after 00:00 today
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
                itemList.add(currentNote);
            }

            return itemList;
        }

        @Override
        protected void onPostExecute(List<Item> items) {
            adapter.setItemList(items);
        }
    }

    public void initList() {
        adapter = new ItemAdapter(this);
        listView = (RecyclerView) findViewById(R.id.recycler_view);
        listView.setAdapter(adapter);
        listView.setLayoutManager(new LinearLayoutManager(this));
        ItemTouchHelper touchHelper = new ItemTouchHelper(new SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            private boolean active=false;

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            /**
             * Disable swipe on sections
             *
             * @param recyclerView RecyclerView
             * @param viewHolder   RecyclerView.ViewHoler
             * @return 0 if section, otherwise super()
             */
            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof ItemAdapter.SectionViewHolder) return 0;
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            /**
             * Delete note if note is swiped to left or right
             *
             * @param viewHolder RecyclerView.ViewHoler
             * @param direction  int
             */
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT || direction == ItemTouchHelper.RIGHT) {
                    final DBNote dbNote = (DBNote) adapter.getItem(viewHolder.getAdapterPosition());
                    db.deleteNoteAndSync((dbNote).getId());
                    adapter.remove(dbNote);
                    refreshList();
                    Log.v("Note", "Item deleted through swipe ----------------------------------------------");
                    Snackbar.make(swipeRefreshLayout, R.string.action_note_deleted, Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_undo, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    db.addNoteAndSync(dbNote);
                                    refreshList();
                                    Snackbar.make(swipeRefreshLayout, R.string.action_note_restored, Snackbar.LENGTH_SHORT)
                                            .show();
                                }
                            })
                            .show();
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if(!active && isCurrentlyActive) {
                    active=true;
                    vibrator.vibrate(20);
                    Log.v(getClass().getSimpleName(), "vibrate");
                }
                ItemAdapter.NoteViewHolder noteViewHolder = (ItemAdapter.NoteViewHolder) viewHolder;
                // show delete icon on the right side
                noteViewHolder.showSwipeDelete(dX>0);
                // move only swipeable part of item (not leave-behind)
                getDefaultUIUtil().onDraw(c, recyclerView, noteViewHolder.noteSwipeable, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                getDefaultUIUtil().clearView(((ItemAdapter.NoteViewHolder) viewHolder).noteSwipeable);
                active=false;
            }
        });
        touchHelper.attachToRecyclerView(listView);
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
                refreshList();
                return true;
            }
        });
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            searchView.setQuery(intent.getStringExtra(SearchManager.QUERY), true);
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

                DBNote createdNote = (DBNote) data.getExtras().getSerializable(CREATED_NOTE);
                adapter.add(createdNote);
                //setListView(db.getNotes());
            }
        } else if (requestCode == show_single_note_cmd) {
            if (resultCode == RESULT_OK || resultCode == RESULT_FIRST_USER) {
                int notePosition = data.getExtras().getInt(EditNoteActivity.PARAM_NOTE_POSITION);
                if(adapter.getItemCount()>notePosition) {
                    Item oldItem = adapter.getItem(notePosition);
                    if (resultCode == RESULT_FIRST_USER) {
                        adapter.remove(oldItem);
                    }
                    if (resultCode == RESULT_OK) {
                        DBNote editedNote = (DBNote) data.getExtras().getSerializable(EditNoteActivity.PARAM_NOTE);
                        adapter.replace(editedNote, notePosition);
                        refreshList();
                    }
                }
            }
        } else if (requestCode == server_settings) {
            // Create new Instance with new URL and credentials
            db = NoteSQLiteOpenHelper.getInstance(this);
            if(db.getNoteServerSyncHelper().isSyncPossible()) {
                adapter.removeAll();
                swipeRefreshLayout.setRefreshing(true);
                synchronize();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.error_sync, getString(NotesClientUtil.LoginStatus.NO_NETWORK.str)), Toast.LENGTH_LONG).show();
            }
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
            Item item = adapter.getItem(position);
            Intent intent = new Intent(getApplicationContext(), EditNoteActivity.class);
            intent.putExtra(EditNoteActivity.PARAM_NOTE, (DBNote) item);
            intent.putExtra(EditNoteActivity.PARAM_NOTE_POSITION, position);
            startActivityForResult(intent, show_single_note_cmd);

        }
    }

    @Override
    public void onNoteFavoriteClick(int position, View view) {
        DBNote note = (DBNote) adapter.getItem(position);
        NoteSQLiteOpenHelper db = NoteSQLiteOpenHelper.getInstance(view.getContext());
        db.toggleFavorite(note, syncCallBack);
        adapter.notifyItemChanged(position);
        refreshList();
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
        if (searchView==null || searchView.isIconified()) {
            super.onBackPressed();
        } else {
            searchView.setIconified(true);
        }
    }

    private void synchronize() {
        db.getNoteServerSyncHelper().addCallbackPull(syncCallBack);
        db.getNoteServerSyncHelper().scheduleSync(false);
    }

    /**
     * Handler for the MultiSelect Actions
     */
    private class MultiSelectedActionModeCallback implements ActionMode.Callback {

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
                        DBNote note = (DBNote) adapter.getItem(i);
                        db.deleteNoteAndSync(note.getId());
                        // Not needed because of dbsync
                        //adapter.remove(note);
                    }
                    mode.finish(); // Action picked, so close the CAB
                    //after delete selection has to be cleared
                    searchView.setIconified(true);
                    refreshList();
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