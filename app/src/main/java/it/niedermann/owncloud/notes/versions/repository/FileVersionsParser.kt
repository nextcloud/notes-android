/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.versions.repository

import com.owncloud.android.lib.common.network.WebdavEntry
import com.owncloud.android.lib.resources.files.model.FileVersion
import org.apache.jackrabbit.webdav.MultiStatus
import org.apache.jackrabbit.webdav.xml.DomUtil
import java.io.InputStream

object FileVersionsParser {

    private const val DAV_PATH = "/remote.php/dav"

    fun parse(fileId: Long, input: InputStream): List<FileVersion> {
        val multiStatus = MultiStatus.createFromXml(DomUtil.parseDocument(input).documentElement)
        return multiStatus.responses.drop(1).map { FileVersion(fileId, WebdavEntry(it, DAV_PATH)) }
    }
}
