/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.model.directediting

import com.google.gson.annotations.Expose

data class DirectEditingRequestBody(
    @Expose
    val path: String,
    @Expose
    val editorId: String,
    @Expose
    val fileId: Long,
)
