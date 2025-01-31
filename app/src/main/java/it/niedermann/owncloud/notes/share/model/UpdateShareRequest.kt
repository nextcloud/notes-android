package it.niedermann.owncloud.notes.share.model

import com.google.gson.annotations.Expose

data class UpdateShareRequest(
    @Expose
    val shareId: String,

    @Expose
    val noteText: String
)
