package it.niedermann.owncloud.notes.main;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import it.niedermann.owncloud.notes.persistence.NotesDatabase;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.Category;
import it.niedermann.owncloud.notes.persistence.entity.NoteWithCategory;
import it.niedermann.owncloud.notes.shared.model.CategorySortingMethod;
import it.niedermann.owncloud.notes.shared.model.Item;
import it.niedermann.owncloud.notes.shared.model.NavigationCategory;

import static androidx.lifecycle.Transformations.distinctUntilChanged;
import static androidx.lifecycle.Transformations.map;
import static it.niedermann.owncloud.notes.main.slots.SlotterUtil.fillListByCategory;
import static it.niedermann.owncloud.notes.main.slots.SlotterUtil.fillListByInitials;
import static it.niedermann.owncloud.notes.main.slots.SlotterUtil.fillListByTime;
import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.RECENT;

public class MainViewModel extends AndroidViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();

    @NonNull
    private NotesDatabase db;

    @NonNull
    private MutableLiveData<Account> currentAccount = new MutableLiveData<>();
    @NonNull
    private MutableLiveData<String> searchTerm = new MutableLiveData<>();
    @NonNull
    private MutableLiveData<NavigationCategory> selectedCategory = new MutableLiveData<>(new NavigationCategory(RECENT));

    public MainViewModel(@NonNull Application application) {
        super(application);
        this.db = NotesDatabase.getInstance(application.getApplicationContext());
    }

    public void postCurrentAccount(@NonNull Account account) {
        this.currentAccount.postValue(account);
    }

    public void postSearchTerm(String searchTerm) {
        this.searchTerm.postValue(searchTerm);
    }

    public void postSelectedCategory(@NonNull NavigationCategory selectedCategory) {
        this.selectedCategory.postValue(selectedCategory);
    }

    public LiveData<Account> getCurrentAccount() {
        return currentAccount;
    }

    public LiveData<String> getSearchTerm() {
        return searchTerm;
    }

    public LiveData<NavigationCategory> getSelectedCategory() {
        return selectedCategory;
    }

    public LiveData<Void> filterChanged() {
        MediatorLiveData<Void> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(currentAccount, (o) -> mediatorLiveData.postValue(null));
        mediatorLiveData.addSource(searchTerm, (o) -> mediatorLiveData.postValue(null));
        mediatorLiveData.addSource(selectedCategory, (o) -> mediatorLiveData.postValue(null));
        return mediatorLiveData;
    }

    public LiveData<List<Item>> getNotesListLiveData() {
        Account currentAccount = getCurrentAccount().getValue();
        NavigationCategory selectedCategory = getSelectedCategory().getValue();
        LiveData<List<NoteWithCategory>> fromDatabase;
        if (currentAccount != null && selectedCategory != null) {
            Long accountId = currentAccount.getId();
            CategorySortingMethod sortingMethod = db.getCategoryOrder(accountId, selectedCategory);
            String searchQuery = getSearchTerm().getValue();
            searchQuery = searchQuery == null ? "%" : "%" + searchQuery.trim() + "%";
            switch (selectedCategory.getType()) {
                case FAVORITES: {
                    fromDatabase = db.getNoteDao().searchNotesFavorites(accountId, searchQuery, sortingMethod.getSorder());
                    break;
                }
                case UNCATEGORIZED: {
                    fromDatabase = db.getNoteDao().searchNotesUncategorized(accountId, searchQuery, sortingMethod.getSorder());
                    break;
                }
                case RECENT: {
                    fromDatabase = db.getNoteDao().searchNotesAll(accountId, searchQuery, sortingMethod.getSorder());
                    break;
                }
                case DEFAULT_CATEGORY:
                default: {
                    Category category = selectedCategory.getCategory();
                    fromDatabase = db.getNoteDao().searchNotesByCategory(accountId, searchQuery, category == null ? "" : category.getTitle(), sortingMethod.getSorder());
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
    }
}
