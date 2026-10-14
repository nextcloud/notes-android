package it.niedermann.owncloud.notes.activities.repository

import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.activities.model.Activity

sealed class ActivitiesResult(open val descriptionId: Int? = null) {

    data class Success(val activities: List<Activity>, val lastGiven: Long?) : ActivitiesResult()

    data class FailedNotModified(
        override val descriptionId: Int = R.string.activities_fetch_failed_not_modified
    ) : ActivitiesResult(descriptionId)

    data class Failed(
        override val descriptionId: Int = R.string.activities_fetch_failed
    ) : ActivitiesResult(descriptionId)

}