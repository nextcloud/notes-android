/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Daniele Verducci <daniele.verducci@nextcloud.com>
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.itemactions

import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import it.niedermann.owncloud.notes.R

enum class NoteAction(
    @param:IdRes val id: Int,
    @param:StringRes val title: Int,
    @param:DrawableRes val icon: Int
)  {
    FAVORITE(R.id.action_favorite, R.string.action_favorite, R.drawable.ic_star_border_white_24dp),
    UNFAVORITE(R.id.action_unfavorite, R.string.action_unfavorite, R.drawable.ic_star_white_24dp),
    EDIT_CATEGORY(R.id.action_edit_category, R.string.action_edit_category, R.drawable.ic_folder_grey600_24dp),
    SEE_DETAILS(R.id.action_see_details, R.string.action_see_details, R.drawable.ic_info_outline_grey600_24dp),
    SEND_SHARE(R.id.action_send_share, R.string.action_send_share, R.drawable.ic_share_white_24dp),
    DELETE(R.id.action_delete, R.string.action_delete_note, R.drawable.ic_delete_grey600_24dp),
}