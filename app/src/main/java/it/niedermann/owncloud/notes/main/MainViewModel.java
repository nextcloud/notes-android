package it.niedermann.owncloud.notes.main;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.main.navigation.NavigationAdapter;
import it.niedermann.owncloud.notes.main.navigation.NavigationItem;
import it.niedermann.owncloud.notes.persistence.NotesDatabase;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Category;
import it.niedermann.owncloud.notes.persistence.entity.NoteWithCategory;
import it.niedermann.owncloud.notes.shared.model.CategorySortingMethod;
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
import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.FAVORITES;
import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.RECENT;
import static it.niedermann.owncloud.notes.shared.util.DisplayUtils.convertToCategoryNavigationItem;

public class MainViewModel extends AndroidViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();

    @NonNull
    private final NotesDatabase db;

    @NonNull
    private final MutableLiveData<Account> currentAccount = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<String> searchTerm = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Pair<NavigationCategory, CategorySortingMethod>> sortOrderChange = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<NavigationCategory> selectedCategory = new MutableLiveData<>(new NavigationCategory(RECENT));

    public MainViewModel(@NonNull Application application) {
        super(application);
        this.db = NotesDatabase.getInstance(application.getApplicationContext());
    }

    public void postCurrentAccount(@NonNull Account account) {
        this.currentAccount.postValue(account);
    }

    @NonNull
    public LiveData<Account> getCurrentAccount() {
        return currentAccount;
    }

    public void postSearchTerm(String searchTerm) {
        this.searchTerm.postValue(searchTerm);
    }

    public void postSelectedCategory(@NonNull NavigationCategory selectedCategory) {
        this.selectedCategory.setValue(selectedCategory);
    }

    /**
     * Needed for special categories like favorites, recent and uncategorized
     */
    public void postSortOrderChange(NavigationCategory navigationSelection, CategorySortingMethod method) {
        this.sortOrderChange.setValue(new Pair<>(navigationSelection, method));
    }

    public LiveData<Pair<NavigationCategory, CategorySortingMethod>> getSortOrderChange() {
        return this.sortOrderChange;
    }

    @NonNull
    public LiveData<String> getSearchTerm() {
        return searchTerm;
    }

    @NonNull
    public LiveData<NavigationCategory> getSelectedCategory() {
        return selectedCategory;
    }

    @NonNull
    private LiveData<Void> filterChanged() {
        MediatorLiveData<Void> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(currentAccount, (o) -> mediatorLiveData.postValue(null));
        mediatorLiveData.addSource(searchTerm, (o) -> mediatorLiveData.postValue(null));
        mediatorLiveData.addSource(selectedCategory, (o) -> mediatorLiveData.postValue(null));
        mediatorLiveData.addSource(sortOrderChange, (o) -> mediatorLiveData.postValue(null));
        return mediatorLiveData;
    }

    @NonNull
    public LiveData<List<Item>> getNotesListLiveData() {
        return switchMap(filterChanged(), input -> {
            Account currentAccount = getCurrentAccount().getValue();
            NavigationCategory selectedCategory = getSelectedCategory().getValue();
            LiveData<List<NoteWithCategory>> fromDatabase;
            if (currentAccount != null && selectedCategory != null) {
                Long accountId = currentAccount.getId();
                CategorySortingMethod sortingMethod = db.getCategoryOrder(selectedCategory);
                String searchQuery = getSearchTerm().getValue();
                searchQuery = searchQuery == null ? "%" : "%" + searchQuery.trim() + "%";
                switch (selectedCategory.getType()) {
                    case RECENT: {
                        fromDatabase = sortingMethod == CategorySortingMethod.SORT_MODIFIED_DESC
                                ? db.getNoteDao().searchRecentByModified(accountId, searchQuery)
                                : db.getNoteDao().searchRecentLexicographically(accountId, searchQuery);
                        break;
                    }
                    case FAVORITES: {
                        fromDatabase = sortingMethod == CategorySortingMethod.SORT_MODIFIED_DESC
                                ? db.getNoteDao().searchFavoritesByModified(accountId, searchQuery)
                                : db.getNoteDao().searchFavoritesLexicographically(accountId, searchQuery);
                        break;
                    }
                    case UNCATEGORIZED: {
                        fromDatabase = sortingMethod == CategorySortingMethod.SORT_MODIFIED_DESC
                                ? db.getNoteDao().searchUncategorizedByModified(accountId, searchQuery)
                                : db.getNoteDao().searchUncategorizedLexicographically(accountId, searchQuery);
                        break;
                    }
                    case DEFAULT_CATEGORY:
                    default: {
                        final Category category = selectedCategory.getCategory();
                        fromDatabase = sortingMethod == CategorySortingMethod.SORT_MODIFIED_DESC
                                ? db.getNoteDao().searchCategoryByModified(accountId, searchQuery, category == null ? "" : category.getTitle())
                                : db.getNoteDao().searchCategoryLexicographically(accountId, searchQuery, category == null ? "" : category.getTitle());
                        break;
                    }
                }

                return distinctUntilChanged(
                        map(fromDatabase, noteList -> {
                            //noinspection SwitchStatementWithTooFewBranches
                            switch (selectedCategory.getType()) {
                                case DEFAULT_CATEGORY: {
                                    Category category = selectedCategory.getCategory();
                                    if (category != null) {
                                        return fillListByCategory(noteList, category.getTitle());
                                    } else {
                                        Log.e(TAG, "Tried to fill list by category, but category is null.");
                                    }
                                }
                                default: {
                                    if (sortingMethod == CategorySortingMethod.SORT_MODIFIED_DESC) {
                                        return fillListByTime(getApplication(), noteList);
                                    } else {
                                        return fillListByInitials(getApplication(), noteList);
                                    }
                                }
                            }
                        })
                );
            } else {
                return new MutableLiveData<>();
            }
        });
    }

    @NonNull
    public LiveData<List<NavigationItem>> getNavigationCategories(String navigationOpen) {
        return switchMap(getCurrentAccount(), currentAccount -> currentAccount == null
                ? new MutableLiveData<>()
                : distinctUntilChanged(
                map(db.getCategoryDao().getCategoriesLiveData(currentAccount.getId()), fromDatabase -> {
                    List<NavigationItem.CategoryNavigationItem> categories = convertToCategoryNavigationItem(getApplication(), db.getCategoryDao().getCategories(currentAccount.getId()));
                    NavigationItem itemRecent = new NavigationItem(ADAPTER_KEY_RECENT, getApplication().getString(R.string.label_all_notes), db.getNoteDao().count(currentAccount.getId()), R.drawable.ic_access_time_grey600_24dp, RECENT);
                    NavigationItem itemFavorites = new NavigationItem(ADAPTER_KEY_STARRED, getApplication().getString(R.string.label_favorites), db.getNoteDao().getFavoritesCount(currentAccount.getId()), R.drawable.ic_star_yellow_24dp, FAVORITES);

                    ArrayList<NavigationItem> items = new ArrayList<>(fromDatabase.size() + 3);
                    items.add(itemRecent);
                    items.add(itemFavorites);
                    NavigationItem lastPrimaryCategory = null;
                    NavigationItem lastSecondaryCategory = null;
                    for (NavigationItem item : categories) {
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
                })
        ));
    }

    public LiveData<Boolean> getSyncStatus() {
        return db.getNoteServerSyncHelper().getSyncStatus();
    }

    public LiveData<ArrayList<Throwable>> getSyncErrors() {
        return db.getNoteServerSyncHelper().getSyncErrors();
    }
}
