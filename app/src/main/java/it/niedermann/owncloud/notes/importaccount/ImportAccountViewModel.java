package it.niedermann.owncloud.notes.importaccount;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import it.niedermann.owncloud.notes.persistence.NotesRepository;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.shared.model.Capabilities;

public class ImportAccountViewModel extends AndroidViewModel {

    private static final String TAG = ImportAccountViewModel.class.getSimpleName();

    @NonNull
    private final NotesRepository repo;

    public ImportAccountViewModel(@NonNull Application application) {
        super(application);
        this.repo = NotesRepository.getInstance(application);
    }

    public LiveData<Account> addAccount(@NonNull String url, @NonNull String username, @NonNull String accountName, @NonNull Capabilities capabilities) {
        return repo.addAccount(url, username, accountName, capabilities);
    }
}
