package it.niedermann.owncloud.notes.main;

import android.animation.AnimatorInflater;
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.exceptions.AccountImportCancelledException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.exceptions.TokenMismatchException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.util.List;

import it.niedermann.owncloud.notes.ImportAccountActivity;
import it.niedermann.owncloud.notes.LockedActivity;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.accountpicker.AccountPickerListener;
import it.niedermann.owncloud.notes.accountswitcher.AccountSwitcherDialog;
import it.niedermann.owncloud.notes.accountswitcher.AccountSwitcherListener;
import it.niedermann.owncloud.notes.branding.BrandedSnackbar;
import it.niedermann.owncloud.notes.databinding.ActivityNotesListViewBinding;
import it.niedermann.owncloud.notes.databinding.DrawerLayoutBinding;
import it.niedermann.owncloud.notes.edit.EditNoteActivity;
import it.niedermann.owncloud.notes.edit.category.CategoryDialogFragment;
import it.niedermann.owncloud.notes.exception.ExceptionDialogFragment;
import it.niedermann.owncloud.notes.main.items.ItemAdapter;
import it.niedermann.owncloud.notes.main.items.grid.GridItemDecoration;
import it.niedermann.owncloud.notes.main.items.list.NotesListViewItemTouchHelper;
import it.niedermann.owncloud.notes.main.items.section.SectionItemDecoration;
import it.niedermann.owncloud.notes.main.menu.MenuAdapter;
import it.niedermann.owncloud.notes.main.navigation.NavigationAdapter;
import it.niedermann.owncloud.notes.main.navigation.NavigationClickListener;
import it.niedermann.owncloud.notes.main.navigation.NavigationItem;
import it.niedermann.owncloud.notes.persistence.CapabilitiesClient;
import it.niedermann.owncloud.notes.persistence.CapabilitiesWorker;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Category;
import it.niedermann.owncloud.notes.persistence.entity.NoteWithCategory;
import it.niedermann.owncloud.notes.shared.model.Capabilities;
import it.niedermann.owncloud.notes.shared.model.CategorySortingMethod;
import it.niedermann.owncloud.notes.shared.model.NavigationCategory;
import it.niedermann.owncloud.notes.shared.model.NoteClickListener;
import it.niedermann.owncloud.notes.shared.util.NoteUtil;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.O;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static it.niedermann.owncloud.notes.NotesApplication.isDarkThemeActive;
import static it.niedermann.owncloud.notes.NotesApplication.isGridViewEnabled;
import static it.niedermann.owncloud.notes.branding.BrandingUtil.getSecondaryForegroundColorDependingOnTheme;
import static it.niedermann.owncloud.notes.main.menu.MenuAdapter.SERVER_SETTINGS;
import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.DEFAULT_CATEGORY;
import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.FAVORITES;
import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.RECENT;
import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.UNCATEGORIZED;
import static it.niedermann.owncloud.notes.shared.util.ColorUtil.contrastRatioIsSufficient;
import static it.niedermann.owncloud.notes.shared.util.SSOUtil.askForNewAccount;

