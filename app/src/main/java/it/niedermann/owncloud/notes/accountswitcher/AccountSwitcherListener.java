/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2020 Stefan Niedermann <info@niedermann.it>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.accountswitcher;

import androidx.annotation.NonNull;

import it.niedermann.owncloud.notes.persistence.entity.Account;

public interface AccountSwitcherListener {
    void addAccount();

    void onAccountChosen(@NonNull Account localAccount);
}
