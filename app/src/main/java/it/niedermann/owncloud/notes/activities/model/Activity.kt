/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.activities.model

import com.google.gson.annotations.SerializedName

data class Activity(
    @SerializedName("activity_id") val id: Long,
    val app: String?,
    val type: String?,
    val user: String?,
    val subject: String?,
    val message: String?,
    val icon: String?,
    val link: String?,
    val datetime: String?,
    @SerializedName("object_id") val objectId: Long?,
    @SerializedName("object_name") val objectName: String?
)
