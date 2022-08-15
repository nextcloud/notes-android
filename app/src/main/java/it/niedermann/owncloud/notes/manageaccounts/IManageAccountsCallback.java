package it.niedermann.owncloud.notes.manageaccounts;

import androidx.annotation.NonNull;

import it.niedermann.owncloud.notes.persistence.entity.Account;

public interface IManageAccountsCallback {

    void onSelect(@NonNull Account account);

    void onDelete(@NonNull Account account);

    void onChangeNotesPath(@NonNull Account account);

    void onChangeFileSuffix(@NonNull Account account);
}
