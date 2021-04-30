package it.niedermann.owncloud.notes.accountswitcher;

import androidx.annotation.NonNull;

import it.niedermann.owncloud.notes.persistence.entity.Account;

public interface AccountSwitcherListener {
    void addAccount();

    void onAccountChosen(@NonNull Account localAccount);
}
