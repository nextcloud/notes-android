/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.versions.model

import java.util.Date

data class FileVersion(
    val fileId: Long,
    val versionId: String,
    val modified: Date,
    val size: Long?,
    val mimeType: String?
)
