/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.share.model

import com.google.gson.annotations.Expose

data class UpdateShareRequest(
    @Expose
    val share_id: Int,

    @Expose
    val permissions: Int?,

    @Expose
    val password: String,

    @Expose
    val publicUpload: String,

    @Expose
    val expireDate: String?,

    @Expose
    val label: String?,

    @Expose
    val note: String,

    /**
     * Array of ShareAttributes data class in JSON format
     */
    @Expose
    val attributes: String?,

    @Expose
    val sendMail: String
)

data class ShareAttributesV2(
    var scope: String,
    var key: String,
    var value: Boolean
)

data class ShareAttributesV1(
    var scope: String,
    var key: String,
    var enabled: Boolean
)
