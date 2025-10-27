/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2020 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2020 Nextcloud GmbH
 * SPDX-License-Identifier: AGPL-3.0-or-later OR GPL-2.0-only
 */
package it.niedermann.owncloud.notes.accountswitcher.adapter.predefinedStatus

import com.owncloud.android.lib.resources.users.PredefinedStatus

interface PredefinedStatusClickListener {
    fun onClick(predefinedStatus: PredefinedStatus)
}
