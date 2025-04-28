package it.niedermann.owncloud.notes.share.model

import com.owncloud.android.lib.resources.shares.OCShare
import com.owncloud.android.lib.resources.shares.ShareType

fun List<CreateShareResponse>.toOCShareList(): List<OCShare> {
    return map { response ->
        response.toOCShare()
    }.filter { it.id != -1L }
}

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
        shareLink =  response.url
        isPasswordProtected = !response.password.isNullOrEmpty()
        note = response.note
        isHideFileDownload = (response.hideDownload == 1L)
        label = response.label
        isHasPreview = response.hasPreview
        mimetype = response.mimetype
        ownerDisplayName = response.displaynameOwner
    }
}
