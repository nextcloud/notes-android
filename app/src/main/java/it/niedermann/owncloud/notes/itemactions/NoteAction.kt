package it.niedermann.owncloud.notes.itemactions

import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import it.niedermann.owncloud.notes.R

/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Daniele Verducci <daniele.verducci@nextcloud.com>
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

enum class NoteAction(
    @param:IdRes val id: Int,
    @param:StringRes val title: Int,
    @param:DrawableRes val icon: Int
)  {
    SEE_DETAILS(R.id.action_see_details, R.string.action_see_details, R.drawable.ic_info_outline_grey600_24dp),
    SEND_SHARE(R.id.action_send_share, R.string.action_send_share, R.drawable.ic_share_white_24dp),
}