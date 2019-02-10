package it.niedermann.owncloud.notes.android.activity;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.Category;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.model.Item;
import it.niedermann.owncloud.notes.model.ItemAdapter;
import it.niedermann.owncloud.notes.model.NavigationAdapter;
import it.niedermann.owncloud.notes.persistence.LoadNotesListTask;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;
import it.niedermann.owncloud.notes.persistence.NoteServerSyncHelper;
import it.niedermann.owncloud.notes.util.ICallback;
import it.niedermann.owncloud.notes.util.NoteUtil;
import it.niedermann.owncloud.notes.util.NotesClientUtil;

public class NotesListViewActivity extends AppCompatActivity implements ItemAdapter.NoteClickListener {

    public final static String CREATED_NOTE = "it.niedermann.owncloud.notes.created_notes";
    public final static String CREDENTIALS_CHANGED = "it.niedermann.owncloud.notes.CREDENTIALS_CHANGED";
    public static final String ADAPTER_KEY_RECENT = "recent";
    public static final String ADAPTER_KEY_STARRED = "starred";

    private static final String SAVED_STATE_NAVIGATION_SELECTION = "navigationSelection";
    private static final String SAVED_STATE_NAVIGATION_ADAPTER_SLECTION = "navigationAdapterSelection";
    private static final String SAVED_STATE_NAVIGATION_OPEN = "navigationOpen";

    private final static int create_note_cmd = 0;
    private final static int show_single_note_cmd = 1;
    private final static int server_settings = 2;
    private final static int about = 3;


    @BindView(R.id.notesListActivityActionBar)
    Toolbar toolbar;
    @BindView(R.id.drawerLayout)
    DrawerLayout drawerLayout;
    @BindView(R.id.account)
    TextView account;
    @BindView(R.id.swiperefreshlayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.fab_create)
    FloatingActionButton fabCreate;
    @BindView(R.id.navigationList)
    RecyclerView listNavigationCategories;
    @BindView(R.id.navigationMenu)
    RecyclerView listNavigationMenu;
    @BindView(R.id.recycler_view)
    RecyclerView listView;

    private ActionBarDrawerToggle drawerToggle;
    private ItemAdapter adapter = null;
    private NavigationAdapter adapterCategories;
    private NavigationAdapter.NavigationItem itemRecent, itemFavorites, itemUncategorized;
    private Category navigationSelection = new Category(null, null);
    private String navigationOpen = "";
    private ActionMode mActionMode;
    private NoteSQLiteOpenHelper db = null;
    private SearchView searchView = null;
    private ICallback syncCallBack = new ICallback() {
        @Override
        public void onFinish() {
            adapter.clearSelection();
            if (mActionMode != null) {
                mActionMode.finish();
            }
            refreshLists();
            swipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onScheduled() {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // First Run Wizard
        if (!NoteServerSyncHelper.isConfigured(this)) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivityForResult(settingsIntent, server_settings);
        }
        String categoryAdapterSelectedItem = ADAPTER_KEY_RECENT;
        if (savedInstanceState != null) {
            navigationSelection = (Category) savedInstanceState.getSerializable(SAVED_STATE_NAVIGATION_SELECTION);
            navigationOpen = savedInstanceState.getString(SAVED_STATE_NAVIGATION_OPEN);
            categoryAdapterSelectedItem = savedInstanceState.getString(SAVED_STATE_NAVIGATION_ADAPTER_SLECTION);
        }

        setContentView(R.layout.drawer_layout);
        ButterKnife.bind(this);

        db = NoteSQLiteOpenHelper.getInstance(this);

        setupActionBar();
        setupNotesList();
        setupNavigationList(categoryAdapterSelectedItem);
        setupNavigationMenu();
    }

    @Override
    protected void onResume() {
        // refresh and sync every time the activity gets visible
        refreshLists();
        db.getNoteServerSyncHelper().addCallbackPull(syncCallBack);
        if (db.getNoteServerSyncHelper().isSyncPossible()) {
            synchronize();
        }
        super.onResume();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.syncState();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVED_STATE_NAVIGATION_SELECTION, navigationSelection);
        outState.putString(SAVED_STATE_NAVIGATION_ADAPTER_SLECTION, adapterCategories.getSelectedItem());
        outState.putString(SAVED_STATE_NAVIGATION_OPEN, navigationOpen);
    }

