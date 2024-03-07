/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
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
