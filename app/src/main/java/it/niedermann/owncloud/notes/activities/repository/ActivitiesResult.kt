/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Daniele Verducci <daniele.verducci@nextcloud.com>
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.activities.repository

import it.niedermann.owncloud.notes.R
import it.niedermann.owncloud.notes.activities.model.Activity

sealed class ActivitiesResult(open val descriptionId: Int? = null) {

    data class Success(val activities: List<Activity>, val lastGiven: Long?) : ActivitiesResult()

    data object FailedNoCapability : ActivitiesResult()

    data class Failed(
        override val descriptionId: Int = R.string.activities_fetch_failed
    ) : ActivitiesResult(descriptionId)

}