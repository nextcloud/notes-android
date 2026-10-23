/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Daniele Verducci <daniele.verducci@nextcloud.com>
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.activities.repository

import com.owncloud.android.lib.resources.files.model.FileVersion

enum class RestoreVersionsResult{
    Success, Failed, FailedUnsupported
}