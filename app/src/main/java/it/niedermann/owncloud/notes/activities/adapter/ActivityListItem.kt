/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Daniele Verducci <daniele.verducci@nextcloud.com>
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.activities.adapter

import android.content.Context
import com.owncloud.android.lib.resources.files.model.FileVersion
import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.activities.model.Activity
import it.niedermann.owncloud.notes.util.HumanReadableFormatter

sealed class ActivityListItem : Comparable<ActivityListItem> {
    abstract fun getTimestamp(): Long
    abstract fun getTitle(): String?
    abstract fun getMessage(): String?
    open fun getIconUrl(): String? = null
    open fun canBeRestored(): Boolean = false

    override fun compareTo(other: ActivityListItem): Int =
        getTimestamp().compareTo(other.getTimestamp())

    data class ActivityItem(val activity: Activity) : ActivityListItem() {
        override fun getTimestamp(): Long = activity.datetime.time
        override fun getTitle(): String = activity.subject
        override fun getMessage(): String? = activity.message
        override fun getIconUrl(): String? = activity.icon
    }

    data class FileVersionItem(val context: Context, val fileVersion: FileVersion) : ActivityListItem() {
        override fun getTimestamp(): Long = fileVersion.modifiedTimestamp
        override fun getTitle(): String = context.getString(R.string.new_version_was_created)
        override fun getMessage(): String = HumanReadableFormatter.formatBytes(fileVersion.fileLength) ?: ""
        override fun canBeRestored(): Boolean = true
    }
}
