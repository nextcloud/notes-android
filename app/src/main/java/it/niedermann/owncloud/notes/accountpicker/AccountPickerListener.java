package it.niedermann.owncloud.notes.accountpicker;

import androidx.annotation.NonNull;

import it.niedermann.owncloud.notes.persistence.entity.LocalAccount;

public interface AccountPickerListener {
    void onAccountPicked(@NonNull LocalAccount account);
}