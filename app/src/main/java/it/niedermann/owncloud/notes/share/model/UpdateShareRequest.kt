/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.share.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class UpdateShareRequest(
    @Expose
    @SerializedName("permissions")
    val permissions: Int? = null,

    @Expose
    @SerializedName("password")
    val password: String? = null,

    @Expose
    @SerializedName("note")
    val note: String? = null,

    @Expose
    @SerializedName("label")
    val label: String? = null,

    @Expose
    @SerializedName("expireDate")
    val expireDate: String? = null,

    @Expose
    @SerializedName("hideDownload")
    val hideDownload: String? = null,

    @Expose
    @SerializedName("attributes")
    val attributes: String? = null
)
