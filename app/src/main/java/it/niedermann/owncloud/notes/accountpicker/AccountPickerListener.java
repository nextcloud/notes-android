package it.niedermann.owncloud.notes.accountpicker;

import androidx.annotation.NonNull;

import it.niedermann.owncloud.notes.persistence.entity.LocalAccountEntity;

public interface AccountPickerListener {
    void onAccountPicked(@NonNull LocalAccountEntity account);
}