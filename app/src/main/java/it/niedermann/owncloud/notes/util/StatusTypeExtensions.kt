/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.util

import com.owncloud.android.lib.resources.users.StatusType
import it.niedermann.owncloud.notes.R

val StatusType.imageResource: Int
    get() = when (this) {
        StatusType.ONLINE -> R.drawable.ic_user_status_online
        StatusType.OFFLINE -> R.drawable.ic_user_status_busy
        StatusType.DND -> R.drawable.ic_user_status_dnd
        StatusType.AWAY -> R.drawable.ic_user_status_away
        StatusType.INVISIBLE -> R.drawable.ic_user_status_invisible
        StatusType.BUSY -> R.drawable.ic_user_status_busy
    }
