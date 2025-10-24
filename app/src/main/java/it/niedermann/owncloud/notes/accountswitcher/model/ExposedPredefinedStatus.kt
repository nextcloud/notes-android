package it.niedermann.owncloud.notes.accountswitcher.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ExposedPredefinedStatus(
    @Expose
    @SerializedName("id")
    val id: String,

    @Expose
    @SerializedName("icon")
    val icon: String,

    @Expose
    @SerializedName("message")
    val message: String,

    @Expose
    @SerializedName("clearAt")
    val exposedClearAt: ExposedClearAt?
)

data class ExposedClearAt(
    @Expose
    @SerializedName("type")
    val type: String,

    @Expose
    @SerializedName("time")
    val time: String
)
