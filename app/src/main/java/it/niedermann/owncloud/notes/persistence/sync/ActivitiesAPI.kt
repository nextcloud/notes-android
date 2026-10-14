/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Daniele Verducci <daniele.verducci@nextcloud.com>
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.sync

import it.niedermann.owncloud.notes.activities.model.Activity
import it.niedermann.owncloud.notes.shared.model.OcsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ActivitiesAPI {
    @GET("activity/filter?format=json&object_type=files&sort=desc")
    fun fetchActivities(
        @Query("object_id") fileId: Long,
        @Query("since") since: Long?,
        @Query("limit") limit: Int
    ): Call<OcsResponse<List<Activity>>>
}
