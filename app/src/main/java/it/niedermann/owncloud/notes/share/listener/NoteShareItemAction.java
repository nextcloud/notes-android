/*
 * Nextcloud Android client application
 *
 * @author TSI-mc
 * Copyright (C) 2021 TSI-mc
 * Copyright (C) 2021 Nextcloud GmbH
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later OR GPL-2.0-only
 */
package it.niedermann.owncloud.notes.share.listener;

import com.owncloud.android.lib.resources.shares.OCShare;
import com.owncloud.android.lib.resources.shares.ShareType;

public interface NoteShareItemAction {

    /**
     * open advanced permission for selected share
     */
    void advancedPermissions(OCShare share);

    /**
     * open note screen to send new email
     */
    void sendNewEmail(OCShare share);

    /**
     * unshare the current share
     */
    void unShare(OCShare share);

    /**
     * send created link only valid for {@link ShareType#PUBLIC_LINK}
     */
    void sendLink(OCShare share);

    /**
     * create another link only valid for {@link ShareType#PUBLIC_LINK}
     */
    void addAnotherLink(OCShare share);
}
