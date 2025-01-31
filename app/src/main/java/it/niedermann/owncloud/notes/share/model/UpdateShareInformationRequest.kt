package it.niedermann.owncloud.notes.share.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class UpdateShareInformationRequest(
    @Expose
    @SerializedName("shareId") val shareId: String,

    @Expose
    @SerializedName("password") val password: String? = null,

    @Expose
    @SerializedName("expireDate") val expireDate: String? = null,

    @Expose
    @SerializedName("permissions") val permissions: Int? = null,

    @Expose
    @SerializedName("hideDownload") val hideDownload: Boolean? = null,

    @Expose
    @SerializedName("note") val note: String? = null,

    @Expose
    @SerializedName("label") val label: String? = null
)
