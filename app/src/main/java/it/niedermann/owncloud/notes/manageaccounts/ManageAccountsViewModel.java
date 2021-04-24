package it.niedermann.owncloud.notes.manageaccounts;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;

import java.util.List;

import it.niedermann.owncloud.notes.persistence.NotesDatabase;
import it.niedermann.owncloud.notes.persistence.SSOClient;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.shared.model.IResponseCallback;

import static androidx.lifecycle.Transformations.distinctUntilChanged;

public class ManageAccountsViewModel extends AndroidViewModel {

    private static final String TAG = ManageAccountsViewModel.class.getSimpleName();

    @NonNull
    private final NotesDatabase db;

    public ManageAccountsViewModel(@NonNull Application application) {
        super(application);
        this.db = NotesDatabase.getInstance(application);
    }

    public void getCurrentAccount(@NonNull Context context, @NonNull IResponseCallback<Account> callback) {
        try {
            callback.onSuccess(db.getAccountDao().getAccountByName((SingleAccountHelper.getCurrentSingleSignOnAccount(context).name)));
        } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
            callback.onError(e);
        }
    }

    public LiveData<List<Account>> getAccounts$() {
        return distinctUntilChanged(db.getAccountDao().getAccounts$());
    }

    public void deleteAccount(@NonNull Account account, @NonNull Context context) {
        new Thread(() -> {
            final List<Account> accounts = db.getAccountDao().getAccounts();
            for (int i = 0; i < accounts.size(); i++) {
                if (accounts.get(i).getId() == account.getId()) {
                    if (i > 0) {
                        selectAccount(accounts.get(i - 1), context);
                    } else if (accounts.size() > 1) {
                        selectAccount(accounts.get(i + 1), context);
                    } else {
                        selectAccount(null, context);
                    }
                    db.deleteAccount(accounts.get(i));
                    break;
                }
            }
        }).start();
    }

    public void selectAccount(@Nullable Account account, @NonNull Context context) {
        SingleAccountHelper.setCurrentAccount(context, (account == null) ? null : account.getAccountName());
    }
}