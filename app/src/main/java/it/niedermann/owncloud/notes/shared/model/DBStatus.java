/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.model;

import androidx.annotation.NonNull;

/**
 * Helps to distinguish between different local change types for Server Synchronization.
 * Created by stefan on 19.09.15.
 */
public enum DBStatus {

    /**
     * VOID means, that the Note was not modified locally
     */
    VOID(""),

    /**
     * LOCAL_EDITED means that a Note was created and/or changed since the last successful synchronization.
     * If it was newly created, then REMOTE_ID is 0
     */
    LOCAL_EDITED("LOCAL_EDITED"),

    /**
     * LOCAL_DELETED means that the Note was deleted locally, but this information was not yet synchronized.
     * Therefore, the Note have to be kept locally until the synchronization has succeeded.
     * However, Notes with this status should not be displayed in the UI.
     */
    LOCAL_DELETED("LOCAL_DELETED");

    @NonNull
    private final String title;

    @NonNull
    public String getTitle() {
        return title;
    }

    DBStatus(@NonNull String title) {
        this.title = title;
    }
}
