package it.niedermann.owncloud.notes.share.model

import com.google.gson.annotations.Expose

data class UpdateShareRequest(
    @Expose
    val share_id: Int,

    @Expose
    val note: String,

    @Expose
    val password: String,

    @Expose
    val expireDate: String?,

    @Expose
    val sendMail: String
)
