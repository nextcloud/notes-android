package it.niedermann.owncloud.notes.share.model

import com.owncloud.android.lib.resources.shares.OCShare
import com.owncloud.android.lib.resources.shares.ShareType

fun List<CreateShareResponse>.toOCShare(): List<OCShare> {
    return map { response ->
        OCShare().apply {
            id = response.id.toLongOrNull() ?: 0
            fileSource = response.fileSource
            itemSource = response.itemSource
            shareType = when (response.shareType) {
                0L -> ShareType.USER
                1L -> ShareType.GROUP
                3L -> ShareType.PUBLIC_LINK
                else -> null
            }
            shareWith = response.shareWith
            path = response.path
            permissions = response.permissions.toInt()
            sharedDate = response.stime
            token = null
            sharedWithDisplayName = response.shareWithDisplayname
            isFolder = response.itemType == "folder"
            userId = response.uidOwner
            shareLink = null
            isPasswordProtected = false
            note = response.note
            isHideFileDownload = response.hideDownload > 0
            label = response.label
            isHasPreview = response.hasPreview
            mimetype = response.mimetype
            ownerDisplayName = response.displaynameOwner
        }
    }
}
