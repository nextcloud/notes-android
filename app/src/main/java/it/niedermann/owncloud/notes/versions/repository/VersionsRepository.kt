/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.versions.repository

import android.content.Context
import android.net.Uri
import com.nextcloud.android.sso.model.SingleSignOnAccount
import it.niedermann.owncloud.notes.persistence.ApiProvider
import org.xml.sax.SAXException
import java.io.IOException

class VersionsRepository(
    private val context: Context,
    private val ssoAccount: SingleSignOnAccount
) {

    private val api by lazy { ApiProvider.getInstance().getVersionsAPI(context, ssoAccount) }

    fun fetchVersions(fileId: Long): VersionsResult = try {
        api.fetchVersions(Uri.encode(ssoAccount.userId), fileId).blockingSingle().byteStream().use { stream ->
            VersionsResult.Success(FileVersionsParser.parse(fileId, stream))
        }
    } catch (_: SAXException) {
        VersionsResult.Failed
    } catch (_: IOException) {
        VersionsResult.Failed
    } catch (_: IllegalArgumentException) {
        VersionsResult.Failed
    }

}
