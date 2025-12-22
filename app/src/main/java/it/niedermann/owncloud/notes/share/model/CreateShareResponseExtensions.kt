/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.share.model

import com.owncloud.android.lib.resources.shares.OCShare
import com.owncloud.android.lib.resources.shares.ShareType
import it.niedermann.owncloud.notes.shared.util.extensions.toExpirationDateLong

/**
 * Maps a list of [CreateShareResponse] to a list of [OCShare] objects.
 *
 * Filters out any responses that could not be parsed correctly (where the ID is -1).
 *
 * @return A list of valid [OCShare] instances.
 */
fun List<CreateShareResponse>.toOCShareList(): List<OCShare> = map { response ->
    response.toOCShare()
}.filter { it.id != -1L }

fun CreateShareResponse.toOCShare(): OCShare {
    val response = this

    return OCShare().apply {
        id = response.id.toLongOrNull() ?: -1L
        fileSource = response.fileSource
        itemSource = response.itemSource
        shareType = ShareType.fromValue(response.shareType.toInt())
        shareWith = response.shareWith
        path = response.path
        permissions = response.permissions.toInt()
        sharedDate = response.stime
        token = response.token
        sharedWithDisplayName = response.shareWithDisplayname
        isFolder = response.itemType == "folder"
        userId = response.uidOwner
        shareLink = response.url
        isPasswordProtected = !response.password.isNullOrEmpty()
        note = response.note
        isHideFileDownload = (response.hideDownload == 1L)
        label = response.label
        isHasPreview = response.hasPreview
        mimetype = response.mimetype
        ownerDisplayName = response.displaynameOwner
        expirationDate = response.expirationDate?.toExpirationDateLong() ?: 0L
    }
}
