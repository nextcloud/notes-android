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
