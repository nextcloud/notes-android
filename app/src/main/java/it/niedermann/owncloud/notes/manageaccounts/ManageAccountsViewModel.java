/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.manageaccounts;

import static androidx.lifecycle.Transformations.distinctUntilChanged;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.niedermann.owncloud.notes.persistence.NotesRepository;
import it.niedermann.owncloud.notes.persistence.entity.Account;
import it.niedermann.owncloud.notes.shared.model.IResponseCallback;

public class ManageAccountsViewModel extends AndroidViewModel {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @NonNull
    private final NotesRepository repo;

    public ManageAccountsViewModel(@NonNull Application application) {
        super(application);
        this.repo = NotesRepository.getInstance(application);
    }

    public void getCurrentAccount(@NonNull Context context, @NonNull IResponseCallback<Account> callback) {
        try {
            callback.onSuccess(repo.getAccountByName((SingleAccountHelper.getCurrentSingleSignOnAccount(context).name)));
        } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
            callback.onError(e);
        }
    }

    public LiveData<List<Account>> getAccounts$() {
        return distinctUntilChanged(repo.getAccounts$());
    }

    public void deleteAccount(@NonNull Account account, @NonNull Context context) {
        executor.submit(() -> {
            final var accounts = repo.getAccounts();
            for (int i = 0; i < accounts.size(); i++) {
                if (accounts.get(i).getId() == account.getId()) {
                    if (i > 0) {
                        selectAccount(accounts.get(i - 1), context);
                    } else if (accounts.size() > 1) {
                        selectAccount(accounts.get(i + 1), context);
                    } else {
                        selectAccount(null, context);
                    }
                    repo.deleteAccount(accounts.get(i));
                    break;
                }
            }
        });
    }

    public void selectAccount(@Nullable Account account, @NonNull Context context) {
        SingleAccountHelper.commitCurrentAccount(context, (account == null) ? null : account.getAccountName());
    }

    public void countUnsynchronizedNotes(long accountId, @NonNull IResponseCallback<Long> callback) {
        executor.submit(() -> callback.onSuccess(repo.countUnsynchronizedNotes(accountId)));
    }
}