package it.niedermann.owncloud.notes.main;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.main.navigation.NavigationAdapter;
import it.niedermann.owncloud.notes.main.navigation.NavigationItem;
import it.niedermann.owncloud.notes.persistence.CapabilitiesClient;
import it.niedermann.owncloud.notes.persistence.NoteServerSyncHelper;
import it.niedermann.owncloud.notes.persistence.NotesDatabase;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Category;
import it.niedermann.owncloud.notes.persistence.entity.CategoryWithNotesCount;
import it.niedermann.owncloud.notes.persistence.entity.NoteWithCategory;
import it.niedermann.owncloud.notes.persistence.entity.SingleNoteWidgetData;
import it.niedermann.owncloud.notes.shared.model.Capabilities;
import it.niedermann.owncloud.notes.shared.model.CategorySortingMethod;
import it.niedermann.owncloud.notes.shared.model.ISyncCallback;
import it.niedermann.owncloud.notes.shared.model.Item;
import it.niedermann.owncloud.notes.shared.model.NavigationCategory;

import static androidx.lifecycle.Transformations.distinctUntilChanged;
import static androidx.lifecycle.Transformations.map;
import static androidx.lifecycle.Transformations.switchMap;
import static it.niedermann.owncloud.notes.main.MainActivity.ADAPTER_KEY_RECENT;
import static it.niedermann.owncloud.notes.main.MainActivity.ADAPTER_KEY_STARRED;
import static it.niedermann.owncloud.notes.main.slots.SlotterUtil.fillListByCategory;
import static it.niedermann.owncloud.notes.main.slots.SlotterUtil.fillListByInitials;
import static it.niedermann.owncloud.notes.main.slots.SlotterUtil.fillListByTime;
import static it.niedermann.owncloud.notes.shared.model.CategorySortingMethod.SORT_MODIFIED_DESC;
import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.DEFAULT_CATEGORY;
import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.FAVORITES;
import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.RECENT;
import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.UNCATEGORIZED;
import static it.niedermann.owncloud.notes.shared.util.DisplayUtils.convertToCategoryNavigationItem;

