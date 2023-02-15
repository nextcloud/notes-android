package it.niedermann.owncloud.notes.shared.model.directediting

import com.google.gson.annotations.Expose

data class DirectEditingRequestBody(
    @Expose
    val path: String,
    @Expose
    val editorId: String,
    @Expose
    val fileId: Long,
)
