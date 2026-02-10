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

        return hasPermission(share.permissions, getMaximumPermission())
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

    fun canReshare(share: OCShare?): Boolean {
        if (share == null) {
            return false
        }

        return (share.permissions and OCShare.SHARE_PERMISSION_FLAG) > 0
    }

    fun getSelectedType(share: OCShare?): QuickPermissionType = if (canEdit(share)) {
        QuickPermissionType.CAN_EDIT
    } else if (isViewOnly(share)) {
        QuickPermissionType.VIEW_ONLY
    } else {
        QuickPermissionType.NONE
    }

    fun getMaximumPermission(): Int = OCShare.MAXIMUM_PERMISSIONS_FOR_FILE
    // endregion
}
