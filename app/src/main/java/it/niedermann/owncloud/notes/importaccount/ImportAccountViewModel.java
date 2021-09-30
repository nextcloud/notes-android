package it.niedermann.owncloud.notes.importaccount;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import it.niedermann.owncloud.notes.persistence.NotesRepository;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.shared.model.Capabilities;
import it.niedermann.owncloud.notes.shared.model.IResponseCallback;
import it.niedermann.owncloud.notes.shared.model.ImportStatus;

public class ImportAccountViewModel extends AndroidViewModel {

    @NonNull
    private final NotesRepository repo;

    public ImportAccountViewModel(@NonNull Application application) {
        super(application);
        this.repo = NotesRepository.getInstance(application);
    }

    public LiveData<ImportStatus> addAccount(@NonNull String url, @NonNull String username, @NonNull String accountName, @NonNull Capabilities capabilities, @Nullable String displayName, @NonNull IResponseCallback<Account> callback) {
        return repo.addAccount(url, username, accountName, capabilities, displayName, callback);
    }
}
