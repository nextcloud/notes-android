/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.share.model

import com.google.gson.annotations.Expose

data class CreateShareRequest(
    @Expose
    val path: String,

    @Expose
    val shareType: Int,

    @Expose
    val shareWith: String,

    @Expose
    val publicUpload: String,

    @Expose
    val password: String?,

    @Expose
    val permissions: Int?,

    @Expose
    val note: String?,

    @Expose
    val attributes: String?
)
