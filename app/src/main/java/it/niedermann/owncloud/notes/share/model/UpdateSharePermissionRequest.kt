package it.niedermann.owncloud.notes.share.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class UpdateSharePermissionRequest(
    @Expose
    @SerializedName("permissions") val permissions: Int? = null,
)
