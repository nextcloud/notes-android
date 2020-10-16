package it.niedermann.owncloud.notes.accountswitcher;

import it.niedermann.owncloud.notes.persistence.entity.Account;

public interface AccountSwitcherListener {
    void addAccount();

    void onAccountChosen(Account localAccount);
}
