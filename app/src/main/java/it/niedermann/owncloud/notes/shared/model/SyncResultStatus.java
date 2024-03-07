/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.model;

public class SyncResultStatus {
    public boolean pullSuccessful = true;
    public boolean pushSuccessful = true;

    public static final SyncResultStatus FAILED = new SyncResultStatus();

    static {
        FAILED.pullSuccessful = false;
        FAILED.pushSuccessful = false;
    }
}
