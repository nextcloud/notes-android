package it.niedermann.owncloud.notes.shared.model;

import androidx.annotation.NonNull;

public interface IResponseCallback<T> {
    void onSuccess(T result);

    void onError(@NonNull Throwable t);
}
