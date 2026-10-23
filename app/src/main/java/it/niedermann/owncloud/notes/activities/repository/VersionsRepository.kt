/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Daniele Verducci <daniele.verducci@nextcloud.com>
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.activities.repository

import android.content.Context
import com.nextcloud.android.sso.model.SingleSignOnAccount
import com.owncloud.android.lib.resources.files.model.FileVersion
import it.niedermann.owncloud.notes.persistence.ApiProvider
import it.niedermann.owncloud.notes.persistence.sync.VersionsAPI

class VersionsRepository (
    private val context: Context,
    private val ssoAccount: SingleSignOnAccount
) {
    private val api by lazy { ApiProvider.getInstance().getVersionsAPI(context, ssoAccount) }

    fun fetchVersions(noteId: Long): VersionsResult {
        return api.fetchVersions(noteId)
    }

    fun restoreVersion(version: FileVersion): RestoreVersionsResult {
        return api.restoreVersion(version)
    }

}