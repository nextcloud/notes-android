package it.niedermann.owncloud.notes.accountpicker;

import androidx.annotation.NonNull;

import it.niedermann.owncloud.notes.persistence.entity.Account;

public interface AccountPickerListener {
    void onAccountPicked(@NonNull Account account);
}