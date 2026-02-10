/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.util

import com.owncloud.android.lib.resources.shares.OCShare
import com.owncloud.android.lib.resources.shares.ShareType

fun OCShare.hasFileRequestPermission(): Boolean = (isFolder && shareType?.isPublicOrMail() == true)

fun ShareType.isPublicOrMail(): Boolean = (this == ShareType.PUBLIC_LINK || this == ShareType.EMAIL)

fun OCShare?.remainingDownloadLimit(): Int? {
    val downloadLimit = this?.fileDownloadLimit ?: return null
    return if (downloadLimit.limit > 0) {
        downloadLimit.limit - downloadLimit.count
    } else {
        null
    }
}
