package it.niedermann.owncloud.notes.exception;

import androidx.annotation.NonNull;

/**
 * This type of {@link Exception} occurs, when a user has an active internet connection but decided by intention not to use it.
 * Example: "Sync only on Wi-Fi" is set to <code>true</code>, Wi-Fi is not connected, mobile data is available
 */
public class IntendedOfflineException extends Exception {

    public IntendedOfflineException(@NonNull String message) {
        super(message);
    }
}
