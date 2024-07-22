/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.O;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.nextcloud.android.common.ui.util.PlatformThemeUtil.isDarkMode;
import static it.niedermann.owncloud.notes.NotesApplication.isGridViewEnabled;
import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.DEFAULT_CATEGORY;
import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.FAVORITES;
import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.RECENT;
import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.UNCATEGORIZED;
import static it.niedermann.owncloud.notes.shared.util.SSOUtil.askForNewAccount;

import android.accounts.NetworkErrorException;
import android.animation.AnimatorInflater;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.nextcloud.android.common.ui.theme.utils.ColorRole;
import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.exceptions.AccountImportCancelledException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.exceptions.TokenMismatchException;
import com.nextcloud.android.sso.exceptions.UnknownErrorException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;

import java.net.HttpURLConnection;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import it.niedermann.android.util.ColorUtil;
import it.niedermann.owncloud.notes.LockedActivity;
import it.niedermann.owncloud.notes.NotesApplication;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.accountpicker.AccountPickerListener;
import it.niedermann.owncloud.notes.accountswitcher.AccountSwitcherDialog;
import it.niedermann.owncloud.notes.accountswitcher.AccountSwitcherListener;
import it.niedermann.owncloud.notes.branding.BrandedSnackbar;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.ActivityNotesListViewBinding;
import it.niedermann.owncloud.notes.databinding.DrawerLayoutBinding;
import it.niedermann.owncloud.notes.edit.EditNoteActivity;
import it.niedermann.owncloud.notes.edit.category.CategoryDialogFragment;
import it.niedermann.owncloud.notes.exception.ExceptionDialogFragment;
import it.niedermann.owncloud.notes.exception.IntendedOfflineException;
import it.niedermann.owncloud.notes.importaccount.ImportAccountActivity;
import it.niedermann.owncloud.notes.main.items.ItemAdapter;
import it.niedermann.owncloud.notes.main.items.grid.GridItemDecoration;
import it.niedermann.owncloud.notes.main.items.list.NotesListViewItemTouchHelper;
import it.niedermann.owncloud.notes.main.items.section.SectionItemDecoration;
import it.niedermann.owncloud.notes.main.items.selection.ItemSelectionTracker;
import it.niedermann.owncloud.notes.main.menu.MenuAdapter;
import it.niedermann.owncloud.notes.main.navigation.NavigationAdapter;
import it.niedermann.owncloud.notes.main.navigation.NavigationClickListener;
import it.niedermann.owncloud.notes.main.navigation.NavigationItem;
import it.niedermann.owncloud.notes.persistence.ApiProvider;
import it.niedermann.owncloud.notes.persistence.CapabilitiesClient;
import it.niedermann.owncloud.notes.persistence.CapabilitiesWorker;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.model.CategorySortingMethod;
import it.niedermann.owncloud.notes.shared.model.IResponseCallback;
import it.niedermann.owncloud.notes.shared.model.NavigationCategory;
import it.niedermann.owncloud.notes.shared.model.NoteClickListener;
import it.niedermann.owncloud.notes.shared.util.CustomAppGlideModule;
import it.niedermann.owncloud.notes.shared.util.NoteUtil;
import it.niedermann.owncloud.notes.shared.util.ShareUtil;

