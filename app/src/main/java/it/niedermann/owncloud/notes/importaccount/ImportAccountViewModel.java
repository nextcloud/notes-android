package it.niedermann.owncloud.notes.importaccount;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import it.niedermann.owncloud.notes.persistence.NotesDatabase;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.shared.model.Capabilities;

public class ImportAccountViewModel extends AndroidViewModel {

    private static final String TAG = ImportAccountViewModel.class.getSimpleName();

    @NonNull
    private final NotesDatabase db;

    public ImportAccountViewModel(@NonNull Application application) {
        super(application);
        this.db = NotesDatabase.getInstance(application.getApplicationContext());
    }

    public LiveData<Account> addAccount(@NonNull String url, @NonNull String username, @NonNull String accountName, @NonNull Capabilities capabilities) {
        return db.addAccount(url, username, accountName, capabilities);
    }
}
