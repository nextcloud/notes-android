/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence

import android.content.Context
import com.nextcloud.android.sso.AccountImporter
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException
import com.nextcloud.android.sso.model.SingleSignOnAccount

object FilesAppAccount {
    @JvmStatic
    @Throws(NextcloudFilesAppAccountNotFoundException::class)
    fun require(context: Context, accountName: String): SingleSignOnAccount {
        ensureExists(context, accountName)

        return AccountImporter.getSingleSignOnAccount(context, accountName)
    }

    @JvmStatic
    @Throws(NextcloudFilesAppAccountNotFoundException::class)
    fun ensureExists(context: Context, accountName: String) {
        if (AccountImporter.getAccountForName(context, accountName) == null) {
            throw NextcloudFilesAppAccountNotFoundException(context, accountName)
        }
    }
}
