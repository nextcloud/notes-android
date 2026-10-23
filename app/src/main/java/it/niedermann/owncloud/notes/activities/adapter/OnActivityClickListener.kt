/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Daniele Verducci <daniele.verducci@nextcloud.com>
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.activities.adapter

import com.owncloud.android.lib.resources.files.model.FileVersion
import it.niedermann.owncloud.notes.activities.model.Activity

interface OnActivityClickListener {
    fun onActivityItemClick(activity: Activity)
    fun onVersionItemClick(version: FileVersion)
    fun lastActivityReached()
}