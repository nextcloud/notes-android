package it.niedermann.owncloud.notes.share.model

import android.net.Uri

data class ShareesData(
    var displayName: String?,
    var dataUri: Uri?,
    var icon: Any?,
)

