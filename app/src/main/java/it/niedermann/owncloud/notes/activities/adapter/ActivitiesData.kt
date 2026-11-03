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
import it.niedermann.owncloud.notes.activities.adapter.model.Activity
import it.niedermann.owncloud.notes.activities.adapter.model.ActivityListItem
import it.niedermann.owncloud.notes.activities.adapter.model.ActivityListItem.FileVersionItem

data class ActivitiesData(
    val activities: ArrayList<Activity> = arrayListOf(),
    val versions: ArrayList<FileVersion> = arrayListOf(),
    var activitiesLastGiven: Long? = null,
    var activitiesMore: Boolean = true
) {
    fun getAggregatedList(context: Context) : List<ActivityListItem> {
        val items = arrayListOf<ActivityListItem>()
        items.addAll(
            activities.map { ActivityListItem.ActivityItem(it) }
        )
        items.addAll(
            versions.map { FileVersionItem(context, it) }
        )
        items.sortDescending()
        return items
    }

    fun isEmpty() : Boolean = activities.isEmpty() && versions.isEmpty()
}