public class MainActivity extends LockedActivity implements NoteClickListener, AccountPickerListener, AccountSwitcherListener, CategoryDialogFragment.CategoryDialogListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    protected final ExecutorService executor = Executors.newCachedThreadPool();

    protected MainViewModel mainViewModel;

    private boolean gridView = true;

    public static final String ADAPTER_KEY_RECENT = "recent";
    public static final String ADAPTER_KEY_STARRED = "starred";
    public static final String ADAPTER_KEY_UNCATEGORIZED = "uncategorized";

    private static final int REQUEST_CODE_CREATE_NOTE = 0;
    private static final int REQUEST_CODE_SERVER_SETTINGS = 1;

    protected ItemAdapter adapter;
    private NavigationAdapter adapterCategories;
    @Nullable
    private MenuAdapter menuAdapter;

    private SelectionTracker<Long> tracker;
    private NotesListViewItemTouchHelper itemTouchHelper;

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
        SplashScreen.installSplashScreen(this);

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

        if (!gridView || isDarkMode(this)) {
            activityBinding.activityNotesListView.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));
        }

        setupToolbars();
        setupNavigationList();
        setupNotesList();

        mainViewModel.getAccountsCount().observe(this, (count) -> {
            if (count == 0) {
                startActivityForResult(new Intent(this, ImportAccountActivity.class), ImportAccountActivity.REQUEST_CODE_IMPORT_ACCOUNT);
            } else {
                executor.submit(() -> {
                    try {
                        final var account = mainViewModel.getLocalAccountByAccountName(SingleAccountHelper.getCurrentSingleSignOnAccount(getApplicationContext()).name);
                        runOnUiThread(() -> mainViewModel.postCurrentAccount(account));
                    } catch (NextcloudFilesAppAccountNotFoundException e) {
                        // Verbose log output for https://github.com/nextcloud/notes-android/issues/1256
                        runOnUiThread(() -> showExceptionAlertDialog(e));
                    } catch (NoCurrentAccountSelectedException e) {
                        runOnUiThread(() -> ExceptionDialogFragment.newInstance(e).show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName()));
                    }
                });
            }
        });

        mainViewModel.hasMultipleAccountsConfigured().observe(this, hasMultipleAccountsConfigured -> canMoveNoteToAnotherAccounts = hasMultipleAccountsConfigured);
        mainViewModel.getSyncStatus().observe(this, syncStatus -> swipeRefreshLayout.setRefreshing(syncStatus));
        mainViewModel.getSyncErrors().observe(this, exceptions -> {
            if (mainViewModel.containsNonInfrastructureRelatedItems(exceptions)) {
                BrandedSnackbar.make(coordinatorLayout, R.string.error_synchronization, Snackbar.LENGTH_LONG)
                        .setAnchorView(binding.activityNotesListView.fabCreate)
                        .setAction(R.string.simple_more, v -> ExceptionDialogFragment.newInstance(exceptions)
                                .show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName()))
                        .show();
            }
        });
        mainViewModel.getSelectedCategory().observe(this, (selectedCategory) -> {
            binding.activityNotesListView.emptyContentView.getRoot().setVisibility(GONE);
            adapter.setShowCategory(selectedCategory.getType() == RECENT || selectedCategory.getType() == FAVORITES);
            fabCreate.show();

            switch (selectedCategory.getType()) {
                case RECENT -> {
                    activityBinding.searchText.setText(getString(R.string.search_in_all));
                }
                case FAVORITES -> {
                    activityBinding.searchText.setText(getString(R.string.search_in_category, getString(R.string.label_favorites)));
                }
                case UNCATEGORIZED -> {
                    activityBinding.searchText.setText(getString(R.string.search_in_category, getString(R.string.action_uncategorized)));
                }
                default -> {
                    final String category = selectedCategory.getCategory();
                    if (category == null) {
                        throw new IllegalStateException(NavigationCategory.class.getSimpleName() + " type is " + DEFAULT_CATEGORY + ", but category is null.");
                    }
                    activityBinding.searchText.setText(getString(R.string.search_in_category, NoteUtil.extendCategory(category)));
                }
            }

            fabCreate.setOnClickListener((View view) -> {
                final var createIntent = new Intent(getApplicationContext(), EditNoteActivity.class);
                createIntent.putExtra(EditNoteActivity.PARAM_CATEGORY, selectedCategory);
                if (activityBinding.searchView.getQuery().length() > 0) {
                    createIntent.putExtra(EditNoteActivity.PARAM_CONTENT, activityBinding.searchView.getQuery().toString());
                    invalidateOptionsMenu();
                }
                startActivityForResult(createIntent, REQUEST_CODE_CREATE_NOTE);
            });
        });
        mainViewModel.getNotesListLiveData().observe(this, notes -> {
            // https://stackoverflow.com/a/37342327
            itemTouchHelper.attachToRecyclerView(null);
            itemTouchHelper.attachToRecyclerView(listView);
            adapter.setItemList(notes);
            binding.activityNotesListView.progressCircular.setVisibility(GONE);
            binding.activityNotesListView.emptyContentView.getRoot().setVisibility(notes.size() > 0 ? GONE : VISIBLE);
            // Remove deleted notes from the selection
            if (tracker.hasSelection()) {
                final var deletedNotes = new LinkedList<Long>();
                for (final var id : tracker.getSelection()) {
                    if (notes
                            .stream()
                            .filter(item -> !item.isSection())
                            .map(item -> (Note) item)
                            .noneMatch(item -> item.getId() == id)) {
                        deletedNotes.add(id);
                    }
                }
                for (final var id : deletedNotes) {
                    tracker.deselect(id);
                }
            }
        });
        mainViewModel.getSearchTerm().observe(this, adapter::setHighlightSearchQuery);
        mainViewModel.getCategorySortingMethodOfSelectedCategory().observe(this, methodOfCategory -> {
            updateSortMethodIcon(methodOfCategory.second);
            activityBinding.sortingMethod.setOnClickListener((v) -> {
                if (methodOfCategory.first != null) {
                    var newMethod = methodOfCategory.second;
                    if (newMethod == CategorySortingMethod.SORT_LEXICOGRAPHICAL_ASC) {
                        newMethod = CategorySortingMethod.SORT_MODIFIED_DESC;
                    } else {
                        newMethod = CategorySortingMethod.SORT_LEXICOGRAPHICAL_ASC;
                    }
                    final var modifyLiveData = mainViewModel.modifyCategoryOrder(methodOfCategory.first, newMethod);
                    modifyLiveData.observe(this, (next) -> modifyLiveData.removeObservers(this));
                }
            });
        });
        mainViewModel.getNavigationCategories().observe(this, navigationItems -> this.adapterCategories.setItems(navigationItems));
        mainViewModel.getCurrentAccount().observe(this, (nextAccount) -> {
            fabCreate.hide();
            Glide
                    .with(this)
                    .load(nextAccount.getUrl() + "/index.php/avatar/" + Uri.encode(nextAccount.getUserName()) + "/64")
                    .placeholder(R.drawable.ic_account_circle_grey_24dp)
                    .error(R.drawable.ic_account_circle_grey_24dp)
                    .apply(RequestOptions.circleCropTransform())
                    .into(activityBinding.launchAccountSwitcher);

            mainViewModel.synchronizeNotes(nextAccount, new IResponseCallback<>() {
                @Override
                public void onSuccess(Void v) {
                    Log.d(TAG, "Successfully synchronized notes for " + nextAccount.getAccountName());
                }

                @Override
                public void onError(@NonNull Throwable t) {
                    runOnUiThread(() -> {
                        if (t instanceof IntendedOfflineException) {
                            Log.i(TAG, "Capabilities and notes not updated because " + nextAccount.getAccountName() + " is offline by intention.");
                        } else if (t instanceof NetworkErrorException) {
                            BrandedSnackbar.make(coordinatorLayout, getString(R.string.error_sync, getString(R.string.error_no_network)), Snackbar.LENGTH_LONG)
                                    .setAnchorView(binding.activityNotesListView.fabCreate)
                                    .show();
                        } else {
                            BrandedSnackbar.make(coordinatorLayout, R.string.error_synchronization, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.simple_more, v -> ExceptionDialogFragment.newInstance(t)
                                            .show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName()))
                                    .setAnchorView(binding.activityNotesListView.fabCreate)
                                    .show();
                        }
                    });
                }
            });
            fabCreate.show();
            activityBinding.launchAccountSwitcher.setOnClickListener((v) -> AccountSwitcherDialog.newInstance(nextAccount.getId()).show(getSupportFragmentManager(), AccountSwitcherDialog.class.getSimpleName()));

            if (menuAdapter == null) {
                menuAdapter = new MenuAdapter(getApplicationContext(), nextAccount, REQUEST_CODE_SERVER_SETTINGS, (menuItem) -> {
                    @Nullable Integer resultCode = menuItem.getResultCode();
                    if (resultCode == null) {
                        startActivity(menuItem.getIntent());
                    } else {
                        startActivityForResult(menuItem.getIntent(), resultCode);
                    }
                }, nextAccount.getColor());

                binding.navigationMenu.setAdapter(menuAdapter);
            } else {
                menuAdapter.updateAccount(this, nextAccount);
            }
        });
    }

    private void showExceptionAlertDialog(NextcloudFilesAppAccountNotFoundException e) {
        final MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this)
                .setTitle(NextcloudFilesAppAccountNotFoundException.class.getSimpleName())
                .setMessage(R.string.backup)
                .setPositiveButton(R.string.simple_backup, (a, b) -> executor.submit(() -> {
                    final var modifiedNotes = new LinkedList<Note>();
                    for (final var account : mainViewModel.getAccounts()) {
                        modifiedNotes.addAll(mainViewModel.getLocalModifiedNotes(account.getId()));
                    }
                    if (modifiedNotes.size() == 1) {
                        final var note = modifiedNotes.get(0);
                        ShareUtil.openShareDialog(this, note.getTitle(), note.getContent());
                    } else {
                        ShareUtil.openShareDialog(this,
                                getResources().getQuantityString(R.plurals.share_multiple, modifiedNotes.size(), modifiedNotes.size()),
                                mainViewModel.collectNoteContents(modifiedNotes.stream().map(Note::getId).collect(Collectors.toList())));
                    }
                }))
                .setNegativeButton(R.string.simple_error, (a, b) -> {
                    final var ssoPreferences = AccountImporter.getSharedPreferences(getApplicationContext());
                    final var ssoPreferencesString = new StringBuilder()
                            .append("Current SSO account: ").append(ssoPreferences.getString("PREF_CURRENT_ACCOUNT_STRING", null)).append("\n")
                            .append("\n")
                            .append("SSO SharedPreferences: ").append("\n");
                    for (final var entry : ssoPreferences.getAll().entrySet()) {
                        ssoPreferencesString.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                    }
                    ssoPreferencesString.append("\n")
                            .append("Available accounts in DB: ").append(TextUtils.join(", ", mainViewModel.getAccounts().stream().map(Account::getAccountName).collect(Collectors.toList())));
                    runOnUiThread(() -> ExceptionDialogFragment.newInstance(new RuntimeException(e.getMessage(), new RuntimeException(ssoPreferencesString.toString(), e))).show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName()));
                });

        NotesApplication.brandingUtil().dialog.colorMaterialAlertDialogBackground(this, alertDialogBuilder);

        alertDialogBuilder.show();
    }

    @Override
    protected void onResume() {
        final var accountLiveData = mainViewModel.getCurrentAccount();
        accountLiveData.observe(this, (currentAccount) -> {
            accountLiveData.removeObservers(this);
            try {
                // It is possible that after the deletion of the last account, this onResponse gets called before the ImportAccountActivity gets started.
                if (SingleAccountHelper.getCurrentSingleSignOnAccount(this) != null) {
                    mainViewModel.synchronizeNotes(currentAccount, new IResponseCallback<>() {
                        @Override
                        public void onSuccess(Void v) {
                            Log.d(TAG, "Successfully synchronized notes for " + currentAccount.getAccountName());
                        }

                        @Override
                        public void onError(@NonNull Throwable t) {
                            t.printStackTrace();
                        }
                    });
                }
            } catch (NextcloudFilesAppAccountNotFoundException e) {
                ExceptionDialogFragment.newInstance(e).show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName());
            } catch (NoCurrentAccountSelectedException e) {
                Log.i(TAG, "No current account is selected - maybe the last account has been deleted?");
            }
        });
        super.onResume();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mainViewModel.restoreInstanceState();
    }

    private void setupToolbars() {
        setSupportActionBar(binding.activityNotesListView.searchToolbar);
        activityBinding.homeToolbar.setOnClickListener((v) -> {
            if (activityBinding.searchToolbar.getVisibility() == GONE) {
                updateToolbars(true);
            }
        });

        final var toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, activityBinding.homeToolbar, 0, 0);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        activityBinding.searchView.setOnCloseListener(() -> {
            if (activityBinding.searchToolbar.getVisibility() == VISIBLE && TextUtils.isEmpty(activityBinding.searchView.getQuery())) {
                updateToolbars(false);
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
        listView.setItemAnimator(null);
        if (gridView) {
            final int spanCount = getResources().getInteger(R.integer.grid_view_span_count);
            final var gridLayoutManager = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL);
            listView.setLayoutManager(gridLayoutManager);
            listView.addItemDecoration(new GridItemDecoration(adapter, spanCount,
                    getResources().getDimensionPixelSize(R.dimen.spacer_3x),
                    getResources().getDimensionPixelSize(R.dimen.spacer_5x),
                    getResources().getDimensionPixelSize(R.dimen.spacer_3x),
                    getResources().getDimensionPixelSize(R.dimen.spacer_1x),
                    getResources().getDimensionPixelSize(R.dimen.spacer_activity_sides) + getResources().getDimensionPixelSize(R.dimen.spacer_1x)
            ));
        } else {
            final var layoutManager = new LinearLayoutManager(this);
            listView.setLayoutManager(layoutManager);
            listView.addItemDecoration(new SectionItemDecoration(adapter,
                    getResources().getDimensionPixelSize(R.dimen.spacer_activity_sides) + getResources().getDimensionPixelSize(R.dimen.spacer_1x) + getResources().getDimensionPixelSize(R.dimen.spacer_3x) + getResources().getDimensionPixelSize(R.dimen.spacer_2x),
                    getResources().getDimensionPixelSize(R.dimen.spacer_5x),
                    getResources().getDimensionPixelSize(R.dimen.spacer_1x),
                    0
            ));
        }

        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0)
                    fabCreate.hide();
                else if (dy < 0)
                    fabCreate.show();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            CustomAppGlideModule.clearCache(this);
            final var syncLiveData = mainViewModel.getCurrentAccount();
            final Observer<Account> syncObserver = currentAccount -> {
                syncLiveData.removeObservers(this);
                mainViewModel.synchronizeCapabilitiesAndNotes(currentAccount, new IResponseCallback<>() {
                    @Override
                    public void onSuccess(Void v) {
                        Log.d(TAG, "Successfully synchronized capabilities and notes for " + currentAccount.getAccountName());
                    }

                    @Override
                    public void onError(@NonNull Throwable t) {
                        runOnUiThread(() -> {
                            swipeRefreshLayout.setRefreshing(false);
                            if (t instanceof IntendedOfflineException) {
                                Log.i(TAG, "Capabilities and notes not updated because " + currentAccount.getAccountName() + " is offline by intention.");
                            } else if (t instanceof NextcloudHttpRequestFailedException && ((NextcloudHttpRequestFailedException) t).getStatusCode() == HttpURLConnection.HTTP_UNAVAILABLE) {
                                BrandedSnackbar.make(coordinatorLayout, R.string.error_maintenance_mode, Snackbar.LENGTH_LONG)
                                        .setAnchorView(binding.activityNotesListView.fabCreate)
                                        .show();
                            } else if (t instanceof NetworkErrorException) {
                                BrandedSnackbar.make(coordinatorLayout, getString(R.string.error_sync, getString(R.string.error_no_network)), Snackbar.LENGTH_LONG)
                                        .setAnchorView(binding.activityNotesListView.fabCreate)
                                        .show();
                            } else {
                                BrandedSnackbar.make(coordinatorLayout, R.string.error_synchronization, Snackbar.LENGTH_LONG)
                                        .setAction(R.string.simple_more, v -> ExceptionDialogFragment.newInstance(t)
                                                .show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName()))
                                        .setAnchorView(binding.activityNotesListView.fabCreate)
                                        .show();
                            }
                        });
                    }
                });
            };
            syncLiveData.observe(this, syncObserver);
        });

        tracker = ItemSelectionTracker.build(listView, adapter);
        adapter.setTracker(tracker);
        tracker.addObserver(new SelectionTracker.SelectionObserver<>() {
                                @Override
                                public void onSelectionChanged() {
                                    super.onSelectionChanged();
                                    if (tracker.hasSelection() && mActionMode == null) {
                                        mActionMode = startSupportActionMode(new MultiSelectedActionModeCallback(MainActivity.this, coordinatorLayout, binding.activityNotesListView.fabCreate, mainViewModel, MainActivity.this, canMoveNoteToAnotherAccounts, tracker, getSupportFragmentManager()));
                                    }
                                    if (mActionMode != null) {
                                        if (tracker.hasSelection()) {
                                            int selected = tracker.getSelection().size();
                                            mActionMode.setTitle(getResources().getQuantityString(R.plurals.ab_selected, selected, selected));
                                        } else {
                                            mActionMode.finish();
                                            mActionMode = null;
                                        }
                                    }
                                }
                            }
        );

        itemTouchHelper = new NotesListViewItemTouchHelper(this, mainViewModel, this, tracker, adapter, swipeRefreshLayout, coordinatorLayout, binding.activityNotesListView.fabCreate, gridView);
        itemTouchHelper.attachToRecyclerView(listView);
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
                        case RECENT -> {
                            mainViewModel.postSelectedCategory(new NavigationCategory(RECENT));
                        }
                        case FAVORITES -> {
                            mainViewModel.postSelectedCategory(new NavigationCategory(FAVORITES));
                        }
                        case UNCATEGORIZED -> {
                            mainViewModel.postSelectedCategory(new NavigationCategory(UNCATEGORIZED));
                        }
                        default -> {
                            if (item.getClass() == NavigationItem.CategoryNavigationItem.class) {
                                mainViewModel.postSelectedCategory(new NavigationCategory(((NavigationItem.CategoryNavigationItem) item).accountId, ((NavigationItem.CategoryNavigationItem) item).category));
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
                final var expandedCategoryLiveData = mainViewModel.getExpandedCategory();
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
    public void applyBrand(int color) {
        final var util = BrandingUtil.of(color, this);
        util.androidx.themeSwipeRefreshLayout(activityBinding.swiperefreshlayout);
        util.platform.colorCircularProgressBar(activityBinding.progressCircular, ColorRole.PRIMARY);
        util.platform.colorNavigationView(binding.navigationView);
        util.material.themeFAB(activityBinding.fabCreate);
        util.notes.themeSearchCardView(binding.activityNotesListView.searchBarWrapper);
        util.notes.themeSearchToolbar(binding.activityNotesListView.searchToolbar);
        util.notes.themeToolbarSearchView(binding.activityNotesListView.searchView);

        binding.headerView.setBackgroundColor(color);
        @ColorInt final int headerTextColor = ColorUtil.getForegroundColorForBackgroundColor(color);
        binding.appName.setTextColor(headerTextColor);
        DrawableCompat.setTint(binding.logo.getDrawable(), headerTextColor);

        adapter.applyBrand(color);
        adapterCategories.applyBrand(color);
        if (menuAdapter != null) {
            menuAdapter.applyBrand(color);
        }
        invalidateOptionsMenu();
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (activityBinding.searchToolbar.getVisibility() == VISIBLE) {
            updateToolbars(false);
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
            case REQUEST_CODE_CREATE_NOTE -> {
                listView.scrollToPosition(0);
            }
            case REQUEST_CODE_SERVER_SETTINGS -> {
                // Recreate activity completely, because theme switching makes problems when only invalidating the views.
                // @see https://github.com/nextcloud/notes-android/issues/529
                if (RESULT_OK == resultCode) {
                    ActivityCompat.recreate(this);
                    return;
                }
            }
            default -> {
                try {
                    AccountImporter.onActivityResult(requestCode, resultCode, data, this, (ssoAccount) -> {
                        CapabilitiesWorker.update(this);
                        executor.submit(() -> {
                            final var importSnackbar = BrandedSnackbar.make(coordinatorLayout, R.string.progress_import_indeterminate, Snackbar.LENGTH_INDEFINITE)
                                    .setAnchorView(binding.activityNotesListView.fabCreate);
                            Log.i(TAG, "Added account: " + "name:" + ssoAccount.name + ", " + ssoAccount.url + ", userId" + ssoAccount.userId);
                            try {
                                Log.i(TAG, "Refreshing capabilities for " + ssoAccount.name);
                                final var capabilities = CapabilitiesClient.getCapabilities(getApplicationContext(), ssoAccount, null, ApiProvider.getInstance());
                                final String displayName = CapabilitiesClient.getDisplayName(getApplicationContext(), ssoAccount, ApiProvider.getInstance());
                                final var status$ = mainViewModel.addAccount(ssoAccount.url, ssoAccount.userId, ssoAccount.name, capabilities, displayName, new IResponseCallback<>() {
                                    @Override
                                    public void onSuccess(Account result) {
                                        executor.submit(() -> {
                                            runOnUiThread(() -> {
                                                importSnackbar.setText(R.string.account_imported);
                                                importSnackbar.setAction(R.string.simple_switch, (v) -> mainViewModel.postCurrentAccount(mainViewModel.getLocalAccountByAccountName(ssoAccount.name)));
                                            });
                                            Log.i(TAG, capabilities.toString());
                                        });
                                    }

                                    @Override
                                    public void onError(@NonNull Throwable t) {
                                        runOnUiThread(() -> {
                                            importSnackbar.dismiss();
                                            ExceptionDialogFragment.newInstance(t).show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName());
                                        });
                                    }
                                });
                                runOnUiThread(() -> status$.observe(this, (status) -> {
                                    importSnackbar.show();
                                    Log.v(TAG, "Status: " + status.count + " of " + status.total);
                                    if (status.count > 0) {
                                        importSnackbar.setText(getString(R.string.progress_import, status.count + 1, status.total));
                                    }
                                }));
                            } catch (Throwable e) {
                                importSnackbar.dismiss();
                                ApiProvider.getInstance().invalidateAPICache(ssoAccount);
                                // Happens when importing an already existing account the second time
                                if (e instanceof TokenMismatchException && mainViewModel.getLocalAccountByAccountName(ssoAccount.name) != null) {
                                    Log.w(TAG, "Received " + TokenMismatchException.class.getSimpleName() + " and the given ssoAccount.name (" + ssoAccount.name + ") does already exist in the database. Assume that this account has already been imported.");
                                    runOnUiThread(() -> {
                                        mainViewModel.postCurrentAccount(mainViewModel.getLocalAccountByAccountName(ssoAccount.name));
                                        // TODO there is already a sync in progress and results in displaying a TokenMissMatchException snackbar which conflicts with this one
                                        coordinatorLayout.post(() -> BrandedSnackbar.make(coordinatorLayout, R.string.account_already_imported, Snackbar.LENGTH_LONG)
                                                .setAnchorView(binding.activityNotesListView.fabCreate)
                                                .show());
                                    });
                                } else if (e instanceof UnknownErrorException && e.getMessage() != null && e.getMessage().contains("No address associated with hostname")) {
                                    // https://github.com/nextcloud/notes-android/issues/1014
                                    runOnUiThread(() -> Snackbar.make(coordinatorLayout, R.string.you_have_to_be_connected_to_the_internet_in_order_to_add_an_account, Snackbar.LENGTH_LONG)
                                            .setAnchorView(binding.activityNotesListView.fabCreate)
                                            .show());
                                } else {
                                    e.printStackTrace();
                                    runOnUiThread(() -> {
                                        binding.activityNotesListView.progressCircular.setVisibility(GONE);
                                        ExceptionDialogFragment.newInstance(e).show(getSupportFragmentManager(), ExceptionDialogFragment.class.getSimpleName());
                                    });
                                }
                            }
                        });
                    });
                } catch (AccountImportCancelledException e) {
                    Log.i(TAG, "AccountImport has been cancelled.");
                }
            }
        }
    }

    @Override
    public void onNoteClick(int position, View v) {
        final boolean hasCheckedItems = tracker.getSelection().size() > 0;
        if (!hasCheckedItems) {
            final var note = (Note) adapter.getItem(position);
            startActivity(new Intent(getApplicationContext(), EditNoteActivity.class)
                    .putExtra(EditNoteActivity.PARAM_NOTE_ID, note.getId()));
        }
    }

    @Override
    public void onNoteFavoriteClick(int position, View view) {
        final var toggleLiveData = mainViewModel.toggleFavoriteAndSync(((Note) adapter.getItem(position)).getId());
        toggleLiveData.observe(this, (next) -> toggleLiveData.removeObservers(this));
    }

    @Override
    public void onBackPressed() {
        if (activityBinding.searchToolbar.getVisibility() == VISIBLE) {
            updateToolbars(false);
        } else if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void updateToolbars(boolean enableSearch) {
        activityBinding.homeToolbar.setVisibility(enableSearch ? GONE : VISIBLE);
        activityBinding.searchToolbar.setVisibility(enableSearch ? VISIBLE : GONE);
        activityBinding.appBar.setStateListAnimator(AnimatorInflater.loadStateListAnimator(activityBinding.appBar.getContext(), enableSearch
                ? R.animator.appbar_elevation_on
                : R.animator.appbar_elevation_off));
        if (enableSearch) {
            activityBinding.searchView.setIconified(false);
            fabCreate.show();
        } else {
            activityBinding.searchView.setQuery(null, true);
        }
    }

    @Override
    public void addAccount() {
        askForNewAccount(this);
    }

    @Override
    public void onAccountChosen(@NonNull Account localAccount) {
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        mainViewModel.postCurrentAccount(localAccount);
    }

    @Override
    public void onAccountPicked(@NonNull Account account) {
        for (final var noteId : tracker.getSelection()) {
            final var moveLiveData = mainViewModel.moveNoteToAnotherAccount(account, noteId);
            moveLiveData.observe(this, (v) -> {
                tracker.deselect(noteId);
                moveLiveData.removeObservers(this);
            });
        }
    }

    @Override
    public void onCategoryChosen(String category) {
        final var categoryLiveData = mainViewModel.setCategory(tracker.getSelection(), category);
        categoryLiveData.observe(this, (next) -> categoryLiveData.removeObservers(this));
        tracker.clearSelection();
    }
}
