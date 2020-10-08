package it.niedermann.owncloud.notes.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import it.niedermann.owncloud.notes.persistence.NotesDatabase;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.persistence.entity.NoteWithCategory;
import it.niedermann.owncloud.notes.shared.model.OldCategory;

public class MainViewModel extends AndroidViewModel {

    @NonNull
    NotesDatabase db;

    @NonNull
    private MutableLiveData<Account> currentAccount = new MutableLiveData<>();
    @NonNull
    private MutableLiveData<String> searchTerm = new MutableLiveData<>();
    @NonNull
    private MutableLiveData<OldCategory> selectedCategory = new MutableLiveData<>();

    private MediatorLiveData<List<NoteWithCategory>> notesListLiveData = new MediatorLiveData<>();

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

    public LiveData<List<NoteWithCategory>> getNotesListLiveData() {
        return notesListLiveData;
    }
}
