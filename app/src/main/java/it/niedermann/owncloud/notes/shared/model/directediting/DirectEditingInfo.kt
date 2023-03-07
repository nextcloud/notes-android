package it.niedermann.owncloud.notes.shared.model.directediting

import com.google.gson.annotations.Expose

data class DirectEditingInfo(
    @Expose
    val editors: Map<String, DirectEditingEditor>,
    @Expose
    val creators: Map<String, DirectEditingCreator>,
)
