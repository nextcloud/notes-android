/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.share.helper;

import static com.owncloud.android.lib.resources.shares.OCShare.CREATE_PERMISSION_FLAG;
import static com.owncloud.android.lib.resources.shares.OCShare.MAXIMUM_PERMISSIONS_FOR_FILE;
import static com.owncloud.android.lib.resources.shares.OCShare.MAXIMUM_PERMISSIONS_FOR_FOLDER;
import static com.owncloud.android.lib.resources.shares.OCShare.NO_PERMISSION;
import static com.owncloud.android.lib.resources.shares.OCShare.READ_PERMISSION_FLAG;
import static com.owncloud.android.lib.resources.shares.OCShare.SHARE_PERMISSION_FLAG;

import android.content.Context;

import com.owncloud.android.lib.resources.shares.OCShare;

import it.niedermann.owncloud.notes.R;

/**
 * Helper calls for visibility logic of the sharing menu.
 */
public final class SharingMenuHelper {

    private SharingMenuHelper() {
        // utility class -> private constructor
    }

    public static boolean isUploadAndEditingAllowed(OCShare share) {
        if (share.getPermissions() == NO_PERMISSION) {
            return false;
        }

        return (share.getPermissions() & (share.isFolder() ? MAXIMUM_PERMISSIONS_FOR_FOLDER :
                MAXIMUM_PERMISSIONS_FOR_FILE)) == (share.isFolder() ? MAXIMUM_PERMISSIONS_FOR_FOLDER :
                MAXIMUM_PERMISSIONS_FOR_FILE);
    }

    public static boolean isReadOnly(OCShare share) {
        if (share.getPermissions() == NO_PERMISSION) {
            return false;
        }

        return (share.getPermissions() & ~SHARE_PERMISSION_FLAG) == READ_PERMISSION_FLAG;
    }

    public static boolean isFileDrop(OCShare share) {
        if (share.getPermissions() == NO_PERMISSION) {
            return false;
        }

        return (share.getPermissions() & ~SHARE_PERMISSION_FLAG) == CREATE_PERMISSION_FLAG;
    }

    public static boolean isSecureFileDrop(OCShare share) {
        if (share.getPermissions() == NO_PERMISSION) {
            return false;
        }

        return (share.getPermissions() & ~SHARE_PERMISSION_FLAG) == CREATE_PERMISSION_FLAG + READ_PERMISSION_FLAG;
    }

    public static String getPermissionName(Context context, OCShare share) {
        if (SharingMenuHelper.isUploadAndEditingAllowed(share)) {
            return context.getResources().getString(R.string.share_permission_can_edit);
        } else if (SharingMenuHelper.isReadOnly(share)) {
            return context.getResources().getString(R.string.share_permission_view_only);
        } else if (SharingMenuHelper.isSecureFileDrop(share)) {
            return context.getResources().getString(R.string.share_permission_secure_file_drop);
        } else if (SharingMenuHelper.isFileDrop(share)) {
            return context.getResources().getString(R.string.share_permission_file_drop);
        }
        return null;
    }

    /**
     * method to get the current checked index from the list of permissions
     *
     */
    public static int getPermissionCheckedItem(Context context, OCShare share, String[] permissionArray) {
        if (SharingMenuHelper.isUploadAndEditingAllowed(share)) {
            if (share.isFolder()) {
                return getPermissionIndexFromArray(context, permissionArray, R.string.link_share_allow_upload_and_editing);
            } else {
                return getPermissionIndexFromArray(context, permissionArray, R.string.link_share_editing);
            }
        } else if (SharingMenuHelper.isReadOnly(share)) {
            return getPermissionIndexFromArray(context, permissionArray, R.string.link_share_view_only);
        } else if (SharingMenuHelper.isFileDrop(share)) {
            return getPermissionIndexFromArray(context, permissionArray, R.string.link_share_file_drop);
        }
        return 0;//default first item selected
    }

    private static int getPermissionIndexFromArray(Context context, String[] permissionArray, int permissionName) {
        for (int i = 0; i < permissionArray.length; i++) {
            if (permissionArray[i].equalsIgnoreCase(context.getResources().getString(permissionName))) {
                return i;
            }
        }
        return 0;
    }

    public static boolean canReshare(OCShare share) {
        return (share.getPermissions() & SHARE_PERMISSION_FLAG) > 0;
    }
}
