package it.niedermann.owncloud.notes.shared.model;

/**
 * Callback
 * Created by stefan on 01.10.15.
 */
public interface ISyncCallback {
    void onFinish();

    default void onScheduled() {

    }
}
