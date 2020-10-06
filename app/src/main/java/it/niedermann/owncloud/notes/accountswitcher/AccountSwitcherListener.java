package it.niedermann.owncloud.notes.accountswitcher;

import it.niedermann.owncloud.notes.persistence.entity.LocalAccountEntity;

public interface AccountSwitcherListener {
    void addAccount();

    void onAccountChosen(LocalAccountEntity localAccount);

    void onAccountDeleted(LocalAccountEntity localAccount);
}
