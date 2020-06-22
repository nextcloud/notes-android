package it.niedermann.owncloud.notes.accountswitcher;

import it.niedermann.owncloud.notes.shared.model.LocalAccount;

public interface AccountSwitcherListener {
    void addAccount();

    void onAccountChosen(LocalAccount localAccount);

    void onAccountDeleted(LocalAccount localAccount);
}
