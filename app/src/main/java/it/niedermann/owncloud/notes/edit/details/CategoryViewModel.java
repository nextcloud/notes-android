package it.niedermann.owncloud.notes.edit.details;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import it.niedermann.owncloud.notes.main.navigation.NavigationItem;
import it.niedermann.owncloud.notes.persistence.NotesRepository;

import static androidx.lifecycle.Transformations.distinctUntilChanged;
import static androidx.lifecycle.Transformations.map;
import static androidx.lifecycle.Transformations.switchMap;
import static it.niedermann.owncloud.notes.shared.util.DisplayUtils.convertToCategoryNavigationItem;

public class CategoryViewModel extends AndroidViewModel {

    private final NotesRepository repo;

    @NonNull
    private final MutableLiveData<String> searchTerm = new MutableLiveData<>("");

    public CategoryViewModel(@NonNull Application application) {
        super(application);
        repo = NotesRepository.getInstance(application);
    }

    public void postSearchTerm(@NonNull String searchTerm) {
        this.searchTerm.postValue(searchTerm);
    }

    @NonNull
    public LiveData<List<NavigationItem.CategoryNavigationItem>> getCategories(long accountId) {
        return switchMap(distinctUntilChanged(this.searchTerm), searchTerm ->
                map(repo.searchCategories$(accountId, TextUtils.isEmpty(searchTerm) ? "%" : "%" + searchTerm + "%"),
                        categories -> convertToCategoryNavigationItem(getApplication(), categories)));
    }
}