public class MainActivity extends LockedActivity implements NoteClickListener, AccountPickerListener, AccountSwitcherListener, CategoryDialogFragment.CategoryDialogListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    protected MainViewModel mainViewModel;

    private boolean gridView = true;

    public static final String CREATED_NOTE = "it.niedermann.owncloud.notes.created_notes";
    public static final String ADAPTER_KEY_RECENT = "recent";
    public static final String ADAPTER_KEY_STARRED = "starred";
    public static final String ADAPTER_KEY_UNCATEGORIZED = "uncategorized";

    private final static int create_note_cmd = 0;
    private final static int show_single_note_cmd = 1;

    protected ItemAdapter adapter;
    private NavigationAdapter adapterCategories;
    private MenuAdapter menuAdapter;

    protected Account localAccount;

    protected DrawerLayoutBinding binding;
    protected ActivityNotesListViewBinding activityBinding;
    protected FloatingActionButton fabCreate;
    private CoordinatorLayout coordinatorLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView listView;
    private ActionMode mActionMode;

    boolean canMoveNoteToAnotherAccounts = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        CapabilitiesWorker.update(this);
        binding = DrawerLayoutBinding.inflate(getLayoutInflater());
        activityBinding = ActivityNotesListViewBinding.bind(binding.activityNotesListView.getRoot());

        setContentView(binding.getRoot());

        this.coordinatorLayout = binding.activityNotesListView.activityNotesListView;
        this.swipeRefreshLayout = binding.activityNotesListView.swiperefreshlayout;
        this.fabCreate = binding.activityNotesListView.fabCreate;
        this.listView = binding.activityNotesListView.recyclerView;

        gridView = isGridViewEnabled();

        if (!gridView || isDarkThemeActive(this)) {
            activityBinding.activityNotesListView.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));
        }

        setupToolbars();
        setupNavigationList();
        setupNotesList();

        mainViewModel.getAccountsCount().observe(this, (count) -> {
            if (count == 0) {
                startActivityForResult(new Intent(this, ImportAccountActivity.class), ImportAccountActivity.REQUEST_CODE_IMPORT_ACCOUNT);
            } else {
                try {
                    mainViewModel.postCurrentAccount(mainViewModel.getLocalAccountByAccountName(SingleAccountHelper.getCurrentSingleSignOnAccount(getApplicationContext()).name));
                } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
                    ExceptionDialogFragment.newInstance(e).show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName());
                }
            }
        });

        mainViewModel.hasMultipleAccountsConfigured().observe(this, (hasMultipleAccountsConfigured) -> canMoveNoteToAnotherAccounts = hasMultipleAccountsConfigured);
        mainViewModel.getSyncStatus().observe(this, (syncStatus) -> swipeRefreshLayout.setRefreshing(syncStatus));
        mainViewModel.getSyncErrors().observe(this, (exceptions) -> BrandedSnackbar.make(coordinatorLayout, R.string.error_synchronization, Snackbar.LENGTH_LONG)
                .setAction(R.string.simple_more, v -> ExceptionDialogFragment.newInstance(exceptions)
                        .show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName()))
                .show());
        mainViewModel.getSelectedCategory().observe(this, (selectedCategory) -> {
            binding.activityNotesListView.emptyContentView.getRoot().setVisibility(GONE);
            binding.activityNotesListView.progressCircular.setVisibility(VISIBLE);
            adapter.setShowCategory(selectedCategory.getType() == RECENT || selectedCategory.getType() == FAVORITES);
            fabCreate.show();

            switch (selectedCategory.getType()) {
                case RECENT: {
                    activityBinding.searchText.setText(getString(R.string.search_in_all));
                    break;
                }
                case FAVORITES: {
                    activityBinding.searchText.setText(getString(R.string.search_in_category, getString(R.string.label_favorites)));
                    break;
                }
                case UNCATEGORIZED: {
                    activityBinding.searchText.setText(getString(R.string.search_in_category, getString(R.string.action_uncategorized)));
                    break;
                }
                case DEFAULT_CATEGORY:
                default: {
                    final Category category = selectedCategory.getCategory();
                    if (category == null) {
                        throw new IllegalStateException(NavigationCategory.class.getSimpleName() + " type is " + DEFAULT_CATEGORY + ", but category is null.");
                    }
                    activityBinding.searchText.setText(getString(R.string.search_in_category, NoteUtil.extendCategory(category.getTitle())));
                    break;
                }
            }

            fabCreate.setOnClickListener((View view) -> {
                Intent createIntent = new Intent(getApplicationContext(), EditNoteActivity.class);
                createIntent.putExtra(EditNoteActivity.PARAM_CATEGORY, selectedCategory);
                if (activityBinding.searchView.getQuery().length() > 0) {
                    createIntent.putExtra(EditNoteActivity.PARAM_CONTENT, activityBinding.searchView.getQuery().toString());
                    invalidateOptionsMenu();
                }
                startActivityForResult(createIntent, create_note_cmd);
            });
        });
        mainViewModel.getNotesListLiveData().observe(this, notes -> {
            adapter.clearSelection(listView);
            if (mActionMode != null) {
                mActionMode.finish();
            }
            adapter.setItemList(notes);
            binding.activityNotesListView.progressCircular.setVisibility(GONE);
            binding.activityNotesListView.emptyContentView.getRoot().setVisibility(notes.size() > 0 ? GONE : VISIBLE);
        });
        mainViewModel.getSearchTerm().observe(this, adapter::setHighlightSearchQuery);
        mainViewModel.getCategorySortingMethodOfSelectedCategory().observe(this, methodOfCategory -> {
            updateSortMethodIcon(methodOfCategory.second);
            activityBinding.sortingMethod.setOnClickListener((v) -> {
                if (methodOfCategory.first != null) {
                    CategorySortingMethod newMethod = methodOfCategory.second;
                    if (newMethod == CategorySortingMethod.SORT_LEXICOGRAPHICAL_ASC) {
                        newMethod = CategorySortingMethod.SORT_MODIFIED_DESC;
                    } else {
                        newMethod = CategorySortingMethod.SORT_LEXICOGRAPHICAL_ASC;
                    }
                    mainViewModel.modifyCategoryOrder(localAccount.getId(), methodOfCategory.first, newMethod);
                }
            });
        });
        mainViewModel.getNavigationCategories().observe(this, navigationItems -> this.adapterCategories.setItems(navigationItems));
        mainViewModel.getCurrentAccount().observe(this, (a) -> {
            fabCreate.hide();
            localAccount = a;
            Glide
                    .with(this)
                    .load(a.getUrl() + "/index.php/avatar/" + Uri.encode(a.getUserName()) + "/64")
                    .placeholder(R.drawable.ic_account_circle_grey_24dp)
                    .error(R.drawable.ic_account_circle_grey_24dp)
                    .apply(RequestOptions.circleCropTransform())
                    .into(activityBinding.launchAccountSwitcher);

            new NotesListViewItemTouchHelper(a, this, mainViewModel, this, adapter, swipeRefreshLayout, coordinatorLayout, gridView)
                    .attachToRecyclerView(listView);
            if (!mainViewModel.synchronize(a)) {
                BrandedSnackbar.make(coordinatorLayout, getString(R.string.error_sync, getString(R.string.error_no_network)), Snackbar.LENGTH_LONG).show();
            }
            fabCreate.show();
            activityBinding.launchAccountSwitcher.setOnClickListener((v) -> {
                AccountSwitcherDialog.newInstance(localAccount.getId()).show(getSupportFragmentManager(), AccountSwitcherDialog.class.getSimpleName());
            });

            if (menuAdapter == null) {
                menuAdapter = new MenuAdapter(getApplicationContext(), localAccount, (menuItem) -> {
                    @Nullable Integer resultCode = menuItem.getResultCode();
                    if (resultCode == null) {
                        startActivity(menuItem.getIntent());
                    } else {
                        startActivityForResult(menuItem.getIntent(), menuItem.getResultCode());
                    }
                });

                binding.navigationMenu.setAdapter(menuAdapter);
            } else {
                menuAdapter.updateAccount(a);
            }
        });
    }

    @Override
    protected void onResume() {
        // refresh and sync every time the activity gets
        if (!mainViewModel.synchronize(localAccount)) {
            BrandedSnackbar.make(coordinatorLayout, getString(R.string.error_sync, getString(R.string.error_no_network)), Snackbar.LENGTH_LONG).show();
        }
        super.onResume();
    }

    private void setupToolbars() {
        setSupportActionBar(binding.activityNotesListView.toolbar);
        activityBinding.homeToolbar.setOnClickListener((v) -> {
            if (activityBinding.toolbar.getVisibility() == GONE) {
                updateToolbars(false);
            }
        });

        activityBinding.menuButton.setOnClickListener((v) -> binding.drawerLayout.openDrawer(GravityCompat.START));

        final LinearLayout searchEditFrame = activityBinding.searchView.findViewById(R.id.search_edit_frame);

        searchEditFrame.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            int oldVisibility = -1;

            @Override
            public void onGlobalLayout() {
                int currentVisibility = searchEditFrame.getVisibility();

                if (currentVisibility != oldVisibility) {
                    if (currentVisibility == VISIBLE) {
                        fabCreate.hide();
                    } else {
                        new Handler().postDelayed(() -> fabCreate.show(), 150);
                    }

                    oldVisibility = currentVisibility;
                }
            }

        });
        activityBinding.searchView.setOnCloseListener(() -> {
            if (activityBinding.toolbar.getVisibility() == VISIBLE && TextUtils.isEmpty(activityBinding.searchView.getQuery())) {
                updateToolbars(true);
                return true;
            }
            return false;
        });
        activityBinding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mainViewModel.postSearchTerm(newText);
                return true;
            }
        });
    }

    private void setupNotesList() {
        adapter = new ItemAdapter(this, gridView);
        listView.setAdapter(adapter);

        if (gridView) {
            int spanCount = getResources().getInteger(R.integer.grid_view_span_count);
            StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL);
            listView.setLayoutManager(gridLayoutManager);
            listView.addItemDecoration(new GridItemDecoration(adapter, spanCount,
                    getResources().getDimensionPixelSize(R.dimen.spacer_3x),
                    getResources().getDimensionPixelSize(R.dimen.spacer_5x),
                    getResources().getDimensionPixelSize(R.dimen.spacer_3x),
                    getResources().getDimensionPixelSize(R.dimen.spacer_1x),
                    getResources().getDimensionPixelSize(R.dimen.spacer_activity_sides) + getResources().getDimensionPixelSize(R.dimen.spacer_1x)
            ));
        } else {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            listView.setLayoutManager(layoutManager);
            listView.addItemDecoration(new SectionItemDecoration(adapter,
                    getResources().getDimensionPixelSize(R.dimen.spacer_activity_sides) + getResources().getDimensionPixelSize(R.dimen.spacer_1x) + getResources().getDimensionPixelSize(R.dimen.spacer_3x) + getResources().getDimensionPixelSize(R.dimen.spacer_2x),
                    getResources().getDimensionPixelSize(R.dimen.spacer_5x),
                    getResources().getDimensionPixelSize(R.dimen.spacer_1x),
                    0
            ));
        }

        ((RecyclerView) findViewById(R.id.recycler_view)).addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0)
                    fabCreate.hide();
                else if (dy < 0)
                    fabCreate.show();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.i(TAG, "Clearing Glide memory cache");
            Glide.get(this).clearMemory();
            new Thread(() -> {
                Log.i(TAG, "Clearing Glide disk cache");
                Glide.get(getApplicationContext()).clearDiskCache();
            }).start();
            final LiveData<Boolean> syncLiveData = mainViewModel.performFullSynchronizationForCurrentAccount();
            final Observer<Boolean> syncObserver = syncSuccess -> {
                if (!syncSuccess) {
                    BrandedSnackbar.make(coordinatorLayout, getString(R.string.error_sync, getString(R.string.error_no_network)), Snackbar.LENGTH_LONG).show();
                }
                syncLiveData.removeObservers(this);
            };
            syncLiveData.observe(this, syncObserver);
        });
    }

    private void setupNavigationList() {
        adapterCategories = new NavigationAdapter(this, new NavigationClickListener() {
            @Override
            public void onItemClick(NavigationItem item) {
                selectItem(item, true);
            }

            private void selectItem(NavigationItem item, boolean closeNavigation) {
                adapterCategories.setSelectedItem(item.id);
                // update current selection
                if (item.type != null) {
                    switch (item.type) {
                        case RECENT: {
                            mainViewModel.postSelectedCategory(new NavigationCategory(RECENT));
                            break;
                        }
                        case FAVORITES: {
                            mainViewModel.postSelectedCategory(new NavigationCategory(FAVORITES));
                            break;
                        }
                        case UNCATEGORIZED: {
                            mainViewModel.postSelectedCategory(new NavigationCategory(UNCATEGORIZED));
                            break;
                        }
                        default: {
                            if (item.getClass() == NavigationItem.CategoryNavigationItem.class) {
                                mainViewModel.postSelectedCategory(new NavigationCategory(mainViewModel.getCategory(((NavigationItem.CategoryNavigationItem) item).categoryId)));
                            } else {
                                throw new IllegalStateException(NavigationItem.class.getSimpleName() + " type is " + DEFAULT_CATEGORY + ", but item is not of type " + NavigationItem.CategoryNavigationItem.class.getSimpleName() + ".");
                            }
                        }
                    }
                } else {
                    Log.e(TAG, "Unknown item navigation type. Fallback to show " + RECENT);
                    mainViewModel.postSelectedCategory(new NavigationCategory(RECENT));
                }

                if (closeNavigation) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                }
            }

            @Override
            public void onIconClick(NavigationItem item) {
                final LiveData<String> expandedCategoryLiveData = mainViewModel.getExpandedCategory();
                expandedCategoryLiveData.observe(MainActivity.this, expandedCategory -> {
                    if (item.icon == NavigationAdapter.ICON_MULTIPLE && !item.label.equals(expandedCategory)) {
                        mainViewModel.postExpandedCategory(item.label);
                        selectItem(item, false);
                    } else if (item.icon == NavigationAdapter.ICON_MULTIPLE || item.icon == NavigationAdapter.ICON_MULTIPLE_OPEN && item.label.equals(expandedCategory)) {
                        mainViewModel.postExpandedCategory(null);
                    } else {
                        onItemClick(item);
                    }
                    expandedCategoryLiveData.removeObservers(MainActivity.this);
                });
            }
        });
        adapterCategories.setSelectedItem(ADAPTER_KEY_RECENT);
        binding.navigationList.setAdapter(adapterCategories);
    }

    @Override
    public void applyBrand(int mainColor, int textColor) {
        applyBrandToPrimaryToolbar(activityBinding.appBar, activityBinding.toolbar);
        applyBrandToFAB(mainColor, textColor, activityBinding.fabCreate);

        binding.headerView.setBackgroundColor(mainColor);
        binding.appName.setTextColor(textColor);
        if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activityBinding.progressCircular.getIndeterminateDrawable().setColorFilter(getSecondaryForegroundColorDependingOnTheme(this, mainColor), PorterDuff.Mode.SRC_IN);
        }

        // TODO We assume, that the background of the spinner is always white
        activityBinding.swiperefreshlayout.setColorSchemeColors(contrastRatioIsSufficient(Color.WHITE, mainColor) ? mainColor : Color.BLACK);
        binding.appName.setTextColor(textColor);
        DrawableCompat.setTint(binding.logo.getDrawable(), textColor);

        adapter.applyBrand(mainColor, textColor);
        adapterCategories.applyBrand(mainColor, textColor);
        invalidateOptionsMenu();
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (activityBinding.toolbar.getVisibility() == VISIBLE) {
            updateToolbars(true);
            return true;
        } else {
            return super.onSupportNavigateUp();
        }
    }

    /**
     * Updates sorting method icon.
     */
    private void updateSortMethodIcon(CategorySortingMethod method) {
        if (method == CategorySortingMethod.SORT_LEXICOGRAPHICAL_ASC) {
            activityBinding.sortingMethod.setImageResource(R.drawable.alphabetical_asc);
            activityBinding.sortingMethod.setContentDescription(getString(R.string.sort_last_modified));
            if (SDK_INT >= O) {
                activityBinding.sortingMethod.setTooltipText(getString(R.string.sort_last_modified));
            }
        } else {
            activityBinding.sortingMethod.setImageResource(R.drawable.modification_desc);
            activityBinding.sortingMethod.setContentDescription(getString(R.string.sort_alphabetically));
            if (SDK_INT >= O) {
                activityBinding.sortingMethod.setTooltipText(getString(R.string.sort_alphabetically));
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            activityBinding.searchView.setQuery(intent.getStringExtra(SearchManager.QUERY), true);
        }
        super.onNewIntent(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AccountImporter.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
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
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case create_note_cmd: {
                listView.scrollToPosition(0);
                break;
            }
            case SERVER_SETTINGS: {
                // Recreate activity completely, because theme switching makes problems when only invalidating the views.
                // @see https://github.com/stefan-niedermann/nextcloud-notes/issues/529
                recreate();
                break;
            }
            default: {
                try {
                    AccountImporter.onActivityResult(requestCode, resultCode, data, this, (ssoAccount) -> {
                        CapabilitiesWorker.update(this);
                        new Thread(() -> {
                            Log.i(TAG, "Added account: " + "name:" + ssoAccount.name + ", " + ssoAccount.url + ", userId" + ssoAccount.userId);
                            try {
                                Log.i(TAG, "Refreshing capabilities for " + ssoAccount.name);
                                final Capabilities capabilities = CapabilitiesClient.getCapabilities(getApplicationContext(), ssoAccount, null);
                                LiveData<Account> createLiveData = mainViewModel.addAccount(ssoAccount.url, ssoAccount.userId, ssoAccount.name, capabilities);
                                runOnUiThread(() -> createLiveData.observe(this, (account) -> {
                                    Log.i(TAG, capabilities.toString());
                                    runOnUiThread(() -> mainViewModel.postCurrentAccount(mainViewModel.getLocalAccountByAccountName(ssoAccount.name)));
                                }));
                            } catch (Exception e) {
                                // Happens when importing an already existing account the second time
                                if (e instanceof TokenMismatchException && mainViewModel.getLocalAccountByAccountName(ssoAccount.name) != null) {
                                    Log.w(TAG, "Received " + TokenMismatchException.class.getSimpleName() + " and the given ssoAccount.name (" + ssoAccount.name + ") does already exist in the database. Assume that this account has already been imported.");
                                    runOnUiThread(() -> {
                                        mainViewModel.postCurrentAccount(mainViewModel.getLocalAccountByAccountName(ssoAccount.name));
                                        // TODO there is already a sync in progress and results in displaying a TokenMissMatchException snackbar which conflicts with this one
                                        coordinatorLayout.post(() -> BrandedSnackbar.make(coordinatorLayout, R.string.account_already_imported, Snackbar.LENGTH_LONG).show());
                                    });
                                } else {
                                    e.printStackTrace();
                                    runOnUiThread(() -> {
                                        binding.activityNotesListView.progressCircular.setVisibility(GONE);
                                        ExceptionDialogFragment.newInstance(e).show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName());
                                    });
                                }
                            }
                        }).start();
                    });
                } catch (AccountImportCancelledException e) {
                    Log.i(TAG, "AccountImport has been cancelled.");
                }
            }
        }
    }

    @Override
    public void onNoteClick(int position, View v) {
        boolean hasCheckedItems = adapter.getSelected().size() > 0;
        if (hasCheckedItems) {
            if (!adapter.select(position)) {
                v.setSelected(false);
                adapter.deselect(position);
            } else {
                v.setSelected(true);
            }
            int size = adapter.getSelected().size();
            if (size > 0) {
                mActionMode.setTitle(getResources().getQuantityString(R.plurals.ab_selected, size, size));
            } else {
                mActionMode.finish();
            }
        } else {
            NoteWithCategory note = (NoteWithCategory) adapter.getItem(position);
            Intent intent = new Intent(getApplicationContext(), EditNoteActivity.class);
            intent.putExtra(EditNoteActivity.PARAM_NOTE_ID, note.getId());
            startActivityForResult(intent, show_single_note_cmd);
        }
    }

    @Override
    public void onNoteFavoriteClick(int position, View view) {
        mainViewModel.toggleFavoriteAndSync(localAccount, ((NoteWithCategory) adapter.getItem(position)).getId());
        adapter.notifyItemChanged(position);
    }

    @Override
    public boolean onNoteLongClick(int position, View v) {
        boolean selected = adapter.select(position);
        if (selected) {
            v.setSelected(true);
            mActionMode = startSupportActionMode(new MultiSelectedActionModeCallback(this, coordinatorLayout, mainViewModel, this, localAccount, canMoveNoteToAnotherAccounts, adapter, listView, getSupportFragmentManager(), activityBinding.searchView));
            int checkedItemCount = adapter.getSelected().size();
            mActionMode.setTitle(getResources().getQuantityString(R.plurals.ab_selected, checkedItemCount, checkedItemCount));
        }
        return selected;
    }

    @Override
    public void onBackPressed() {
        if (activityBinding.toolbar.getVisibility() == VISIBLE) {
            updateToolbars(true);
        } else {
            super.onBackPressed();
        }
    }

    private void updateToolbars(boolean disableSearch) {
        activityBinding.homeToolbar.setVisibility(disableSearch ? VISIBLE : GONE);
        activityBinding.toolbar.setVisibility(disableSearch ? GONE : VISIBLE);
        if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activityBinding.appBar.setStateListAnimator(AnimatorInflater.loadStateListAnimator(activityBinding.appBar.getContext(),
                    disableSearch ? R.animator.appbar_elevation_off : R.animator.appbar_elevation_on));
        } else {
            ViewCompat.setElevation(activityBinding.appBar, disableSearch ? 0 : getResources().getDimension(R.dimen.design_appbar_elevation));
        }
        if (disableSearch) {
            activityBinding.searchView.setQuery(null, true);
        }
        activityBinding.searchView.setIconified(disableSearch);
    }

    @Override
    public void addAccount() {
        askForNewAccount(this);
    }

    @Override
    public void onAccountChosen(Account localAccount) {
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        mainViewModel.postCurrentAccount(localAccount);
    }

    @Override
    public void onAccountPicked(@NonNull Account account) {
        for (Integer i : adapter.getSelected()) {
            final LiveData<NoteWithCategory> moveLiveData = mainViewModel.moveNoteToAnotherAccount(account, (NoteWithCategory) adapter.getItem(i));
            moveLiveData.observe(this, (v) -> moveLiveData.removeObservers(this));
        }
    }

    @Override
    public void onCategoryChosen(String category) {
        for (Integer i : adapter.getSelected()) {
            mainViewModel.setCategory(localAccount, ((NoteWithCategory) adapter.getItem(i)).getId(), category);
        }
    }
}
