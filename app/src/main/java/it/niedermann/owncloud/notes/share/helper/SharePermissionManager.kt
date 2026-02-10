/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.share.helper

import com.owncloud.android.lib.resources.shares.OCShare
import it.niedermann.owncloud.notes.share.model.QuickPermissionType

object SharePermissionManager {

    // region Permission check
    fun hasPermission(permission: Int, permissionFlag: Int): Boolean =
        permission != OCShare.NO_PERMISSION && (permission and permissionFlag) == permissionFlag

    // region Helper Methods
    fun canEdit(share: OCShare?): Boolean {
        if (share == null) {
            return false
        }

        return hasPermission(share.permissions, getMaximumPermission(share.isFolder))
    }

    fun isViewOnly(share: OCShare?): Boolean {
        if (share == null) {
            return false
        }

        return share.permissions != OCShare.NO_PERMISSION &&
            (
                share.permissions == OCShare.READ_PERMISSION_FLAG ||
                    share.permissions == OCShare.READ_PERMISSION_FLAG + OCShare.SHARE_PERMISSION_FLAG
                )
    }

    @Suppress("ReturnCount")
    fun isFileRequest(share: OCShare?): Boolean {
        if (share == null) {
            return false
        }

        if (!share.isFolder) {
            return false
        }

        return share.permissions != OCShare.NO_PERMISSION && share.permissions == OCShare.CREATE_PERMISSION_FLAG
    }

    fun isSecureFileDrop(share: OCShare?): Boolean {
        if (share == null) {
            return false
        }

        return hasPermission(share.permissions, OCShare.CREATE_PERMISSION_FLAG + OCShare.READ_PERMISSION_FLAG)
    }

    fun canReshare(share: OCShare?): Boolean {
        if (share == null) {
            return false
        }

        return (share.permissions and OCShare.SHARE_PERMISSION_FLAG) > 0
    }

    fun getSelectedType(share: OCShare?, encrypted: Boolean): QuickPermissionType = if (canEdit(share)) {
        QuickPermissionType.CAN_EDIT
    } else if (encrypted && isSecureFileDrop(share)) {
        QuickPermissionType.SECURE_FILE_DROP
    } else if (isViewOnly(share)) {
        QuickPermissionType.VIEW_ONLY
    } else if (isFileRequest(share)) {
        QuickPermissionType.FILE_REQUEST
    } else {
        QuickPermissionType.NONE
    }

    fun getMaximumPermission(isFolder: Boolean): Int = if (isFolder) {
        OCShare.MAXIMUM_PERMISSIONS_FOR_FOLDER
    } else {
        OCShare.MAXIMUM_PERMISSIONS_FOR_FILE
    }
    // endregion
}
