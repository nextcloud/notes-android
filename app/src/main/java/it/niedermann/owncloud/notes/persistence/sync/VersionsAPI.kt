/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.sync

import com.nextcloud.android.sso.model.SingleSignOnAccount
import com.owncloud.android.lib.common.network.WebdavEntry
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.resources.files.model.FileVersion
import it.niedermann.owncloud.notes.activities.repository.VersionsResult
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpStatus
import org.apache.jackrabbit.webdav.DavConstants
import org.apache.jackrabbit.webdav.MultiStatus
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod


class VersionsAPI (
    private val ssoAccount: SingleSignOnAccount,
    private val davEndpoint: String
) {
    fun fetchVersions(fileId: Long) : VersionsResult {
        val uri = "${getDavPath()}/versions/${ssoAccount.userId}/versions/$fileId"
        val propSet = WebdavUtils.getFileVersionPropSet()

        val query = PropFindMethod(uri, propSet, DavConstants.DEPTH_1)
        val status: Int = HttpClient().executeMethod(query)

        // check and process response
        val isSuccess = (status == HttpStatus.SC_MULTI_STATUS || status == HttpStatus.SC_OK)

        if (isSuccess) {
            // get data from remote folder
            val dataInServer: MultiStatus = query.getResponseBodyAsMultiStatus()
            val versions = readData(dataInServer, fileId)
            return VersionsResult.Success(versions)
        }

        return VersionsResult.Failed
    }

    /**
     * Read the data retrieved from the server about the file versions.
     *
     * @param remoteData Full response got from the server with the version data.
     * @param fileId     File id the versions refer to
     */
    private fun readData(remoteData: MultiStatus, fileId: Long): List<FileVersion> {
        val responses = remoteData.responses
        val versions = arrayListOf<FileVersion>()

        // parse data from remote folder
        val splitElement = getDavPath()

        // loop to update every child
        for (resp in responses) {
            versions.add(
                FileVersion(
                    fileId,
                    WebdavEntry(resp, splitElement)
                )
            )
        }
        return versions
    }

    private fun getDavPath(): String {
        return "${ssoAccount.url}$davEndpoint"
    }
}
