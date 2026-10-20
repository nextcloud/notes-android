/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.sync

import android.net.Uri
import android.util.Log
import com.nextcloud.android.sso.aidl.NextcloudRequest
import com.nextcloud.android.sso.api.NextcloudAPI
import com.nextcloud.android.sso.api.Response
import com.nextcloud.android.sso.exceptions.NextcloudUnsupportedMethodException
import com.nextcloud.android.sso.model.SingleSignOnAccount
import com.owncloud.android.lib.common.network.WebdavEntry
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.resources.files.model.FileVersion
import it.niedermann.owncloud.notes.activities.repository.RestoreVersionsResult
import it.niedermann.owncloud.notes.activities.repository.VersionsResult
import org.apache.jackrabbit.webdav.DavConstants
import org.apache.jackrabbit.webdav.MultiStatus
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod
import org.apache.jackrabbit.webdav.xml.DomUtil
import java.io.ByteArrayOutputStream


class VersionsAPI (
    private val nextcloudAPI: NextcloudAPI,
    private val ssoAccount: SingleSignOnAccount,
    private val davEndpoint: String
) {
    fun fetchVersions(fileId: Long) : VersionsResult {
        val uri = "${getDavPath()}versions/${ssoAccount.userId}/versions/$fileId"
        val propSet = WebdavUtils.getFileVersionPropSet()

        val query = PropFindMethod(uri, propSet, DavConstants.DEPTH_1)

        // We manually build the request to pass it to nextcloudAPI (required for authentication)
        val requestBodyOutStr = ByteArrayOutputStream()
        query.requestEntity.writeRequest(requestBodyOutStr)
        val requestBody = requestBodyOutStr.toString()
        val request = NextcloudRequest.Builder()
            .setMethod("PROPFIND")
            .setUrl(uri)
            .setRequestBody(requestBody)
            .build()

        return try {
            val response = nextcloudAPI.performNetworkRequestV2(request)
            VersionsResult.Success(readData(response, fileId))
        } catch (e: Exception) {
            Log.e(TAG, "Unable to fetch versions: $e")
            VersionsResult.Failed
        }
    }

    fun restoreVersion(version: FileVersion): RestoreVersionsResult {
        // Version restore is a move from the versions file path to the original one
        val versionsPath = "${getDavPath()}versions/${ssoAccount.userId}"
        val source = "$versionsPath/versions/${version.localId}/${Uri.encode(version.fileName)}"
        val destination = "$versionsPath/restore/${version.localId}"
        val request = NextcloudRequest.Builder()
            .setMethod("MOVE")
            .setUrl(source)
            .setAdditionalParameter(NextcloudRequest.ADDITIONAL_PARAMETER_MOVE_DESTINATION, destination)
            .setAdditionalParameter(NextcloudRequest.ADDITIONAL_PARAMETER_MOVE_OVERWRITE, true.toString())
            .build()

        try {
            val response = nextcloudAPI.performNetworkRequestV2(request)
            Log.i(TAG, response.toString())
            return RestoreVersionsResult.Success
        } catch (e: NextcloudUnsupportedMethodException) {
            Log.e(TAG, "restoreVersion used an unsupported method: ${e.message}")
            return RestoreVersionsResult.FailedUnsupported
        } catch (e: Exception) {
            Log.e(TAG, "restoreVersion failed: ${e.message}")
            return RestoreVersionsResult.Failed
        }
    }

    /**
     * Read the data retrieved from the server about the file versions.
     *
     * @param remoteData Full response got from the server with the version data.
     * @param fileId     File id the versions refer to
     */
    private fun readData(response: Response, fileId: Long): List<FileVersion> {
        // Use jackrabbit tp parse a webdav MultiStatus from response
        val ms = MultiStatus.createFromXml(DomUtil.parseDocument(response.body).documentElement)
        val responses = ms.responses
        // Each response is a version
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
        return davEndpoint
    }

    companion object {
        val TAG = "VersionsAPI"
    }
}
