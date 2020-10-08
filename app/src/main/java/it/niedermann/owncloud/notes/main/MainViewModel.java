package it.niedermann.owncloud.notes.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

import it.niedermann.owncloud.notes.persistence.NotesDatabase;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.NoteWithCategory;
import it.niedermann.owncloud.notes.shared.model.CategorySortingMethod;
import it.niedermann.owncloud.notes.shared.model.Item;
import it.niedermann.owncloud.notes.shared.model.OldCategory;

import static it.niedermann.owncloud.notes.main.slots.SlotterUtil.fillListByCategory;
import static it.niedermann.owncloud.notes.main.slots.SlotterUtil.fillListByInitials;
import static it.niedermann.owncloud.notes.main.slots.SlotterUtil.fillListByTime;

public class MainViewModel extends AndroidViewModel {

    @NonNull
    private NotesDatabase db;

    @NonNull
    private MutableLiveData<Account> currentAccount = new MutableLiveData<>();
    @NonNull
    private MutableLiveData<String> searchTerm = new MutableLiveData<>();
    @NonNull
    private MutableLiveData<OldCategory> selectedCategory = new MutableLiveData<>(new OldCategory(null, null));

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

    public void postSelectedCategory(@NonNull OldCategory selectedCategory) {
        this.selectedCategory.postValue(selectedCategory);
    }

    public LiveData<Account> getCurrentAccount() {
        return currentAccount;
    }

    public LiveData<String> getSearchTerm() {
        return searchTerm;
    }

    public LiveData<OldCategory> getSelectedCategory() {
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
        OldCategory selectedCategory = getSelectedCategory().getValue();
        LiveData<List<NoteWithCategory>> fromDatabase;
        if (currentAccount != null && selectedCategory != null) {
            Long accountId = currentAccount.getId();
            CategorySortingMethod sortingMethod = db.getCategoryOrder(accountId, selectedCategory);
            String searchQuery = getSearchTerm().getValue();
            searchQuery = searchQuery == null ? "%" : "%" + searchQuery.trim() + "%";
            if (Boolean.TRUE.equals(selectedCategory.favorite)) { // Favorites
                fromDatabase = db.getNoteDao().searchNotesFavorites(accountId, searchQuery, sortingMethod.getSorder());
            } else if (selectedCategory.category != null && selectedCategory.category.length() == 0) { // Uncategorized
                fromDatabase = db.getNoteDao().searchNotesUncategorized(accountId, searchQuery, sortingMethod.getSorder());
            } else if( selectedCategory.category == null && selectedCategory.favorite == null) { // Recent
                fromDatabase = db.getNoteDao().searchNotesAll(accountId, searchQuery, sortingMethod.getSorder());
            } else { // A special category
                fromDatabase = db.getNoteDao().searchNotesByCategory(accountId, searchQuery, selectedCategory.category, sortingMethod.getSorder());
            }

            return Transformations.map(fromDatabase, (Function<List<NoteWithCategory>, List<Item>>) noteList -> {
                if (selectedCategory.category == null) {
                    if (sortingMethod == CategorySortingMethod.SORT_MODIFIED_DESC) {
                        return fillListByTime(getApplication(), noteList);
                    } else {
                        return fillListByInitials(getApplication(), noteList);
                    }
                } else {
                    return fillListByCategory(noteList, selectedCategory.category);
                }
            });
        } else {
            return new MutableLiveData<>();
        }
    }
}