public class MainViewModel extends AndroidViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();

    @NonNull
    private final NotesDatabase db;

    @NonNull
    private final MutableLiveData<Account> currentAccount = new MutableLiveData<>();

    @NonNull
    private final MutableLiveData<String> searchTerm = new MutableLiveData<>(null);

    @NonNull
    private final MutableLiveData<NavigationCategory> selectedCategory = new MutableLiveData<>(new NavigationCategory(RECENT));

    @NonNull
    private final MutableLiveData<String> expandedCategory = new MutableLiveData<>(null);

    public MainViewModel(@NonNull Application application) {
        super(application);
        this.db = NotesDatabase.getInstance(application.getApplicationContext());
    }

    @NonNull
    public LiveData<Account> getCurrentAccount() {
        return distinctUntilChanged(currentAccount);
    }

    public void postCurrentAccount(@NonNull Account account) {
        BrandingUtil.saveBrandColors(getApplication(), account.getColor(), account.getTextColor());
        SingleAccountHelper.setCurrentAccount(getApplication(), account.getAccountName());
        this.currentAccount.postValue(account);
    }

    @NonNull
    public LiveData<String> getSearchTerm() {
        return distinctUntilChanged(searchTerm);
    }

    public void postSearchTerm(String searchTerm) {
        this.searchTerm.postValue(searchTerm);
    }

    @NonNull
    public LiveData<NavigationCategory> getSelectedCategory() {
        return distinctUntilChanged(selectedCategory);
    }

    public void postSelectedCategory(@NonNull NavigationCategory selectedCategory) {
        this.selectedCategory.postValue(selectedCategory);

        // Close sub categories
        switch (selectedCategory.getType()) {
            case RECENT:
            case FAVORITES:
            case UNCATEGORIZED: {
                postExpandedCategory(null);
                break;
            }
            case DEFAULT_CATEGORY:
            default: {
                Category category = selectedCategory.getCategory();
                if (category == null) {
                    postExpandedCategory(null);
                    Log.e(TAG, "navigation selection is a " + DEFAULT_CATEGORY + ", but the contained category is null.");
                } else {
                    String title = category.getTitle();
                    int slashIndex = title == null ? -1 : title.indexOf('/');
                    String rootCategory = slashIndex < 0 ? title : title.substring(0, slashIndex);
                    String expandedCategory = getExpandedCategory().getValue();
                    if (expandedCategory != null && !expandedCategory.equals(rootCategory)) {
                        postExpandedCategory(null);
                    }
                }
                break;
            }
        }
    }

    @NonNull
    @MainThread
    public LiveData<Pair<NavigationCategory, CategorySortingMethod>> getCategorySortingMethodOfSelectedCategory() {
        return switchMap(getSelectedCategory(), selectedCategory -> map(db.getCategoryOrder(selectedCategory), sortingMethod -> new Pair<>(selectedCategory, sortingMethod)));
    }

    public void postExpandedCategory(@Nullable String expandedCategory) {
        this.expandedCategory.postValue(expandedCategory);
    }

    @NonNull
    public LiveData<String> getExpandedCategory() {
        return distinctUntilChanged(expandedCategory);
    }

    @NonNull
    @MainThread
    public LiveData<List<Item>> getNotesListLiveData() {
        final MutableLiveData<List<Item>> insufficientInformation = new MutableLiveData<>();
        return switchMap(getCurrentAccount(), currentAccount -> {
            Log.v(TAG, "[getNotesListLiveData] - currentAccount: " + currentAccount);
            if (currentAccount == null) {
                return insufficientInformation;
            } else {
                return switchMap(getSelectedCategory(), selectedCategory -> {
                    if (selectedCategory == null) {
                        return insufficientInformation;
                    } else {
                        return switchMap(getSearchTerm(), searchTerm -> {
                            Log.v(TAG, "[getNotesListLiveData] - searchTerm: " + searchTerm);
                            return switchMap(getCategorySortingMethodOfSelectedCategory(), sortingMethod -> {
                                final Long accountId = currentAccount.getId();
                                final String searchQueryOrWildcard = searchTerm == null ? "%" : "%" + searchTerm.trim() + "%";
                                Log.v(TAG, "[getNotesListLiveData] - sortMethod: " + sortingMethod.second);
                                final LiveData<List<NoteWithCategory>> fromDatabase;
                                switch (selectedCategory.getType()) {
                                    case RECENT: {
                                        Log.v(TAG, "[getNotesListLiveData] - category: " + RECENT);
                                        fromDatabase = sortingMethod.second == SORT_MODIFIED_DESC
                                                ? db.getNoteDao().searchRecentByModified(accountId, searchQueryOrWildcard)
                                                : db.getNoteDao().searchRecentLexicographically(accountId, searchQueryOrWildcard);
                                        break;
                                    }
                                    case FAVORITES: {
                                        Log.v(TAG, "[getNotesListLiveData] - category: " + FAVORITES);
                                        fromDatabase = sortingMethod.second == SORT_MODIFIED_DESC
                                                ? db.getNoteDao().searchFavoritesByModified(accountId, searchQueryOrWildcard)
                                                : db.getNoteDao().searchFavoritesLexicographically(accountId, searchQueryOrWildcard);
                                        break;
                                    }
                                    case UNCATEGORIZED: {
                                        Log.v(TAG, "[getNotesListLiveData] - category: " + UNCATEGORIZED);
                                        fromDatabase = sortingMethod.second == SORT_MODIFIED_DESC
                                                ? db.getNoteDao().searchUncategorizedByModified(accountId, searchQueryOrWildcard)
                                                : db.getNoteDao().searchUncategorizedLexicographically(accountId, searchQueryOrWildcard);
                                        break;
                                    }
                                    case DEFAULT_CATEGORY:
                                    default: {
                                        final Category category = selectedCategory.getCategory();
                                        if (category == null) {
                                            throw new IllegalStateException(NavigationCategory.class.getSimpleName() + " type is " + DEFAULT_CATEGORY + ", but category is null.");
                                        }
                                        Log.v(TAG, "[getNotesListLiveData] - category: " + category.getTitle());
                                        fromDatabase = sortingMethod.second == SORT_MODIFIED_DESC
                                                ? db.getNoteDao().searchCategoryByModified(accountId, searchQueryOrWildcard, category.getTitle())
                                                : db.getNoteDao().searchCategoryLexicographically(accountId, searchQueryOrWildcard, category.getTitle());
                                        break;
                                    }
                                }

                                Log.v(TAG, "[getNotesListLiveData] - -------------------------------------");
                                return distinctUntilChanged(map(fromDatabase, noteList -> fromNotesWithCategory(noteList, selectedCategory, sortingMethod.second)));
                            });
                        });
                    }
                });
            }
        });
    }

    private List<Item> fromNotesWithCategory(List<NoteWithCategory> noteList, @NonNull NavigationCategory selectedCategory, @Nullable CategorySortingMethod sortingMethod) {
        if (selectedCategory.getType() == DEFAULT_CATEGORY) {
            final Category category = selectedCategory.getCategory();
            if (category != null) {
                return fillListByCategory(noteList, category.getTitle());
            } else {
                throw new IllegalStateException(NavigationCategory.class.getSimpleName() + " type is " + DEFAULT_CATEGORY + ", but category is null.");
            }
        }
        if (sortingMethod == SORT_MODIFIED_DESC) {
            return fillListByTime(getApplication(), noteList);
        } else {
            return fillListByInitials(getApplication(), noteList);
        }
    }

    @NonNull
    @MainThread
    public LiveData<List<NavigationItem>> getNavigationCategories() {
        final MutableLiveData<List<NavigationItem>> insufficientInformation = new MutableLiveData<>();
        return switchMap(getCurrentAccount(), currentAccount -> {
            if (currentAccount == null) {
                return insufficientInformation;
            } else {
                Log.v(TAG, "[getNavigationCategories] - currentAccount: " + currentAccount.getAccountName());
                return switchMap(getExpandedCategory(), expandedCategory -> {
                    Log.v(TAG, "[getNavigationCategories] - expandedCategory: " + expandedCategory);
                    return distinctUntilChanged(map(db.getCategoryDao().getCategoriesLiveData(currentAccount.getId()), fromDatabase ->
                            fromCategoriesWithNotesCount(getApplication(), expandedCategory, fromDatabase, db.getNoteDao().count(currentAccount.getId()), db.getNoteDao().getFavoritesCount(currentAccount.getId()))
                    ));
                });
            }
        });
    }

    private static List<NavigationItem> fromCategoriesWithNotesCount(@NonNull Context context, @Nullable String expandedCategory, @NonNull List<CategoryWithNotesCount> fromDatabase, int count, int favoritesCount) {
        final List<NavigationItem.CategoryNavigationItem> categories = convertToCategoryNavigationItem(context, fromDatabase);
        final NavigationItem itemRecent = new NavigationItem(ADAPTER_KEY_RECENT, context.getString(R.string.label_all_notes), count, R.drawable.ic_access_time_grey600_24dp, RECENT);
        final NavigationItem itemFavorites = new NavigationItem(ADAPTER_KEY_STARRED, context.getString(R.string.label_favorites), favoritesCount, R.drawable.ic_star_yellow_24dp, FAVORITES);

        final ArrayList<NavigationItem> items = new ArrayList<>(fromDatabase.size() + 3);
        items.add(itemRecent);
        items.add(itemFavorites);
        NavigationItem lastPrimaryCategory = null;
        NavigationItem lastSecondaryCategory = null;
        for (NavigationItem item : categories) {
            int slashIndex = item.label.indexOf('/');
            String currentPrimaryCategory = slashIndex < 0 ? item.label : item.label.substring(0, slashIndex);
            String currentSecondaryCategory = null;
            boolean isCategoryOpen = currentPrimaryCategory.equals(expandedCategory);

            if (isCategoryOpen && !currentPrimaryCategory.equals(item.label)) {
                String currentCategorySuffix = item.label.substring(expandedCategory.length() + 1);
                int subSlashIndex = currentCategorySuffix.indexOf('/');
                currentSecondaryCategory = subSlashIndex < 0 ? currentCategorySuffix : currentCategorySuffix.substring(0, subSlashIndex);
            }

            boolean belongsToLastPrimaryCategory = lastPrimaryCategory != null && currentPrimaryCategory.equals(lastPrimaryCategory.label);
            boolean belongsToLastSecondaryCategory = belongsToLastPrimaryCategory && lastSecondaryCategory != null && lastSecondaryCategory.label.equals(currentPrimaryCategory + "/" + currentSecondaryCategory);

            if (isCategoryOpen && !belongsToLastPrimaryCategory && currentSecondaryCategory != null) {
                lastPrimaryCategory = new NavigationItem("category:" + currentPrimaryCategory, currentPrimaryCategory, 0, NavigationAdapter.ICON_MULTIPLE_OPEN);
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

    public void modifyCategoryOrder(long accountId, @NonNull NavigationCategory selectedCategory, @NonNull CategorySortingMethod sortingMethod) {
        db.modifyCategoryOrder(accountId, selectedCategory, sortingMethod);
    }

    /**
     * @return <code>true</code>, if a synchronization could successfully be triggered.
     */
    public boolean synchronize(SingleSignOnAccount ssoAccount) {
        NoteServerSyncHelper syncHelper = db.getNoteServerSyncHelper();
        if (!syncHelper.isSyncPossible()) {
            syncHelper.updateNetworkStatus();
        }
        if (syncHelper.isSyncPossible()) {
            syncHelper.scheduleSync(ssoAccount, false);
            return true;
        } else { // Sync is not possible
            if (syncHelper.isNetworkConnected() && syncHelper.isSyncOnlyOnWifi()) {
                Log.d(TAG, "Network is connected, but sync is not possible");
            } else {
                Log.d(TAG, "Sync is not possible, because network is not connected");
            }
        }
        return false;
    }

    public LiveData<Boolean> getSyncStatus() {
        return db.getNoteServerSyncHelper().getSyncStatus();
    }

    public LiveData<ArrayList<Throwable>> getSyncErrors() {
        return db.getNoteServerSyncHelper().getSyncErrors();
    }

    public LiveData<Boolean> hasMultipleAccountsConfigured() {
        return map(db.getAccountDao().getAccountsCountLiveData(), (counter) -> counter != null && counter > 1);
    }

    public LiveData<Boolean> performFullSynchronizationForCurrentAccount() {
        final MutableLiveData<Boolean> insufficientInformation = new MutableLiveData<>();
        return switchMap(getCurrentAccount(), currentAccount -> {
            Log.v(TAG, "[performFullSynchronizationForCurrentAccount] - currentAccount: " + currentAccount);
            if (currentAccount == null) {
                return insufficientInformation;
            } else {
                Log.i(TAG, "[performFullSynchronizationForCurrentAccount] Refreshing capabilities for " + currentAccount.getAccountName());
                MutableLiveData<Boolean> syncSuccess = new MutableLiveData<>();
                new Thread(() -> {
                    try {
                        SingleSignOnAccount ssoAccount = AccountImporter.getSingleSignOnAccount(getApplication(), currentAccount.getAccountName());
                        final Capabilities capabilities;
                        try {
                            capabilities = CapabilitiesClient.getCapabilities(getApplication(), ssoAccount, currentAccount.getCapabilitiesETag());
                            db.getAccountDao().updateCapabilitiesETag(currentAccount.getId(), capabilities.getETag());
                            db.getAccountDao().updateBrand(currentAccount.getId(), capabilities.getColor(), capabilities.getTextColor());
                            currentAccount.setColor(capabilities.getColor());
                            currentAccount.setTextColor(capabilities.getTextColor());
                            BrandingUtil.saveBrandColors(getApplication(), currentAccount.getColor(), currentAccount.getTextColor());
                            db.updateApiVersion(currentAccount.getId(), capabilities.getApiVersion());
                            Log.i(TAG, capabilities.toString());
                        } catch (Exception e) {
                            if (e instanceof NextcloudHttpRequestFailedException && ((NextcloudHttpRequestFailedException) e).getStatusCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                                Log.i(TAG, "Capabilities not modified.");
                            } else {
                                e.printStackTrace();
                            }
                        }
                        // Even if the capabilities endpoint makes trouble, we can still try to synchronize the notes
                        syncSuccess.postValue(synchronize(ssoAccount));
                    } catch (NextcloudFilesAppAccountNotFoundException e) {
                        e.printStackTrace();
                        // TODO should we just remove this account from the database?
                        syncSuccess.postValue(true);
                    }
                }).start();
                return syncSuccess;
            }
        });
    }

    @WorkerThread
    public Account getLocalAccountByAccountName(String accountName) {
        return db.getAccountDao().getLocalAccountByAccountName(accountName);
    }

    @WorkerThread
    public List<Account> getAccounts() {
        return db.getAccountDao().getAccounts();
    }

    public void setCategory(SingleSignOnAccount ssoAccount, long accountId, Long noteId, @NonNull String category) {
        db.setCategory(ssoAccount, accountId, noteId, category);
    }

    public LiveData<NoteWithCategory> moveNoteToAnotherAccount(SingleSignOnAccount ssoAccount, NoteWithCategory note, long newAccountId) {
        return db.moveNoteToAnotherAccount(ssoAccount, note, newAccountId);
    }

    @WorkerThread
    public Category getCategory(long id) {
        return db.getCategoryDao().getCategory(id);
    }

    public LiveData<Void> deleteAccount(@NonNull Account account) {
        return db.deleteAccount(account);
    }

    public void toggleFavoriteAndSync(SingleSignOnAccount ssoAccount, long noteId) {
        db.toggleFavoriteAndSync(ssoAccount, noteId);
    }

    public void deleteNoteAndSync(SingleSignOnAccount ssoAccount, long id) {
        db.deleteNoteAndSync(ssoAccount, id);
    }

    public LiveData<Account> addAccount(@NonNull String url, @NonNull String username, @NonNull String accountName, @NonNull Capabilities capabilities) {
        return db.addAccount(url, username, accountName, capabilities);
    }

    @WorkerThread
    public NoteWithCategory getNoteWithCategory(long accountId, long id) {
        return db.getNoteDao().getNoteWithCategory(accountId, id);
    }

    public LiveData<NoteWithCategory> addNoteAndSync(SingleSignOnAccount ssoAccount, long accountId, NoteWithCategory note) {
        return db.addNoteAndSync(ssoAccount, accountId, note);
    }

    @WorkerThread
    public void updateNoteAndSync(SingleSignOnAccount ssoAccount, @NonNull Account localAccount, @NonNull NoteWithCategory oldNote, @Nullable String newContent, @Nullable String newTitle, @Nullable ISyncCallback callback) {
        db.updateNoteAndSync(ssoAccount, localAccount, oldNote, newContent, newTitle, callback);
    }

    public void createOrUpdateSingleNoteWidgetData(SingleNoteWidgetData data) {
        db.getWidgetSingleNoteDao().createOrUpdateSingleNoteWidgetData(data);
    }

    public LiveData<Integer> getAccountsCount() {
        return db.getAccountDao().getAccountsCountLiveData();
    }
}
