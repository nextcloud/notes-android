package it.niedermann.owncloud.notes.activities.adapter

import android.content.Context
import com.owncloud.android.lib.resources.files.model.FileVersion
import it.niedermann.owncloud.notes.activities.model.Activity

class ActivitiesData {
    val activities = arrayListOf<Activity>()
    val versions = arrayListOf<FileVersion>()
    var activitiesLastGiven: Long? = null
    var activitiesMore: Boolean = true

    fun getAggregatedList(context: Context) : List<ActivityListItem> {
        val items = arrayListOf<ActivityListItem>()
        items.addAll(
            activities.map { ActivityListItem(context, it) }
        )
        items.addAll(
            versions.map { ActivityListItem(context, it) }
        )
        items.sortDescending()
        return items
    }

    fun isEmpty() : Boolean = activities.isEmpty() && versions.isEmpty()
}