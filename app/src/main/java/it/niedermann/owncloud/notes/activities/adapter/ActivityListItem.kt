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

class ActivityListItem : Comparable<ActivityListItem> {
    val context: Context
    val activity: Activity?
    val fileVersion: FileVersion?

    constructor(context: Context, activity: Activity) {
        this.context = context
        this.activity = activity
        this.fileVersion = null
    }

    constructor(context: Context, fileVersion: FileVersion) {
        this.context = context
        this.activity = null
        this.fileVersion = fileVersion
    }

    fun getTimestamp() : Long =
        when {
            activity != null -> activity.datetime.time
            fileVersion != null -> fileVersion.modifiedTimestamp
            else -> 0
        }

    fun getTitle() : String? =
        when {
            activity != null -> activity.subject
            fileVersion != null -> context.getString(R.string.new_version_was_created)
            else -> null
        }

    fun getMessage() : String? =
        when {
            activity != null -> activity.message
            fileVersion != null -> HumanReadableFormatter.formatBytes(fileVersion.fileLength)
            else -> null
        }

    fun getIconUrl() : String? = activity?.icon

    fun canBeRestored() : Boolean = fileVersion != null

    override fun compareTo(other: ActivityListItem): Int =
        getTimestamp().compareTo(other.getTimestamp())
}