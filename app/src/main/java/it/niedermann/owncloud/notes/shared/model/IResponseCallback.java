package it.niedermann.owncloud.notes.shared.model;

import androidx.annotation.NonNull;

public interface IResponseCallback {
    void onSuccess();

    void onError(@NonNull Throwable t);
}