    private void setupActionBar() {
        setSupportActionBar(toolbar);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.action_drawer_open, R.string.action_drawer_close);
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(drawerToggle);
    }

    private void setupNotesList() {
        initList();
        // Pull to Refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (db.getNoteServerSyncHelper().isSyncPossible()) {
                synchronize();
            } else {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getApplicationContext(), getString(R.string.error_sync, getString(NotesClientUtil.LoginStatus.NO_NETWORK.str)), Toast.LENGTH_LONG).show();
            }
        });

        // Floating Action Button
        fabCreate.setOnClickListener((View view) -> {
            Intent createIntent = new Intent(getApplicationContext(), EditNoteActivity.class);
            createIntent.putExtra(EditNoteActivity.PARAM_CATEGORY, navigationSelection);
            startActivityForResult(createIntent, create_note_cmd);
        });
    }

    private void setupNavigationList(final String selectedItem) {
        itemRecent = new NavigationAdapter.NavigationItem(ADAPTER_KEY_RECENT, getString(R.string.label_all_notes), null, R.drawable.ic_access_time_grey600_24dp);
        itemFavorites = new NavigationAdapter.NavigationItem(ADAPTER_KEY_STARRED, getString(R.string.label_favorites), null, R.drawable.ic_star_yellow_24dp);
        adapterCategories = new NavigationAdapter(new NavigationAdapter.ClickListener() {
            @Override
            public void onItemClick(NavigationAdapter.NavigationItem item) {
                selectItem(item, true);
            }

            private void selectItem(NavigationAdapter.NavigationItem item, boolean closeNavigation) {
                adapterCategories.setSelectedItem(item.id);

                // update current selection
                if (itemRecent == item) {
                    navigationSelection = new Category(null, null);
                } else if (itemFavorites == item) {
                    navigationSelection = new Category(null, true);
                } else if (itemUncategorized == item) {
                    navigationSelection = new Category("", null);
                } else {
                    navigationSelection = new Category(item.label, null);
                }

                // auto-close sub-folder in Navigation if selection is outside of that folder
                if (navigationOpen != null) {
                    int slashIndex = navigationSelection.category == null ? -1 : navigationSelection.category.indexOf('/');
                    String rootCategory = slashIndex < 0 ? navigationSelection.category : navigationSelection.category.substring(0, slashIndex);
                    if (!navigationOpen.equals(rootCategory)) {
                        navigationOpen = null;
                    }
                }

                // update views
                if (closeNavigation) {
                    drawerLayout.closeDrawers();
                }
                refreshLists(true);
            }

            @Override
            public void onIconClick(NavigationAdapter.NavigationItem item) {
                if (item.icon == NavigationAdapter.ICON_MULTIPLE && !item.label.equals(navigationOpen)) {
                    navigationOpen = item.label;
                    selectItem(item, false);
                } else if (item.icon == NavigationAdapter.ICON_MULTIPLE || item.icon == NavigationAdapter.ICON_MULTIPLE_OPEN && item.label.equals(navigationOpen)) {
                    navigationOpen = null;
                    refreshLists();
                } else {
                    onItemClick(item);
                }
            }
        });
        adapterCategories.setSelectedItem(selectedItem);
        listNavigationCategories.setAdapter(adapterCategories);
    }

    private class LoadCategoryListTask extends AsyncTask<Void, Void, List<NavigationAdapter.NavigationItem>> {
        @Override
        protected List<NavigationAdapter.NavigationItem> doInBackground(Void... voids) {
            List<NavigationAdapter.NavigationItem> categories = db.getCategories();
            if (!categories.isEmpty() && categories.get(0).label.isEmpty()) {
                itemUncategorized = categories.get(0);
                itemUncategorized.label = getString(R.string.action_uncategorized);
                itemUncategorized.icon = NavigationAdapter.ICON_NOFOLDER;
            } else {
                itemUncategorized = null;
            }

            Map<String, Integer> favorites = db.getFavoritesCount();
            int numFavorites = favorites.containsKey("1") ? favorites.get("1") : 0;
            int numNonFavorites = favorites.containsKey("0") ? favorites.get("0") : 0;
            itemFavorites.count = numFavorites;
            itemRecent.count = numFavorites + numNonFavorites;

            ArrayList<NavigationAdapter.NavigationItem> items = new ArrayList<>();
            items.add(itemRecent);
            items.add(itemFavorites);
            NavigationAdapter.NavigationItem lastPrimaryCategory = null, lastSecondaryCategory = null;
            for (NavigationAdapter.NavigationItem item : categories) {
                int slashIndex = item.label.indexOf('/');
                String currentPrimaryCategory = slashIndex < 0 ? item.label : item.label.substring(0, slashIndex);
                String currentSecondaryCategory = null;
                boolean isCategoryOpen = currentPrimaryCategory.equals(navigationOpen);

                if (isCategoryOpen && !currentPrimaryCategory.equals(item.label)) {
                    String currentCategorySuffix = item.label.substring(navigationOpen.length() + 1);
                    int subSlashIndex = currentCategorySuffix.indexOf('/');
                    currentSecondaryCategory = subSlashIndex < 0 ? currentCategorySuffix : currentCategorySuffix.substring(0, subSlashIndex);
                }

                boolean belongsToLastPrimaryCategory = lastPrimaryCategory != null && currentPrimaryCategory.equals(lastPrimaryCategory.label);
                boolean belongsToLastSecondaryCategory = belongsToLastPrimaryCategory && lastSecondaryCategory != null && lastSecondaryCategory.label.equals(currentPrimaryCategory + "/" + currentSecondaryCategory);

                if (isCategoryOpen && !belongsToLastPrimaryCategory && currentSecondaryCategory != null) {
                    lastPrimaryCategory = new NavigationAdapter.NavigationItem("category:" + currentPrimaryCategory, currentPrimaryCategory, 0, NavigationAdapter.ICON_MULTIPLE_OPEN);
                    items.add(lastPrimaryCategory);
                    belongsToLastPrimaryCategory = true;
                }

                if (belongsToLastPrimaryCategory && belongsToLastSecondaryCategory) {
                    lastSecondaryCategory.count += item.count;
                    lastSecondaryCategory.icon = NavigationAdapter.ICON_SUB_MULTIPLE;
                } else if (belongsToLastPrimaryCategory) {
                    if (isCategoryOpen) {
                        item.label = currentPrimaryCategory + "/" + currentSecondaryCategory;
                        item.id = "category:" + item.label;
                        item.icon = NavigationAdapter.ICON_SUB_FOLDER;
                        items.add(item);
                        lastSecondaryCategory = item;
                    } else {
                        lastPrimaryCategory.count += item.count;
                        lastPrimaryCategory.icon = NavigationAdapter.ICON_MULTIPLE;
                        lastSecondaryCategory = null;
                    }
                } else {
                    if (isCategoryOpen) {
                        item.icon = NavigationAdapter.ICON_MULTIPLE_OPEN;
                    } else {
                        item.label = currentPrimaryCategory;
                        item.id = "category:" + item.label;
                    }
                    items.add(item);
                    lastPrimaryCategory = item;
                    lastSecondaryCategory = null;
                }
            }
            return items;
        }

        @Override
        protected void onPostExecute(List<NavigationAdapter.NavigationItem> items) {
            adapterCategories.setItems(items);
        }
    }

    private void setupNavigationMenu() {
        final NavigationAdapter.NavigationItem itemTrashbin = new NavigationAdapter.NavigationItem("trashbin", getString(R.string.action_trashbin), null, R.drawable.ic_delete_grey600_24dp);
        final NavigationAdapter.NavigationItem itemSettings = new NavigationAdapter.NavigationItem("settings", getString(R.string.action_settings), null, R.drawable.ic_settings_grey600_24dp);
        final NavigationAdapter.NavigationItem itemAbout = new NavigationAdapter.NavigationItem("about", getString(R.string.simple_about), null, R.drawable.ic_info_outline_grey600_24dp);

        ArrayList<NavigationAdapter.NavigationItem> itemsMenu = new ArrayList<>();
        itemsMenu.add(itemTrashbin);
        itemsMenu.add(itemSettings);
        itemsMenu.add(itemAbout);

        NavigationAdapter adapterMenu = new NavigationAdapter(new NavigationAdapter.ClickListener() {
            @Override
            public void onItemClick(NavigationAdapter.NavigationItem item) {
                if (item == itemSettings) {
                    Intent settingsIntent = new Intent(getApplicationContext(), PreferencesActivity.class);
                    startActivityForResult(settingsIntent, server_settings);
                } else if (item == itemAbout) {
                    Intent aboutIntent = new Intent(getApplicationContext(), AboutActivity.class);
                    startActivityForResult(aboutIntent, about);
                } else if (item == itemTrashbin) {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String url = preferences.getString(SettingsActivity.SETTINGS_URL, SettingsActivity.DEFAULT_SETTINGS);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url + "index.php/apps/files/?dir=/&view=trashbin")));
                }
            }

            @Override
            public void onIconClick(NavigationAdapter.NavigationItem item) {
                onItemClick(item);
            }
        });


        this.updateUsernameInDrawer();
        final NotesListViewActivity that = this;
        this.account.setOnClickListener((View v) -> {
            Intent settingsIntent = new Intent(that, SettingsActivity.class);
            startActivityForResult(settingsIntent, server_settings);
        });

        adapterMenu.setItems(itemsMenu);
        listNavigationMenu.setAdapter(adapterMenu);
    }

    public void initList() {
        adapter = new ItemAdapter(this);
        listView.setAdapter(adapter);
        listView.setLayoutManager(new LinearLayoutManager(this));
        ItemTouchHelper touchHelper = new ItemTouchHelper(new SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
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
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
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
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                switch (direction) {
                    case ItemTouchHelper.LEFT: {
                        final DBNote dbNote = (DBNote) adapter.getItem(viewHolder.getAdapterPosition());
                        db.deleteNoteAndSync((dbNote).getId());
                        adapter.remove(dbNote);
                        refreshLists();
                        Log.v("Note", "Item deleted through swipe ----------------------------------------------");
                        Snackbar.make(swipeRefreshLayout, R.string.action_note_deleted, Snackbar.LENGTH_LONG)
                                .setAction(R.string.action_undo, (View v) -> {
                                    db.addNoteAndSync(dbNote);
                                    refreshLists();
                                    Snackbar.make(swipeRefreshLayout, R.string.action_note_restored, Snackbar.LENGTH_SHORT)
                                            .show();
                                })
                                .show();
                        break;
                    }
                    case ItemTouchHelper.RIGHT: {
                        final DBNote dbNote = (DBNote) adapter.getItem(viewHolder.getAdapterPosition());
                        db.toggleFavorite(dbNote, syncCallBack);
                        refreshLists();
                        break;
                    }
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                ItemAdapter.NoteViewHolder noteViewHolder = (ItemAdapter.NoteViewHolder) viewHolder;
                // show swipe icon on the side
                noteViewHolder.showSwipe(dX > 0);
                // move only swipeable part of item (not leave-behind)
                getDefaultUIUtil().onDraw(c, recyclerView, noteViewHolder.noteSwipeable, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                getDefaultUIUtil().clearView(((ItemAdapter.NoteViewHolder) viewHolder).noteSwipeable);
            }
        });
        touchHelper.attachToRecyclerView(listView);
    }

    private void refreshLists() {
        refreshLists(false);
    }

    private void refreshLists(final boolean scrollToTop) {
        String subtitle = "";
        if (navigationSelection.category != null) {
            if (navigationSelection.category.isEmpty()) {
                subtitle = getString(R.string.action_uncategorized);
            } else {
                subtitle = NoteUtil.extendCategory(navigationSelection.category);
            }
        } else if (navigationSelection.favorite != null && navigationSelection.favorite) {
            subtitle = getString(R.string.label_favorites);
        } else {
            subtitle = getString(R.string.app_name);
        }
        setTitle(subtitle);
        CharSequence query = null;
        if (searchView != null && !searchView.isIconified() && searchView.getQuery().length() != 0) {
            query = searchView.getQuery();
        }

        LoadNotesListTask.NotesLoadedListener callback = (List<Item> notes, boolean showCategory) -> {
            adapter.setShowCategory(showCategory);
            adapter.setItemList(notes);
            if (scrollToTop) {
                listView.scrollToPosition(0);
            }
        };
        new LoadNotesListTask(getApplicationContext(), callback, navigationSelection, query).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new LoadCategoryListTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public ItemAdapter getItemAdapter() {
        return adapter;
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
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
        searchView = (SearchView) item.getActionView();

        final LinearLayout searchEditFrame = searchView.findViewById(R.id
                .search_edit_frame);

        searchEditFrame.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            int oldVisibility = -1;

            @Override
            public void onGlobalLayout() {
                int currentVisibility = searchEditFrame.getVisibility();

                if (currentVisibility != oldVisibility) {
                    if (currentVisibility == View.VISIBLE) {
                        fabCreate.hide();
                    } else {
                        new Handler().postDelayed(() -> {
                            fabCreate.show();
                        }, 150);
                    }

                    oldVisibility = currentVisibility;
                }
            }

        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                refreshLists();
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

                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    DBNote createdNote = (DBNote) data.getExtras().getSerializable(CREATED_NOTE);
                    if (createdNote != null) {
                        adapter.add(createdNote);
                    } else {
                        Log.w(NotesListViewActivity.class.getSimpleName(), "createdNote is null");
                    }
                } else {
                    Log.w(NotesListViewActivity.class.getSimpleName(), "bundle is null");
                }
            }
            listView.scrollToPosition(0);
        } else if (requestCode == server_settings) {
            // Recreate activity completely, because theme switchting makes problems when only invalidating the views.
            // @see https://github.com/stefan-niedermann/nextcloud-notes/issues/529
            recreate();
        }
    }

    private void updateUsernameInDrawer() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String username = preferences.getString(SettingsActivity.SETTINGS_USERNAME, SettingsActivity.DEFAULT_SETTINGS);
        String url = preferences.getString(SettingsActivity.SETTINGS_URL, SettingsActivity.DEFAULT_SETTINGS);
        if (url != null) {
            url = url.replace("https://", "").replace("http://", "");
        } else {
            Log.w(NotesListViewActivity.class.getSimpleName(), "url is null");
        }
        if (!SettingsActivity.DEFAULT_SETTINGS.equals(username) && !SettingsActivity.DEFAULT_SETTINGS.equals(url)) {
            this.account.setText(username + "@" + url.substring(0, url.length() - 1));
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
            DBNote note = (DBNote) adapter.getItem(position);
            Intent intent = new Intent(getApplicationContext(), EditNoteActivity.class);
            intent.putExtra(EditNoteActivity.PARAM_NOTE_ID, note.getId());
            startActivityForResult(intent, show_single_note_cmd);

        }
    }

    @Override
    public void onNoteFavoriteClick(int position, View view) {
        DBNote note = (DBNote) adapter.getItem(position);
        NoteSQLiteOpenHelper db = NoteSQLiteOpenHelper.getInstance(view.getContext());
        db.toggleFavorite(note, syncCallBack);
        adapter.notifyItemChanged(position);
        refreshLists();
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
        if (searchView == null || searchView.isIconified()) {
            super.onBackPressed();
        } else {
            searchView.setIconified(true);
        }
    }

    private void synchronize() {
        swipeRefreshLayout.setRefreshing(true);
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
            mode.getMenuInflater().inflate(R.menu.menu_list_context_multiple, menu);
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
                    refreshLists();
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