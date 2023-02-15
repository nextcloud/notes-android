package it.niedermann.owncloud.notes.shared.model

import com.google.gson.annotations.Expose

data class OcsUrl(
    @Expose
    @JvmField
    var url: String? = null
)
