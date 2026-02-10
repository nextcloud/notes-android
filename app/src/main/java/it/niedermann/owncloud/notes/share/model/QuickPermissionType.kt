/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.share.model

import android.content.Context
import com.owncloud.android.lib.resources.shares.OCShare
import it.niedermann.owncloud.notes.R

enum class QuickPermissionType(val iconId: Int, val textId: Int) {
    NONE(R.drawable.ic_unknown, R.string.share_permission_unknown),
    VIEW_ONLY(R.drawable.ic_eye, R.string.share_permission_view_only),
    CAN_EDIT(R.drawable.ic_edit, R.string.share_permission_can_edit);

    fun getText(context: Context): String = context.getString(textId)

    fun getPermissionFlag(): Int = when (this) {
        NONE -> OCShare.NO_PERMISSION
        VIEW_ONLY -> OCShare.READ_PERMISSION_FLAG
        CAN_EDIT -> OCShare.MAXIMUM_PERMISSIONS_FOR_FILE
    }

    fun getAvailablePermissions(): List<QuickPermission> {
        val permissions = listOf(VIEW_ONLY, CAN_EDIT)

        return permissions.map { type ->
            QuickPermission(
                type = type,
                isSelected = (type == this)
            )
        }
    }
}
