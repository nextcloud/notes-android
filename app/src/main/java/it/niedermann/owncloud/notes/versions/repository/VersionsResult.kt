/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.versions.repository

import it.niedermann.owncloud.notes.versions.model.FileVersion

sealed interface VersionsResult {

    data class Success(val versions: List<FileVersion>) : VersionsResult

    data object Failed : VersionsResult

}
