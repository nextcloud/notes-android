/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2022 Tim Krüger <t@timkrueger.me
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.accountswitcher.adapter.predefinedStatus

import com.owncloud.android.lib.resources.users.PredefinedStatus

interface PredefinedStatusClickListener {
    fun onClick(predefinedStatus: PredefinedStatus)
}
