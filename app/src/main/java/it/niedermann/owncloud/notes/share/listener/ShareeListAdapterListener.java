/*
 *
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * @author TSI-mc
 * Copyright (C) 2020 Tobias Kaminsky
 * Copyright (C) 2020 Nextcloud GmbH
 * Copyright (C) 2021 TSI-mc
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later OR GPL-2.0-only
 */
package it.niedermann.owncloud.notes.share.listener;

import com.owncloud.android.lib.resources.shares.OCShare;

import it.niedermann.owncloud.notes.persistence.entity.Account;

public interface ShareeListAdapterListener {
    void copyLink(OCShare share);

    void showSharingMenuActionSheet(OCShare share);

    void copyInternalLink();

    void createPublicShareLink();

    void createSecureFileDrop();

    void requestPasswordForShare(OCShare share, boolean askForPassword);

    void showPermissionsDialog(OCShare share);

    void showProfileBottomSheet(Account account, String shareWith);
}
