/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.share.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class UpdateSharePermissionRequest(
    @Expose
    @SerializedName("permissions") val permissions: Int? = null,
)
