package it.niedermann.owncloud.notes.accountpicker;

import androidx.annotation.NonNull;

import it.niedermann.owncloud.notes.shared.model.LocalAccount;

public interface AccountPickerListener {
    void onAccountPicked(@NonNull LocalAccount account);
}